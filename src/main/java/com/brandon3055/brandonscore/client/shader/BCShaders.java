package com.brandon3055.brandonscore.client.shader;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import codechicken.lib.util.ClientUtils;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by brandon3055 on 15/05/2022
 */
public class BCShaders {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final ChaosEntityShader CHAOS_ENTITY_SHADER = new ChaosEntityShader("chaos_entity", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> {
                Player player = Minecraft.getInstance().player;
                e.getTimeUniform().glUniform1f((float) ClientUtils.getRenderTime());
                e.getYawUniform().glUniform1f((float) (player.getYRot() * MathHelper.torad));
                e.getPitchUniform().glUniform1f((float) -(player.getXRot() * MathHelper.torad));
            });

    public static final ContribShader CONTRIB_BASE_SHADER = new ContribShader("contributor/contrib_base", DefaultVertexFormat.NEW_ENTITY);
    public static final ContribShader WINGS_WEB_SHADER = new ContribShader("contributor/wings_web", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader WINGS_BONE_SHADER = new ContribShader("contributor/wings_bone", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final ContribShader VET_BADGE_SHADER = new ContribShader("contributor/vet_badge", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final ContribShader BADGE_OUTLINE_SHADER = new ContribShader("contributor/badge_outline", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader BADGE_CORE_SHADER = new ContribShader("contributor/patreon_core", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader BADGE_FOIL_SHADER = new ContribShader("contributor/badge_foil", DefaultVertexFormat.NEW_ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 40)));

    public static CCShaderInstance energyBarShader;
    public static CCUniform energyBarTime;
    public static CCUniform energyBarCharge;
    public static CCUniform energyBarEPos; //Position on screen in "real" screen pixels
    public static CCUniform energyBarESize; //Size on screen in "real" screen pixels
    public static CCUniform energyBarScreenSize; //The resolution of the actual minecraft window

    public static CCShaderInstance posColourTexAlpha0;

    public static void init() {
        LOCK.lock();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        CONTRIB_BASE_SHADER.register(bus);
        WINGS_WEB_SHADER.register(bus);
        WINGS_BONE_SHADER.register(bus);
        VET_BADGE_SHADER.register(bus);
        CHAOS_ENTITY_SHADER.register(bus);
        BADGE_OUTLINE_SHADER.register(bus);
        BADGE_CORE_SHADER.register(bus);
        BADGE_FOIL_SHADER.register(bus);

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
