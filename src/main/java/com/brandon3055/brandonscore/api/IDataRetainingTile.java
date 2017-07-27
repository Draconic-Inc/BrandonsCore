package com.brandon3055.brandonscore.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 12/06/2017.
 * Implemented by tile entities that save their data when harvested.
 */
public interface IDataRetainingTile {

    NBTTagCompound writeToItemStack(ItemStack stack, boolean willHarvest);

    NBTTagCompound readFromItemStack(ItemStack stack);

    /**
     * @return false to completely disable tile data saving and restore default harvest logic. Deeded because IDataRetainingTile is now implemented on {@link com.brandon3055.brandonscore.blocks.TileBCBase}
     */
    default boolean saveToItem() { return true; }
}
