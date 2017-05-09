package com.brandon3055.brandonscore.utils;


import com.google.common.base.Predicate;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;

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

    public static boolean consumeStack(ItemStack stack, IInventory inventory) {
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

    /**
     * Inserts item held in the players hand or extract the item into the players hand or on the ground if there is already a stack in the slot.
     * @param slot The inventory slot to extract from or insert into.
     * @param inventory The inventory.
     * @param player The player.
     * @param validator An optional validator that allows you to specifu if an item is valid for the inventory
     */
    public static void handleAddOrTakeStack(int slot, IInventory inventory, EntityPlayer player, @Nullable Predicate<ItemStack> validator) {
        if (player.worldObj.isRemote) {
            return;
        }
        if (inventory.getStackInSlot(slot) != null) {
            if (player.getHeldItemMainhand() == null) {
                player.setHeldItem(EnumHand.MAIN_HAND, inventory.getStackInSlot(slot));
                inventory.setInventorySlotContents(slot, null);
            } else {
                player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, inventory.getStackInSlot(slot)));
                inventory.setInventorySlotContents(slot, null);
            }
        }
        else {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack != null && (validator == null || validator.apply(stack))) {
                inventory.setInventorySlotContents(slot, player.getHeldItemMainhand());
                player.setHeldItem(EnumHand.MAIN_HAND, null);
            }
        }
    }

    public static void consumeHeldItem(EntityPlayer player, ItemStack stack, EnumHand hand) {
        stack.stackSize--;
        player.setHeldItem(hand, stack.stackSize > 0 ? stack.copy() : null);
    }
}
