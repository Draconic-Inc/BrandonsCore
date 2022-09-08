package com.brandon3055.brandonscore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Created by brandon3055 on 02/09/2022
 */
public interface StructurePart {

    boolean is(Level level, BlockPos pos, TagKey<Block> key);

    boolean is(Level level, BlockPos pos, Block block);
}
