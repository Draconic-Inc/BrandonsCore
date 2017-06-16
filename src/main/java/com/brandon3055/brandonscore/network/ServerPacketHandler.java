package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ServerPacketHandler implements ICustomPacketHandler.IServerPacketHandler {
    @Override
    public void handlePacket(PacketCustom packetCustom, EntityPlayerMP sender, INetHandlerPlayServer handler) {
        if (packetCustom.getType() == PacketDispatcher.S_TILE_MESSAGE) {
            BlockPos pos = packetCustom.readPos();
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
            if (tile instanceof TileBCBase) {
                ((TileBCBase) tile).receivePacketFromClient(packetCustom, sender);
            }
        }
    }
}