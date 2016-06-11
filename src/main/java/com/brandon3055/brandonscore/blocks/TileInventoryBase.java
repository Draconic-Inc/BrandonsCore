package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.api.IDataRetainerTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

/**
 * Created by brandon3055 on 26/3/2016.
 * The base class for all inventory tiles
 */
public class TileInventoryBase extends TileBCBase implements IInventory, IDataRetainerTile {

    private ItemStack[] inventoryStacks = new ItemStack[0];
    protected int stackLimit = 64;

    public TileInventoryBase() {

    }

    protected void setInventorySize(int size) {
        inventoryStacks = new ItemStack[size];
    }

    @Override
    public int getSizeInventory() {
        return inventoryStacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index < inventoryStacks.length && index >= 0 ? inventoryStacks[index] : null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = getStackInSlot(index);

        if (itemstack != null) {
            if (itemstack.stackSize <= count) {
                setInventorySlotContents(index, null);
            } else {
                itemstack = itemstack.splitStack(count);
                if (itemstack.stackSize == 0) {
                    setInventorySlotContents(index, null);
                }
            }
        }
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack item = getStackInSlot(index);

        if (item != null) {
            setInventorySlotContents(index, null);
        }

        return item;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= inventoryStacks.length){
            return;
        }

        inventoryStacks[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return stackLimit;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (worldObj == null) {
            return false;    //todo Was this supposed to be true?
        }
        if (worldObj.getTileEntity(pos) != this) {
            return false;
        }
        return player.getDistanceSq(pos.add(0.5, 0.5, 0.5)) < 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventoryStacks = new ItemStack[inventoryStacks.length];
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    private void writeInventoryToNBT(NBTTagCompound compound) {
        NBTTagCompound[] tag = new NBTTagCompound[inventoryStacks.length];

        for (int i = 0; i < inventoryStacks.length; i++) {
            tag[i] = new NBTTagCompound();

            if (inventoryStacks[i] != null) {
                tag[i] = inventoryStacks[i].writeToNBT(tag[i]);
            }

            compound.setTag("Item" + i, tag[i]);
        }
    }

    private void readInventoryFromNBT(NBTTagCompound compound) {
        NBTTagCompound[] tag = new NBTTagCompound[inventoryStacks.length];

        for (int i = 0; i < inventoryStacks.length; i++) {
            tag[i] = compound.getCompoundTag("Item" + i);
            inventoryStacks[i] = ItemStack.loadItemStackFromNBT(tag[i]);
        }
    }


    @Override
    public void writeDataToNBT(NBTTagCompound dataCompound) {
        writeInventoryToNBT(dataCompound);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound dataCompound) {
        readInventoryFromNBT(dataCompound);
    }
}
