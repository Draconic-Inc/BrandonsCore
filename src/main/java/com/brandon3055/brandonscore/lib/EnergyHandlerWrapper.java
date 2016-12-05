package com.brandon3055.brandonscore.lib;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created by brandon3055 on 15/11/2016.
 */
public class EnergyHandlerWrapper implements IEnergyStorage {

    private TileEntity tile;
    private EnumFacing side;

    public EnergyHandlerWrapper(TileEntity tile, EnumFacing side) {
        this.tile = tile;
        this.side = side;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (tile instanceof IEnergyReceiver) {
            return ((IEnergyReceiver) tile).receiveEnergy(side, maxReceive, simulate);
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (tile instanceof IEnergyProvider) {
            return ((IEnergyProvider) tile).extractEnergy(side, maxExtract, simulate);
        }
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return tile instanceof IEnergyHandler ? ((IEnergyHandler) tile).getEnergyStored(side) : 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return tile instanceof IEnergyHandler ? ((IEnergyHandler) tile).getMaxEnergyStored(side) : 0;
    }

    @Override
    public boolean canExtract() {
        return tile instanceof IEnergyProvider;
    }

    @Override
    public boolean canReceive() {
        return tile instanceof IEnergyReceiver;
    }
}
