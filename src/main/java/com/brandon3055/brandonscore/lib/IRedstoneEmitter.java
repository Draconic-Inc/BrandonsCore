package com.brandon3055.brandonscore.lib;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by brandon3055 on 21/10/2016.
 * A simple interface that can be implemented by any tile whose block extends BlockBCore.
 * Note: canProvidePower mist be set to true in the blocks constructor.
 */
public interface IRedstoneEmitter {

    int getWeakPower(BlockState blockState, Direction side);

    int getStrongPower(BlockState blockState, Direction side);
}
