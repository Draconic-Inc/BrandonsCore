package com.brandon3055.brandonscore.lib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 23/11/2016.
 * Can be implemented by tiles that need to run custom code when placed. Works with {@link com.brandon3055.brandonscore.blocks.ItemBlockBCore}
 */
public interface ITilePlaceListener {

    void onTilePlaced(World world, BlockPos pos, EnumFacing placedAgainst, float hitX, float hitY, float hitZ, EntityPlayer placer, ItemStack stack);
}
