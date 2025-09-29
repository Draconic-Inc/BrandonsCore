package com.brandon3055.brandonscore.client.hud;

import codechicken.lib.gui.modular.lib.GuiRender;
import com.brandon3055.brandonscore.api.hud.AbstractHudElement;
import com.brandon3055.brandonscore.api.math.Vector2;
import com.brandon3055.brandonscore.client.gui.HudConfigGui;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 30/7/21
 */
public class HudManager {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final ResourceKey<Registry<AbstractHudElement>> HUD_TYPE = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "hud_elements"));
    public static Registry<AbstractHudElement> HUD_REGISTRY;
    protected static Map<ResourceLocation, AbstractHudElement> hudElements = new HashMap<>();

    public static void init(IEventBus modBus) {
        LOCK.lock();

        NeoForge.EVENT_BUS.addListener(HudManager::onDrawOverlayPre);
        NeoForge.EVENT_BUS.addListener(HudManager::onDrawOverlayPost);
        NeoForge.EVENT_BUS.addListener(HudManager::onClientTick);
        modBus.addListener(HudManager::createRegistry);
        modBus.addListener(HudManager::onLoadComplete);
        modBus.addListener(HudManager::registerBuiltIn);
    }

    public static void onDrawOverlayPre(RenderGuiEvent.Pre event) {
        if (event.isCanceled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        GuiRender render = GuiRender.convert(event.getGuiGraphics());
        boolean configuring = mc.screen instanceof HudConfigGui.Screen;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); //Fixes broken hud when underwater
        for (AbstractHudElement element : hudElements.values()) {
            if (element.shouldRender(true)) {
                render.pose().pushPose();
                element.render(render, event.getPartialTick().getGameTimeDeltaPartialTick(false), configuring);
                render.pose().popPose();
            }
        }
    }

    public static void onDrawOverlayPost(RenderGuiEvent.Post event) {
        GuiRender render = GuiRender.convert(event.getGuiGraphics());
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        boolean configuring = mc.screen instanceof HudConfigGui.Screen;
        for (AbstractHudElement element : hudElements.values()) {
            if (element.shouldRender(false)) {
                render.pose().pushPose();
                element.render(render, event.getPartialTick().getGameTimeDeltaPartialTick(false), configuring);
                render.pose().popPose();
            }
        }
    }

    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        boolean configuring = mc.screen instanceof HudConfigGui.Screen;
        for (AbstractHudElement element : hudElements.values()) {
            element.tick(configuring);
        }
        HudData.clientTick();
    }

    private static void createRegistry(NewRegistryEvent event) {
        HUD_REGISTRY = event.create(new RegistryBuilder<>(HUD_TYPE)
                .sync(false)
        );
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        hudElements.clear();
        for (ResourceLocation key : HUD_REGISTRY.keySet()) {
            hudElements.put(key, HUD_REGISTRY.get(key));
        }
        HudData.loadSettings();
    }

    public static void registerBuiltIn(RegisterEvent event) {
        event.register(HUD_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "item_hud"), () -> new HudDataElement(new Vector2(0, 0.20494), true, false).setEnabled(false));
        event.register(HUD_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "block_hud"), () -> new HudDataElement(new Vector2(0, 0.04593), false, true).setEnabled(false));
        event.register(HUD_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "block_item_hud"), () -> new HudDataElement(new Vector2(0.99023, 0.72438), true, true));
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
