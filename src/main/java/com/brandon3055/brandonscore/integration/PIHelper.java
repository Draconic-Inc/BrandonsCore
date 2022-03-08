package com.brandon3055.brandonscore.integration;

import com.brandon3055.projectintelligence.api.PiAPI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 21/09/18.
 */
public class PIHelper {

    public static boolean isInstalled() {
        return ModList.get().isLoaded("projectintelligence");
    }

    public static KeyBinding getETGuiKey() {
        assert isInstalled();
        return _getETGuiKey();
    }

    private static KeyBinding _getETGuiKey() {
        return PiAPI.getETGuiKey();
    }

    public static List<String> getRelatedPages(ItemStack stack) {
        assert isInstalled();
        return _getRelatedPages(stack);
    }

    private static List<String> _getRelatedPages(ItemStack stack) {
        return PiAPI.getRelatedPages(stack);
    }

    public static void openGui(@Nullable Screen parentScreen, List<String> pageURIs) {
        assert isInstalled();
        _openGui(parentScreen, pageURIs);
    }

    public static void openMod(@Nullable Screen parentScreen, String modid)  {
        assert isInstalled();
        _openMod(parentScreen, modid);
    }

    private static void _openGui(@Nullable Screen parentScreen, List<String> pageURIs) {
        PiAPI.openGui(parentScreen, pageURIs);
    }

    private static void _openMod(@Nullable Screen parentScreen, String modid) {
        PiAPI.openModPage(parentScreen, modid);
    }
}