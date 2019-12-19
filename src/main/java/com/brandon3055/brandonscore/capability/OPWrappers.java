package com.brandon3055.brandonscore.capability;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created by brandon3055 on 16/9/19.
 */
public class OPWrappers {

    public static class FE implements IOPStorage {

        private IEnergyStorage storage;

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
        public boolean canExtract() {
            return storage.canExtract();
        }

        @Override
        public boolean canReceive() {
            return storage.canReceive();
        }
    }

    public static class RF implements IOPStorage {
        private IEnergyHandler handler;
        private Direction side;

        public RF(IEnergyHandler handler, Direction side) {
            this.handler = handler;
            this.side = side;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return handler instanceof IEnergyReceiver ? ((IEnergyReceiver) handler).receiveEnergy(side, maxReceive, simulate) : 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return handler instanceof IEnergyProvider ? ((IEnergyProvider) handler).extractEnergy(side, maxExtract, simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            return handler.getEnergyStored(side);
        }

        @Override
        public int getMaxEnergyStored() {
            return handler.getMaxEnergyStored(side);
        }

        @Override
        public boolean canExtract() {
            return handler instanceof IEnergyProvider;
        }

        @Override
        public boolean canReceive() {
            return handler instanceof IEnergyReceiver;
        }
    }

    @Deprecated //ItemStack invalidation may be an issue in 1.14
    public static class RFItem implements IOPStorage {
        private IEnergyContainerItem handler;
        private ItemStack stack;

        public RFItem(IEnergyContainerItem handler, ItemStack stack) {
            this.handler = handler;
            this.stack = stack;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return handler.receiveEnergy(stack, maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return handler.extractEnergy(stack, maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return handler.getEnergyStored(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return handler.getMaxEnergyStored(stack);
        }

        @Override
        public boolean canExtract() {
            return true; //There is no way to know other than to just try extracting. And there is no point simulating an extraction before actually extracting. Its just more overhead.
        }

        @Override
        public boolean canReceive() {
            return true; //There is no way to know other than to just try inserting. And there is no point simulating an insertion before actually inserting. Its just more overhead.
        }
    }
}
