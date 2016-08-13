package com.brandon3055.brandonscore.config;

import java.lang.annotation.*;

/**
 * Created by brandon3055 on 18/3/2016.
 * Based on OpenModsLib ConfigProperty
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModConfigProperty {

    /**
     * @return the name of this config property.
     */
    String name();

    /**
     * @return the category for this config property.
     */
    String category();

    /**
     * @return the comment for this config property.
     */
    String comment() default "";
}
