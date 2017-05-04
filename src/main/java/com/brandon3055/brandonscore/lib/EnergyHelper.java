package com.brandon3055.brandonscore.lib;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
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

    public static boolean isEnergyTile(TileEntity tile) {
        return isEnergyTile(tile, null);
    }

    public static boolean isEnergyTile(TileEntity tile, EnumFacing side) {
        return tile instanceof IEnergyReceiver || tile.hasCapability(CapabilityEnergy.ENERGY, side);
    }

    public static int getEnergyStored(TileEntity tile) {
        return getEnergyStored(tile, null);
    }

    public static int getEnergyStored(TileEntity tile, EnumFacing side) {
        if (tile instanceof IEnergyHandler) {
            return ((IEnergyHandler) tile).getEnergyStored(side);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            return tile.getCapability(CapabilityEnergy.ENERGY, side).getEnergyStored();
        }
        else {
            return 0;
        }
    }

    public static int getMaxEnergyStored(TileEntity tile) {
        return getEnergyStored(tile, null);
    }

    public static int getMaxEnergyStored(TileEntity tile, EnumFacing side) {
        if (tile instanceof IEnergyHandler) {
            return ((IEnergyHandler) tile).getMaxEnergyStored(side);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            return tile.getCapability(CapabilityEnergy.ENERGY, side).getMaxEnergyStored();
        }
        else {
            return 0;
        }
    }

    //endregion

    //region Receive Tile

    public static boolean canReceiveEnergy(TileEntity tile) {
        return canReceiveEnergy(tile, null);
    }

    public static boolean canReceiveEnergy(TileEntity tile, EnumFacing side) {
        return (tile instanceof IEnergyReceiver && (side == null || ((IEnergyReceiver) tile).canConnectEnergy(side))) || (tile.hasCapability(CapabilityEnergy.ENERGY, side) && tile.getCapability(CapabilityEnergy.ENERGY, side).canReceive());
    }

    public static int insertEnergy(TileEntity tile, int energy, boolean simulate) {
        return insertEnergy(tile, energy, null, simulate);
    }

    public static int insertEnergy(TileEntity tile, int energy, EnumFacing side, boolean simulate) {
        if (tile.getWorld().isRemote) {
            LogHelperBC.bigDev("Attempt to do energy operation client side!");
            return 0;
        }
        if (tile instanceof IEnergyReceiver) {
            return ((IEnergyReceiver) tile).receiveEnergy(side, energy, simulate);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap.canReceive()) {
                return cap.receiveEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion

    //region Extract Tile

    public static boolean canExtractEnergy(TileEntity tile) {
        return canReceiveEnergy(tile, null);
    }

    public static boolean canExtractEnergy(TileEntity tile, EnumFacing side) {
        return tile instanceof IEnergyProvider || (tile.hasCapability(CapabilityEnergy.ENERGY, side) && tile.getCapability(CapabilityEnergy.ENERGY, side).canExtract());
    }

    public static int extractEnergy(TileEntity tile, int energy, boolean simulate) {
        return extractEnergy(tile, energy, null, simulate);
    }

    public static int extractEnergy(TileEntity tile, int energy, EnumFacing side, boolean simulate) {
        if (tile.getWorld().isRemote) {
            LogHelperBC.bigDev("Attempt to do energy operation client side!");
            return 0;
        }
        if (tile instanceof IEnergyProvider) {
            return ((IEnergyProvider) tile).extractEnergy(side, energy, simulate);
        }
        else if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) {
            net.minecraftforge.energy.IEnergyStorage cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap.canExtract()) {
                return cap.extractEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion


    //region Energy ItemStack

    public static boolean isEnergyStack(ItemStack stack) {
        return stack != null && (stack.getItem() instanceof IEnergyContainerItem || stack.hasCapability(CapabilityEnergy.ENERGY, null));
    }

    public static int getEnergyStored(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored();
        }
        else {
            return 0;
        }
    }

    public static int getMaxEnergyStored(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).getMaxEnergyStored(stack);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return stack.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored();
        }
        else {
            return 0;
        }
    }

    //endregion

    //region Receive ItemStack

    public static boolean canReceiveEnergy(ItemStack stack) {
        return stack != null && (stack.getItem() instanceof IEnergyContainerItem || (stack.hasCapability(CapabilityEnergy.ENERGY, null) && stack.getCapability(CapabilityEnergy.ENERGY, null).canReceive()));
    }

    public static int insertEnergy(ItemStack stack, int energy, boolean simulate) {
        if (stack == null) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyReceiver) {
            return ((IEnergyContainerItem) stack.getItem()).receiveEnergy(stack, energy, simulate);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap.canReceive()) {
                return cap.receiveEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion

    //region Extract ItemStack

    public static boolean canExtractEnergy(ItemStack stack) {
        return stack != null && (stack.getItem() instanceof IEnergyContainerItem || (stack.hasCapability(CapabilityEnergy.ENERGY, null) && stack.getCapability(CapabilityEnergy.ENERGY, null).canExtract()));
    }

    public static int extractEnergy(ItemStack stack, int energy, boolean simulate) {
        if (stack == null) {
            return 0;
        }
        else if (stack.getItem() instanceof IEnergyContainerItem) {
            return ((IEnergyContainerItem) stack.getItem()).extractEnergy(stack, energy, simulate);
        }
        else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            net.minecraftforge.energy.IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap.canExtract()) {
                return cap.extractEnergy(energy, simulate);
            }
        }
        return 0;
    }

    //endregion
}
