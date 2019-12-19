//package com.brandon3055.brandonscore.registry;
//
///**
// * Created by brandon3055 on 21/3/2016.
// * Implemented by blocks and items that require different registration code
// *
// * For example blocks that need to register more than 1 TileEntity
// */
//public interface IRegistryOverride {
//
//    /**
//     * Use this if you need to run additional registration tasks such as registering additional tile entities.
//     * By default normal registration will still occur.
//     */
//    void handleCustomRegistration(Feature feature);
//
//    /**
//     * Override this and return false if you want to completely disable default registration and handle
//     * everything via handleCustomRegistration.
//     */
//    default boolean enableDefaultRegistration(Feature feature) { return true; }
//}
