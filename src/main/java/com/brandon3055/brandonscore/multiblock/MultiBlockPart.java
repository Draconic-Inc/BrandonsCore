package com.brandon3055.brandonscore.multiblock;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collection;
import java.util.List;

/**
 * Created by brandon3055 on 27/06/2022
 * <p>
 * Defines an individual block within a multi-block structure.
 */
public interface MultiBlockPart {

    boolean isMatch(Level level, BlockPos pos);

    Collection<Block> validBlocks();

    default Block getFirstValidBlock() {
        List<Block> list = Lists.newArrayList(validBlocks());
        return list.isEmpty() ? Blocks.AIR : list.get(0);
    }
}
