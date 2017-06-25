package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.registry.IModConfigHelper;
import com.brandon3055.brandonscore.registry.ModConfigContainer;
import com.brandon3055.brandonscore.registry.ModConfigProperty;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * Created by brandon3055 on 15/06/2017.
 */
@ModConfigContainer(modid = "brandonscore")
public class BCTestConfig implements IModConfigHelper {
    @Override
    public Configuration createConfiguration(FMLPreInitializationEvent event) {
        return new Configuration(new File(FileHandler.brandon3055Folder, "BrandonsCore.cfg"), true);
    }

    @Override
    public String getCategoryComment(String category) {
        return "Comment for category: " + category;
    }

    @Override
    public void onConfigChanged(String name, String category) {
        LogHelperBC.dev("Config Changed! " + name);
    }

    //region Strings

    @ModConfigProperty(name = "aString", category = "Category Strings", comment = "A string value", requiresSync = true)
    public static String aString = "A string Value";

    @ModConfigProperty.ValidValues(values = {"A string Value", "A","restricted","List", "Some", "Other", "Values"})
    @ModConfigProperty(name = "aStringWithValues", category = "Category Strings", comment = "A string with predefined values", requiresSync = true)
    public static String aStringWithValues = "A string Value";

    @ModConfigProperty(name = "aStringList", category = "Category Strings", comment = "A string list value", requiresSync = true)
    public static String[] aStringList = {"A","string","List"};

    @ModConfigProperty.ListRestrictions(maxLength = 3, fixedLength = true)
    @ModConfigProperty(name = "restrictedList", category = "Category Strings", comment = "A string list with restrictions", requiresSync = true)
    public static String[] restrictedList = {"A","restricted","List"};

    //endregion


    //region Ints

    @ModConfigProperty(name = "aint", category = "Category Ints", comment = "A int value", requiresSync = true)
    public static int aint = 0;

    @ModConfigProperty(name = "aintList", category = "Category Ints", comment = "A int list value", requiresSync = true)
    public static int[] aintList = {0, 1, 6};

    @ModConfigProperty.MinMax(min = "10", max = "234")
    @ModConfigProperty.ListRestrictions(maxLength = 3, fixedLength = true)
    @ModConfigProperty(name = "restrictedintList", category = "Category Ints", comment = "A int list with restrictions", autoSync = true)
    public static int[] restrictedintList = {23, 16, 128};

    //endregion


    //region Doubles


    //endregion

    //region Booleans

    @ModConfigProperty(name = "aBoolean", category = "Category Booleans", comment = "A booelan value", requiresSync = true)
    public static boolean aBoolean = false;

    //endregion










}
