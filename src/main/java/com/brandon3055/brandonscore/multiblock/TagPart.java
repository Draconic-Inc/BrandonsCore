package com.brandon3055.brandonscore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 07/07/2022
 */
public class TagPart implements MultiBlockPart {
    private TagKey<Block> tag;
    private List<Block> blockCache;

    public TagPart(TagKey<Block> tag) {
        this.tag = tag;
    }

    @Override
    public boolean isMatch(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof StructurePart) {
            return ((StructurePart) state.getBlock()).is(level, pos, tag);
        }
        return state.is(tag);
    }

    @Override
    public Collection<Block> validBlocks() {
        if (blockCache == null) {
            blockCache = ForgeRegistries.BLOCKS.getValues()
                    .stream()
                    .filter(block -> block.defaultBlockState().is(tag))
                    .collect(Collectors.toList());
        }
        return blockCache;
    }
}
