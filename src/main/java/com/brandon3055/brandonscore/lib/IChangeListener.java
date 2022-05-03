package com.brandon3055.brandonscore.lib;

import net.minecraft.core.BlockPos;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public interface IChangeListener {

    void onNeighborChange(BlockPos neighbor);
}
