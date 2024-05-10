package com.brandon3055.brandonscore;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by Brandon on 14/5/2015.
 */
public class CommonProxy {

    public MinecraftServer getMCServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public Level getClientWorld() {
        return null;
    }

    public boolean isCTRLKeyDown() {
        return false;
    }

    public Player getClientPlayer() {
        return null;
    }

    public void addProcess(IProcess iProcess) {
        ProcessHandler.addProcess(iProcess);
    }

    public void runSidedProcess(IProcess process) {
        ProcessHandler.addProcess(process);
    }

    @Deprecated //Local Multiplayer issue
    public void sendIndexedMessage(Player player, Component message, MessageSignature signature) {
        BCoreNetwork.sendIndexedMessage((ServerPlayer) player, message, signature);
    }

    @Deprecated //Local Multiplayer issue
    public void sendIndexedMessage(Player player, Component message, UUID sig) {
        BCoreNetwork.sendIndexedMessage((ServerPlayer) player, message, Utils.uuidToSig(sig));
    }

    public void setClipboardString(String text) {}
}
