package com.brandon3055.brandonscore.handlers;

import net.covers1624.quack.util.CrashLock;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brandon3055 on 26/08/2016.
 */
public class BCEventHandler {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static List<UUID> noClipPlayers = new ArrayList<>();

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.addListener(BCEventHandler::playerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(BCEventHandler::livingUpdate);
    }

    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        noClipPlayers.remove(event.getPlayer().getUUID());
    }

    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof Player && noClipPlayers.contains(event.getEntity().getUUID())) {
            event.getEntity().noPhysics = true;
            ((Player) event.getEntity()).getAbilities().flying = true;
        }
    }
}
