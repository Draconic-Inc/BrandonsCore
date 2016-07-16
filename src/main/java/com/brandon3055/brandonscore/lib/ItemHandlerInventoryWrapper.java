package com.brandon3055.brandonscore.lib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 16/07/2016.
 *
 * This was just an experiment do not use it! Its borked!
 * */
@Deprecated
public class ItemHandlerInventoryWrapper implements IInventory {

    private IItemHandler iItemHandler;

    public ItemHandlerInventoryWrapper(IItemHandler iItemHandler) {
        this.iItemHandler = iItemHandler;
    }

    @Override
    public int getSizeInventory() {
        return iItemHandler.getSlots();
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return iItemHandler.getStackInSlot(index);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        iItemHandler.extractItem(index, count, false);
        return iItemHandler.getStackInSlot(index);
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = iItemHandler.getStackInSlot(index);

        if (stack != null) {
            iItemHandler.extractItem(index, stack.stackSize, false);
        }

        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        removeStackFromSlot(index);
        if (stack != null){
            iItemHandler.insertItem(index, stack, false);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        ItemStack previousStack = removeStackFromSlot(0);
        ItemStack dummy = new ItemStack(Blocks.STONE, 64);
        dummy = iItemHandler.insertItem(0, dummy, true);
        setInventorySlotContents(0, previousStack);
        return dummy == null ? 64 : 64 - dummy.stackSize;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        ItemStack previousStack = removeStackFromSlot(0);
        ItemStack dummy = new ItemStack(Blocks.STONE, 1);
        dummy = iItemHandler.insertItem(0, dummy, true);
        setInventorySlotContents(0, previousStack);
        return dummy == null || dummy.stackSize == 0;
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
}
