package com.brandon3055.brandonscore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by brandon3055 on 07/07/2022
 */
public class BlockPart implements MultiBlockPart {
    private final Block block;

    public BlockPart(ResourceLocation id) {
        if (!ForgeRegistries.BLOCKS.containsKey(id)) {
            throw new IllegalStateException("Specified block could not be found: " + id);
        }
        this.block = ForgeRegistries.BLOCKS.getValue(id);
    }

    @Override
    public boolean isMatch(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof StructurePart) {
            return ((StructurePart) state.getBlock()).is(level, pos, block);
        }
        return state.is(block);
    }

    @Override
    public Collection<Block> validBlocks() {
        return Collections.singleton(block);
    }
}
