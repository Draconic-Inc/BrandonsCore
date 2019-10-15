package com.brandon3055.brandonscore.api.power;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.lib.IValueHashable;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 14/8/19.
 * <p>
 * Operational Potential is the power system used by Draconic Evolution and related mods.
 * This system is an extension of Forge Energy that allows long based power transfer and storage.
 * <p>
 * When implementing this capability cap should be provided as both {@link net.minecraftforge.energy.CapabilityEnergy#ENERGY} and {@link CapabilityOP#OP}
 * So any mod that implements FE will find the FE cap and interact with it normally. However any mod that implements OP will fist check for the OP cap before falling back to RF.
 */
public class OPStorage implements IOPStorage, INBTSerializable<NBTTagCompound>, IValueHashable<OPStorage.ComparableValue>, IMCDataSerializable {

    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;
    protected IOTracker ioTracker = new IOTracker();
    protected boolean ioTrackerEnabled = true;

    public OPStorage(long capacity) {
        this(capacity, capacity);
    }

    public OPStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public OPStorage(long capacity, long maxReceive, long maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public OPStorage(long capacity, long maxReceive, long maxExtract, long energy) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = energy;
    }

    @Override
    public long receiveOP(long maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }

        long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            if (ioTrackerEnabled && ioTracker != null) {
                ioTracker.energyInserted(energyReceived);
            }
        }
        return energyReceived;
    }

    @Override
    public long extractOP(long maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }

        long energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            if (ioTrackerEnabled && ioTracker != null) {
                ioTracker.energyExtracted(energyExtracted);
            }
        }
        return energyExtracted;
    }

    @Override
    public long getOPStored() {
        return energy;
    }

    @Override
    public long getMaxOPStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }

    /**
     * This is a raw unchecked setter for the energy value.
     * This is for internal use only. For things like saving and loading data.
     *
     * @param storedOP the new energy value.
     */
    public OPStorage setOPStored(long storedOP) {
        this.energy = storedOP * 1000;
        return this;
    }

    public OPStorage modifyEnergyStored(long amount) {
        if (amount > capacity - energy) {
            amount = capacity - energy;
        }
        else if (amount < -energy) {
            amount = -energy;
        }

        energy += amount;
        if (ioTrackerEnabled && ioTracker != null) {
            ioTracker.energyModified(amount);
        }
        return this;
    }

    /**
     * This is a raw unchecked setter for the capacity value.
     * This is for internal use only. For things like saving and loading data.
     *
     * @param capacity the new capacity.
     */
    public OPStorage setCapacity(long capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * This is a raw unchecked setter for the maxExtract value.
     * This is for internal use only. For things like saving and loading data.
     *
     * @param maxExtract the new max extract.
     */
    public OPStorage setMaxExtract(long maxExtract) {
        this.maxExtract = maxExtract;
        return this;
    }

    /**
     * This is mostly for internal use. For things like saving and loading data.
     */
    public long getMaxExtract() {
        return maxExtract;
    }

    /**
     * This is a raw unchecked setter for the maxInsert value.
     * This is for internal use only. For things like saving and loading data.
     *
     * @param maxReceive the new max insert.
     */
    public OPStorage setMaxReceive(long maxReceive) {
        this.maxReceive = maxReceive;
        return this;
    }

    /**
     * This is mostly for internal use. For things like saving and loading data.
     */
    public long getMaxReceive() {
        return maxReceive;
    }

    public OPStorage setMaxTransfer(long maxTransfer) {
        this.maxReceive = this.maxExtract = maxTransfer;
        return this;
    }

    //FE Methods

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) receiveOP(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return (int) extractOP(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(getOPStored(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(getMaxOPStored(), Integer.MAX_VALUE);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        smartWrite("energy", energy, compound);
        smartWrite("capacity", capacity, compound);
        smartWrite("max_receive", maxReceive, compound);
        smartWrite("max_extract", maxExtract, compound);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        energy = smartRead("energy", nbt);
        capacity = smartRead("capacity", nbt);
        maxReceive = smartRead("max_receive", nbt);
        maxExtract = smartRead("max_extract", nbt);
    }

    private void smartWrite(String name, long value, NBTTagCompound compound) {
        if (value > Integer.MAX_VALUE) {
            compound.setLong(name, value);
        }
        else {
            compound.setInteger(name, (int) value);
        }
    }

    private long smartRead(String name, NBTTagCompound compound) {
        NBTBase tag = compound.getTag(name);
        if (tag instanceof NBTPrimitive) {
            return ((NBTPrimitive) tag).getLong();
        }
        return 0;
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        output.writeVarLong(energy);
        output.writeVarLong(capacity);
        output.writeVarLong(maxReceive);
        output.writeVarLong(maxExtract);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        energy = input.readVarLong();
        capacity = input.readVarLong();
        maxReceive = input.readVarLong();
        maxExtract = input.readVarLong();
    }

    @Override
    public ComparableValue getValueHash() {
        return new ComparableValue(this);
    }

    @Override
    public boolean checkValueHash(Object vh) {
        if (vh instanceof ComparableValue) {
            ComparableValue v = (ComparableValue) vh;
            boolean mainCheck = v.energy == energy && v.capacity == capacity && v.maxExtract == maxExtract && v.maxReceive == maxReceive;
            if (ioTrackerEnabled && ioTracker != null) {
                return mainCheck && v.currentInput == ioTracker.currentInput() && v.currentOutput == ioTracker.currentOutput();
            }
            return mainCheck;
        }

        return false;
    }

    public void setIoTracker(@Nullable IOTracker ioTracker) {
        this.ioTracker = ioTracker;
    }

    public void setIoTrackerEnabled(boolean ioTrackerEnabled) {
        this.ioTrackerEnabled = ioTrackerEnabled;
    }

    @Nullable
    @Override
    public IOInfo getIOInfo() {
        return ioTrackerEnabled ? ioTracker : null;
    }

    protected static class ComparableValue {
        private final long energy;
        private final long capacity;
        private final long maxReceive;
        private final long maxExtract;
        private long currentInput = 0;
        private long currentOutput = 0;

        public ComparableValue(OPStorage storage) {
            energy = storage.energy;
            capacity = storage.capacity;
            maxReceive = storage.maxReceive;
            maxExtract = storage.maxExtract;
            if (storage.ioTrackerEnabled && storage.ioTracker != null) {
                currentInput = storage.ioTracker.currentInput();
                currentOutput = storage.ioTracker.currentOutput();
            }
        }
    }
}
