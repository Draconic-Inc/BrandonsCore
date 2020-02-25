package com.brandon3055.brandonscore.blocks;

import net.minecraft.tileentity.TileEntityType;

/**
 * Created by brandon3055 on 28/3/2016.
 * The base tile for energy providers and receivers that have inventories. When extending ether implement implement IEnergyReceiver,
 * IEnergyProvider or both.
 */
@Deprecated
public class TileEnergyInventoryBase extends TileInventoryBase {
    public TileEnergyInventoryBase(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }


//    public ManagedInt energySync = null;
//    public EnergyStorage energyStorage = new EnergyStorage(0, 0, 0);
//
//    @Override
//    public void update() {
//        super.update();
//        if (energySync != null) {
//            if (world.isRemote) {
//                energyStorage.setEnergyStored(energySync.get());
//            } else {
//                energySync.set(energyStorage.getEnergyStored());
//            }
//        }
//    }
//
//    public ManagedInt setEnergySyncMode(DataFlags... flags) {
//        energySync = dataManager.register(new ManagedInt("anInt", 0, flags));
//        return energySync;
//    }
//
//    protected void setCapacityAndTransfer(int capacity, int receive, int extract) {
//        energyStorage.setCapacity(capacity);
//        energyStorage.setMaxReceive(receive);
//        energyStorage.setMaxExtract(extract);
//    }
//
//    public int getEnergyStored(Direction from) {
//        return energyStorage.getEnergyStored();
//    }
//
//    public int getMaxEnergyStored(Direction from) {
//        return energyStorage.getMaxEnergyStored();
//    }
//
//    public int extractEnergy(Direction from, int maxExtract, boolean simulate) {
//        return energyStorage.extractEnergy(maxExtract, simulate);
//    }
//
//    public int receiveEnergy(Direction from, int maxReceive, boolean simulate) {
//        return energyStorage.receiveEnergy(maxReceive, simulate);
//    }
//
//    public boolean canConnectEnergy(Direction from) {
//        return true;
//    }
//
//    @Override
//    public void writeToItemStack(CompoundNBT compound, boolean willHarvest) {
//        super.writeToItemStack(compound, willHarvest);
//        if (energyStorage.getEnergyStored() > 0) {
//            energyStorage.writeToNBT(compound);
//        }
//    }
//
//    @Override
//    public void readFromItemStack(CompoundNBT compound) {
//        super.readFromItemStack(compound);
//        energyStorage.readFromNBT(compound);
//    }
//
//    @Override
//    public void writeExtraNBT(CompoundNBT compound) {
//        super.writeExtraNBT(compound);
//        energyStorage.writeToNBT(compound);
//    }
//
//    @Override
//    public void readExtraNBT(CompoundNBT compound) {
//        super.readExtraNBT(compound);
//        energyStorage.readFromNBT(compound);
//    }
//
//    protected int getEnergyStored() {
//        return energyStorage.getEnergyStored();
//    }
//
//    protected int getMaxEnergyStored() {
//        return energyStorage.getMaxEnergyStored();
//    }
//
//    public int sendEnergyToAll() {
//        if (getEnergyStored() <= 0) {
//            return 0;
//        }
//        int i = 0;
//        for (Direction direction : Direction.values()) {
//            i += sendEnergyTo(direction);
//        }
//        return i;
//    }
//
//    public int sendEnergyTo(Direction side) {
////        if (getEnergyStored() == 0) {
////            return 0;
////        }
////        TileEntity tile = world.getTileEntity(pos.offset(side));
////        if (tile != null && EnergyUtils.canReceiveEnergy(tile, side.getOpposite())) {
////            return EnergyUtils.insertEnergy(tile, getEnergyStored(), side.getOpposite(), false);
////        }
//        return 0;
//    }
//
//    public static int sendEnergyTo(IBlockAccess world, BlockPos pos, int maxSend, Direction side) {
////        TileEntity tile = world.getTileEntity(pos.offset(side));
////        if (tile != null && EnergyUtils.canReceiveEnergy(tile, side.getOpposite())) {
////            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
////        }
//        return 0;
//    }
//
//    public static int sendEnergyToAll(IBlockAccess world, BlockPos pos, int maxSend) {
//        int i = 0;
//        for (Direction direction : Direction.values()) {
//            i += sendEnergyTo(world, pos, maxSend - i, direction);
//        }
//        return i;
//    }
//
//    public int extractEnergyFromItem(ItemStack stack, int maxExtract, boolean simulate) {
////        if (EnergyUtils.isEnergyStack(stack)) {
////            return EnergyUtils.extractEnergy(stack, maxExtract, simulate);
////        }
//        return 0;
//    }
//
//    //region Capability
//
//    @Override
//    public boolean hasCapability(Capability<?> capability, Direction facing) {
//        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
//    }
//
//    @Override
//    public <T> T getCapability(Capability<T> capability, Direction facing) {
//        if (capability == CapabilityEnergy.ENERGY) {
//            return CapabilityEnergy.ENERGY.cast(new EnergyHandlerWrapper(this, facing));
//        }
//
//        return super.getCapability(capability, facing);
//    }
//
//    //endregion
}
