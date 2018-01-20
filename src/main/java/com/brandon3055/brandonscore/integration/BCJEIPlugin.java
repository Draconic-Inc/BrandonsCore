package com.brandon3055.brandonscore.integration;

import com.brandon3055.brandonscore.registry.ModFeatureParser;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;

/**
 * Created by brandon3055 on 14/06/2017.
 */
@JEIPlugin
public class BCJEIPlugin extends BlankModPlugin {

    public static IJeiRuntime jeiRuntime = null;
    public static IIngredientRegistry ingredientRegistry = null;

    @Override
    public void register(IModRegistry registry) {
        ModFeatureParser.getFeaturesToHide(stack -> registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack));
        ingredientRegistry = registry.getIngredientRegistry();
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime iJeiRuntime) {
        jeiRuntime = iJeiRuntime;
    }
}
