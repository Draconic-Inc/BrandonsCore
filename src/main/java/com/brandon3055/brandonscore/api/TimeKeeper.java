package com.brandon3055.brandonscore.api;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
//            LogHelperBC.dev("Client: " + FMLCommonHandler.instance().getSide());
            LogHelperBC.dev("Client-Effective: " + FMLCommonHandler.instance().getEffectiveSide());
        }
    }

    @SubscribeEvent
    protected void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            serverTick++;
//            LogHelperBC.dev("Server: " + FMLCommonHandler.instance().getSide());
            LogHelperBC.dev("Server-Effective: " + FMLCommonHandler.instance().getEffectiveSide());
        }
    }

    public static int getServerTick() {
        return serverTick;
    }

    public static int getClientTick() {
        return clientTick;
    }

}
