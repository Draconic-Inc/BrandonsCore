package com.brandon3055.brandonscore.common.config;

import net.minecraft.item.ItemBlock;

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
	public String name();

	public boolean isActive() default true;

	public boolean isConfigurable() default true;

	public String[] variantMap() default {};

	public boolean hasCustomItemBlock() default false;

	public Class<? extends ItemBlock> getItemBlock() default ItemBlock.class;

	public int cTab() default 0;
}
