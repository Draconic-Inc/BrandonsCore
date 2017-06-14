package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packetCustom, Minecraft mc, INetHandlerPlayClient handler) {
        if (packetCustom.readerIndex() == PacketDispatcher.C_TILE_DATA_MANAGER) {
            BlockPos pos = packetCustom.readPos();
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
            if (tile instanceof IDataManagerProvider) {
                ((IDataManagerProvider) tile).getDataManager().receiveSyncData(packetCustom);
            }
        }
        else if (packetCustom.readerIndex() == PacketDispatcher.C_TILE_MESSAGE) {
            BlockPos pos = packetCustom.readPos();
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
            if (tile instanceof TileBCBase) {
                ((TileBCBase) tile).receivePacketFromServer(packetCustom);
            }
        }
    }
}