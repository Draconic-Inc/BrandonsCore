package com.brandon3055.brandonscore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * These methods can be used with DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientInit::getClientPlayer); in server/client contexts
 * <p>
 * Created by brandon3055 on 30/11/2022
 */
public class ClientOnly {

    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    public static boolean isClientPaused() {
        return Minecraft.getInstance().isPaused();
    }
}
