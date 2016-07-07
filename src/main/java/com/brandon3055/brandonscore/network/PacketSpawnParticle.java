package com.brandon3055.brandonscore.network;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.particle.BCEffectHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 23/4/2016.
 */
public class PacketSpawnParticle implements IMessage {

    private int particleID;
    private double xCoord;
    private double yCoord;
    private double zCoord;
    private double xSpeed;
    private double ySpeed;
    private double zSpeed;
    private double viewRange;
    private int[] args;

    public PacketSpawnParticle() {
    }

    public PacketSpawnParticle(int particleID, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, double viewRange, int... args) {
        this.particleID = particleID;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
        this.viewRange = viewRange;
        this.args = args;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(particleID);
        buf.writeDouble(xCoord);
        buf.writeDouble(yCoord);
        buf.writeDouble(zCoord);
        buf.writeDouble(xSpeed);
        buf.writeDouble(ySpeed);
        buf.writeDouble(zSpeed);
        buf.writeDouble(viewRange);
        buf.writeByte(args.length);
        for (int i : args) {
            buf.writeInt(i);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        particleID = buf.readInt();
        xCoord = buf.readDouble();
        yCoord = buf.readDouble();
        zCoord = buf.readDouble();
        xSpeed = buf.readDouble();
        ySpeed = buf.readDouble();
        zSpeed = buf.readDouble();
        viewRange = buf.readDouble();
        int argsL = buf.readByte();
        args = new int[argsL];
        for (int i = 0; i < argsL; i++) {
            args[i] = buf.readInt();
        }

    }

    public static class Handler extends MessageHandlerWrapper<PacketSpawnParticle, IMessage> {

        @Override
        public IMessage handleMessage(PacketSpawnParticle message, MessageContext ctx) {

            if (ctx.side == Side.CLIENT) {
                BCEffectHandler.spawnFX(message.particleID, BrandonsCore.proxy.getClientWorld(), message.xCoord, message.yCoord, message.zCoord, message.xSpeed, message.ySpeed, message.zSpeed, message.viewRange, message.args);
            }

            return null;
        }
    }
}
