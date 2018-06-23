package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.client.gui.GuiPlayerAccess;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        switch (packet.getType()) {
            case PacketDispatcher.C_TILE_DATA_MANAGER: {
                BlockPos pos = packet.readPos();
                TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
                if (tile instanceof IDataManagerProvider) {
                    ((IDataManagerProvider) tile).getDataManager().receiveSyncData(packet);
                }
                break;
            }
            case PacketDispatcher.C_TILE_MESSAGE: {
                BlockPos pos = packet.readPos();
                TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
                if (tile instanceof TileBCBase) {
                    int id = packet.readByte() & 0xFF;
                    ((TileBCBase) tile).receivePacketFromServer(packet, id);
                }
                break;
            }
            case PacketDispatcher.C_SERVER_CONFIG_SYNC:
                ModConfigParser.readConfigForSync(packet);
                break;
            case PacketDispatcher.C_NO_CLIP:
                boolean enable = packet.readBoolean();
                if (enable) {
                    BCEventHandler.noClipPlayers.add(mc.player.getName());
                }
                else {
                    BCEventHandler.noClipPlayers.add(mc.player.getName());
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
                ChatHelper.indexedMsg(mc.player, I18n.format(packet.readString()));
                break;
        }
    }
}