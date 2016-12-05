package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.config.FeatureWrapper;
import net.minecraft.block.Block;

/**
 * Created by brandon3055 on 14/11/2016.
 */
@Deprecated //Moving functionality to ItemBlockBCore
public class ItemBlockSubs extends ItemBlockBasic {
    public ItemBlockSubs(Block block, FeatureWrapper feature) {
        super(block, feature);
        this.setHasSubtypes(true);
    }

    public ItemBlockSubs(Block block) {
        super(block);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
