package com.brandon3055.brandonscore;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Created by Brandon on 14/5/2015.
 */
public class CommonProxy {

    public void construct() {
        BCoreNetwork.init();
        BCConfig.load();
    }

    public void commonSetup(FMLCommonSetupEvent event) {

        //Switched to annotation
//        MinecraftForge.EVENT_BUS.register(new BCEventHandler());
//        MinecraftForge.EVENT_BUS.register(new ModFeatureParser());
        MinecraftForge.EVENT_BUS.register(new ProcessHandler());
        ModHelperBC.init();
    }

    public void clientSetup(FMLClientSetupEvent event) {}

    public void serverSetup(FMLDedicatedServerSetupEvent event) {

    }

    public MinecraftServer getMCServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public Level getClientWorld() {
        return null;
    }

//    public boolean isOp(String paramString) {
//        MinecraftServer localMinecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
//        paramString = paramString.trim();
//        for (String str : localMinecraftServer.getPlayerList().getOppedPlayerNames()) {
//            if (paramString.equalsIgnoreCase(str)) {
//                return true;
//            }
//        }
//        return false;
//    }

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

    public Player getClientPlayer() {
        return null;
    }

//    public void setChatAtIndex(ITextComponent chat, int index) {
//
//        getClientPlayer().sendMessage();
//
//    }

    public void addProcess(IProcess iProcess) {
        ProcessHandler.addProcess(iProcess);
    }

//    public void registerModFeatures(String modid) {
//        ModFeatureParser.registerMod(modid);
//    }

    public void runSidedProcess(IProcess process) {
        ProcessHandler.addProcess(process);
    }

    public void resetEffectRenderer(Level world) {

    }

    public int tickTimer() {
        return TimeKeeper.getServerTick();
    }

    public void sendIndexedMessage(Player player, Component message, int index) {
        BCoreNetwork.sendIndexedMessage((ServerPlayer) player, message, index);
    }

    public void setClipboardString(String text) {}

    public void sendToServer(PacketCustom packet) {

    }
}
