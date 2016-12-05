package com.brandon3055.brandonscore.lib;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 15/11/2016.
 */
public class EnergyContainerWrapper implements IEnergyStorage, ICapabilityProvider {

    private ItemStack stack;

    public EnergyContainerWrapper(ItemStack stack) {
        this.stack = stack;
    }

    private boolean isStackValid() {
        return stack != null && stack.getItem() instanceof IEnergyContainerItem;
    }

    private IEnergyContainerItem getContainer() {
        return (IEnergyContainerItem) stack.getItem();
    }

    //region IEnergyStorage

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (isStackValid()) {
            return getContainer().receiveEnergy(stack, maxReceive, simulate);
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (isStackValid()) {
            return getContainer().extractEnergy(stack, maxExtract, simulate);
        }
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return isStackValid() ? getContainer().getEnergyStored(stack) : 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return isStackValid() ? getContainer().getMaxEnergyStored(stack) : 0;
    }

    @Override
    public boolean canExtract() {
        return isStackValid();
    }

    @Override
    public boolean canReceive() {
        return isStackValid();
    }

    //endregion

    //region ICapabilityProvider

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        }
        return null;
    }

    //endregion
}
