package com.brandon3055.brandonscore.utils;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.capability.OPWrappers;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created by brandon3055 on 15/11/2016.
 */
public class EnergyUtils {

    // ================= Get Storage =================

    public static IOPStorage getStorage(TileEntity tile, EnumFacing side) {
        if (tile.getWorld().isRemote) {
            LogHelperBC.bigDev("Attempt to do energy operation client side!");
            return null;
        }
        if (tile instanceof IEnergyHandler) {
            return new OPWrappers.RF((IEnergyHandler) tile, side);
        }
        return getStorageFromProvider(tile, side);
    }

    public static IOPStorage getStorage(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof IEnergyContainerItem) {
                return new OPWrappers.RFItem((IEnergyContainerItem) stack.getItem(), stack);
            }
            else {
                return getStorageFromProvider(stack, null);
            }
        }

        return null;
    }

    /**
     * This should not be used on tiles or items as it will miss anything that implements the RF API.
     * This is only public to allow for other ICapabilityProvider's
     */
    public static IOPStorage getStorageFromProvider(ICapabilityProvider provider, EnumFacing side) {
        if (provider.hasCapability(CapabilityOP.OP, side)) {
            return provider.getCapability(CapabilityOP.OP, side);
        }
        else if (provider.hasCapability(CapabilityEnergy.ENERGY, side)) {
            IEnergyStorage storage = provider.getCapability(CapabilityEnergy.ENERGY, side);
            if (storage != null) {
                return new OPWrappers.FE(storage);
            }
        }
        return null;
    }

    // ================= Receive =================

    public static long insertEnergy(TileEntity tile, long energy, EnumFacing side, boolean simulate) {
        IOPStorage storage = getStorage(tile, side);
        if (storage != null && storage.canReceive()) {
            return storage.receiveOP(energy, simulate);
        }
        return 0;
    }

    public static long insertEnergy(ItemStack stack, long energy, boolean simulate) {
        IOPStorage storage = getStorage(stack);
        if (storage != null && storage.canReceive()) {
            return storage.receiveOP(energy, simulate);
        }
        return 0;
    }

    /**
     * This should not be used on tiles or items as it will miss anything that implements the RF API.
     * This is only public to allow for other ICapabilityProvider's
     */
    public static long insertEnergyIntoProvider(ICapabilityProvider provider, long energy, EnumFacing side, boolean simulate) {
        IOPStorage storage = getStorageFromProvider(provider, side);
        if (storage != null && storage.canReceive()) {
            return storage.receiveOP(energy, simulate);
        }
        return 0;
    }

    // ================= Extract =================

    public static long extractEnergy(TileEntity tile, long energy, EnumFacing side, boolean simulate) {
        IOPStorage storage = getStorage(tile, side);
        if (storage != null && storage.canExtract()) {
            return storage.extractOP(energy, simulate);
        }
        return 0;
    }

    public static long extractEnergy(ItemStack stack, long energy, boolean simulate) {
        IOPStorage storage = getStorage(stack);
        if (storage != null && storage.canExtract()) {
            return storage.extractOP(energy, simulate);
        }
        return 0;
    }

    /**
     * This should not be used on tiles or items as it will miss anything that implements the RF API.
     * This is only public to allow for other ICapabilityProvider's
     */
    public static long extractEnergyFromProvider(ICapabilityProvider provider, long energy, EnumFacing side, boolean simulate) {
        IOPStorage storage = getStorageFromProvider(provider, side);
        if (storage != null && storage.canExtract()) {
            return storage.extractOP(energy, simulate);
        }
        return 0;
    }

    // ================= Transfer =================

    public static long transferEnergy(IOPStorage source, IOPStorage target) {
        return target.receiveOP(source.extractOP(target.receiveOP(target.getMaxOPStored(), true), false), false);
    }

    public static long transferEnergy(TileEntity source, EnumFacing sourceSide, IOPStorage target) {
        IOPStorage storage = getStorage(source, sourceSide);
        return storage == null ? 0 : transferEnergy(storage, target);
    }

    public static long transferEnergy(IOPStorage source, TileEntity target, EnumFacing targetSide) {
        IOPStorage storage = getStorage(target, targetSide);
        return storage == null ? 0 : transferEnergy(source, storage);
    }

    public static long transferEnergy(ItemStack source, IOPStorage target) {
        IOPStorage storage = getStorage(source);
        return storage == null ? 0 : transferEnergy(storage, target);
    }

    public static long transferEnergy(IOPStorage source, ItemStack target) {
        IOPStorage storage = getStorage(target);
        return storage == null ? 0 : transferEnergy(source, storage);
    }

    public static long transferEnergy(ItemStack source, TileEntity target, EnumFacing targetSide) {
        IOPStorage storage = getStorage(source);
        return storage == null ? 0 : transferEnergy(storage, target, targetSide);
    }

    public static long transferEnergy(TileEntity source, EnumFacing sourceSide, ItemStack target) {
        IOPStorage storage = getStorage(target);
        return storage == null ? 0 : transferEnergy(source, sourceSide, storage);
    }

    public static long transferEnergy(TileEntity source, EnumFacing sourceSide, TileEntity target, EnumFacing targetSide) {
        IOPStorage sourceStorage = getStorage(source, sourceSide);
        if (sourceStorage == null) {
            return 0;
        }
        IOPStorage targetStorage = getStorage(target, targetSide);
        return targetStorage == null ? 0 : transferEnergy(sourceStorage, targetStorage);
    }

    // ================= Checks =================

    public static boolean canExtractEnergy(ItemStack stack) {
        if (stack.getItem() instanceof IEnergyContainerItem) {
            return true;
        }
        IOPStorage storage = getStorage(stack);
        return storage != null && storage.canExtract();
    }

    public static boolean canReceiveEnergy(ItemStack stack) {
        if (stack.getItem() instanceof IEnergyContainerItem) {
            return true;
        }
        IOPStorage storage = getStorage(stack);
        return storage != null && storage.canReceive();
    }

    public static boolean canExtractEnergy(TileEntity tile, EnumFacing side) {
        IOPStorage storage = getStorage(tile, side);
        return storage != null && storage.canExtract();
    }

    public static boolean canReceiveEnergy(TileEntity tile, EnumFacing side) {
        IOPStorage storage = getStorage(tile, side);
        return storage != null && storage.canReceive();
    }

    public static long getEnergyStored(TileEntity tile, EnumFacing side) {
        IOPStorage storage = getStorage(tile, side);
        return storage == null ? 0 : storage.getOPStored();
    }

    public static long getMaxEnergyStored(TileEntity tile, EnumFacing side) {
        IOPStorage storage = getStorage(tile, side);
        return storage == null ? 0 : storage.getMaxOPStored();
    }

    public static long getEnergyStored(ItemStack stack) {
        IOPStorage storage = getStorage(stack);
        return storage == null ? 0 : storage.getOPStored();
    }

    public static long getMaxEnergyStored(ItemStack stack) {
        IOPStorage storage = getStorage(stack);
        return storage == null ? 0 : storage.getMaxOPStored();
    }

    public static boolean isFullyOrInvalid(ItemStack stack) {
        IOPStorage storage = getStorage(stack);
        if (storage == null) return true;
        return storage.getOPStored() >= storage.getMaxOPStored();
    }


//    //region Energy Tile
//
//    @Deprecated
//    public static boolean isEnergyTile(TileEntity tile, EnumFacing side) {
//        return tile instanceof IEnergyReceiver || tile.hasCapability(CapabilityEnergy.ENERGY, side);
//    }
//
//    @Deprecated
//    public static int getEnergyStored(TileEntity tile, EnumFacing side) {
//        if (tile instanceof IEnergyHandler) {
//            return ((IEnergyHandler) tile).getEnergyStored(side);
//        }
//        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
//            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
//            if (cap != null) {
//                return cap.getEnergyStored();
//            }
//            return 0;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    @Deprecated
//    public static long getEnergyStoredLong(TileEntity tile, EnumFacing side) {
//        if (tile.hasCapability(CapabilityOP.OP, side)) {
//            IOPStorage cap = tile.getCapability(CapabilityOP.OP, side);
//            if (cap != null) {
//                return cap.getOPStored();
//            }
//            return getEnergyStored(tile, side);
//        }
//        else {
//            return getEnergyStored(tile, side);
//        }
//    }
//
//    @Deprecated
//    public static int getMaxEnergyStored(TileEntity tile, EnumFacing side) {
//        if (tile instanceof IEnergyHandler) {
//            return ((IEnergyHandler) tile).getMaxEnergyStored(side);
//        }
//        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
//            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
//            if (cap != null && cap.canReceive()) {
//                return cap.getMaxEnergyStored();
//            }
//            return 0;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    @Deprecated
//    public static long getMaxEnergyStoredLong(TileEntity tile, EnumFacing side) {
//        if (tile.hasCapability(CapabilityOP.OP, side)) {
//            IOPStorage cap = tile.getCapability(CapabilityOP.OP, side);
//            if (cap != null) {
//                return cap.getMaxOPStored();
//            }
//            return getMaxEnergyStored(tile, side);
//        }
//        else {
//            return getMaxEnergyStored(tile, side);
//        }
//    }

    //endregion

    //region Receive Tile

//    @Deprecated
//    public static boolean canReceiveEnergy(TileEntity tile, EnumFacing side) {
//        if (tile instanceof IEnergyProvider) {
//            return true;
//        }
//        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
//            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
//            return cap != null && cap.canReceive();
//        }
//        return false;
//    }

//    @Deprecated
//    public static int insertEnergy(TileEntity tile, int energy, EnumFacing side, boolean simulate) {
//        if (tile.getWorld().isRemote) {
//            LogHelperBC.bigDev("Attempt to do energy operation client side!");
//            return 0;
//        }
//        if (energy < 0) {
//            return 0;
//        }
//        if (tile instanceof IEnergyReceiver) {
//            return ((IEnergyReceiver) tile).receiveEnergy(side, energy, simulate);
//        }
//        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
//            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
//            if (cap != null && cap.canReceive()) {
//                return cap.receiveEnergy(energy, simulate);
//            }
//        }
//        return 0;
//    }

    //endregion

    //region Extract Tile

//    @Deprecated
//    public static boolean canExtractEnergy(TileEntity tile, EnumFacing side) {
//        if (tile instanceof IEnergyProvider) {
//            return true;
//        }
//        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
//            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
//            return cap != null && cap.canExtract();
//        }
//        return false;
//    }

//    @Deprecated
//    public static int extractEnergy(TileEntity tile, int energy, EnumFacing side, boolean simulate) {
//        if (tile.getWorld().isRemote) {
//            LogHelperBC.bigDev("Attempt to do energy operation client side!");
//            return 0;
//        }
//        if (energy < 0) {
//            return 0;
//        }
//        if (tile instanceof IEnergyProvider) {
//            return ((IEnergyProvider) tile).extractEnergy(side, energy, simulate);
//        }
//        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
//            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
//            if (cap != null && cap.canExtract()) {
//                return cap.extractEnergy(energy, simulate);
//            }
//        }
//        return 0;
//    }

    //endregion


    //region Energy ItemStack
//    @Deprecated
//    public static boolean isEnergyStack(ItemStack stack) {
//        return !stack.isEmpty() && (stack.getItem() instanceof IEnergyContainerItem || stack.hasCapability(CapabilityEnergy.ENERGY, null));
//    }
//    @Deprecated
//    public static int getEnergyStored(ItemStack stack) {
//        if (stack.isEmpty()) {
//            return 0;
//        }
//        else if (stack.getItem() instanceof IEnergyContainerItem) {
//            return ((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack);
//        }
//        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
//            if (cap != null) {
//                return cap.getEnergyStored();
//            }
//            return 0;
//        }
//        else {
//            return 0;
//        }
//    }
//    @Deprecated
//    public static int getMaxEnergyStored(ItemStack stack) {
//        if (stack.isEmpty()) {
//            return 0;
//        }
//        else if (stack.getItem() instanceof IEnergyContainerItem) {
//            return ((IEnergyContainerItem) stack.getItem()).getMaxEnergyStored(stack);
//        }
//        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
//            if (cap != null) {
//                return cap.getMaxEnergyStored();
//            }
//            return 0;
//        }
//        else {
//            return 0;
//        }
//    }

    //endregion

    //region Receive ItemStack
//    @Deprecated
//    public static boolean canReceiveEnergy(ItemStack stack) {
//        if (stack.isEmpty()) {
//            return false;
//        }
//        else if (stack.getItem() instanceof IEnergyContainerItem) {
//            return true;
//        }
//        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
//            return cap != null && cap.canReceive();
//        }
//        return false;
//    }
//    @Deprecated
//    public static int insertEnergy(ItemStack stack, int energy, boolean simulate) {
//        if (stack.isEmpty() || energy < 0) {
//            return 0;
//        }
//        else if (stack.getItem() instanceof IEnergyContainerItem) {
//            return ((IEnergyContainerItem) stack.getItem()).receiveEnergy(stack, energy, simulate);
//        }
//        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
//            if (cap != null && cap.canReceive()) {
//                return cap.receiveEnergy(energy, simulate);
//            }
//        }
//        return 0;
//    }

    //endregion

    //region Extract ItemStack
//    @Deprecated
//    public static boolean canExtractEnergy(ItemStack stack) {
//        if (stack.isEmpty()) {
//            return false;
//        }
//        else if (stack.getItem() instanceof IEnergyContainerItem) {
//            return true;
//        }
//        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
//            return cap != null && cap.canExtract();
//        }
//        return false;
//    }
//    @Deprecated
//    public static int extractEnergy(ItemStack stack, int energy, boolean simulate) {
//        if (stack.isEmpty() || energy < 0) {
//            return 0;
//        }
//        else if (stack.getItem() instanceof IEnergyContainerItem) {
//            return ((IEnergyContainerItem) stack.getItem()).extractEnergy(stack, energy, simulate);
//        }
//        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
//            if (cap != null && cap.canExtract()) {
//                return cap.extractEnergy(energy, simulate);
//            }
//        }
//        return 0;
//    }

    //endregion
}
