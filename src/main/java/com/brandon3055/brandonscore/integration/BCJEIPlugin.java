package com.brandon3055.brandonscore.integration;

import com.brandon3055.brandonscore.api.IJEIClearance;
import com.brandon3055.brandonscore.registry.ModFeatureParser;
import mezz.jei.api.*;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by brandon3055 on 14/06/2017.
 */
@JEIPlugin
public class BCJEIPlugin implements IModPlugin {

    public static IJeiRuntime jeiRuntime = null;
    public static IIngredientRegistry ingredientRegistry = null;

    @Override
    public void register(IModRegistry registry) {
        ModFeatureParser.getFeaturesToHide(stack -> registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack));
        ingredientRegistry = registry.getIngredientRegistry();
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler() {
            @Override
            public Class getGuiContainerClass() {
                return IJEIClearance.class;
            }

            @Nullable
            @Override
            public java.util.List<Rectangle> getGuiExtraAreas(GuiContainer guiContainer) {
                if (guiContainer instanceof IJEIClearance) {
                    return ((IJEIClearance) guiContainer).getGuiExtraAreas();
                }
                return new ArrayList<>();
            }
        });

    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime iJeiRuntime) {
        jeiRuntime = iJeiRuntime;
    }
}
