package com.brandon3055.brandonscore.api;

import net.minecraft.nbt.CompoundTag;

/**
 * Created by brandon3055 on 12/06/2017.
 * Implemented by tile entities that save their data when harvested.
 */
public interface IDataRetainingTile {

    void writeToItemStack(CompoundTag tileCompound, boolean willHarvest);

    void readFromItemStack(CompoundTag tileCompound);

    /**
     * @return false to completely disable tile data saving and restore default harvest logic. Needed because IDataRetainingTile is now implemented on {@link com.brandon3055.brandonscore.blocks.TileBCBase}
     */
    default boolean saveToItem() { return true; }
}
