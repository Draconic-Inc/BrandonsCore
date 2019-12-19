package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.lib.IBCoreBlock;
import com.brandon3055.brandonscore.lib.ITilePlaceListener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base item block for all custom item blocks.
 */
public class ItemBlockBCore extends ItemBlock {

    private String registryDomain = null;

    public ItemBlockBCore(Block block) {
        super(block);
        if (block instanceof IBCoreBlock) {
            setHasSubtypes(((IBCoreBlock) block).hasSubItemTypes());
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (block instanceof IBCoreBlock && ((IBCoreBlock) block).getNameOverrides().containsKey(stack.getItemDamage())) {
            return "tile." + getRegistryDomain() + ":" +((IBCoreBlock) block).getNameOverrides().get(stack.getItemDamage());
        }

        return super.getUnlocalizedName(stack);
    }

    @Override
    public int getMetadata(int damage) {
        return getHasSubtypes() ? damage : 0;
    }

    public String getRegistryDomain() {
        if (registryDomain == null) {
            if (getRegistryName() == null) {
                return "null";
            }
            else {
                registryDomain = getRegistryName().getResourceDomain();
            }
        }

        return registryDomain;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

        TileEntity tile = world.getTileEntity(pos);
        if (placed && tile instanceof ITilePlaceListener) {
            ((ITilePlaceListener) tile).onTilePlaced(world, pos, side, hitX, hitY, hitZ, player, stack);
        }

        return placed;
    }

    @Override
    public boolean getShareTag() {
        return super.getShareTag();
    }

    @Override
    public CompoundNBT getNBTShareTag(ItemStack stack) {
        if (block instanceof IBCoreBlock && ((IBCoreBlock) block).overrideShareTag()) {
            return ((IBCoreBlock) block).getNBTShareTag(stack);
        }
        return super.getNBTShareTag(stack);
    }
}
