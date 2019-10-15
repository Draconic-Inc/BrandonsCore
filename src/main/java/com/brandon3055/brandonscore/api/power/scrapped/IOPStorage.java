//package com.brandon3055.brandonscore.api.power.scrapped;
//
//import net.minecraftforge.energy.IEnergyStorage;
//
///**
// * Created by brandon3055 on 14/8/19.
// *
// * Operational Potential is the power system used by Draconic Evolution and related mods.
// * This system is an extension of Forge Energy that allows long based power transfer and storage.
// *
// * When implementing this capability cap should be provided as both {@link net.minecraftforge.energy.CapabilityEnergy#ENERGY} and {@link com.brandon3055.brandonscore.capability.CapabilityOP#OP_ENERGY}
// * So any mod that implements FE will find the FE cap and interact with it normally. However any mod that implements OP will fist check for the OP cap before falling back to RF.
// *
// */
//public interface IOPStorage extends IEnergyStorage {
//    /**
//     * Adds operational potential to the storage. Returns quantity of operational potential that was accepted.
//     *
//     * @param maxReceive Maximum amount of operational potential to be inserted.
//     * @param simulate   If TRUE, the insertion will only be simulated.
//     * @return Amount of operational potential that was (or would have been, if simulated) accepted by the storage.
//     */
//    long receiveOP(long maxReceive, boolean simulate);
//
//    /**
//     * Removes operational potential from the storage. Returns quantity of operational potential that was removed.
//     *
//     * @param maxExtract Maximum amount of operational potential to be extracted.
//     * @param simulate   If TRUE, the extraction will only be simulated.
//     * @return Amount of operational potential that was (or would have been, if simulated) extracted from the storage.
//     */
//    long extractOP(long maxExtract, boolean simulate);
//
//    /**
//     * Returns the amount of operational potential currently stored.
//     */
//    long getOPStored();
//
//    /**
//     * Returns the maximum amount of operational potential that can be stored.
//     */
//    long getMaxOPStored();
//
//    /**
//     * Returns if this storage can have operational potential extracted.
//     * If this is false, then any calls to extractOP will return 0.
//     */
//    boolean canExtract();
//
//    /**
//     * Used to determine if this storage can receive operational potential.
//     * If this is false, then any calls to receiveOP will return 0.
//     */
//    boolean canReceive();
//}
