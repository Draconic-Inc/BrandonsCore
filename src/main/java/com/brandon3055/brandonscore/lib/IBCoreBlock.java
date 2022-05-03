package com.brandon3055.brandonscore.lib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Created by brandon3055 on 31/10/18.
 */
public interface IBCoreBlock {

    default boolean overrideShareTag() {
        return false;
    }

    default CompoundTag getNBTShareTag(ItemStack stack) {
        return stack.getTag();
    }
}
