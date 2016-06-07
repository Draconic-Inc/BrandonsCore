package com.brandon3055.brandonscore.lib;

import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * Created by brandon3055 on 25/3/2016.
 * This stores all sound events for Brandon's Core
 */
public class BCSoundEvents {

    public static final SoundEvent portalSound;

    private static SoundEvent getRegisteredSoundEvent(String id) {
        SoundEvent soundevent = (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation(id));

        if (soundevent == null) {
            throw new IllegalStateException("Invalid Sound requested: " + id);
        } else {
            return soundevent;
        }
    }

    static {
        if (!Bootstrap.isRegistered()) {
            throw new RuntimeException("Accessed Sounds before Bootstrap!");
        } else {
            portalSound = getRegisteredSoundEvent("brandonscore:portal");
        }
    }
}
