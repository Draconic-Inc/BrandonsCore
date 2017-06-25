package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        if (packet.getType() == PacketDispatcher.C_TILE_DATA_MANAGER) {
            BlockPos pos = packet.readPos();
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
            if (tile instanceof IDataManagerProvider) {
                ((IDataManagerProvider) tile).getDataManager().receiveSyncData(packet);
            }
        }
        else if (packet.getType() == PacketDispatcher.C_TILE_MESSAGE) {
            BlockPos pos = packet.readPos();
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
            if (tile instanceof TileBCBase) {
                int id = packet.readByte() & 0xFF;
                ((TileBCBase) tile).receivePacketFromServer(packet, id);
            }
        }
        else if (packet.getType() == PacketDispatcher.C_SERVER_CONFIG_SYNC) {
            ModConfigParser.readConfigForSync(packet);
        }
    }
}