package com.brandon3055.brandonscore.api.power;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.lib.IValueHashable;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
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
public class OPStorage implements IOPStorage, INBTSerializable<CompoundNBT>, IValueHashable<OPStorage.ComparableValue>, IMCDataSerializable {

    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;
    protected IOTracker ioTracker;
    protected boolean allowExtract = true;
    protected boolean allowReceive = true;

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

    public OPStorage setIOMode(boolean allowExtract, boolean allowReceive) {
        this.allowExtract = allowExtract;
        this.allowReceive = allowReceive;
        return this;
    }

    public OPStorage setExtractOnly() {
        return setIOMode(true, false);
    }

    public OPStorage setReceiveOnly() {
        return setIOMode(false, true);
    }

    /**
     * @param inputOutput true = Input Only, false = Output Only.
     */
    public OPStorage setIOMode(boolean inputOutput) {
        return setIOMode(!inputOutput, inputOutput);
    }

    @Override
    public long receiveOP(long maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }

        long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            if (ioTracker != null) {
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
            if (ioTracker != null) {
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
        return allowExtract && maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return allowReceive && maxReceive > 0;
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

    public long modifyEnergyStored(long amount) {
        if (amount > capacity - energy) {
            amount = capacity - energy;
        } else if (amount < -energy) {
            amount = -energy;
        }

        energy += amount;
        if (ioTracker != null) {
            ioTracker.energyModified(amount);
        }
        return Math.abs(amount);
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
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        smartWrite("energy", energy, compound);
//        smartWrite("capacity", capacity, compound);  On second thought i think its better if the tile has full control over this.
//        smartWrite("max_receive", maxReceive, compound);
//        smartWrite("max_extract", maxExtract, compound);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        energy = smartRead("energy", nbt);
//        capacity = smartRead("capacity", nbt);
//        maxReceive = smartRead("max_receive", nbt);
//        maxExtract = smartRead("max_extract", nbt);
    }

    private void smartWrite(String name, long value, CompoundNBT compound) {
        if (value > Integer.MAX_VALUE) {
            compound.putLong(name, value);
        } else {
            compound.putInt(name, (int) value);
        }
    }

    private long smartRead(String name, CompoundNBT compound) {
        INBT tag = compound.get(name);
        if (tag instanceof NumberNBT) {
            return ((NumberNBT) tag).getLong();
        }
        return 0;
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        output.writeVarLong(energy);
        output.writeVarLong(capacity);
        output.writeVarLong(maxReceive);
        output.writeVarLong(maxExtract);
        output.writeBoolean(ioTracker != null);
        if (ioTracker != null) {
            output.writeVarLong(ioTracker.currentInput());
            output.writeVarLong(ioTracker.currentOutput());
        }
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        energy = input.readVarLong();
        capacity = input.readVarLong();
        maxReceive = input.readVarLong();
        maxExtract = input.readVarLong();
        if (input.readBoolean()) {
            if (ioTracker == null) {
                ioTracker = new IOTrackerSelfTimed();
            }
            ioTracker.syncClientValues(input.readVarLong(), input.readVarLong());
        } else if (ioTracker != null) {
            ioTracker = null;
        }
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
            if (ioTracker != null) {
                return mainCheck && v.currentInput == ioTracker.currentInput() && v.currentOutput == ioTracker.currentOutput();
            }
            return mainCheck;
        }

        return false;
    }

    public void setIOTracker(@Nullable IOTracker ioTracker) {
        this.ioTracker = ioTracker;
    }


    @Nullable
    @Override
    public IOInfo getIOInfo() {
        return ioTracker;
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
            if (storage.ioTracker != null) {
                currentInput = storage.ioTracker.currentInput();
                currentOutput = storage.ioTracker.currentOutput();
            }
        }
    }
}
