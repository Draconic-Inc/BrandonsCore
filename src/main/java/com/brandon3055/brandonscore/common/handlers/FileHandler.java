package com.brandon3055.brandonscore.common.handlers;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * Created by Brandon on 7/6/2015.
 */
public class FileHandler {
	public static File configFolder;
	public static File mcDirectory;

	public static void init(FMLPreInitializationEvent event){
		configFolder = event.getModConfigurationDirectory();
		mcDirectory = configFolder.getParentFile();
	}
}
