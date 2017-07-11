package com.brandon3055.brandonscore;

import codechicken.lib.CodeChickenLib;
import com.brandon3055.brandonscore.command.BCUtilCommands;
import com.brandon3055.brandonscore.command.CommandTickTime;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.network.PacketContributor;
import com.brandon3055.brandonscore.network.PacketSpawnParticle;
import com.brandon3055.brandonscore.network.PacketTickTime;
import com.brandon3055.brandonscore.network.PacketUpdateMount;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.registry.ModFeatureParser;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = BrandonsCore.MODID,
        version = BrandonsCore.VERSION,
        name = BrandonsCore.MODNAME,
        guiFactory = "com.brandon3055.brandonscore.BCGuiFactory",
        dependencies = "required-after:codechickenlib@[" + CodeChickenLib.MOD_VERSION + ",);required-after:redstoneflux;")
public class BrandonsCore {
    public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "brandonscore";
    public static final String VERSION = "${mod_version}";
    public static final String NET_CHANNEL = "BCPCChannel";

    @Mod.Instance(BrandonsCore.MODID)
    public static BrandonsCore instance;

    @SidedProxy(clientSide = "com.brandon3055.brandonscore.client.ClientProxy", serverSide = "com.brandon3055.brandonscore.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;

    public BrandonsCore() {
        Logger deLog = LogManager.getLogger("projectintelligence");
        LogHelperBC.info("Brandon's Core online! Waiting for Draconic Evolution to join the party....");
        if (Loader.isModLoaded("projectintelligence")) {
            deLog.log(Level.INFO, "Draconic Evolution online!");
            LogHelperBC.info("Hay! There you are! Now lets destroy some worlds!!");
            deLog.log(Level.INFO, "Sounds like fun! Lets get to it!");
        }
        else {
            deLog.log(Level.INFO, "...");
            LogHelperBC.info("Aww... Im sad now...");
        }
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTickTime());
        event.registerServerCommand(new BCUtilCommands());
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        ProcessHandler.clearHandler();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FileHandler.init(event);
        ModFeatureParser.parseASMData(event.getAsmData());
        ModConfigParser.parseASMData(event.getAsmData());
        ModConfigParser.loadConfigs(event);
        proxy.preInit(event);
        ProcessHandler.init();
        registerNetwork();
        proxy.registerPacketHandlers();
    }

    public void registerNetwork() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel("BCoreNet");
        network.registerMessage(PacketSpawnParticle.Handler.class, PacketSpawnParticle.class, 0, Side.CLIENT);
        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 1, Side.CLIENT);
        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 2, Side.SERVER);
        network.registerMessage(PacketTickTime.Handler.class, PacketTickTime.class, 3, Side.CLIENT);
        network.registerMessage(PacketContributor.Handler.class, PacketContributor.class, 4, Side.CLIENT);
        network.registerMessage(PacketContributor.Handler.class, PacketContributor.class, 5, Side.SERVER);
    }
}

