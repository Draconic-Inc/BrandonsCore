package com.brandon3055.brandonscore.utils;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 31/05/2016.
 */

public class InventoryUtils {

    public static boolean hasStack(ItemStack stack, IInventory inventory) {
        if (stack == null) {
            return false;
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack s = inventory.getStackInSlot(i);

            if (ItemStack.areItemsEqual(stack, s) && stack.getItemDamage() == s.getItemDamage() && s.stackSize >= stack.stackSize) {
                return true;
            }
        }

        return false;
    }

    public static boolean conumeStack(ItemStack stack, IInventory inventory) {
        if (stack == null) {
            return false;
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack s = inventory.getStackInSlot(i);
            if (s == null) {
                continue;
            }

            if (ItemStack.areItemsEqual(stack, s) && stack.getItemDamage() == s.getItemDamage() && s.stackSize >= stack.stackSize) {
                s.stackSize -= stack.stackSize;

                if (s.stackSize <= 0) {
                    inventory.setInventorySlotContents(i, null);
                }

                return true;
            }
        }

        return false;
    }

}
