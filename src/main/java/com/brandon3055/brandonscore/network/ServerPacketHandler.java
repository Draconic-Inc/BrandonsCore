package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ServerPacketHandler implements ICustomPacketHandler.IServerPacketHandler {
    @Override
    public void handlePacket(PacketCustom packet, EntityPlayerMP sender, INetHandlerPlayServer handler) {
        try {
            if (packet.getType() == PacketDispatcher.S_TILE_MESSAGE) {
                BlockPos pos = packet.readPos();
                TileEntity tile = sender.world.getTileEntity(pos);
                if (tile instanceof TileBCBase && ((TileBCBase) tile).verifyPlayerPermission(sender)) {
                    int id = packet.readByte() & 0xFF;
                    ((TileBCBase) tile).receivePacketFromClient(packet, sender, id);
                }
            }
        }
        catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to read a packet sent from this client: " + sender);
            e.printStackTrace();
        }
    }
}