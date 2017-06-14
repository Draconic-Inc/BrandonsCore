package com.brandon3055.brandonscore.utils;


import codechicken.lib.vec.Vector3;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 31/05/2016.
 */

public class InventoryUtils {

    public static boolean hasStack(ItemStack stack, IInventory inventory) {
        if (stack.isEmpty()) {
            return false;
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack s = inventory.getStackInSlot(i);

            if (ItemStack.areItemsEqual(stack, s) && stack.getItemDamage() == s.getItemDamage() && s.getCount() >= stack.getCount()) {
                return true;
            }
        }

        return false;
    }

    public static boolean consumeStack(ItemStack stack, IInventory inventory) {
        if (stack.isEmpty()) {
            return false;
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack s = inventory.getStackInSlot(i);
            if (s.isEmpty()) {
                continue;
            }

            if (ItemStack.areItemsEqual(stack, s) && stack.getItemDamage() == s.getItemDamage() && s.getCount() >= stack.getCount()) {
                s.shrink(stack.getCount());
                inventory.markDirty();
                return true;
            }
        }

        return false;
    }

//    /**//TODO This is cancer... If i need this then make it better.
//     * Inserts item held in the players hand or extract the item into the players hand or on the ground if there is already a stack in the slot.
//     * @param slot The inventory slot to extract from or insert into.
//     * @param inventory The inventory.
//     * @param player The player.
//     * @param validator An optional validator that allows you to specifu if an item is valid for the inventory
//     */
//    public static void handleAddOrTakeStack(int slot, IInventory inventory, EntityPlayer player, @Nullable Predicate<ItemStack> validator) {
//        if (player.world.isRemote) {
//            return;
//        }
//        if (!inventory.getStackInSlot(slot).isEmpty()) {
//            if (player.getHeldItemMainhand().isEmpty()) {
//                player.setHeldItem(EnumHand.MAIN_HAND, inventory.getStackInSlot(slot));
//                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
//            } else {
//                player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, inventory.getStackInSlot(slot)));
//                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
//            }
//        }
//        else {
//            ItemStack stack = player.getHeldItemMainhand();
//            if (!stack.isEmpty() && (validator == null || validator.test(stack))) {
//                inventory.setInventorySlotContents(slot, player.getHeldItemMainhand());
//                player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
//            }
//        }
//    }

    public static void consumeHeldItem(EntityPlayer player, ItemStack stack, EnumHand hand) {
        stack.shrink(1);
        player.setHeldItem(hand, stack.getCount() > 0 ? stack.copy() : ItemStack.EMPTY);
    }

    public static void givePlayerStack(EntityPlayer player, ItemStack stack) {
        if (player.world.isRemote) {
            return;
        }
        player.inventory.addItemStackToInventory(stack);
        if (stack.getCount() > 0) {
            player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, stack));
        }
    }

    public static void dropItem(ItemStack stack, World world, Vector3 dropLocation) {
        EntityItem item = new EntityItem(world, dropLocation.x, dropLocation.y, dropLocation.z, stack);
        item.motionX = world.rand.nextGaussian() * 0.05;
        item.motionY = world.rand.nextGaussian() * 0.05 + 0.2F;
        item.motionZ = world.rand.nextGaussian() * 0.05;
        world.spawnEntity(item);
    }
}
