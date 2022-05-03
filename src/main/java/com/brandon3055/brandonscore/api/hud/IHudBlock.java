package com.brandon3055.brandonscore.api.hud;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Implementing this interface on a block or tile entity will allow you to display information on the users screen while they are looking at said block or tile entity.
 * The default method here simply lets you specify a list of text components to be displayed but by overriding the default methods in
 * {@link IHudDisplay} you can customize the display however you like.
 * <p>
 * Note: If the user is looking at an {@link IHudBlock} while also holding an {@link IHudItem} the block will take priority.
 * Also if the user is holding {@link IHudItem}'s in both hands the main hand will take priority
 * <p>
 * Created by brandon3055 on 19/8/21
 */
public interface IHudBlock extends IHudDisplay {

    /**
     * Note: The hud is entirely handled client side so your hud data must be available on the client.
     * Each {@link Component} will be rendered as its own line on the hud. There is no line wrapping.
     *
     * @param world       The world.
     * @param pos         The position of this block.
     * @param player      The player.
     * @param displayList The list to which hud info should be added.
     */
    void generateHudText(Level world, BlockPos pos, Player player, List<Component> displayList);

    /**
     * @param world  The world.
     * @param pos    The position of this block.
     * @param player The player.
     * @return True if the hud data for this block should be rendered.
     */
    default boolean shouldDisplayHudText(Level world, BlockPos pos, Player player) {
        return true;
    }
}
