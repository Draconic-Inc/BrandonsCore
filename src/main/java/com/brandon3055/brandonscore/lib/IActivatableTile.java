package com.brandon3055.brandonscore.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 21/10/2016.
 */
public interface IActivatableTile {

    boolean onBlockActivated(IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ);

}
