package com.brandon3055.brandonscore.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 19/12/2022
 */
public class EntityBlockBCore extends BlockBCore implements EntityBlock {

    private Supplier<BlockEntityType<? extends TileBCore>> blockEntityType = null;
    private boolean enableTicking;

    public EntityBlockBCore(Properties properties) {
        super(properties);
    }

    public BlockBCore setBlockEntity(Supplier<BlockEntityType<? extends TileBCore>> blockEntityType, boolean enableTicking) {
        this.blockEntityType = blockEntityType;
        this.enableTicking = enableTicking;
        return this;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockEntityType.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        if (enableTicking && blockEntityType.get() == entityType) {
            return (e, e2, e3, tile) -> ((TileBCore) tile).tick();
        }
        return null;
    }

    @Deprecated //Probably dont need this?
    protected boolean hasBlockEntity() {
        return this instanceof EntityBlockBCore;
    }
}
