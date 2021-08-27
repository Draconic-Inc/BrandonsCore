package com.brandon3055.brandonscore.lib;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * Implement this on a tile that uses {@link com.brandon3055.brandonscore.blocks.BlockBCore} and you will be able to receive block use and block attack calls.
 * Created by brandon3055 on 21/10/2016.
 */
public interface IInteractTile {

    @Deprecated //Use onBlockUse
    default boolean onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return false;
    }

    /**
     * Called when the hosed block is used (right clicked)
     */
    default ActionResultType onBlockUse(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return onBlockActivated(state, player, hand, hit) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    }

    /**
     * Called when the hosed block is attacked (left clicked)
     */
    default void onBlockAttack(BlockState state, PlayerEntity player) {}

}
