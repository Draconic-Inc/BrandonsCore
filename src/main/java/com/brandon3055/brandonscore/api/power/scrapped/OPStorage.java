//package com.brandon3055.brandonscore.api.power.scrapped;
//
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraftforge.common.util.INBTSerializable;
//
///**
// * Created by brandon3055 on 14/8/19.
// *
// * Operational Potential is the power system used by Draconic Evolution and related mods.
// * This system is very similar to Forge Energy and Redstone Flux but there are two main differences.
// * Operational Potential is long based so it can more easily handle larger energy values and 1 OP is equivalent to 1000 RF/FE
// *
// * Conversion:
// * Conversion from OP to RF/FE is free and should be supported by anything that supplies OP.
// */
//public class OPStorage implements IOPStorage, INBTSerializable<NBTTagCompound> {
//
//    protected long capacity;
//    protected long maxReceive;
//    protected long maxExtract;
//    /**
//     * OP is stored here as RF because this makes it much easier to convert from OP to RF.
//     * This allows you to pull out any amount of RF as opposed multiples of 1000.
//     */
//    protected long storedRF;
//
//    public OPStorage(long capacity) {
//        this(capacity, capacity);
//    }
//
//    public OPStorage(long capacity, long maxTransfer) {
//        this(capacity, maxTransfer, maxTransfer, 0);
//    }
//
//    public OPStorage(long capacity, long maxReceive, long maxExtract) {
//        this(capacity, maxReceive, maxExtract, 0);
//    }
//
//    public OPStorage(long capacity, long maxReceive, long maxExtract, long storedOP) {
//        this.capacity = capacity;
//        this.maxReceive = maxReceive;
//        this.maxExtract = maxExtract;
//        this.storedRF = storedOP * 1000;
//    }
//
//    @Override
//    public long receiveOP(long maxReceive, boolean simulate) {
//        if (!canReceive()){
//            return 0;
//        }
//
//        long energyReceived = Math.min(capacity - getOPStored(), Math.min(this.maxReceive, maxReceive));
//        if (!simulate){
//            storedRF += energyReceived * 1000;
//        }
//        return energyReceived;
//    }
//
//    @Override
//    public long extractOP(long maxExtract, boolean simulate) {
//        if (!canExtract()){
//            return 0;
//        }
//
//        long energyExtracted = Math.min(getOPStored(), Math.min(this.maxExtract, maxExtract));
//        if (!simulate){
//            storedRF -= energyExtracted * 1000;
//        }
//        return energyExtracted;
//    }
//
//    public int extractRF(int maxExtract, boolean simulate) {
//        if (!canExtract()){
//            return 0;
//        }
//
//        long energyExtracted = Math.min(storedRF, Math.min(this.maxExtract * 1000, maxExtract));
//        if (!simulate){
//            storedRF -= energyExtracted;
//        }
//        return (int) energyExtracted;
//    }
//
//    @Override
//    public long getOPStored() {
//        return storedRF / 1000;
//    }
//
//    @Override
//    public long getMaxOPStored() {
//        return capacity;
//    }
//
//    @Override
//    public boolean canExtract() {
//        return maxExtract > 0;
//    }
//
//    @Override
//    public boolean canReceive() {
//        return maxReceive > 0;
//    }
//
//    /**
//     * This is a raw unchecked setter for the energy value.
//     * This is for internal use only. For things like saving and loading data.
//     *
//     * @param storedOP the new energy value.
//     */
//    public void setOPStored(long storedOP) {
//        this.storedRF = storedOP * 1000;
//    }
//
//    public void modifyOPStored(long amount) {
//        this.storedRF += amount * 1000;
//        if (getOPStored() > this.capacity) {
//            this.storedRF = this.capacity * 1000;
//        } else if (storedRF < 0) {
//            this.storedRF = 0;
//        }
//    }
//
//    public void modifyRFStored(long amount) {
//        this.storedRF += amount;
//        if (getOPStored() > this.capacity) {
//            this.storedRF = this.capacity * 1000;
//        } else if (storedRF < 0) {
//            this.storedRF = 0;
//        }
//    }
//
//    /**
//     * This is a raw unchecked setter for the capacity value.
//     * This is for internal use only. For things like saving and loading data.
//     *
//     * @param capacity the new capacity.
//     */
//    public void setCapacity(long capacity) {
//        this.capacity = capacity;
//    }
//
//    /**
//     * This is a raw unchecked setter for the maxExtract value.
//     * This is for internal use only. For things like saving and loading data.
//     *
//     * @param maxExtract the new max extract.
//     */
//    public void setMaxExtract(long maxExtract) {
//        this.maxExtract = maxExtract;
//    }
//
//    /**
//     * This is mostly for internal use. For things like saving and loading data.
//     */
//    public long getMaxExtract() {
//        return maxExtract;
//    }
//
//    /**
//     * This is a raw unchecked setter for the maxInsert value.
//     * This is for internal use only. For things like saving and loading data.
//     *
//     * @param maxReceive the new max insert.
//     */
//    public void setMaxReceive(long maxReceive) {
//        this.maxReceive = maxReceive;
//    }
//
//    /**
//     * This is mostly for internal use. For things like saving and loading data.
//     */
//    public long getMaxReceive() {
//        return maxReceive;
//    }
//
//    @Override
//    public NBTTagCompound serializeNBT() {
//        NBTTagCompound compound = new NBTTagCompound();
//        compound.setLong("stored_rf", storedRF);
//        compound.setLong("capacity", capacity);
//        compound.setLong("max_receive", maxReceive);
//        compound.setLong("max_extract", maxExtract);
//        return compound;
//    }
//
//    @Override
//    public void deserializeNBT(NBTTagCompound nbt) {
//        storedRF = nbt.getLong("stored_rf");
//        capacity = nbt.getLong("capacity");
//        maxReceive = nbt.getLong("max_receive");
//        maxExtract = nbt.getLong("max_extract");
//    }
//}
