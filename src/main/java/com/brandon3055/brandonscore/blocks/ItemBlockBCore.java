package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.config.FeatureWrapper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(BlockBCore.TILE_DATA_TAG)) {
            tooltip.add(I18n.translateToLocal("info.de.hasSavedData.txt"));
        }
    }
}
