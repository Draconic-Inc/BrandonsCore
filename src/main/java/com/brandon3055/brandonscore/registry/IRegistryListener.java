//package com.brandon3055.brandonscore.registry;
//
///**
// * Created by brandon3055 on 14/06/2017.<br>
// * Implement this on blocks or items where you need to run code before or after any of the registry events.
// * This interface can also be implemented on ItemBlocks used by your blocks.
// * Note: If you need to override registration then implement {@link IRegistryOverride}.
// * If you need to override renderer registration then implement {@link IRenderOverride}.
// */
//public interface IRegistryListener {
//
//    /**
//     * Called during pre init immediately after this feature is parsed and before any registration has occurred.
//     * @param feature The {@link Feature} object containing all of the feature information for this item/block.
//     * @return false to prevent this feature from being loaded.
//     */
//    default boolean featureParsed(Feature feature) { return true; }
//
//    /**
//     * Called during pre init both before and after the config for this feature is loaded
//     * @param feature The {@link Feature} object containing all of the feature information for this item/block.
//     * @param isPre indicates whether this is pre or post config load.
//     */
//    default void featureConfig(Feature feature, boolean isPre) {}
//}
