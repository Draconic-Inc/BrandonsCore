package com.brandon3055.brandonscore.init;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.*;
import com.brandon3055.brandonscore.client.hud.HudManager;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import net.covers1624.quack.util.CrashLock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by brandon3055 on 15/11/2022
 */
public class ClientInit {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init() {
        LOCK.lock();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(BCGuiSprites::initialize);

        MinecraftForge.EVENT_BUS.addListener(CursorHelper::closeGui);
        ProcessHandlerClient.init();
        HudManager.init();
        BCShaders.init();
        BCProfiler.init();
        DLRSCache.init();
    }
}
