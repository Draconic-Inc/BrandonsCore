package com.brandon3055.brandonscore.integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.*;

/**
 * Created by brandon3055 on 29/9/2015.
 */
public class ModHelperBC {


    private static boolean initialized = false;
    private static List<String> loadedMods = null;
    private static Map<String, String> modNameMap = null;
    private static Map<String, String> modVersionMap = null;

    public static boolean isJEIInstalled;
    public static boolean isPIInstalled;

    public static void init() {
        if (initialized) return;

        loadedMods = Collections.synchronizedList(new ArrayList<>());
        modNameMap = Collections.synchronizedMap(new HashMap<String, String>());
        modVersionMap = Collections.synchronizedMap(new HashMap<String, String>());

        for (ModInfo mod : ModList.get().getMods()) {
            loadedMods.add(mod.getModId());
            modNameMap.put(mod.getModId(), mod.getDisplayName());
            String version = mod.getVersion().toString();
            if (version.equals("${mod_version}")) {
                version = "9.9.9.9";
            }
            modVersionMap.put(mod.getModId(), version);
        }

        isJEIInstalled = ModList.get().isLoaded("jei");
        isPIInstalled = ModList.get().isLoaded("projectintelligence");

        initialized = true;
    }


    /**
     * @return a list of all loaded mod id's
     */
    public static List<String> getLoadedMods() {
        init();
        return ImmutableList.copyOf(loadedMods);
    }

    /**
     * @return a map of all loaded mod id's to mod names
     */
    public static Map<String, String> getModNameMap() {
        init();
        return ImmutableMap.copyOf(modNameMap);
    }

    /**
     * @return a map of all loaded mod id's to mod versions
     */
    public static Map<String, String> getModVersionMap() {
        init();
        return ImmutableMap.copyOf(modVersionMap);
    }

    /**
     * @param modid The mod id of the target mod.
     * @return Returns the human readable name for the specified mod id or nul;l if the mod is not installed.
     */
    public static String getModName(String modid) {
        return getModNameMap().get(modid);
    }

    /**
     * @param modid The mod id of the target mod.
     * @return Returns the version for the specified mod id or null if the mod is not installed.
     */
    public static String getModVersion(String modid) {
        return getModVersionMap().get(modid);
    }
}
