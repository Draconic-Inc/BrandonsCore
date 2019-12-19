package com.brandon3055.brandonscore.integration;

import com.brandon3055.projectintelligence.api.PiAPI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 21/09/18.
 */
public class PIHelper {

    public static boolean isInstalled() {
        return Loader.isModLoaded("projectintelligence");
    }

    @Optional.Method(modid = "projectintelligence")
    public static KeyBinding getETGuiKey() {
        return PiAPI.getETGuiKey();
    }

    @Optional.Method(modid = "projectintelligence")
    public static List<String> getRelatedPages(ItemStack stack) {
        return PiAPI.getRelatedPages(stack);
    }

    @Optional.Method(modid = "projectintelligence")
    public static void openGui(@Nullable Screen parentScreen, List<String> pageURIs) {
        PiAPI.openGui(parentScreen, pageURIs);
    }

}