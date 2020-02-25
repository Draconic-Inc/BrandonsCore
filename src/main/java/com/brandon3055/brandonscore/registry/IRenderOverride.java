//package com.brandon3055.brandonscore.registry;
//
//import codechicken.lib.render.item.IItemRenderer;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
///**
// * Created  by brandon3055 on 21/3/2016.
// * Implement this to override normal renderer registration ant implement custom renderers.
// * Use this for things like Tile entity renderers and {@link IItemRenderer} registration
// */
//public interface IRenderOverride {
//
//    /**
//     * Use this to register a custom renderer for your item or block.
//     * Keep in mind that by default normal registration will still occur.
//     * Override registerNormal to change this.
//     * Remember to add SideOnly to this or you WILL crash servers.
//     */
//    @OnlyIn(Dist.CLIENT)
//    void registerRenderer(Feature feature);
//
//    /**
//     * If you still want normal registration to occur then override this method and return true.
//     */
//    default boolean registerNormal(Feature feature) { return false; }
//}
