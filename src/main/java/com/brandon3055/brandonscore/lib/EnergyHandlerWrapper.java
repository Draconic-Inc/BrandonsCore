package com.brandon3055.brandonscore.lib;

import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created by brandon3055 on 15/11/2016.
 */
@Deprecated //Wont be needing wrappers in 1.14
public class EnergyHandlerWrapper implements IEnergyStorage {

    private TileEntity tile;
    private Direction side;

    public EnergyHandlerWrapper(TileEntity tile, Direction side) {
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
        return tile instanceof IEnergyProvider && ((IEnergyProvider) tile).canConnectEnergy(side);
    }

    @Override
    public boolean canReceive() {
        return tile instanceof IEnergyReceiver && ((IEnergyReceiver) tile).canConnectEnergy(side);
    }
}
