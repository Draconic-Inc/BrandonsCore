package com.brandon3055.brandonscore.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by brandon3055 on 13/06/2017.<br><br>
 * This annotations allows BrandonsCore to find this class and register the features defined within it.
 * You are allowed to have more than one of these per mod if for example you want separate classes for items and blocks.<br><br>
 *
 * Classes using this annotation can also optionally implement {@link IModFeatures} which allows
 * you to specify creative tabs to have your features added to.<br>
 * Note: although your mod is allowed to have more than 1 class annotated with @ModFeatures only one if those classes can implement {@link IModFeatures}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModFeatures {

    /**
     * @return the mod id of the mod these features belong to.
     */
    String modid();
}
