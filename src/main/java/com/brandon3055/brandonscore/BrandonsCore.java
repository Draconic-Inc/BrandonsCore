package com.brandon3055.brandonscore;

import codechicken.lib.reflect.ObfMapping;
import codechicken.lib.reflect.ReflectionManager;
import com.brandon3055.brandonscore.client.ClientProxy;
import com.brandon3055.brandonscore.command.BCUtilCommands;
import com.brandon3055.brandonscore.command.CommandTPX;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.lib.IEquipmentManager;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.worldentity.WorldEntityHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;


@Mod(BrandonsCore.MODID)
public class BrandonsCore {
    public static final Logger LOGGER = LogManager.getLogger("BrandonsCore"); //TODO going to slowly transition everything to this.
    public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "brandonscore";
    public static final String VERSION = "${mod_version}";
    public static CommonProxy proxy;
    public static boolean inDev = false;
    public static IEquipmentManager equipmentManager = null;

    public BrandonsCore() {
        FileHandler.init();

        //Knock Knock...
        Logger deLog = LogManager.getLogger("draconicevolution");
        if (ModList.get().isLoaded("draconicevolution")) {
            LogHelperBC.info("Knock Knock...");
            deLog.log(Level.WARN, "Reactor detonation initiated.");
            LogHelperBC.info("Wait... NO! What?");
            LogHelperBC.info("Stop That! That's not how this works!");
            deLog.log(Level.WARN, "Calculating explosion ETA");
            LogHelperBC.info("Ahh... NO... NONONO! DONT DO THAT!!! STOP THIS NOW!");
            deLog.log(Level.WARN, "**Explosion Imminent!!!**");
            LogHelperBC.info("Well...... fork...");
        } else {
            LogHelperBC.info("Hey! Where's DE?");
            LogHelperBC.info("Oh well.. At least we dont have to worry about getting blown up now...");
        }

        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        proxy.construct();
        FMLJavaModLoadingContext.get().getModEventBus().register(this);

        MinecraftForge.EVENT_BUS.addListener(BrandonsCore::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(BrandonsCore::onServerStop);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(WorldEntityHandler::createRegistry);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        proxy.commonSetup(event);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        proxy.clientSetup(event);
    }

    @SubscribeEvent
    public void onServerSetup(FMLDedicatedServerSetupEvent event) {
        proxy.serverSetup(event);
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        BCUtilCommands.register(event.getDispatcher());
        if (BCConfig.enable_tpx){
            CommandTPX.register(event.getDispatcher());
        }
    }

    public static void onServerStop(FMLServerStoppedEvent event) {
        ProcessHandler.clearHandler();
        WorldEntityHandler.serverStopped();
    }
}

