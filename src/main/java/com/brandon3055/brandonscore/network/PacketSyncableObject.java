package com.brandon3055.brandonscore.network;

import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.lib.Vec3I;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 26/3/2016.
 * This packet will be used to sync ISyncableObjects
 */
public class PacketSyncableObject implements IMessage {
    public static final byte BOOLEAN_INDEX = 0;
    public static final byte BYTE_INDEX = 1;
    public static final byte INT_INDEX = 2;
    public static final byte DOUBLE_INDEX = 3;
    public static final byte FLOAT_INDEX = 4;
    public static final byte STRING_INDEX = 5;
    public static final byte TAG_INDEX = 6;
    public static final byte VEC3I_INDEX = 7;
    public static final byte LONG_INDEX = 8;
    public static final byte SHORT_INDEX = 9;
    public static final byte VEC3D_INDEX = 10;


    public BlockPos tilePos;
    public byte index;
    public String stringValue = "";
    public float floatValue = 0F;
    public double doubleValue = 0;
    public int intValue = 0;
    public short shortValue = 0;
    public byte byteValue = 0;
    public boolean booleanValue = false;
    public NBTTagCompound compound;
    public Vec3I vec3I;
    public Vec3D vec3D;
    public long longValue;
    public boolean updateOnReceived;
    public byte dataType;

    public PacketSyncableObject() {
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, boolean booleanValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.booleanValue = booleanValue;
        this.dataType = BOOLEAN_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, byte byteValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.byteValue = byteValue;
        this.dataType = BYTE_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, short shortValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.shortValue = shortValue;
        this.dataType = SHORT_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, int intValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.intValue = intValue;
        this.dataType = INT_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, double doubleValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.doubleValue = doubleValue;
        this.dataType = DOUBLE_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, float floatValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.floatValue = floatValue;
        this.dataType = FLOAT_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, String stringValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.stringValue = stringValue;
        this.dataType = STRING_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, NBTTagCompound compound, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.compound = compound;
        this.dataType = TAG_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, Vec3I vec3I, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.vec3I = vec3I;
        this.dataType = VEC3I_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, long longValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.longValue = longValue;
        this.dataType = LONG_INDEX;
    }

    public PacketSyncableObject(TileBCBase tile, byte syncableIndex, Vec3D vec3D, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.index = syncableIndex;
        this.vec3D = vec3D;
        this.dataType = VEC3D_INDEX;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(dataType);
        buf.writeByte(index);

        buf.writeInt(tilePos.getX());
        buf.writeInt(tilePos.getY());
        buf.writeInt(tilePos.getZ());


        switch (dataType) {
            case BOOLEAN_INDEX:
                buf.writeBoolean(booleanValue);
                break;
            case BYTE_INDEX:
                buf.writeByte(byteValue);
                break;
            case INT_INDEX:
                buf.writeInt(intValue);
                break;
            case DOUBLE_INDEX:
                buf.writeDouble(doubleValue);
                break;
            case FLOAT_INDEX:
                buf.writeFloat(floatValue);
                break;
            case STRING_INDEX:
                ByteBufUtils.writeUTF8String(buf, stringValue);
                break;
            case TAG_INDEX:
                ByteBufUtils.writeTag(buf, compound);
                break;
            case VEC3I_INDEX:
                buf.writeInt(vec3I.x);
                buf.writeInt(vec3I.y);
                buf.writeInt(vec3I.z);
                break;
            case VEC3D_INDEX:
                buf.writeDouble(vec3D.x);
                buf.writeDouble(vec3D.y);
                buf.writeDouble(vec3D.z);
                break;
            case LONG_INDEX:
                buf.writeLong(longValue);
                break;
            case SHORT_INDEX:
                buf.writeShort(shortValue);
                break;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dataType = buf.readByte();
        index = buf.readByte();

        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        tilePos = new BlockPos(x, y, z);


        switch (dataType) {
            case BOOLEAN_INDEX:
                booleanValue = buf.readBoolean();
                break;
            case BYTE_INDEX:
                byteValue = buf.readByte();
                break;
            case INT_INDEX:
                intValue = buf.readInt();
                break;
            case DOUBLE_INDEX:
                doubleValue = buf.readDouble();
                break;
            case FLOAT_INDEX:
                floatValue = buf.readFloat();
                break;
            case STRING_INDEX:
                stringValue = ByteBufUtils.readUTF8String(buf);
                break;
            case TAG_INDEX:
                compound = ByteBufUtils.readTag(buf);
                break;
            case VEC3I_INDEX:
                vec3I = new Vec3I(0, 0, 0);
                vec3I.x = buf.readInt();
                vec3I.y = buf.readInt();
                vec3I.z = buf.readInt();
                break;
            case VEC3D_INDEX:
                vec3D = new Vec3D(0, 0, 0);
                vec3D.x = buf.readDouble();
                vec3D.y = buf.readDouble();
                vec3D.z = buf.readDouble();
                break;
            case LONG_INDEX:
                longValue = buf.readLong();
                break;
            case SHORT_INDEX:
                shortValue = buf.readShort();
                break;
        }
    }

    public static class Handler extends MessageHandlerWrapper<PacketSyncableObject, IMessage> {

        @Override
        public IMessage handleMessage(PacketSyncableObject message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                TileEntity tile = FMLClientHandler.instance().getWorldClient().getTileEntity(message.tilePos);
                if (tile instanceof TileBCBase) {
                    ((TileBCBase) tile).receiveSyncPacketFromServer(message);
                }
            }
            return null;
        }
    }
}
