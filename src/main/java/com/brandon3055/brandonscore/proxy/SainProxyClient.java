package com.brandon3055.brandonscore.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class SainProxyClient {

    public boolean isDedicatedServer() {
        return false;
    }

//    public MinecraftServer getMCServer() {
//        return super.getMCServer();
//    }

    public static World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    public static boolean isJumpKeyDown() {
        return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
    }

    public static boolean isSneakKeyDown() {
        return Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown();
    }

    public static boolean isSprintKeyDown() {
        return Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
    }

    public static EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

}
