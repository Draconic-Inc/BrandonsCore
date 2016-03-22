package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public MinecraftServer getMCServer() {
		return super.getMCServer();
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	@Override
	public boolean isSpaceDown() {
		return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
	}

	@Override
	public boolean isShiftDown() {
		return Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown();
	}

	@Override
	public boolean isCtrlDown() {
		return Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}
}
