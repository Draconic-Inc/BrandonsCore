package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.lib.IBCoreBlock;
import com.brandon3055.brandonscore.lib.ITilePlaceListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base item block for all custom item blocks.
 */
public class ItemBlockBCore extends BlockItem {

    public ItemBlockBCore(Block block, Item.Properties builder) {
        super(block, builder);
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);

        TileEntity tile = context.getWorld().getTileEntity(context.getPos());
        if (placed && tile instanceof ITilePlaceListener) {
            ((ITilePlaceListener) tile).onTilePlaced(context, state);
        }

        return placed;
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        if (getBlock() instanceof IBCoreBlock && ((IBCoreBlock) getBlock()).overrideShareTag()) {
            return ((IBCoreBlock) getBlock()).getNBTShareTag(stack);
        }
        return super.getShareTag(stack);
    }
}
