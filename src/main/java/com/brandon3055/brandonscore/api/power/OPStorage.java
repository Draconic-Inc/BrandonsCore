package com.brandon3055.brandonscore.api.power;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import com.brandon3055.brandonscore.lib.IValueHashable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
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
public class OPStorage implements INBTSerializable<CompoundTag>, IValueHashable<OPStorage.ComparableValue>, IMCDataSerializable, IOPStorage {

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
        this(capacity, maxTransfer, maxTransfer);
    }

    public OPStorage(long capacity, long maxReceive, long maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Deprecated
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

        long energyReceived = Math.min(getMaxOPStored() - energy, Math.min(this.maxReceive(), maxReceive));
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

        long energyExtracted = Math.min(energy, Math.min(this.maxExtract(), maxExtract));
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
        return allowExtract && maxExtract() > 0;
    }

    @Override
    public boolean canReceive() {
        return allowReceive && maxReceive() > 0;
    }

    @Override
    public long modifyEnergyStored(long amount) {
        if (amount > getMaxOPStored() - energy) {
            amount = getMaxOPStored() - energy;
        } else if (amount < -energy) {
            amount = -energy;
        }

        energy += amount;
        if (ioTracker != null) {
            ioTracker.energyModified(amount);
        }
        return Math.abs(amount);
    }

    @Override
    public long maxExtract() {
        return maxExtract;
    }

    @Override
    public long maxReceive() {
        return maxReceive;
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
     * This is a raw unchecked setter for the maxInsert value.
     * This is for internal use only. For things like saving and loading data.
     *
     * @param maxReceive the new max insert.
     */
    public OPStorage setMaxReceive(long maxReceive) {
        this.maxReceive = maxReceive;
        return this;
    }

    public OPStorage setMaxTransfer(long maxTransfer) {
        this.maxReceive = this.maxExtract = maxTransfer;
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        smartWrite("energy", energy, compound);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        energy = smartRead("energy", nbt);
    }

    private void smartWrite(String name, long value, CompoundTag compound) {
        if (value > Integer.MAX_VALUE) {
            compound.putLong(name, value);
        } else {
            compound.putInt(name, (int) value);
        }
    }

    private long smartRead(String name, CompoundTag compound) {
        Tag tag = compound.get(name);
        if (tag instanceof NumericTag) {
            return ((NumericTag) tag).getAsLong();
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
