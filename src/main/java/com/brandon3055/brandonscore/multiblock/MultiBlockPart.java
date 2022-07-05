package com.brandon3055.brandonscore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

/**
 * Created by brandon3055 on 27/06/2022
 * <p>
 * Defines an individual block within a multi-block structure.
 */
public interface MultiBlockPart {

    boolean isMatch(Level level, BlockPos pos);

    Collection<Block> validBlocks();
}
