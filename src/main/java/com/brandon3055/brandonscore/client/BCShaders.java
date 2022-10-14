package com.brandon3055.brandonscore.client;

import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by brandon3055 on 15/05/2022
 */
public class BCShaders {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static CCShaderInstance energyBarShader;
    public static CCUniform energyBarTime;
    public static CCUniform energyBarCharge;
    public static CCUniform energyBarEPos; //Position on screen in "real" screen pixels
    public static CCUniform energyBarESize; //Size on screen in "real" screen pixels
    public static CCUniform energyBarScreenSize; //The resolution of the actual minecraft window

    public static CCShaderInstance posColourTexAlpha0;

    public static void init() {
        LOCK.lock();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BCShaders::onRegisterShaders);
    }

    private static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(CCShaderInstance.create(event.getResourceManager(), new ResourceLocation(BrandonsCore.MODID, "energy_bar"), DefaultVertexFormat.POSITION), e -> {
            energyBarShader = (CCShaderInstance) e;
            energyBarTime = energyBarShader.getUniform("time");
            energyBarCharge = energyBarShader.getUniform("charge");
            energyBarEPos = energyBarShader.getUniform("ePos");
            energyBarESize = energyBarShader.getUniform("eSize");
            energyBarScreenSize = energyBarShader.getUniform("screenSize");
            energyBarShader.onApply(() -> energyBarTime.glUniform1f(BCClientEventHandler.elapsedTicks / 10F));
        });

        event.registerShader(CCShaderInstance.create(event.getResourceManager(), new ResourceLocation(BrandonsCore.MODID, "position_color_tex_alpha0"), DefaultVertexFormat.POSITION_COLOR_TEX), e -> {
            posColourTexAlpha0 = (CCShaderInstance) e;
        });
    }

}
