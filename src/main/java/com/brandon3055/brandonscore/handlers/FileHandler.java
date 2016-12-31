package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * Created by Brandon on 7/6/2015.
 */
public class FileHandler {
    public static File rootConfigFolder;
    public static File brandon3055Folder;
    public static File mcDirectory;

    public static void init(FMLPreInitializationEvent event) {
        rootConfigFolder = event.getModConfigurationDirectory();
        brandon3055Folder = new File(rootConfigFolder, "brandon3055");

        if (!brandon3055Folder.exists() && brandon3055Folder.mkdirs()) {
            LogHelperBC.error("Could not create config directory! Things are probably going to break!");
        }

        mcDirectory = rootConfigFolder.getParentFile();
    }
}
