package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.GuiPlayerAccess;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, IClientPlayNetHandler handler) {
        switch (packet.getType()) {
            case BCoreNetwork.C_TILE_DATA_MANAGER: {
                BlockPos pos = packet.readPos();
                TileEntity tile = mc.world.getTileEntity(pos);
                if (tile instanceof IDataManagerProvider) {
                    ((IDataManagerProvider) tile).getDataManager().receiveSyncData(packet);
                }
                break;
            }
            case BCoreNetwork.C_TILE_MESSAGE: {
                BlockPos pos = packet.readPos();
                TileEntity tile = mc.world.getTileEntity(pos);
                if (tile instanceof TileBCore) {
                    int id = packet.readByte() & 0xFF;
                    ((TileBCore) tile).receivePacketFromServer(packet, id);
                }
                break;
            }
            case BCoreNetwork.C_SERVER_CONFIG_SYNC:
//                ModConfigParser.readConfigForSync(packet);
                break;
            case BCoreNetwork.C_NO_CLIP:
                boolean enable = packet.readBoolean();
                if (enable) {
                    BCEventHandler.noClipPlayers.add(mc.player.getUniqueID());
                } else {
                    BCEventHandler.noClipPlayers.add(mc.player.getUniqueID());
                }
                break;
            case BCoreNetwork.C_PLAYER_ACCESS:
                //I dont think i need this any more?
//                int windowID = packet.readInt();
//                GuiPlayerAccess guiPlayerAccess = new GuiPlayerAccess(mc.player);
//                mc.displayGuiScreen(guiPlayerAccess);
//                mc.player.openContainer.windowId = windowID;
                break;
            case BCoreNetwork.C_PLAYER_ACCESS_UPDATE:
                GuiPlayerAccess gui = mc.currentScreen instanceof GuiPlayerAccess ? (GuiPlayerAccess) mc.currentScreen : null;
                if (gui != null) {
                    gui.name = packet.readString();
                    gui.pos = packet.readPos();
                    gui.dimension = packet.readInt();
                }
                break;
            case BCoreNetwork.C_INDEXED_MESSAGE:
                BrandonsCore.proxy.sendIndexedMessage(mc.player, packet.readTextComponent(), packet.readInt());
                break;
            case BCoreNetwork.C_TILE_CAP_DATA:
                BlockPos pos = packet.readPos();
                TileEntity tile = Minecraft.getInstance().world.getTileEntity(pos);
                if (tile instanceof TileBCore) {
                    ((TileBCore) tile).getCapManager().receiveCapSyncData(packet);
                }
                break;
            case BCoreNetwork.C_PLAY_SOUND:
                handlePlaySound(packet, mc);
                break;
        }
    }

    private static void handlePlaySound(PacketCustom packet, Minecraft mc) {
        if (mc.world == null) return;
        BlockPos pos = packet.readPos();
        SoundEvent sound = packet.readRegistryId();
        SoundCategory category = SoundCategory.values()[packet.readVarInt()];
        float volume = packet.readFloat();
        float pitch = packet.readFloat();
        boolean distanceDelay = packet.readBoolean();
        mc.world.playSound(pos, sound, category, volume, pitch, distanceDelay);
    }
}