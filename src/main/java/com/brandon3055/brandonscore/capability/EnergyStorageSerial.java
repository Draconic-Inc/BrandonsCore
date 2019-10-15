package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.lib.IValueHashable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

/**
 * Created by brandon3055 on 6/9/19.
 * <p>
 * aka EnergyStorageNBTSerializable
 */
public class EnergyStorageSerial extends EnergyStorage implements INBTSerializable<NBTTagCompound>, IValueHashable<EnergyStorageSerial.ComparableValue> {

    public EnergyStorageSerial(int capacity) {
        super(capacity);
    }

    public EnergyStorageSerial(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public EnergyStorageSerial(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public EnergyStorageSerial(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("energy", energy);
        compound.setInteger("capacity", capacity);
        compound.setInteger("max_receive", maxReceive);
        compound.setInteger("max_extract", maxExtract);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        energy = nbt.getInteger("energy");
        capacity = nbt.getInteger("capacity");
        maxReceive = nbt.getInteger("max_receive");
        maxExtract = nbt.getInteger("max_extract");
    }

    @Override
    public ComparableValue getValueHash() {
        return new ComparableValue(this);
    }

    @Override
    public boolean checkValueHash(Object vh) {
        if (vh instanceof ComparableValue) {
            ComparableValue v = (ComparableValue) vh;
            return v.energy == energy && v.capacity == capacity && v.maxExtract == maxExtract && v.maxReceive == maxReceive;
        }

        return false;
    }

    protected static class ComparableValue {
        private final int energy;
        private final int capacity;
        private final int maxReceive;
        private final int maxExtract;

        public ComparableValue(EnergyStorageSerial storage) {
            energy = storage.energy;
            capacity = storage.capacity;
            maxReceive = storage.maxReceive;
            maxExtract = storage.maxExtract;
        }
    }
}
