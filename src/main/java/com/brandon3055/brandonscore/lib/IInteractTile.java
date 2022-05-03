package com.brandon3055.brandonscore.lib;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Implement this on a tile that uses {@link com.brandon3055.brandonscore.blocks.BlockBCore} and you will be able to receive block use and block attack calls.
 * Created by brandon3055 on 21/10/2016.
 */
public interface IInteractTile {

    @Deprecated //Use onBlockUse
    default boolean onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    /**
     * Called when the host block is used (right clicked)
     */
    default InteractionResult onBlockUse(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        return onBlockActivated(state, player, hand, hit) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * Called when the host block is attacked (left clicked)
     */
    default void onBlockAttack(BlockState state, Player player) {}

}
