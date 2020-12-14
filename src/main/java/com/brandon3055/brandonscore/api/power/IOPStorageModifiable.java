package com.brandon3055.brandonscore.api.power;

public interface IOPStorageModifiable extends IOPStorage {

    /**
     * This method bypasses the usual can insert, can extract and transfer rate checks and modifies
     * the energy storage directly. This is primarily for internal use and is the method by which an
     * energy item / machine should modify its own energy value.
     * This only exists in the api for edge cases like energy modules which need to directly modify
     * the stored energy value when installed or removed.
     * @param amount the amount (positive or negative)
     * @return The amount of energy that was actually added / removed (always positive or zero)
     */
    long modifyEnergyStored(long amount);


    /**
     * This is the maximum allowed extraction rate for this IOStorage. This is independent of {@link #canExtract()} meaning even if
     * {@link #canExtract()} returns false {@link #maxExtract()} should still return a value greater than zero. In fact this should always return a value greater than zero.
     *
     * When using {@link #modifyEnergyStored(long)} to bypass normal IO restrictions obeying this limit is optional.
     *
     * @return The maximum energy extraction rate.
     */
    long maxExtract();

    /**
     * This is the maximum allowed energy insertion rate for this IOStorage. This is independent of {@link #canReceive()} meaning even if
     * {@link #canReceive()} returns false {@link #maxReceive()} should still return a value greater than zero. In fact this should always return a value greater than zero.
     *
     * When using {@link #modifyEnergyStored(long)} to bypass normal IO restrictions obeying this limit is optional.
     *
     * @return The maximum energy insertion rate.
     */
    long maxReceive();
}
