package com.brandon3055.brandonscore.integration;

import com.brandon3055.brandonscore.registry.ModFeatureParser;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

/**
 * Created by brandon3055 on 14/06/2017.
 */
@JEIPlugin
public class BCJEIPlugin extends BlankModPlugin {

    @Override
    public void register(IModRegistry registry) {
        ModFeatureParser.getFeaturesToHide(stack -> registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack));
    }
}
