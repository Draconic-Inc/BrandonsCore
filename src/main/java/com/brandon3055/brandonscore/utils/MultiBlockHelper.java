package com.brandon3055.brandonscore.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by brandon3055 on 1/4/2016.
 * Base class for custom multi block structures.
 */
public class MultiBlockHelper {
    public BlockPos invalidBlock = null;
    public String expectedBlock = null;

    public boolean checkBlock(String name, World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (name.equals("")) {
            return true;
        } else if (name.equals("air")) {
            return state.getBlock().isAir(state, world, pos);
        } else {
            return ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().equals(name);
        }
    }

    public void setBlock(String name, World world, BlockPos pos) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
        if (block != null) {
            world.setBlockState(pos, block.getDefaultState());
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

    }

    /**
     * This is called for every block in the structure when MultiBlockStorage#forEachInStructure is called.
     *
     * @param pos      is the position of the block in the world.
     * @param startPos is position 0, 0, 0 in the storage array
     * @param flag     is passed through from forEachInStructure and can be used for whatever you want.
     */
    public void forBlock(String name, World world, BlockPos pos, BlockPos startPos, int flag) {
        //You can override this method and do some custom stuff with this block
    }
}
