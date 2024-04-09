package com.brandon3055.brandonscore.client;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.ListIterator;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public MinecraftServer getMCServer() {
        return super.getMCServer();
    }

    @Override
    public Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public boolean isCTRLKeyDown() {
        return Screen.hasControlDown();
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void addProcess(IProcess iProcess) {
        ProcessHandlerClient.addProcess(iProcess);
    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }

    @Override
    public void sendIndexedMessage(Player player, Component message, MessageSignature signature) {
        Minecraft mc = Minecraft.getInstance();
        if (message == null) {
            deleteMessage(signature);
        } else {
            deleteMessage(signature);
            mc.gui.getChat().addMessage(message, signature, null);
        }
    }

    public void deleteMessage(MessageSignature signature) {
        Minecraft mc = Minecraft.getInstance();
        ListIterator<GuiMessage> listiterator = mc.gui.getChat().allMessages.listIterator();

        while(listiterator.hasNext()) {
            GuiMessage guimessage = listiterator.next();
            if (signature.equals(guimessage.signature())) {
                listiterator.remove();
                mc.gui.getChat().refreshTrimmedMessage();
            }
        }
    }

    @Override
    public void setClipboardString(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
