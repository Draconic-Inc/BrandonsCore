package com.brandon3055.brandonscore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by brandon3055 on 07/07/2022
 */
class EmptyPart implements MultiBlockPart {
    @Override
    public boolean isMatch(Level level, BlockPos pos) {
        return level.isEmptyBlock(pos);
    }

    @Override
    public Collection<Block> validBlocks() {
        return Collections.singleton(Blocks.AIR);
    }
}
