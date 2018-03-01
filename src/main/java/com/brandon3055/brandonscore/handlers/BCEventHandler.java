package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.inventory.BlockToStackHelper;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 26/08/2016.
 */
public class BCEventHandler {

    public static List<String> noClipPlayers = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem && BlockToStackHelper.itemCollection != null && !event.isCanceled()) {
            BlockToStackHelper.itemCollection.add(((EntityItem) event.getEntity()).getItem());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent()
    public void onConfigChanges(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (ModConfigParser.hasConfig(event.getModID())) {
            ModConfigParser.onConfigChanged(event.getModID());
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent()
    public void disconnectEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ModConfigParser.disconnectFromServer();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            noClipPlayers.remove(mc.player.getName());
        }
    }

    @SubscribeEvent()
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP && event.player.getServer() != null && event.player.getServer().isDedicatedServer()) {
            PacketDispatcher.sendConfigToClient((EntityPlayerMP) event.player);
        }
    }

    public void playerLoggehOut(PlayerEvent.PlayerLoggedOutEvent event) {
        noClipPlayers.remove(event.player.getName());
    }

    @SubscribeEvent()
    public void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof EntityPlayer && noClipPlayers.contains(event.getEntity().getName())) {
            event.getEntity().noClip = true;
            ((EntityPlayer) event.getEntity()).capabilities.isFlying = true;
        }
    }
}
