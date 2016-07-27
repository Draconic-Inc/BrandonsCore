package com.brandon3055.brandonscore.network;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 27/07/2016.
 */
public class PacketTickTime implements IMessage {

    public Map<Integer, Integer> tickTimes;
    public int overall;

    public PacketTickTime() {}

    public PacketTickTime(Map<Integer, Integer> dimTimes, int overall) {
        this.tickTimes = dimTimes;
        this.overall = overall;
    }


    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(overall - Integer.MAX_VALUE);
        buf.writeShort(tickTimes.size());
        for (Integer dim : tickTimes.keySet()){
            buf.writeShort(dim);
            buf.writeShort(tickTimes.get(dim) - Short.MAX_VALUE);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        overall = buf.readInt() + Integer.MAX_VALUE;

        tickTimes = new HashMap<Integer, Integer>();
        int dimCount = buf.readShort();
        for (int i = 0; i < dimCount; i++) {
            tickTimes.put((int)buf.readShort(), buf.readShort() + Short.MAX_VALUE);
        }
    }

    public static class Handler extends MessageHandlerWrapper<PacketTickTime, IMessage> {

        @Override
        public IMessage handleMessage(PacketTickTime message, MessageContext ctx) {

            if (ctx.side == Side.CLIENT) {
                BCClientEventHandler.handleTickPacket(message);
            }

            return null;
        }
    }
}
