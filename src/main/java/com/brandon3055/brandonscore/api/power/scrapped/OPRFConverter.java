//package com.brandon3055.brandonscore.api.power.scrapped;
//
//import net.minecraftforge.energy.IEnergyStorage;
//
///**
// * Created by brandon3055 on 13/9/19.
// */
//public class OPRFConverter implements IEnergyStorage {
//
//    private OPStorage opStorage;
//
//    public OPRFConverter(OPStorage opStorage) {
//        this.opStorage = opStorage;
//    }
//
//    @Override
//    public int receiveEnergy(int maxReceive, boolean simulate) {
//        return 0;
//    }
//
//    @Override
//    public int extractEnergy(int maxExtract, boolean simulate) {
//        return opStorage.extractRF(maxExtract, simulate);
//    }
//
//    @Override
//    public int getEnergyStored() {
//        return (int) Math.min(opStorage.storedRF, Integer.MAX_VALUE);
//    }
//
//    @Override
//    public int getMaxEnergyStored() {
//        return (int) Math.min(opStorage.capacity * 1000, Integer.MAX_VALUE);
//    }
//
//    @Override
//    public boolean canExtract() {
//        return opStorage.storedRF > 0;
//    }
//
//    @Override
//    public boolean canReceive() {
//        return false;
//    }
//}
