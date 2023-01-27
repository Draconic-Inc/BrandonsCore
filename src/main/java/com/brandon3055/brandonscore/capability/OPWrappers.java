package com.brandon3055.brandonscore.capability;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created by brandon3055 on 16/9/19.
 */
public class OPWrappers {

    public static class FE implements IOPStorage {

        private final IEnergyStorage storage;

        public FE(IEnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return storage.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return storage.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return storage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return storage.getMaxEnergyStored();
        }

        @Override
        public long modifyEnergyStored(long amount) {
            amount = Math.min(Math.max(amount, Integer.MIN_VALUE), Integer.MAX_VALUE);
            if (amount > 0) {
                return receiveEnergy((int)amount, false);
            } else {
                return extractEnergy((int)-amount, false);
            }
        }

        @Override
        public boolean canExtract() {
            return storage.canExtract();
        }

        @Override
        public boolean canReceive() {
            return storage.canReceive();
        }
    }
}
