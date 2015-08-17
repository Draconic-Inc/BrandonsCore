package com.brandon3055.brandonscore.common;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Created by Brandon on 14/5/2015.
 */
public class CommonProxy {


	public boolean isDedicatedServer() {return true;}

	public MinecraftServer getMCServer(){
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	public World getClientWorld() { return null; }

	public boolean isOp(String paramString)
	{
		MinecraftServer localMinecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
		paramString = paramString.trim();
		for (String str : localMinecraftServer.getConfigurationManager().func_152606_n()) {
			if (paramString.equalsIgnoreCase(str)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpaceDown()
	{
		return false;
	}

	public boolean isCtrlDown()
	{
		return false;
	}

	public boolean isShiftDown()
	{
		return false;
	}

	public EntityPlayer getClientPlayer(){
		return null;
	}
}
