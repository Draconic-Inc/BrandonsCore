package com.brandon3055.brandonscore.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 25/3/2016.
 * This class is to help out with all of the duel wielding stuff in 1.9
 */
public class HandHelper {

    /**
     * Returns the first item found in ether of the players hands starting with the main hand
     */
    public static ItemStack getMainFirst(PlayerEntity player) {
        if (!player.getMainHandItem().isEmpty()) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem();
    }

    /**
     * Returns the first item found in ether of the players hands starting with the off hand
     */
    public static ItemStack getOffFirst(PlayerEntity player) {
        if (!player.getOffhandItem().isEmpty()) {
            return player.getOffhandItem();
        }
        return player.getMainHandItem();
    }

    /**
     * Returns the first item found in ether of the players hands that is the same as the given item
     */
    public static ItemStack getItem(PlayerEntity player, Item item) {
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == item) {
            return player.getMainHandItem();
        }
        else if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() == item) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

//    public static ItemStack getItemStack(PlayerEntity player, ItemStack itemStack) {
//        if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == itemStack.getItem() && player.getHeldItemMainhand().getItemDamage() == itemStack.getItemDamage()) {
//            return player.getHeldItemMainhand();
//        }
//        else if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() == itemStack.getItem() && player.getHeldItemOffhand().getItemDamage() == itemStack.getItemDamage()) {
//            return player.getHeldItemOffhand();
//        }
//        return ItemStack.EMPTY;
//    }

    public static boolean isHoldingItemEther(PlayerEntity player, Item item) {
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == item) {
            return true;
        }
        else {
            return !player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() == item;
        }
    }
}
