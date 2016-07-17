package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.network.PacketUpdateMount;
import com.brandon3055.brandonscore.utils.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class BCClientEventHandler {

    private static int remountTicksRemaining = 0;
    private static int remountEntityID = 0;

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            searchForPlayerMount();
        }
    }

    @SubscribeEvent
    public void joinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerSP) {
            BrandonsCore.getNetwork().sendToServer(new PacketUpdateMount(0));
        }
    }

    private void searchForPlayerMount() {
		if (remountTicksRemaining > 0){
			Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(remountEntityID);
			if (e != null){
				Minecraft.getMinecraft().thePlayer.startRiding(e);
				LogHelper.info("Successfully placed player on mount after "+(500 - remountTicksRemaining)+" ticks");
				remountTicksRemaining = 0;
				return;
			}
			remountTicksRemaining--;
			if (remountTicksRemaining == 0){
				LogHelper.error("Unable to locate player mount after 500 ticks! Aborting");
				BrandonsCore.getNetwork().sendToServer(new PacketUpdateMount(-1));
			}
		}
    }

    public static void tryRepositionPlayerOnMount(int id) {
        if (remountTicksRemaining == 500) return;
        remountTicksRemaining = 500;
        remountEntityID = id;
        LogHelper.info("Started checking for player mount"); //Todo move to core as this is part of the teleporter
    }
}
