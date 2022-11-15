package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.covers1624.quack.util.CrashLock;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by brandon3055 on 14/8/19.
 * <p>
 * Operational Potential is the power system used by Draconic Evolution and related mods.
 * This system is an extension of Forge Energy that allows long based power transfer and storage.
 */
public class CapabilityOP {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static Capability<IOPStorage> OP = CapabilityManager.get(new CapabilityToken<>(){});

    public static void init() {
        LOCK.lock();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(CapabilityOP::register);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IOPStorage.class);
    }
}
