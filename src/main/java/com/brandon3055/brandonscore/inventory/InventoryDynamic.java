package com.brandon3055.brandonscore.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 26/08/2016.
 * This is a dynamic inventory that will automatically expand its size to told any number of item stacks.
 */
@Deprecated //Old IInventory stuff i probably dont need. Though i may need a dynamic IItemHandler at some point?
public class InventoryDynamic implements IInventory {

    private LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();
    public int xp = 0;

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index < 0){
            return;
        }

        if (stack.isEmpty()) {
            if (index < stacks.size()){
                stacks.remove(index);
            }
        }
        else if (index < stacks.size()) {
            stacks.set(index, stack);
        }
        else {
            stacks.add(stack);
        }
    }

    @Override
    public int getContainerSize() {
        return stacks.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return stacks.size() == 0;
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
    }

    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = getItem(index);

        if (!itemstack.isEmpty()) {
            if (itemstack.getCount() <= count) {
                setItem(index, ItemStack.EMPTY);
            } else {
                itemstack = itemstack.split(count);
                if (itemstack.getCount() == 0) {
                    setItem(index, ItemStack.EMPTY);
                }
            }
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = getItem(index);

        if (!stack.isEmpty()) {
            setItem(index, ItemStack.EMPTY);
        }

        return stack;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        stacks.removeIf(ItemStack::isEmpty);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }

    @Override
    public void startOpen(PlayerEntity player) {

    }

    @Override
    public void stopOpen(PlayerEntity player) {

    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }



    @Override
    public void clearContent() {
        stacks.clear();
    }



    public void writeToNBT(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && stack.getCount() > 0) {
                CompoundNBT tag = new CompoundNBT();
                stack.save(tag);
                list.add(tag);
            }
        }

        compound.put("InvItems", list);
    }

    public void readFromNBT(CompoundNBT compound) {
        ListNBT list = compound.getList("InvItems", 10);
        stacks.clear();

        for (int i = 0; i < list.size(); i++) {
            stacks.add(ItemStack.of(list.getCompound(i)));
        }
    }

    public void removeIf(Predicate<ItemStack> filter) {
        stacks.removeIf(filter);
    }
}
