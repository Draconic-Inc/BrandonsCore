package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.client.particle.BCEffectHandler;
import com.brandon3055.brandonscore.utils.ModelUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        BCEffectHandler.initialize();
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ModelUtils());
    }

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
