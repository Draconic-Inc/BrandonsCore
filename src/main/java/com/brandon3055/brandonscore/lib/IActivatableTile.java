package com.brandon3055.brandonscore.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;

/**
 * Created by brandon3055 on 21/10/2016.
 */
public interface IActivatableTile {

    boolean onBlockActivated(IBlockState state, PlayerEntity player, EnumHand hand, Direction side, float hitX, float hitY, float hitZ);

}
