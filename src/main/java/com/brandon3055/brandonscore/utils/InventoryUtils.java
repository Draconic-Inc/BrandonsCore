package com.brandon3055.brandonscore.utils;


import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.lib.functions.TriPredicate;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

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

    /**
     * Used to transfer an item between an inventory slot and the players hand. E.g. inserting or removing a dislocator from a receptacle.
     * If the player is holding an item that is valid for the slot it will be transferred to the slot.
     * If the slot is already occupied the stack in the slot will be transferred to the players main hand, failing that the players inventory
     * or if the players inventory is full the stack will be dropped at the players feet.
     * <p>
     * When inserting will first try to insert the item in the main hand and if that fails it will try the off hand.
     * Will not transfer partial stacks.
     */
    public static void handleHeldStackTransfer(int slot, IInventory inventory, EntityPlayer player) {
        if (player.world.isRemote) {
            return;
        }

        if (!inventory.getStackInSlot(slot).isEmpty()) {
            if (player.getHeldItemMainhand().isEmpty()) {
                player.setHeldItem(EnumHand.MAIN_HAND, inventory.getStackInSlot(slot));
            }
            else {
                givePlayerStack(player, inventory.getStackInSlot(slot));
            }
            inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        }
        else {
            DataUtils.forEach(EnumHand.values(), enumHand -> {
                ItemStack stack = player.getHeldItem(enumHand);
                if (!stack.isEmpty() && inventory.isItemValidForSlot(slot, stack) && inventory.getStackInSlot(slot).isEmpty()) {
                    inventory.setInventorySlotContents(slot, stack);
                    player.setHeldItem(enumHand, ItemStack.EMPTY);
                }
            });
        }
    }

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
            dropItemNoDelay(stack, player.world, Vector3.fromEntity(player));
        }
    }

    public static void dropItemNoDelay(ItemStack stack, World world, Vector3 dropLocation) {
        EntityItem item = new EntityItem(world, dropLocation.x, dropLocation.y, dropLocation.z, stack);
        item.motionX = world.rand.nextGaussian() * 0.05;
        item.motionY = world.rand.nextGaussian() * 0.05 + 0.2F;
        item.motionZ = world.rand.nextGaussian() * 0.05;
        world.spawnEntity(item);
        item.setPickupDelay(0);
    }

    public static int findMatchingStack(IItemHandler itemHandler, TriPredicate<IItemHandler, ItemStack, Integer> predicate) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (predicate.test(itemHandler, itemHandler.getStackInSlot(i), i)) {
                return i;
            }
        }
        return -1;
    }
}
