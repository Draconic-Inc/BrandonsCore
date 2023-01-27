package com.brandon3055.brandonscore.api.power;

import com.brandon3055.brandonscore.capability.CapabilityOP;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 14/8/19.
 * <p>
 * Operational Potential is the power system used by Draconic Evolution and related mods.
 * This system is an extension of Forge Energy that allows long based power transfer and storage.
 * <p>
 * When implementing this capability cap should be provided as both {@link net.minecraftforge.energy.CapabilityEnergy#ENERGY} and {@link CapabilityOP#OP}
 * So any mod that implements FE will find the FE cap and interact with it normally. However any mod that implements OP will fist check for the OP cap before falling back to RF.
 * <p>
 * When creating an energy storage that extends this you MUST override ether the fe, op or both sets of io/storage methods.
 * Not doing so will result in a stack overflow because by default they just map to each other.
 */
public interface IOPStorage extends IEnergyStorage {
    /**
     * Adds operational potential to the storage. Returns quantity of operational potential that was accepted.
     *
     * @param maxReceive Maximum amount of operational potential to be inserted.
     * @param simulate   If TRUE, the insertion will only be simulated.
     * @return Amount of operational potential that was (or would have been, if simulated) accepted by the storage.
     */
    default long receiveOP(long maxReceive, boolean simulate) {
        return receiveEnergy((int) Math.min(maxReceive, Integer.MAX_VALUE), simulate);
    }

    /**
     * Removes operational potential from the storage. Returns quantity of operational potential that was removed.
     *
     * @param maxExtract Maximum amount of operational potential to be extracted.
     * @param simulate   If TRUE, the extraction will only be simulated.
     * @return Amount of operational potential that was (or would have been, if simulated) extracted from the storage.
     */
    default long extractOP(long maxExtract, boolean simulate) {
        return extractEnergy((int) Math.min(maxExtract, Integer.MAX_VALUE), simulate);
    }

    /**
     * Returns the amount of operational potential currently stored.
     */
    default long getOPStored() {
        return getEnergyStored();
    }

    /**
     * Returns the maximum amount of operational potential that can be stored.
     */
    default long getMaxOPStored() {
        return getMaxEnergyStored();
    }

    /**
     * Returns if this storage can have operational potential extracted.
     * If this is false, then any calls to extractOP will return 0.
     */
    boolean canExtract();

    /**
     * Used to determine if this storage can receive operational potential.
     * If this is false, then any calls to receiveOP will return 0.
     */
    boolean canReceive();

    /**
     * This is the maximum energy that can be extracted from this storage per tick. (Or rather per {@link #extractOP(long, boolean)} call)
     *
     * @return The maximum energy extraction rate.
     */
    default long maxExtract() {
        return Long.MAX_VALUE;
    }

    /**
     * This is the maximum energy that can be inserted into this storage per tick. (Or rather per {@link #receiveOP(long, boolean)} call)
     *
     * @return The maximum energy insertion rate.
     */
    default long maxReceive() {
        return Long.MAX_VALUE;
    }

    @Nullable
    default IOInfo getIOInfo() {
        return null;
    }

    @Override
    default int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) receiveOP(maxReceive, simulate);
    }

    @Override
    default int extractEnergy(int maxExtract, boolean simulate) {
        return (int) extractOP(maxExtract, simulate);
    }

    @Override
    default int getEnergyStored() {
        return (int) Math.min(getOPStored(), Integer.MAX_VALUE);
    }

    @Override
    default int getMaxEnergyStored() {
        return (int) Math.min(getMaxOPStored(), Integer.MAX_VALUE);
    }

    /**
     * This method bypasses the usual can insert, can extract and transfer rate checks and modifies
     * the energy storage directly. This is primarily for internal use and is the method by which an
     * energy item / machine should modify its own energy value.
     * <p>
     * <strong>Only use this if you know what your doing!</strong>
     * <p>
     * Note: in cases where {@link IOPStorage} is wrapping {@link IEnergyStorage} this is still limited
     * by the receive/extract methods. So unless your sure your dealing with an actual {@link IOPStorage}
     * do not just assume this will work.
     *
     * @param amount the amount (positive or negative)
     * @return The amount of energy that was actually added / removed (always positive or zero)
     */
    long modifyEnergyStored(long amount);
}
