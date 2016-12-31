package com.brandon3055.brandonscore.config;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 24/3/2016.
 * This is based on the config system in Open Mods but almost completely rewritten and simplified
 */
public class ModConfigProcessor {

    private Configuration config;
    private Map<String, String> categoryComments;
    private Class[] configClasses;

    public ModConfigProcessor() {
    }

    public void initialize(Configuration config, Class... configClass) {
        initialize(config, new HashMap<String, String>(), configClass);
    }

    public void initialize(Configuration config, Map<String, String> categoryComments, Class... configClasses) {
        this.config = config;
        this.categoryComments = categoryComments;
        this.configClasses = configClasses;
    }

    public void loadConfig() {
        if (config == null || configClasses == null) {
            throw new RuntimeException("A mod using ModConfigProcessor attempted to load configuration before initializing the config processor or after initializing with null arguments");
        }

        for (Class clazz : configClasses) {
            for (Field field : clazz.getFields()) {
                if (field.isAnnotationPresent(ModConfigProperty.class)) {
                    ModConfigProperty property = field.getAnnotation(ModConfigProperty.class);
                    try {
                        Object defaultValue = field.get(null);
                        Object newValue = getConfigValue(defaultValue, config, property);
                        field.set(null, newValue);
                    }
                    catch (Exception e) {
                        LogHelperBC.error("Something when wrong while loading config value [" + property.name() + "]");
                        e.printStackTrace();
                    }
                }
            }
        }

        for (String category : categoryComments.keySet()) {
            //if (config.hasCategory(category)) {
                config.setCategoryComment(category, categoryComments.get(category));
            //}
        }

        saveConfig();
    }

    public static Object getConfigValue(Object defaultValue, Configuration configuration, ModConfigProperty property) throws Exception {
        if (defaultValue instanceof Boolean) {
            return configuration.get(property.category(), property.name(), (Boolean) defaultValue, property.comment()).getBoolean((Boolean) defaultValue);
        }
        else if (defaultValue instanceof boolean[]) {
            return configuration.get(property.category(), property.name(), (boolean[]) defaultValue, property.comment()).getBooleanList();
        }
        else if (defaultValue instanceof Double) {
            return configuration.get(property.category(), property.name(), (Double) defaultValue, property.comment()).getDouble((Double) defaultValue);
        }
        else if (defaultValue instanceof double[]) {
            return configuration.get(property.category(), property.name(), (double[]) defaultValue, property.comment()).getDoubleList();
        }
        else if (defaultValue instanceof Integer) {
            return configuration.get(property.category(), property.name(), (Integer) defaultValue, property.comment()).getInt((Integer) defaultValue);
        }
        else if (defaultValue instanceof int[]) {
            return configuration.get(property.category(), property.name(), (int[]) defaultValue, property.comment()).getIntList();
        }
        else if (defaultValue instanceof String) {
            return configuration.get(property.category(), property.name(), (String) defaultValue, property.comment()).getString();
        }
        else if (defaultValue instanceof String[]) {
            return configuration.get(property.category(), property.name(), (String[]) defaultValue, property.comment()).getStringList();
        }
        throw new Exception("Config data class is unknown");
    }

    /**
     * Returns a config property fur the purpose of editing config values.
     * saveConfig() must be called to write the changes to disk.
     * This will not change the static values that have already been loaded so those will have to also be changed if
     * you make a change to the property associated with them.
     *
     * @param category The property category.
     * @param name The name or key for the property.
     * @return The config property if it exists or null if it could not be found.
     */
    public Property findProperty(String category, String name) {
        if (config.getCategory(category) != null) {
            return config.getCategory(category).get(name);
        }
        return null;
    }

    public void saveConfig() {
        if (config != null && config.hasChanged()) {
            config.save();
        }
    }
}
