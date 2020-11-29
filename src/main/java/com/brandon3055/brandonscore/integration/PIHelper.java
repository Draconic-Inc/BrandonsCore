package com.brandon3055.brandonscore.integration;

//import com.brandon3055.projectintelligence.api.PiAPI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by brandon3055 on 21/09/18.
 */
public class PIHelper {
    //TODO find an alternative to @Optional

    public static boolean isInstalled() {
        return ModList.get().isLoaded("projectintelligence");
    }

//    @Optional.Method(modid = "projectintelligence")
    public static KeyBinding getETGuiKey() {
//        return PiAPI.getETGuiKey();
        return null;
    }

//    @Optional.Method(modid = "projectintelligence")
    public static List<String> getRelatedPages(ItemStack stack) {
//        return PiAPI.getRelatedPages(stack);
        return Collections.emptyList();
    }

//    @Optional.Method(modid = "projectintelligence")
    public static void openGui(@Nullable Screen parentScreen, List<String> pageURIs) {
//        PiAPI.openGui(parentScreen, pageURIs);
    }

}