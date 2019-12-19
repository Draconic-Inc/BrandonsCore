//package com.brandon3055.brandonscore.registry;
//
//import net.minecraft.creativetab.CreativeTabs;
//
//import javax.annotation.Nullable;
//
///**
// * Created by brandon3055 on 14/06/2017.<br><br>
// *
// * This is an optional interface that can be implemented on a class annotated with @{@link ModFeatures}
// * Currently this is only used to supply creative tabs for mod features.<br>
// * Note: If you have more than 1 {@link ModFeatures} class only one of them may implement this interface.
// */
//public interface IModFeatures {
//
//    /**
//     * Used to get a the creative tab for the specified feature.
//     * You can get the "cTab" value that you may have specified in your {@link ModFeature}
//     * annotation from the supplied {@link Feature}.
//     * The default creativeTab value if you did not specify anything will be 0.
//     *
//     * @param feature The feature information.
//     * @return a creative tab to add this feature to.
//     */
//    @Nullable
//    CreativeTabs getCreativeTab(Feature feature);
//
//}
