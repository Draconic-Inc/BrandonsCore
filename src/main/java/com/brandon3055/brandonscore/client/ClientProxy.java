package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import com.brandon3055.brandonscore.utils.ModelUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.sql.Time;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
        super.commonSetup(event);
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new ModelUtils());
        MinecraftForge.EVENT_BUS.register(new BCClientEventHandler());
        DLRSCache.initialize();
        ProcessHandlerClient.init();
        BCProfiler.init();
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {

    }


    @Override
    public MinecraftServer getMCServer() {
        return super.getMCServer();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public boolean isJumpKeyDown() {
        return Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown();
    }

    @Override
    public boolean isSneakKeyDown() {
        return Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown();
    }

    @Override
    public boolean isSprintKeyDown() {
        return Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown();
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
    public void setChatAtIndex(ITextComponent chat, int index) {
        if (chat == null) {
            Minecraft.getInstance().ingameGUI.getChatGUI().deleteChatLine(index);
        } else {
            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat, index);
        }
    }

    @Override
    public void addProcess(IProcess iProcess) {
        ProcessHandlerClient.addProcess(iProcess);
    }

//    @Override
//    public void registerModFeatures(String modid) {
//        super.registerModFeatures(modid);
//        ModFeatureParser.registerModRendering(modid);
//    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }

    @Override
    public void resetEffectRenderer(World world) {
//        BCEffectHandler.effectRenderer.clearEffects(world);
    }

    @Override
    public IAnimationStateMachine loadASM(ResourceLocation location, ImmutableMap<String, ITimeValue> customParameters) {
        return ModelLoaderRegistry.loadASM(location, customParameters);
    }

    @Override
    public int tickTimer() {
        return TimeKeeper.getClientTick();
    }
}
