package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.network.PacketSpawnParticle;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.network.PacketTileMessage;
import com.brandon3055.brandonscore.network.PacketUpdateMount;
import com.brandon3055.brandonscore.utils.LogHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod(modid = BrandonsCore.MODID, version = BrandonsCore.VERSION, name = BrandonsCore.MODNAME)
public class BrandonsCore {
    public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "BrandonsCore";
    public static final String VERSION = "${mod_version}";//MC Update, Major version, Minor Version, Release number (private or public)

    @Mod.Instance(BrandonsCore.MODID)
    public static BrandonsCore instance;

    @SidedProxy(clientSide = "com.brandon3055.brandonscore.client.ClientProxy", serverSide = "com.brandon3055.brandonscore.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;

    @NetworkCheckHandler
    public boolean networkCheck(Map<String, String> map, Side side) {
        return true;
    }

    public BrandonsCore() {
        LogHelper.info("Hello Minecraft!!!");
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);

        FileHandler.init(event);
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
    }
}
