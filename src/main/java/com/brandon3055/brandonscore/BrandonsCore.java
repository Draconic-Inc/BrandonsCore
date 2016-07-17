package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.proxy.CommonProxy;
import com.brandon3055.brandonscore.utils.LogHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = BrandonsCore.MODID, version = BrandonsCore.VERSION, name = BrandonsCore.MODNAME)
public class BrandonsCore {
    public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "BrandonsCore";
    public static final String VERSION = "${mod_version}";//MC Update, Major version, Minor Version, Release number (private or public)

    @Mod.Instance(BrandonsCore.MODID)
    public static BrandonsCore instance;

    @SidedProxy(clientSide = "com.brandon3055.brandonscore.proxy.ClientProxy", serverSide = "com.brandon3055.brandonscore.proxy.CommonProxy")
    public static CommonProxy proxy;

//    public static SimpleNetworkWrapper getNetwork() {
//        return NetworkManager.getNetwork();
//    }

//    @NetworkCheckHandler//TODO
//    public boolean networkCheck(Map<String, String> map, Side side) {
//        return true;
//    }

    public BrandonsCore() {
        LogHelper.info("Hello Minecraft!!!");
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {

    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);

        LogHelper.bigInfo("BrandonsCore Pre Initialization");

        FileHandler.init(event);
        ProcessHandler.init();
        NetworkManager.registerNetwork();
    }
}
