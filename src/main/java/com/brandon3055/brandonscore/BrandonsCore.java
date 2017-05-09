package com.brandon3055.brandonscore;

import codechicken.lib.CodeChickenLib;
import com.brandon3055.brandonscore.command.CommandTickTime;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.network.*;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = BrandonsCore.MODID, version = BrandonsCore.VERSION, name = BrandonsCore.MODNAME, dependencies = "required-after:CodeChickenLib@[" + CodeChickenLib.version + ",)")
public class BrandonsCore {
    public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "brandonscore";
    public static final String VERSION = "9.9.9";//${mod_version}";

    @Mod.Instance(BrandonsCore.MODID)
    public static BrandonsCore instance;

    @SidedProxy(clientSide = "com.brandon3055.brandonscore.client.ClientProxy", serverSide = "com.brandon3055.brandonscore.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;

    public BrandonsCore() {
        LogHelperBC.info("Hello Minecraft!!!");
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTickTime());
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        ProcessHandler.clearHandler();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FileHandler.init(event);
        proxy.preInit(event);
        ProcessHandler.init();
        registerNetwork();
    }

    public void registerNetwork() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel("BrCoreNet");
        network.registerMessage(PacketSyncableObject.Handler.class, PacketSyncableObject.class, 0, Side.CLIENT);
        network.registerMessage(PacketTileMessage.Handler.class, PacketTileMessage.class, 1, Side.SERVER);
        network.registerMessage(PacketSpawnParticle.Handler.class, PacketSpawnParticle.class, 2, Side.CLIENT);
        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 3, Side.CLIENT);
        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 4, Side.SERVER);
        network.registerMessage(PacketTickTime.Handler.class, PacketTickTime.class, 5, Side.CLIENT);
        network.registerMessage(PacketTileMessage.Handler.class, PacketTileMessage.class, 6, Side.CLIENT);
    }
}

