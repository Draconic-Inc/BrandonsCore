package com.brandon3055.brandonscore.handlers;

import net.covers1624.quack.util.CrashLock;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.*;

/**
 * Created by brandon3055 on 26/08/2016.
 */
public class BCEventHandler {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static Set<UUID> noClipPlayers = new HashSet<>();

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.addListener(BCEventHandler::playerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(BCEventHandler::livingUpdate);
    }

    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        noClipPlayers.remove(event.getEntity().getUUID());
    }

    public static void livingUpdate(LivingTickEvent event) {
        if (event.getEntity() instanceof Player && noClipPlayers.contains(event.getEntity().getUUID())) {
            event.getEntity().noPhysics = true;
            ((Player) event.getEntity()).getAbilities().flying = true;
        }
    }
}
