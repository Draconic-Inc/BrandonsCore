package com.brandon3055.brandonscore.registry;

import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by brandon3055 on 13/06/2017.<br>
 * Base feature annotation used by ModFeatureHandler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModFeature {

    /**
     * @return The registry name for the feature.
     */
    String name();

    /**
     * @return whether or not this feature is active. Sets the default state for the config.
     */
    boolean isActive() default true;

    /**
     * @return false to prevent the generation of a config option to disable this feature.
     */
    boolean canBeDisabled() default true;

    /**
     * Use this to map item metadata to BlockState variants.
     * Example variant map:<br>
     * {"0:type=normal", "1:type=nether", "2:type=end"}<br><br>
     * Used for both Items and ItemBlocks.<br>
     * In the case of blocks this applies to the ItemBlock.
     */
    String[] variantMap() default {};

    /**
     * "As is BlockState override"
     * This can be used in 2 ways. The first is to supply a fully qualified model resource location<br>
     * e.g. armor#type=wyvernHelm<br>
     * where "armor" is the name of the BlockState json and "type=wyvernHelm" is the variant.<br>
     * The other option is to just specify the blocState without the variant and use variantMap to map the variants.<br>
     * e.g. armor <br>
     * then variantMap = {"0:type=wyvernHelm", "1:type=wyvernChest", ...etc}<br><br>
     * Used for both Items and ItemBlocks.
     */
    String stateOverride() default "";

    /**
     * Allows you to prevent the registration of an ItemBlock.
     * Used For Blocks.
     */
    boolean hasItemBlock() default true;

    /**
     * Allows you to override the ItemBlock used if this feature is a block.<br><br>
     * Used for Blocks.
     */
    Class<? extends ItemBlock> itemBlock() default ItemBlock.class;

    /**
     * Used to register a tile entity for this block if it has one.
     * If you need to register more than 1 tile then implement {@link IRegistryOverride}
     * on your block and register them manually.<br><br>
     * Used For Blocks
     */
    Class<? extends TileEntity> tileEntity() default TileEntity.class;

    /**
     * Adds this feature to one of the creative tabs defined in @ModFeatureContainer
     * set to -1 to exclude from creative tabs.<br><br>
     * Used for both Items and Blocks.
     */
    int cTab() default 0;
}
