package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.inventory.BlockToStackHelper;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brandon3055 on 26/08/2016.
 */
@Mod.EventBusSubscriber(modid = BrandonsCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BCEventHandler {

    public static List<UUID> noClipPlayers = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ItemEntity && BlockToStackHelper.itemCollection != null && !event.isCanceled()) {
            BlockToStackHelper.itemCollection.add(((ItemEntity) event.getEntity()).getItem());
            event.setCanceled(true);
        }
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void disconnectEvent(ClientPlayerNetworkEvent.LoggedOutEvent event) {
//        ModConfigParser.disconnectFromServer();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            noClipPlayers.remove(mc.player.getUUID());
        }
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer && event.getPlayer().getServer() != null && event.getPlayer().getServer().isDedicatedServer()) {
            BCoreNetwork.sendConfigToClient((ServerPlayer) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        noClipPlayers.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof Player && noClipPlayers.contains(event.getEntity().getUUID())) {
            event.getEntity().noPhysics = true;
            ((Player) event.getEntity()).getAbilities().flying = true;
        }
    }
}
