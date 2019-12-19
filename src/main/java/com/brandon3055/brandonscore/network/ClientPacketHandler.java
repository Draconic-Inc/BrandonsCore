package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.GuiPlayerAccess;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, IClientPlayNetHandler handler) {
        switch (packet.getType()) {
            case PacketDispatcher.C_TILE_DATA_MANAGER: {
                BlockPos pos = packet.readPos();
                TileEntity tile = mc.world.getTileEntity(pos);
                if (tile instanceof IDataManagerProvider) {
                    ((IDataManagerProvider) tile).getDataManager().receiveSyncData(packet);
                }
                break;
            }
            case PacketDispatcher.C_TILE_MESSAGE: {
                BlockPos pos = packet.readPos();
                TileEntity tile = mc.world.getTileEntity(pos);
                if (tile instanceof TileBCore) {
                    int id = packet.readByte() & 0xFF;
                    ((TileBCore) tile).receivePacketFromServer(packet, id);
                }
                break;
            }
            case PacketDispatcher.C_SERVER_CONFIG_SYNC:
//                ModConfigParser.readConfigForSync(packet);
                break;
            case PacketDispatcher.C_NO_CLIP:
                boolean enable = packet.readBoolean();
                if (enable) {
                    BCEventHandler.noClipPlayers.add(mc.player.getUniqueID());
                }
                else {
                    BCEventHandler.noClipPlayers.add(mc.player.getUniqueID());
                }
                break;
            case PacketDispatcher.C_PLAYER_ACCESS:
                int windowID = packet.readInt();
                GuiPlayerAccess guiPlayerAccess = new GuiPlayerAccess(mc.player);
                mc.displayGuiScreen(guiPlayerAccess);
                mc.player.openContainer.windowId = windowID;
                break;
            case PacketDispatcher.C_PLAYER_ACCESS_UPDATE:
                GuiPlayerAccess gui = mc.currentScreen instanceof GuiPlayerAccess ? (GuiPlayerAccess) mc.currentScreen : null;
                if (gui != null) {
                    gui.name = packet.readString();
                    gui.pos = packet.readPos();
                    gui.dimension = packet.readInt();
                }
                break;
            case PacketDispatcher.C_INDEXED_LOCALIZED_CHAT:
                ChatHelper.indexedMsg(mc.player, I18n.format(packet.readString()), packet.readInt());
                break;
            case PacketDispatcher.C_TILE_CAP_DATA:
                BlockPos pos = packet.readPos();
                TileEntity tile = Minecraft.getInstance().world.getTileEntity(pos);
                if (tile instanceof TileBCore) {
                    ((TileBCore) tile).receiveCapSyncData(packet);
                }
                break;
        }
    }
}