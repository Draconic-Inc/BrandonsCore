package com.brandon3055.brandonscore.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

/**
 * Created by brandon3055 on 21/10/2016.
 * A simple interface that can be implemented by any tile whose block extends BlockBCore.
 * Note: canProvidePower mist be set to true in the blocks constructor.
 */
public interface IRedstoneEmitter {

    int getWeakPower(IBlockState blockState, EnumFacing side);

    int getStrongPower(IBlockState blockState, EnumFacing side);
}
