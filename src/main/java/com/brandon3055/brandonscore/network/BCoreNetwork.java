package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustomChannelBuilder;
import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

/**
 * Created by brandon3055 on 17/12/19.
 */
public class BCoreNetwork {

    public static final ResourceLocation CHANNEL = new ResourceLocation(BrandonsCore.MODID + ":network");
    public static EventNetworkChannel netChannel;

    public static void init() {
        netChannel = PacketCustomChannelBuilder.named(CHANNEL)
                .networkProtocolVersion(() -> "1")//
                .clientAcceptedVersions(e -> true)//
                .serverAcceptedVersions(e -> true)//
                .assignClientHandler(() -> ClientPacketHandler::new)//
                .assignServerHandler(() -> ServerPacketHandler::new)//
                .build();
    }


    public static void dispatchToggleDislocators() {
        PacketCustom packet = new PacketCustom(CHANNEL, 1);
        packet.sendToServer();
    }

    public static void dispatchToolProfileChange(boolean armor) {
        PacketCustom packet = new PacketCustom(CHANNEL, 2);
        packet.writeBoolean(armor);
        packet.sendToServer();
    }

    public static void dispatchCycleDigAOE(boolean depth) {
        PacketCustom packet = new PacketCustom(CHANNEL, 3);
        packet.writeBoolean(depth);
        packet.sendToServer();
    }

    public static void dispatchCycleAttackAOE(boolean reverse) {
        PacketCustom packet = new PacketCustom(CHANNEL, 4);
        packet.writeBoolean(reverse);
        packet.sendToServer();
    }

}
