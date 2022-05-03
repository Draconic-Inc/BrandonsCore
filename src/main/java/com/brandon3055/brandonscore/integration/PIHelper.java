package com.brandon3055.brandonscore.integration;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by brandon3055 on 21/09/18.
 */
public class PIHelper {

    public static boolean isInstalled() {
        return ModList.get().isLoaded("projectintelligence");
    }

    public static KeyMapping getETGuiKey() {
        assert isInstalled();
        return _getETGuiKey();
    }

    private static KeyMapping _getETGuiKey() {
        return null;//PiAPI.getETGuiKey();
    }

    public static List<String> getRelatedPages(ItemStack stack) {
        assert isInstalled();
        return _getRelatedPages(stack);
    }

    private static List<String> _getRelatedPages(ItemStack stack) {
        return Collections.emptyList();//PiAPI.getRelatedPages(stack);
    }

    public static void openGui(@Nullable Screen parentScreen, List<String> pageURIs) {
        assert isInstalled();
        _openGui(parentScreen, pageURIs);
    }

    public static void openMod(@Nullable Screen parentScreen, String modid) {
        assert isInstalled();
        _openMod(parentScreen, modid);
    }

    private static void _openGui(@Nullable Screen parentScreen, List<String> pageURIs) {
//        PiAPI.openGui(parentScreen, pageURIs);
    }

    private static void _openMod(@Nullable Screen parentScreen, String modid) {
//        PiAPI.openModPage(parentScreen, modid);
    }
}