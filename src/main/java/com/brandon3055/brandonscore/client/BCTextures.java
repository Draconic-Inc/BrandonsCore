package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class BCTextures {
    public static final String RESOURCE_PREFIX = BrandonsCore.MODID.toLowerCase() + ":";
    @Deprecated //TODO remove this
    public static final ResourceLocation MODULAR_GUI = new ResourceLocation(RESOURCE_PREFIX + "textures/gui/modular_gui.png");

    private static final ResourceLocation WIDGETS_LIGHT = new ResourceLocation(RESOURCE_PREFIX + "textures/gui/light/widgets.png");
    private static final ResourceLocation WIDGETS_DARK = new ResourceLocation(RESOURCE_PREFIX + "textures/gui/dark/widgets.png");

    public static final ResourceLocation WIDGETS_GENERIC = new ResourceLocation(RESOURCE_PREFIX + "textures/gui/generic/widgets_generic.png");

    public static ResourceLocation widgets() {
        return BCConfig.darkMode ? WIDGETS_DARK : WIDGETS_LIGHT;
    }
}
