package com.brandon3055.brandonscore.config;

/**
 * Created by brandon3055 on 21/3/2016.
 * Implemented by blocks and items that require different registration code
 *
 * For example blocks that need to register more than 1 TileEntity
 */
public interface ICustomRegistry {

    /**
     * Use this for example to register multiple tile entities or to do custom item/block registration.
     * If you do not register your item/block here it will not be registered!
     */
    void registerFeature(Feature feature);
}
