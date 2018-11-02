package com.brandon3055.brandonscore.lib;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;

/**
 * Created by brandon3055 on 15/11/2016.
 */
public class EnergyHelper {

    //region Energy Tile

    @Deprecated//Should always use sided methods fore tiles
    public static boolean isEnergyTile(TileEntity tile) {
        return isEnergyTile(tile, null);
    }

    public static boolean isEnergyTile(TileEntity tile, EnumFacing side) {
        return tile instanceof IEnergyReceiver || tile.hasCapability(CapabilityEnergy.ENERGY, side);
    }

    @Deprecated//Should always use sided methods fore tiles
    public static int getEnergyStored(TileEntity tile) {
        return getEnergyStored(tile, null);
    }

    public static int getEnergyStored(TileEntity tile, EnumFacing side) {
        if (tile instanceof IEnergyHandler) {
            return ((IEnergyHandler) tile).getEnergyStored(side);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap != null && cap.canReceive()) {
                return cap.getEnergyStored();
            }
            return 0;
        }
        else {
            return 0;
        }
    }

    @Deprecated//Should always use sided methods fore tiles
    public static int getMaxEnergyStored(TileEntity tile) {
        return getEnergyStored(tile, null);
    }

    public static int getMaxEnergyStored(TileEntity tile, EnumFacing side) {
        if (tile instanceof IEnergyHandler) {
            return ((IEnergyHandler) tile).getMaxEnergyStored(side);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap != null && cap.canReceive()) {
                return cap.getMaxEnergyStored();
            }
            return 0;
        }
        else {
            return 0;
        }
    }

    //endregion

    //region Receive Tile

    @Deprecated//Should always use sided methods fore tiles
    public static boolean canReceiveEnergy(TileEntity tile) {
        return canReceiveEnergy(tile, null);
    }

    public static boolean canReceiveEnergy(TileEntity tile, EnumFacing side) {
        if (tile instanceof IEnergyProvider) {
            return true;
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            return cap != null && cap.canReceive();
        }
        return false;
    }

    @Deprecated//Should always use sided methods fore tiles
    public static int insertEnergy(TileEntity tile, int energy, boolean simulate) {
        return insertEnergy(tile, energy, null, simulate);
    }

    public static int insertEnergy(TileEntity tile, int energy, EnumFacing side, boolean simulate) {
        if (tile.getWorld().isRemote) {
            LogHelperBC.bigDev("Attempt to do energy operation client side!");
            return 0;
        }
        if (energy < 0) {
            return 0;
        }
        if (tile instanceof IEnergyReceiver) {
            return ((IEnergyReceiver) tile).receiveEnergy(side, energy, simulate);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap != null && cap.canReceive()) {
                return cap.receiveEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion

    //region Extract Tile

    @Deprecated//Should always use sided methods fore tiles
    public static boolean canExtractEnergy(TileEntity tile) {
        return canReceiveEnergy(tile, null);
    }

    public static boolean canExtractEnergy(TileEntity tile, EnumFacing side) {
        if (tile instanceof IEnergyProvider) {
            return true;
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            return cap != null && cap.canExtract();
        }
        return false;
    }

    @Deprecated//Should always use sided methods fore tiles
    public static int extractEnergy(TileEntity tile, int energy, boolean simulate) {
        return extractEnergy(tile, energy, null, simulate);
    }

    public static int extractEnergy(TileEntity tile, int energy, EnumFacing side, boolean simulate) {
        if (tile.getWorld().isRemote) {
            LogHelperBC.bigDev("Attempt to do energy operation client side!");
            return 0;
        }
        if (energy < 0) {
            return 0;
        }
        if (tile instanceof IEnergyProvider) {
            return ((IEnergyProvider) tile).extractEnergy(side, energy, simulate);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap != null && cap.canExtract()) {
                return cap.extractEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion


    //region Energy ItemStack

    public static boolean isEnergyStack(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof IEnergyContainerItem || stack.hasCapability(CapabilityEnergy.ENERGY, null));
    }

    public static int getEnergyStored(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null) {
                return cap.getEnergyStored();
            }
            return 0;
        }
        else {
            return 0;
        }
    }

    public static int getMaxEnergyStored(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).getMaxEnergyStored(stack);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null) {
                return cap.getMaxEnergyStored();
            }
            return 0;
        }
        else {
            return 0;
        }
    }

    //endregion

    //region Receive ItemStack

    public static boolean canReceiveEnergy(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return true;
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            return cap != null && cap.canReceive();
        }
        return false;
    }

    public static int insertEnergy(ItemStack stack, int energy, boolean simulate) {
        if (stack.isEmpty() || energy < 0) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).receiveEnergy(stack, energy, simulate);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null && cap.canReceive()) {
                return cap.receiveEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion

    //region Extract ItemStack

    public static boolean canExtractEnergy(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return true;
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            return cap != null && cap.canExtract();
        }
        return false;
    }

    public static int extractEnergy(ItemStack stack, int energy, boolean simulate) {
        if (stack.isEmpty() || energy < 0) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).extractEnergy(stack, energy, simulate);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null && cap.canExtract()) {
                return cap.extractEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion
}
