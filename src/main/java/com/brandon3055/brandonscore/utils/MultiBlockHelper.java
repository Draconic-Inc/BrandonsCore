package com.brandon3055.brandonscore.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by brandon3055 on 1/4/2016.
 * Base class for custom multi block structures.
 */
public class MultiBlockHelper {
    public BlockPos invalidBlock = null;
    public String expectedBlock = null;

    public boolean checkBlock(String name, Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (name.equals("")) {
            return true;
        } else if (name.equals("air")) {
            return state.isAir();
        } else {
            return ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().equals(name);
        }
    }

    public void setBlock(String name, Level world, BlockPos pos) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
        if (block != null) {
            world.setBlockAndUpdate(pos, block.defaultBlockState());
        } else {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }

    }

    /**
     * This is called for every block in the structure when MultiBlockStorage#forEachInStructure is called.
     *
     * @param pos      is the position of the block in the world.
     * @param startPos is position 0, 0, 0 in the storage array
     * @param flag     is passed through from forEachInStructure and can be used for whatever you want.
     */
    public void forBlock(String name, Level world, BlockPos pos, BlockPos startPos, int flag) {
        //You can override this method and do some custom stuff with this block
    }
}
