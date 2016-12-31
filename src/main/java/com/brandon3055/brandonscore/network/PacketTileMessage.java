package com.brandon3055.brandonscore.network;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 26/3/2016.
 * This packet will be used to sync ISyncableObjects
 */
public class PacketTileMessage implements IMessage {
    public static final byte BOOLEAN_INDEX = 0;
    public static final byte BYTE_INDEX = 1;
    public static final byte INT_INDEX = 2;
    public static final byte DOUBLE_INDEX = 3;
    public static final byte FLOAT_INDEX = 4;
    public static final byte STRING_INDEX = 5;
    public static final byte TAG_INDEX = 6;

    public BlockPos tilePos;
    private byte pktIndex;
    public String stringValue = "";
    public float floatValue = 0F;
    public double doubleValue = 0;
    public int intValue = 0;
    public byte byteValue = 0;
    public boolean booleanValue = false;
    public NBTTagCompound compound;
    public byte dataType;

    public PacketTileMessage() {
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, boolean booleanValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.booleanValue = booleanValue;
        this.dataType = BOOLEAN_INDEX;
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, byte byteValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.byteValue = byteValue;
        this.dataType = BYTE_INDEX;
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, int intValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.intValue = intValue;
        this.dataType = INT_INDEX;
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, double doubleValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.doubleValue = doubleValue;
        this.dataType = DOUBLE_INDEX;
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, float floatValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.floatValue = floatValue;
        this.dataType = FLOAT_INDEX;
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, String stringValue, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.stringValue = stringValue;
        this.dataType = STRING_INDEX;
    }

    public PacketTileMessage(TileBCBase tile, byte pktIndex, NBTTagCompound compound, boolean updateOnReceived) {
        this.tilePos = tile.getPos();
        this.pktIndex = pktIndex;
        this.compound = compound;
        this.dataType = TAG_INDEX;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(dataType);
        buf.writeByte(pktIndex);

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
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dataType = buf.readByte();
        pktIndex = buf.readByte();

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
        }
    }

    public boolean isBool() {
        return dataType == BOOLEAN_INDEX;
    }

    public boolean isByte() {
        return dataType == BYTE_INDEX;
    }

    public boolean isInt() {
        return dataType == INT_INDEX;
    }

    public boolean isDouble() {
        return dataType == DOUBLE_INDEX;
    }

    public boolean isFload() {
        return dataType == FLOAT_INDEX;
    }

    public boolean isString() {
        return dataType == STRING_INDEX;
    }

    public boolean isNBT() {
        return dataType == TAG_INDEX && compound != null;
    }

    public byte getIndex() {
        return pktIndex;
    }

    public static class Handler extends MessageHandlerWrapper<PacketTileMessage, IMessage> {

        @Override
        public IMessage handleMessage(PacketTileMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                TileEntity tile = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.tilePos);
                if (tile instanceof TileBCBase) {
                    ((TileBCBase) tile).receivePacketFromClient(message, ctx.getServerHandler().playerEntity);
                }
            }
            else if (ctx.side == Side.CLIENT) {
                TileEntity tile = BrandonsCore.proxy.getClientWorld().getTileEntity(message.tilePos);
                if (tile instanceof TileBCBase) {
                    ((TileBCBase) tile).receivePacketFromServer(message);
                }
            }
            return null;
        }
    }
}
