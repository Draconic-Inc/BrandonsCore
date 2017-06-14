package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedEnum<T extends Enum<T>> extends AbstractManagedData {

    public T value;
    public T lastTickValue;
    public Map<Integer, T> indexToValue = new HashMap<>();
    public Map<T, Integer> valueToIndex = new HashMap<>();

    public ManagedEnum(T value) {
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
    public boolean detectChanges() {
        if (value != lastTickValue) {
            lastTickValue = value;
            return true;
        }
        return false;
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writeByte(valueToIndex.get(value).byteValue());
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = indexToValue.get(input.readByte() & 0xFF);
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setByte(name, valueToIndex.get(value).byteValue());
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = indexToValue.get(compound.getByte(name) & 0xFF);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
