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
//        inDev = Minecraft///VERSION.equals("${mod_version}");
        FileHandler.init();

        //Knock Knock...
        synchronized (MinecraftForge.EVENT_BUS) {
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
        }

        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        proxy.construct();
        FMLJavaModLoadingContext.get().getModEventBus().register(this);

        MinecraftForge.EVENT_BUS.addListener(BrandonsCore::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(BrandonsCore::onServerStop);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(WorldEntityHandler::createRegistry);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, BCConfig.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, BCConfig.SERVER_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, BCConfig.COMMON_SPEC);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        proxy.commonSetup(event);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        proxy.clientSetup(event);
        posWindow();
    }

    @OnlyIn(Dist.CLIENT)
    private void posWindow() {
        MainWindow window = Minecraft.getInstance().getMainWindow();
        GLFW.glfwSetWindowPos(window.getHandle(), 0, 1080);
        GLFW.glfwMaximizeWindow(window.getHandle());
    }

//    private boolean autoConnected = false;
//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent
//    public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
//        if (event.getGui() instanceof MainMenuScreen && !autoConnected) {
//            autoConnected = true;
//            new Thread(() -> {
////                SneakyUtils.sneak(() -> Thread.sleep(3000)).run();
//                Minecraft mc = Minecraft.getInstance();
////                mc.deferTask(() -> mc.launchIntegratedServer("Main Test World", "Main Test World", null));
//            }).start();
//        }
//    }

    @SubscribeEvent
    public void onServerSetup(FMLDedicatedServerSetupEvent event) {
        proxy.serverSetup(event);
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        BCUtilCommands.register(event.getDispatcher());
        CommandTPX.register(event.getDispatcher());
    }

    public static void onServerStop(FMLServerStoppedEvent event) {
        ProcessHandler.clearHandler();
        WorldEntityHandler.serverStopped();
    }

    @SubscribeEvent
    public void fixFletcher(RegistryEvent.Register<PointOfInterestType> event) {

//        event.getRegistry().register(new PointOfInterestType("fletcher", PointOfInterestType.getAllStates(Blocks.FLETCHING_TABLE), 1, thing -> thing == PointOfInterestType.FLETCHER, 1).setRegistryName("fletcher_fix"));
//        event.getRegistry().register(new PointOfInterestType("fletcher", PointOfInterestType.getAllStates(Blocks.COAL_BLOCK), 1, thing -> thing == PointOfInterestType.FLETCHER, 1).setRegistryName("fletcher_fix2"));

        //Fix fletcher
        Set<BlockState> stateSet = new HashSet<>(PointOfInterestType.getAllStates(Blocks.FLETCHING_TABLE));
        PointOfInterestType.FLETCHER.blockStates = ImmutableSet.copyOf(stateSet);
        PointOfInterestType.POIT_BY_BLOCKSTATE.put(Blocks.FLETCHING_TABLE.getDefaultState(), PointOfInterestType.FLETCHER);

        //Fix Armorer
        stateSet = new HashSet<>(PointOfInterestType.getAllStates(Blocks.BLAST_FURNACE));
        PointOfInterestType.ARMORER.blockStates = ImmutableSet.copyOf(stateSet);
        PointOfInterestType.POIT_BY_BLOCKSTATE.put(Blocks.BLAST_FURNACE.getDefaultState(), PointOfInterestType.ARMORER);
    }

//
//    @Mod.EventHandler
//    public void serverStart(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandTickTime());
//        event.registerServerCommand(new BCUtilCommands());
//        event.registerServerCommand(new CommandTPX());
//    }
//
//    @Mod.EventHandler
//    public void preInit(FMLPreInitializationEvent event) {
//        FileHandler.init(event);
//        ModFeatureParser.parseASMData(event.getAsmData());
//        ModConfigParser.parseASMData(event.getAsmData());
//        ModConfigParser.loadConfigs(event);
//        proxy.preInit(event);
//        ProcessHandler.init();
//        proxy.registerPacketHandlers();
//    }

//    public void registerNetwork() {
//        network = NetworkRegistry.INSTANCE.newSimpleChannel("BCoreNet");
//        network.registerMessage(PacketSpawnParticle.Handler.class, PacketSpawnParticle.class, 0, Side.CLIENT);
//        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 1, Side.CLIENT);
//        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 2, Side.SERVER);
//        network.registerMessage(PacketTickTime.Handler.class, PacketTickTime.class, 3, Side.CLIENT);
//        network.registerMessage(PacketContributor.Handler.class, PacketContributor.class, 4, Side.CLIENT);
//        network.registerMessage(PacketContributor.Handler.class, PacketContributor.class, 5, Side.SERVER);
//    }
}

