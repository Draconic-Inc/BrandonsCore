package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.common.CommonProxy;
import com.brandon3055.brandonscore.common.handlers.FileHandler;
import com.brandon3055.brandonscore.common.handlers.ProcessHandler;
import com.brandon3055.brandonscore.common.utills.LogHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod(modid = BrandonsCore.MODID, version = BrandonsCore.VERSION, name = BrandonsCore.MODNAME)
public class BrandonsCore
{
	public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "BrandonsCore";
    public static final String VERSION = "2.1.0.11";//MC Update, Major version, Minor Version, Release number (private or public)

	@Mod.Instance(BrandonsCore.MODID)
	public static BrandonsCore instance;

	@SidedProxy(clientSide = "com.brandon3055.brandonscore.client.ClientProxy", serverSide = "com.brandon3055.brandonscore.common.CommonProxy")
	public static CommonProxy proxy;

	@NetworkCheckHandler
	public boolean networkCheck(Map<String, String> map, Side side) {
		return true;
	}

	public BrandonsCore(){
		LogHelper.info("Hello Minecraft!!!");
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{

	}

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
		FileHandler.init(event);
		ProcessHandler.init();
    }
}
