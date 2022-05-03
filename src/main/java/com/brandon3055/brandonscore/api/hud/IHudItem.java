package com.brandon3055.brandonscore.api.hud;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Implementing this interface on an item will allow you to display information on the users screen while they are holding the item.
 * The default method here simply lets you specify a list of text components to be displayed but by overriding the default methods in
 * {@link IHudDisplay} you can customize the display however you like.
 * <p>
 * Created by brandon3055 on 19/8/21
 */
public interface IHudItem extends IHudDisplay {

    /**
     * Note: The hud is entirely handled client side so your hud data must be available on the client.
     * Each {@link ITextComponent} will be rendered as its own line on the hud. There is no line wrapping.
     *
     * @param stack       The ItemStack the player is holding (this item)
     * @param player      The player holding the item.
     * @param displayList The list to which hud info should be added.
     */
    void generateHudText(ItemStack stack, Player player, List<Component> displayList);

    /**
     * @param stack  The ItemStack the player is holding (this item)
     * @param player The player holding the item.
     * @return True if the hud data for this item should be rendered.
     */
    default boolean shouldDisplayHudText(ItemStack stack, Player player) {
        return true;
    }
}
