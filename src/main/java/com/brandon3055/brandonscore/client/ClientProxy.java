package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.client.particle.BCEffectHandler;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.ModelUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        BCEffectHandler.iniEffectRenderer();
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ModelUtils());
        MinecraftForge.EVENT_BUS.register(new BCClientEventHandler());
        DLRSCache.initialize();
        ProcessHandlerClient.init();
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
    public boolean isJumpKeyDown() {
        return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
    }

    @Override
    public boolean isSneakKeyDown() {
        return Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown();
    }

    @Override
    public boolean isSprintKeyDown() {
        return Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public void setChatAtIndex(ITextComponent chat, int index) {
        if (chat == null) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().deleteChatLine(index);
        } else {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat, index);
        }
    }
}
