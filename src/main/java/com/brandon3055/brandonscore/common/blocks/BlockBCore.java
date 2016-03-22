package com.brandon3055.brandonscore.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base block class form all blocks.
 */
public class BlockBCore extends Block {

	public BlockBCore() {
		super(Material.rock);
	}

	public BlockBCore(Material material) {
		super(material);
	}

	//region Rename field names
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		super.getSubBlocks(item, tab, list);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
	//endregion

	//region Setters & Getters
	public Block setHarvestTool(String toolClass, int level){
		this.setHarvestLevel(toolClass, level);
		return this;
	}
}
	//endregion
