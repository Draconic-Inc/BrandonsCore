package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.handlers.BCEventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Brandon on 14/5/2015.
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new BCEventHandler());
    }

    public boolean isDedicatedServer() {
        return true;
    }

    public MinecraftServer getMCServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public World getClientWorld() {
        return null;
    }

    public boolean isOp(String paramString) {
        MinecraftServer localMinecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        paramString = paramString.trim();
        for (String str : localMinecraftServer.getPlayerList().getOppedPlayerNames()) {
            if (paramString.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public boolean isJumpKeyDown() {
        return false;
    }

    public boolean isSprintKeyDown() {
        return false;
    }

    public boolean isSneakKeyDown() {
        return false;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void setChatAtIndex(ITextComponent chat, int index) {

    }
}
