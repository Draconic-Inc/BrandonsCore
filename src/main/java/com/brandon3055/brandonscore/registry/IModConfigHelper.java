package com.brandon3055.brandonscore.registry;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by brandon3055 on 14/06/2017.<br><br>
 *
 */
public interface IModConfigHelper {

    /**
     * Create a configuration with the file name and location of your choice and return it via this method.
     */
    Configuration createConfiguration(FMLPreInitializationEvent event);

    /**
     * Specify a comment for the given category.
     */
    default String getCategoryComment(String category) {
        return "";
    }

    /**
     * Called when a config property is changed ether via the config gui or via syncing values with server.
     * @param propertyName
     */
    default void onConfigChanged(String propertyName, String propertyCategory) {}
}
