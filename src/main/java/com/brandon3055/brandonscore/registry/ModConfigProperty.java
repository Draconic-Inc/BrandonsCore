//package com.brandon3055.brandonscore.registry;
//
//import java.lang.annotation.*;
//
///**
// * Created by brandon3055 on 18/3/2016.
// * Based on OpenModsLib ConfigProperty
// */
//@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.FIELD)
//public @interface ModConfigProperty {
//
//    /**
//     * @return the name of this config property.
//     */
//    String name();
//
//    /**
//     * @return the category for this config property.
//     */
//    String category();
//
//    /**
//     * @return the comment for this config property.
//     */
//    String comment() default "";
//
//    boolean requiresMCRestart() default false;
//
//    boolean requiresWorldRestart() default false;
//
//    /**
//     * Return true if this config option needs to be the same both client and server side..
//     */
//    boolean requiresSync() default false;
//
//    /**
//     * Set this to true to have the server automatically sync this config with the client on connect.
//     * <p>
//     * Note: this will not actually write the config to disk client side it will be reverted when the player disconnects.
//     */
//    boolean autoSync() default false;
//
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.FIELD)
//    @interface MinMax {
//        String min() default "-2147483648";
//        String max() default "2147483647";
//    }
//
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.FIELD)
//    @interface ValidValues {
//        /**
//         * Allows you to define a list of valid values.
//         */
//        String[] values();
//    }
//
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.FIELD)
//    @interface ListRestrictions {
//
//        /**
//         * Define the max allowed length of this list.
//         */
//        int maxLength() default 2147483647;
//
//        /**
//         * If true the maxLength becomes that fixed length.
//         */
//        boolean fixedLength() default false;
//    }
//}
