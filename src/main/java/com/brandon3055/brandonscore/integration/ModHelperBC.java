package com.brandon3055.brandonscore.integration;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 29/9/2015.
 */
public class ModHelperBC {

    private static Map<String, String> loadedMods = null;

    public static boolean isJEIInstalled;


    public static void init() {
        isJEIInstalled = Loader.isModLoaded("jei");
    }


    /**
     * @return a map of Modid to Mod Name for all loaded mods
     */
    public static Map<String, String> getLoadedMods() {
        if (loadedMods == null) {
            loadedMods = Collections.synchronizedMap(new HashMap<String, String>());
            for (ModContainer mod : Loader.instance().getModList()) {
                loadedMods.put(mod.getModId(), mod.getName());
            }
        }
        return loadedMods;
    }

}
