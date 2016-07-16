package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.config.FeatureWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base item block for all custom item blocks.
 */
public class ItemBlockBCore extends ItemBlock {

    public ItemBlockBCore(Block block, FeatureWrapper feature) {
        super(block);
    }

    public ItemBlockBCore(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(BlockBCore.TILE_DATA_TAG)) {
            tooltip.add(I18n.format("info.de.hasSavedData.txt"));
        }
    }
}
