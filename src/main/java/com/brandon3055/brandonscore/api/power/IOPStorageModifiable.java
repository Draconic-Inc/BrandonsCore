package com.brandon3055.brandonscore.api.power;

import com.brandon3055.brandonscore.api.power.IOPStorage;

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
}
