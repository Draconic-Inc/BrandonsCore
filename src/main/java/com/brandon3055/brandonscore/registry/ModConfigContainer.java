//package com.brandon3055.brandonscore.registry;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
///**
// * Created by brandon3055 on 13/06/2017.<br><br>
// * This annotations allows BrandonsCore to find this class and handle the config fields contained within it.
// * You are allowed to have more than one of these per mod.<br><br>
// *
// * one (and only one) class implementing this annotation MUST implement {@link IModConfigHelper} which allows you to specify a config file
// * and adds a lot of extra config functionality and useful events.<br>
// */
//@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.TYPE)
//public @interface ModConfigContainer {
//
//    /**
//     * @return the mod id of the mod this config class belongs to.
//     */
//    String modid();
//}
