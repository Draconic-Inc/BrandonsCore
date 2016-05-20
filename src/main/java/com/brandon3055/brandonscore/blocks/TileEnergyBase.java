package com.brandon3055.brandonscore.blocks;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyReceiver;
import com.brandon3055.brandonscore.api.IDataRetainerTile;
import com.brandon3055.brandonscore.network.wrappers.SyncableInt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Created by brandon3055 on 28/3/2016.
 * The base tile for energy providers and receivers that do not have inventories. When extending ether implement implement IEnergyReceiver,
 * IEnergyProvider or both. //Untested...
 */
public class TileEnergyBase extends TileBCBase implements IDataRetainerTile {
    //to-do add additional functionality as needed

    public final SyncableInt energyStored = new SyncableInt(0, false, true);
    protected EnergyStorage energyStorage = new EnergyStorage(0, 0, 0);

    @Override
    public void detectAndSendChanges(boolean forceSync) {
        if (worldObj.isRemote) return;
        energyStored.value = energyStorage.getEnergyStored();
        super.detectAndSendChanges(forceSync);
    }

    @Override
    public void detectAndSendChangesToPlayer(boolean forceSync, EntityPlayerMP playerMP) {
        if (worldObj.isRemote) return;
        energyStored.value = energyStorage.getEnergyStored();
        super.detectAndSendChangesToPlayer(forceSync, playerMP);
    }

    protected void setCapacityAndTransfer(int capacity, int receive, int transfer) {
        energyStorage.setCapacity(capacity);
        energyStorage.setMaxReceive(receive);
        energyStorage.setMaxExtract(transfer);
    }

    public int getEnergyStored(EnumFacing from) {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored(EnumFacing from) {
        return energyStorage.getMaxEnergyStored();
    }

    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return energyStorage.extractEnergy(maxExtract, simulate);
    }

    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return energyStorage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public void writeDataToNBT(NBTTagCompound compound) {
        energyStorage.writeToNBT(compound);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        energyStorage.readFromNBT(compound);
    }

    protected int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    protected int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int sendEnergyTo(EnumFacing direction) {
        if (getEnergyStored() == 0) return 0;
        TileEntity tile = worldObj.getTileEntity(pos.add(direction.getFrontOffsetX(), direction.getFrontOffsetY(), direction.getFrontOffsetZ()));
        if (tile instanceof IEnergyReceiver) {
            return energyStorage.extractEnergy(((IEnergyReceiver) tile).receiveEnergy(direction.getOpposite(), energyStorage.extractEnergy(energyStorage.getMaxExtract(), true), false), false);
        }
        return 0;
    }

    public int sendEnergyToAll() {
        if (getEnergyStored() == 0) return 0;
        int i = 0;
        for (EnumFacing direction : EnumFacing.VALUES) {
            i += sendEnergyTo(direction);
        }
        return i;
    }

    public static int sendEnergyTo(IBlockAccess world, BlockPos pos, int maxSend, EnumFacing direction) {
        TileEntity tile = world.getTileEntity(pos.add(direction.getFrontOffsetX(), direction.getFrontOffsetY(), direction.getFrontOffsetZ()));
        if (tile instanceof IEnergyReceiver) {
            return ((IEnergyReceiver) tile).receiveEnergy(direction.getOpposite(), maxSend, false);
        }
        return 0;
    }

    public static int sendEnergyToAll(IBlockAccess world, BlockPos pos, int maxSend) {
        int i = 0;
        for (EnumFacing direction : EnumFacing.VALUES) {
            i += sendEnergyTo(world, pos, maxSend - i, direction);
        }
        return i;
    }

    public int extractEnergyFromItem(ItemStack stack, int maxExtract, boolean simulate) {
        if (stack != null && stack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();
            return item.extractEnergy(stack, maxExtract, simulate);
        }
        return 0;
    }
}
