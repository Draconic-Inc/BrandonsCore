package com.brandon3055.brandonscore.client;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ResourceUtils;
import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.hud.HudManager;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import com.brandon3055.brandonscore.utils.ModelUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void construct() {
        super.construct();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BCSprites::initialize);
        MinecraftForge.EVENT_BUS.addListener(this::registerShaderReloads);
        HudManager.init();





    }

    private void registerShaderReloads(ParticleFactoryRegisterEvent event) {
        if (Minecraft.getInstance() != null && BCConfig.useShaders) {
            ResourceUtils.registerReloadListener(GuiEnergyBar.barShaderH);
            ResourceUtils.registerReloadListener(GuiEnergyBar.barShaderV);
        }
    }

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
        super.commonSetup(event);
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new ModelUtils());
        MinecraftForge.EVENT_BUS.register(new BCClientEventHandler());
        DLRSCache.initialize();
        ProcessHandlerClient.init();
        BCProfiler.init();
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(CursorHelper::closeGui);
    }


    @Override
    public MinecraftServer getMCServer() {
        return super.getMCServer();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public boolean isJumpKeyDown() {
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    public boolean isSneakKeyDown() {
        return Minecraft.getInstance().options.keyShift.isDown();
    }

    @Override
    public boolean isSprintKeyDown() {
        return Minecraft.getInstance().options.keySprint.isDown();
    }

    @Override
    public boolean isCTRLKeyDown() {
        return Screen.hasControlDown();
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void addProcess(IProcess iProcess) {
        ProcessHandlerClient.addProcess(iProcess);
    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }

    @Override
    public int tickTimer() {
        return TimeKeeper.getClientTick();
    }

    @Override
    public void sendIndexedMessage(PlayerEntity player, ITextComponent message, int index) {
        if (message == null) {
            Minecraft.getInstance().gui.getChat().removeById(index);
        } else {
            Minecraft.getInstance().gui.getChat().addMessage(message, index);
        }
    }

    @Override
    public void setClipboardString(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    @Override
    public void sendToServer(PacketCustom packet) {
        packet.sendToServer();
    }
}
