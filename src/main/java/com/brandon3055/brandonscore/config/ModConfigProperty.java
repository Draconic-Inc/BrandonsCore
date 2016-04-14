package com.brandon3055.brandonscore.config;

import java.lang.annotation.*;

/**
 * Created by brandon3055 on 18/3/2016.
 * Based on OpenModsLib ConfigProperty
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModConfigProperty {
	public String name();

	public String category();

	public String comment() default "";
}
