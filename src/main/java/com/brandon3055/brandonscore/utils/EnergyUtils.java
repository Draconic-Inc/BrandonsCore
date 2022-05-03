package com.brandon3055.brandonscore.utils;


import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.capability.OPWrappers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

/**
 * Created by brandon3055 on 15/11/2016.
 */
public class EnergyUtils {

    // ================= Get Storage =================

    public static IOPStorage getStorage(BlockEntity tile, Direction side) {
        if (tile.getLevel().isClientSide) {
            LogHelperBC.bigDev("Attempt to do energy operation client side!");
            return null;
        }

        return getStorageFromProvider(tile, side);
    }

    public static IOPStorage getStorage(ItemStack stack) {
        if (!stack.isEmpty()) {
            return getStorageFromProvider(stack, null);
        }

        return null;
    }

    /**
     * This should not be used on tiles or items as it will miss anything that implements the RF API.
     * This is only public to allow for other ICapabilityProvider's
     */
    public static IOPStorage getStorageFromProvider(ICapabilityProvider provider, Direction side) {
        LazyOptional<IOPStorage> op = provider.getCapability(CapabilityOP.OP, side);
        if (op.isPresent()) {
            return op.orElseThrow(ImpossibleException::new);
        }
        LazyOptional<IEnergyStorage> fe = provider.getCapability(CapabilityEnergy.ENERGY, side);
        if (fe.isPresent()) {
            return new OPWrappers.FE(fe.orElseThrow(ImpossibleException::new));
        }
        return null;
    }

    // ================= Receive =================

    public static long insertEnergy(BlockEntity tile, long energy, Direction side, boolean simulate) {
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
    public static long insertEnergyIntoProvider(ICapabilityProvider provider, long energy, Direction side, boolean simulate) {
        IOPStorage storage = getStorageFromProvider(provider, side);
        if (storage != null && storage.canReceive()) {
            return storage.receiveOP(energy, simulate);
        }
        return 0;
    }

    // ================= Extract =================

    public static long extractEnergy(BlockEntity tile, long energy, Direction side, boolean simulate) {
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
    public static long extractEnergyFromProvider(ICapabilityProvider provider, long energy, Direction side, boolean simulate) {
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

    public static long transferEnergy(BlockEntity source, Direction sourceSide, IOPStorage target) {
        IOPStorage storage = getStorage(source, sourceSide);
        return storage == null ? 0 : transferEnergy(storage, target);
    }

    public static long transferEnergy(IOPStorage source, BlockEntity target, Direction targetSide) {
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

    public static long transferEnergy(ItemStack source, BlockEntity target, Direction targetSide) {
        IOPStorage storage = getStorage(source);
        return storage == null ? 0 : transferEnergy(storage, target, targetSide);
    }

    public static long transferEnergy(BlockEntity source, Direction sourceSide, ItemStack target) {
        IOPStorage storage = getStorage(target);
        return storage == null ? 0 : transferEnergy(source, sourceSide, storage);
    }

    public static long transferEnergy(BlockEntity source, Direction sourceSide, BlockEntity target, Direction targetSide) {
        IOPStorage sourceStorage = getStorage(source, sourceSide);
        if (sourceStorage == null) {
            return 0;
        }
        IOPStorage targetStorage = getStorage(target, targetSide);
        return targetStorage == null ? 0 : transferEnergy(sourceStorage, targetStorage);
    }

    // ================= Checks =================

    public static boolean canExtractEnergy(ItemStack stack) {
        IOPStorage storage = getStorage(stack);
        return storage != null && storage.canExtract();
    }

    public static boolean canReceiveEnergy(ItemStack stack) {
        IOPStorage storage = getStorage(stack);
        return storage != null && storage.canReceive();
    }

    public static boolean canExtractEnergy(BlockEntity tile, Direction side) {
        IOPStorage storage = getStorage(tile, side);
        return storage != null && storage.canExtract();
    }

    public static boolean canReceiveEnergy(BlockEntity tile, Direction side) {
        IOPStorage storage = getStorage(tile, side);
        return storage != null && storage.canReceive();
    }

    public static long getEnergyStored(BlockEntity tile, Direction side) {
        IOPStorage storage = getStorage(tile, side);
        return storage == null ? 0 : storage.getOPStored();
    }

    public static long getMaxEnergyStored(BlockEntity tile, Direction side) {
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

    public static boolean isEnergyItem(ItemStack stack) {
        return getStorage(stack) != null;
    }

    public static boolean isEmptyOrInvalid(ItemStack stack) {
        IOPStorage storage = getStorage(stack);
        if (storage == null) return true;
        return storage.getOPStored() == 0;
    }

    private static class ImpossibleException extends RuntimeException {
        public ImpossibleException() {
            super("This exception is impossible. If you're seeing this in a crash report then... shit...");
        }
    }

    // ================= Utils =================

    @OnlyIn(Dist.CLIENT)
    public static void addEnergyInfo(ItemStack stack, List<Component> list) {
        IOPStorage storage = getStorage(stack);
        if (storage != null) {
            String energy = Utils.formatNumber(storage.getOPStored());
            String maxEnergy = Utils.formatNumber(storage.getMaxOPStored());
            String postFix = Screen.hasShiftDown() ? "(" + I18n.get("op.brandonscore.operational_potential") + ")" : I18n.get("op.brandonscore.op");
            list.add(new TextComponent(I18n.get("op.brandonscore.charge") + ": " + energy + " / " + maxEnergy + " " + postFix).withStyle(ChatFormatting.GRAY));
        }
    }
}
