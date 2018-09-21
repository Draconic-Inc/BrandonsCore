package com.brandon3055.brandonscore;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.brandonscore.network.ServerPacketHandler;
import com.brandon3055.brandonscore.registry.ModFeatureParser;
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
        MinecraftForge.EVENT_BUS.register(new ModFeatureParser());
        ModHelperBC.init();
    }

    public void registerPacketHandlers() {
        PacketCustom.assignHandler("BCPCChannel", new ServerPacketHandler());
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

    public boolean isCTRLKeyDown() {
        return false;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void setChatAtIndex(ITextComponent chat, int index) {

    }

    public void addProcess(IProcess iProcess) {
        ProcessHandler.addProcess(iProcess);
    }

    public void registerModFeatures(String modid) {
        ModFeatureParser.registerMod(modid);
    }

    public void runSidedProcess(IProcess process) {
        ProcessHandler.addProcess(process);
    }
}
