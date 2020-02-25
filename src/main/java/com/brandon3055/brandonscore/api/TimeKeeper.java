package com.brandon3055.brandonscore.api;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by brandon3055 on 12/10/19.
 */
public class TimeKeeper {

    private static int serverTick = 0;
    private static int clientTick = 0;

    static {
        MinecraftForge.EVENT_BUS.register(new TimeKeeper());
    }

    @SubscribeEvent
    protected void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            clientTick++;
        }
    }

    @SubscribeEvent
    protected void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            serverTick++;
        }
    }

    public static int getServerTick() {
        return serverTick;
    }

    public static int getClientTick() {
        return clientTick;
    }

}
