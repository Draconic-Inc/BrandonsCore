package com.brandon3055.brandonscore.utils;

import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Vector3;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 20/07/2016.
 * This will contain some useful utils for handling features (items/blocks) registered via a {@link ModFeatureParser}
 */
public class FeatureUtils {

    /**
     * Safely spawns an ItemStack in the world after checking that the item/block is enabled.
     * Also safe to use with any item/block that is not handled by a {@link ModFeatureParser}
     *
     * @param stack        The stack to spawn in the world
     * @param world        The world
     * @param dropLocation The location
     */
    public static void dropItem(ItemStack stack, World world, Vector3 dropLocation) {
        if (stack.isEmpty()) {
            return;
        }

//        Item item = stack.getItem();
//        Object object = item instanceof BlockItem ? ((BlockItem) item).getBlock() : item;
//
//        TODO features?
//        if (ModFeatureParser.isFeature(object) && !ModFeatureParser.isEnabled(object)){
//            return;
//        }

        ItemUtils.dropItem(stack, world, dropLocation);
    }

    public static void dropItemNoDellay(ItemStack stack, World world, Vector3 dropLocation) {
        if (stack.isEmpty()) {
            return;
        }

//        Item item = stack.getItem();
//        Object object = item instanceof BlockItem ? ((BlockItem) item).getBlock() : item;
//
//        if (ModFeatureParser.isFeature(object) && !ModFeatureParser.isEnabled(object)){
//            return;
//        }

        ItemEntity entityItem = new ItemEntity(world, dropLocation.x, dropLocation.y, dropLocation.z, stack);
        entityItem.setMotion(world.rand.nextGaussian() * 0.05, world.rand.nextGaussian() * 0.05 + 0.2F, world.rand.nextGaussian() * 0.05);
        entityItem.setNoPickupDelay();
        world.addEntity(entityItem);
    }

}
