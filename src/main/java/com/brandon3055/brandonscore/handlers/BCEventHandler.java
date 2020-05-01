package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.inventory.BlockToStackHelper;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brandon3055 on 26/08/2016.
 */
@Mod.EventBusSubscriber(modid = BrandonsCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCEventHandler {

    public static List<UUID> noClipPlayers = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ItemEntity && BlockToStackHelper.itemCollection != null && !event.isCanceled()) {
            BlockToStackHelper.itemCollection.add(((ItemEntity) event.getEntity()).getItem());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onConfigChanges(ModConfig.ModConfigEvent event) {
//        if (ModConfigParser.hasConfig(event.getModID())) {
//            ModConfigParser.onConfigChanged(event.getModID());
//        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void disconnectEvent(ClientPlayerNetworkEvent.LoggedOutEvent event) {
//        ModConfigParser.disconnectFromServer();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            noClipPlayers.remove(mc.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity && event.getPlayer().getServer() != null && event.getPlayer().getServer().isDedicatedServer()) {
            BCoreNetwork.sendConfigToClient((ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        noClipPlayers.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof PlayerEntity && noClipPlayers.contains(event.getEntity().getUniqueID())) {
            event.getEntity().noClip = true;
            ((PlayerEntity) event.getEntity()).abilities.isFlying = true;
        }
    }
}
