package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.config.FeatureWrapper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 21/3/2016.
 * Used for simple blocks that require some minor additional features such as blocks that have subtypes.
 */
@Deprecated //Moving functionality to ItemBlockBCore
public class ItemBlockBasic extends ItemBlockBCore {

    private FeatureWrapper feature;

    public ItemBlockBasic(Block block) {
        super(block);
    }

    public ItemBlockBasic(Block block, FeatureWrapper feature) {
        this(block);
        this.feature = feature;
        this.setHasSubtypes(feature.variantMap().length > 0);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (getHasSubtypes() && feature.nameMap.containsKey(stack.getItemDamage())) {
            return (super.getUnlocalizedName(stack) + "." + feature.nameMap.get(stack.getItemDamage())).replaceAll("=", ".").replaceAll(",", ".");
        } else return super.getUnlocalizedName(stack);
    }
}
