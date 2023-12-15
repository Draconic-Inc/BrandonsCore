package com.brandon3055.brandonscore.client.hud;

import com.brandon3055.brandonscore.api.hud.AbstractHudElement;
import com.brandon3055.brandonscore.api.math.Vector2;
import com.brandon3055.brandonscore.client.gui.HudConfigGui;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 30/7/21
 */
public class HudManager {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final ResourceKey<Registry<AbstractHudElement>> HUD_TYPE = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "hud_elements"));
    public static IForgeRegistry<AbstractHudElement> HUD_REGISTRY;
    protected static Map<ResourceLocation, AbstractHudElement> hudElements = new HashMap<>();

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.addListener(HudManager::onDrawOverlayPre);
        MinecraftForge.EVENT_BUS.addListener(HudManager::onDrawOverlayPost);
        MinecraftForge.EVENT_BUS.addListener(HudManager::onClientTick);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(HudManager::createRegistry);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(HudManager::onLoadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(HudManager::registerBuiltIn);
    }

    public static void onDrawOverlayPre(RenderGuiEvent.Pre event) {
        if (event.isCanceled()) return;
        PoseStack stack = event.getPoseStack();
        boolean configuring = Minecraft.getInstance().screen instanceof HudConfigGui.Screen;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); //Fixes broken hud when underwater
        for (AbstractHudElement element : hudElements.values()) {
            if (element.shouldRender(true)) {
                stack.pushPose();
                element.render(stack, event.getPartialTick(), configuring);
                stack.popPose();
            }
        }
    }

    public static void onDrawOverlayPost(RenderGuiEvent.Post event) {
        if (event.isCanceled()) return;
        PoseStack stack = event.getPoseStack();
        boolean configuring = Minecraft.getInstance().screen instanceof HudConfigGui.Screen;
        for (AbstractHudElement element : hudElements.values()) {
            if (element.shouldRender(false)) {
                stack.pushPose();
                element.render(stack, event.getPartialTick(), configuring);
                stack.popPose();
            }
        }
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean configuring = Minecraft.getInstance().screen instanceof HudConfigGui.Screen;
        for (AbstractHudElement element : hudElements.values()) {
            element.tick(configuring);
        }
        HudData.clientTick();
    }

    private static void createRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<AbstractHudElement>()
                .setName(HUD_TYPE.location())
                .disableSaving()
                .disableSync(), ts -> HUD_REGISTRY = ts);
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        hudElements.clear();
        for (ResourceLocation key : HUD_REGISTRY.getKeys()) {
            hudElements.put(key, HUD_REGISTRY.getValue(key));
        }
        HudData.loadSettings();
    }

    public static void registerBuiltIn(RegisterEvent event) {
        event.register(HUD_TYPE, new ResourceLocation(MODID, "item_hud"), () -> new HudDataElement(new Vector2(0, 0.20494), true, false).setEnabled(false));
        event.register(HUD_TYPE, new ResourceLocation(MODID, "block_hud"), () -> new HudDataElement(new Vector2(0, 0.04593), false, true).setEnabled(false));
        event.register(HUD_TYPE, new ResourceLocation(MODID, "block_item_hud"), () -> new HudDataElement(new Vector2(0.99023, 0.72438), true, true));
    }

    //Utils, getters, setters

    public static Map<ResourceLocation, AbstractHudElement> getHudElements() {
        return ImmutableMap.copyOf(hudElements);
    }

    @Nullable
    public static AbstractHudElement getHudElement(ResourceLocation key) {
        return hudElements.get(key);
    }
}
