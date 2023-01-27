package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;

/**
 * Created by brandon3055 on 20/3/20.
 */
public class OPIOControl implements IOPStorage {

    protected boolean allowExtract = true;
    protected boolean allowReceive = true;
    private IOPStorage wrapped;

    public OPIOControl(IOPStorage wrapped) {
        this.wrapped = wrapped;
    }

    public OPIOControl setIOMode(boolean allowExtract, boolean allowReceive) {
        this.allowExtract = allowExtract;
        this.allowReceive = allowReceive;
        return this;
    }

    public OPIOControl setExtractOnly() {
        return setIOMode(true, false);
    }

    public OPIOControl setReceiveOnly() {
        return setIOMode(false, true);
    }

    /**
     * @param inputOutput true = Input Only, false = Output Only.
     */
    public OPIOControl setIOMode(boolean inputOutput) {
        return setIOMode(!inputOutput, inputOutput);
    }

    @Override
    public boolean canExtract() {
        return allowExtract && wrapped.canExtract();
    }

    @Override
    public boolean canReceive() {
        return allowReceive && wrapped.canReceive();
    }

    @Override
    public long maxExtract() {
        return wrapped.maxExtract();
    }

    @Override
    public long maxReceive() {
        return wrapped.maxReceive();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return wrapped.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return wrapped.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return wrapped.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return wrapped.getMaxEnergyStored();
    }

    @Override
    public long modifyEnergyStored(long amount) {
        return wrapped.modifyEnergyStored(amount);
    }

    @Override
    public long receiveOP(long maxReceive, boolean simulate) {
        return wrapped.receiveOP(maxReceive, simulate);
    }

    @Override
    public long extractOP(long maxExtract, boolean simulate) {
        return wrapped.extractOP(maxExtract, simulate);
    }

    @Override
    public long getOPStored() {
        return wrapped.getOPStored();
    }

    @Override
    public long getMaxOPStored() {
        return wrapped.getMaxOPStored();
    }
}
