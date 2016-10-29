package com.brandon3055.brandonscore.config;

import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by brandon3055 on 18/3/2016.
 * Used to identify a feature (item or block) that can be disabled via the config
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Feature {
    /**
     * used For All (Must be snake_case)
     */
    String registryName();

    /**
     * used For All
     */
    boolean isActive() default true;

    /**
     * used For All
     */
    boolean isConfigurable() default true;

    /**
     * used For All
     */
    String[] variantMap() default {};

    /**
     * used For Items with a single variant (Kinda works with variantMap)
     * Used to load an items variant from an alternate location.
     * Usefull if you want to load multiple items from a single blockstate.
     */
    String stateOverride() default "";

    /**
     * used For Blocks
     */
    Class<? extends ItemBlock> itemBlock() default ItemBlock.class;

    /**
     * used For Blocks
     */
    Class<? extends TileEntity> tileEntity() default TileEntity.class;

    /**
     * used For All
     * set to -1 to exclude from creative tab.
     */
    int cTab() default 0;
}
