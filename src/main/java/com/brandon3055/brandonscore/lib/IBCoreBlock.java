package com.brandon3055.brandonscore.lib;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Map;

/**
 * Created by brandon3055 on 31/10/18.
 */
public interface IBCoreBlock {

    boolean hasSubItemTypes();

    Map<Integer, String> getNameOverrides();

    default boolean overrideShareTag() {
        return false;
    }

    default CompoundNBT getNBTShareTag(ItemStack stack) {
        return stack.getTagCompound();
    }
}
