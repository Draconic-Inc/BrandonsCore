package com.brandon3055.brandonscore.network.wrappers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 26/3/2016.
 */
public class SyncableEnum<T extends Enum<T>> extends SyncableObject {

    public T value;
    public T lastTickValue;
    public Map<Integer, T> indexToValue = new HashMap<>();
    public Map<T, Integer> valueToIndex = new HashMap<>();

    public SyncableEnum(T value, boolean syncInTile, boolean syncInContainer, boolean updateOnReceived) {
        super(syncInTile, syncInContainer, updateOnReceived);
        this.value = this.lastTickValue = value;
        T[] v = value.getDeclaringClass().getEnumConstants();
        if (v.length > 255) {
            throw new RuntimeException("Max enum size supported by SyncableEnum is 255");
        }
        for (int i = 0; i < v.length; i++) {
            this.indexToValue.put(i, v[i]);
            this.valueToIndex.put(v[i], i);
        }
    }

    public SyncableEnum(T value, boolean syncInTile, boolean syncInContainer) {
        super(syncInTile, syncInContainer);
        this.value = this.lastTickValue = value;
        T[] v = value.getDeclaringClass().getEnumConstants();
        if (v.length > 255) {
            throw new RuntimeException("Max enum size supported by SyncableEnum is 255");
        }
        for (int i = 0; i < v.length; i++) {
            this.indexToValue.put(i, v[i]);
            this.valueToIndex.put(v[i], i);
        }
    }

    @Override
    public void detectAndSendChanges(TileBCBase tile, EntityPlayer player, boolean forceSync) {
        if (lastTickValue != value || forceSync) {
            lastTickValue = value;
            tile.dirtyBlock();

            if (player == null) {
                BrandonsCore.network.sendToAllAround(new PacketSyncableObject(tile, index, (byte) ((int) valueToIndex.get(value)), updateOnReceived), tile.syncRange());
            } else if (player instanceof EntityPlayerMP) {
                BrandonsCore.network.sendTo(new PacketSyncableObject(tile, index, (byte) ((int) valueToIndex.get(value)), updateOnReceived), (EntityPlayerMP) player);
            }
        }
    }

    @Override
    public void updateReceived(PacketSyncableObject packet) {
        if (packet.dataType == PacketSyncableObject.BYTE_INDEX) {
            value = indexToValue.get(packet.byteValue & 0xFF);
        }
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setByte("SyncableEnum" + index, (byte) ((int) valueToIndex.get(value)));
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        if (compound.hasKey("SyncableEnum" + index)) {
            value = indexToValue.get(compound.getByte("SyncableEnum" + index) & 0xFF);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
