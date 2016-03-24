package com.brandon3055.brandonscore.common.config;

import com.brandon3055.brandonscore.common.utills.LogHelper;
import net.minecraftforge.common.config.Configuration;

import java.lang.reflect.Field;

/**
 * Created by brandon3055 on 24/3/2016.
 * This is based on the config system in Open Mods but almost completely rewritten and simplified
 */
public class ModConfigProcessor {

	public ModConfigProcessor(){}

	public void processConfig(Class configClass, Configuration config){
		for (Field field : configClass.getFields()) {
			if (field.isAnnotationPresent(ModConfigProperty.class)){
				ModConfigProperty property = field.getAnnotation(ModConfigProperty.class);
				try {
					Object defaultValue = field.get(null);
					Object newValue = getConfigValue(defaultValue, config, property);
					field.set(null, newValue);
				}
				catch (Exception e) {
					LogHelper.error("Something when wrong while loading config value ["+property.name()+"]");
					e.printStackTrace();
				}
			}
		}
		if (config.hasChanged()) config.save();
	}

	public static Object getConfigValue(Object defaultValue, Configuration configuration, ModConfigProperty property) throws Exception {
		if (defaultValue instanceof Boolean){
			return configuration.get(property.category(), property.name(), (Boolean)defaultValue, property.comment()).getBoolean();
		}else if (defaultValue instanceof boolean[]){
			return configuration.get(property.category(), property.name(), (boolean[])defaultValue, property.comment()).getBooleanList();
		}else if (defaultValue instanceof Double){
			return configuration.get(property.category(), property.name(), (Double)defaultValue, property.comment()).getDouble((Double)defaultValue);
		}else if (defaultValue instanceof double[]){
			return configuration.get(property.category(), property.name(), (double[])defaultValue, property.comment()).getDoubleList();
		}else if (defaultValue instanceof Integer){
			return configuration.get(property.category(), property.name(), (Integer)defaultValue, property.comment()).getInt((Integer) defaultValue);
		}else if (defaultValue instanceof int[]){
			return configuration.get(property.category(), property.name(), (int[])defaultValue, property.comment()).getIntList();
		}else if (defaultValue instanceof String){
			return configuration.get(property.category(), property.name(), (String)defaultValue, property.comment()).getString();
		}else if (defaultValue instanceof String[]){
			return configuration.get(property.category(), property.name(), (String[])defaultValue, property.comment()).getStringList();
		}
		throw new Exception("Config data class is unknown");
	}
}
