package com.brandon3055.brandonscore.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class SainProxyHandler {

    public static boolean isDedicatedServer() {
        return getMCServer() != null && getMCServer().isDedicatedServer();
    }

    public static MinecraftServer getMCServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public static World getClientWorld() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
            return SainProxyClient.getClientWorld();
        }

        return null;
    }

    public static boolean isOp(String paramString) {
        MinecraftServer localMinecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        paramString = paramString.trim();
        for (String str : localMinecraftServer.getPlayerList().getOppedPlayerNames()) {
            if (paramString.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isJumpKeyDown() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
            return SainProxyClient.isJumpKeyDown();
        }
        return false;
    }

    public static boolean isSprintKeyDown() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
            return SainProxyClient.isSprintKeyDown();
        }
        return false;
    }

    public static boolean isSneakKeyDown() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
            return SainProxyClient.isSneakKeyDown();
        }
        return false;
    }

    public static EntityPlayer getClientPlayer() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
            return SainProxyClient.getClientPlayer();
        }
        return null;
    }

}
