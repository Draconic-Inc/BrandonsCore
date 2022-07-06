package com.brandon3055.brandonscore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by brandon3055 on 07/07/2022
 */
class BlockPart implements MultiBlockPart {
    private final Block block;

    public BlockPart(ResourceLocation id) {
        if (!ForgeRegistries.BLOCKS.containsKey(id)) {
            throw new IllegalStateException("Specified block could not be found: " + id);
        }
        this.block = ForgeRegistries.BLOCKS.getValue(id);
    }

    @Override
    public boolean isMatch(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(block);
    }

    @Override
    public Collection<Block> validBlocks() {
        return Collections.singleton(block);
    }
}
