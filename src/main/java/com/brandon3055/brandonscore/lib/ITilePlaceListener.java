package com.brandon3055.brandonscore.lib;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;

/**
 * Created by brandon3055 on 23/11/2016.
 * Can be implemented by tiles that need to run custom code when placed. Works with {@link com.brandon3055.brandonscore.blocks.ItemBlockBCore}
 */
public interface ITilePlaceListener {

    void onTilePlaced(BlockItemUseContext context, BlockState state);
}
