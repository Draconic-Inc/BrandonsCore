package com.brandon3055.brandonscore.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 27/10/2016.
 * This is a simple interface that allows an item to modify the players field of view.
 * This just needs to be implemented on an item. Thats it. Your done.
 * See the bows in Draconic Evolution for an example.
 *
 * Note: This works with armor and items held in both the main hand and the off hand.
 * If the player is using more than 1 item that implements this interface the effect of all will be combined.
 * The effects are applied in the order of armor (Starting with the boots) then off hand and finally main hand.
 * The main hand is called last because the last item is the one that takes priority and can overrule all the others.
 *
 * Also Note the event that runs this interface is set to a lower priority than normal so you hopefully wont have to
 * deal with other poorly coded mods just stacking their FOV changes on top of yours and breaking the players FOV.
 * That says if your using this in your own mod please dont make one of those poorly coded mods that just stacks their
 * FOV changes ontop of everyone eases. Check the currentFOV if its already really high or low
 * then do some clever math to avoid breaking everything. Or just override it completely. At least that would be better than the alternative...
 */
public interface IFOVModifierItem {

    /**
     * @param player The player.
     * @param stack The items stack who's item this interface is implemented on.
     * @param currentFOV The current FOV value inclining any other IFOVModifierItem's that have already been applied.
     * @param originalFOV The original FOV.
     * @param slot The slot the item is in.
     * @return a new FOV value. This value will overwrite any previously applied IFOVModifierItem's.
     *
     * Note: 0.1 is the min allowed FOV and 1.5 is the max. Anything outside this range will have no effect.
     */
    float getNewFOV(EntityPlayer player, ItemStack stack, float currentFOV, float originalFOV, EntityEquipmentSlot slot);
}
