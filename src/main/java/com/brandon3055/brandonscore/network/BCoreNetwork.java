package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustomChannelBuilder;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

/**
 * Created by brandon3055 on 17/12/19.
 */
public class BCoreNetwork {

    public static final ResourceLocation CHANNEL = new ResourceLocation(BrandonsCore.MODID + ":network");
    public static EventNetworkChannel netChannel;

    //Server to client
    public static final int C_TILE_DATA_MANAGER = 1;
    public static final int C_TILE_MESSAGE = 2;
    public static final int C_SERVER_CONFIG_SYNC = 3;
    public static final int C_NO_CLIP = 4;
    public static final int C_PLAYER_ACCESS = 5;
    public static final int C_PLAYER_ACCESS_UPDATE = 6;
    public static final int C_INDEXED_MESSAGE = 7;
    public static final int C_TILE_CAP_DATA = 8;
    //Client to server
    public static final int S_TILE_MESSAGE = 1;
    public static final int S_PLAYER_ACCESS_BUTTON = 2;
    public static final int S_TILE_DATA_MANAGER = 3;


    public static void sendConfigToClient(ServerPlayerEntity player) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_SERVER_CONFIG_SYNC);
//        ModConfigParser.writeConfigForSync(packet);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending Config To Client: " + player);
    }

    public static void sendNoClip(ServerPlayerEntity player, boolean enabled) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_SERVER_CONFIG_SYNC);
        packet.writeBoolean(enabled);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending NoClip update to player: " + player + " Enabled: " + enabled);
    }

    public static void sendOpenPlayerAccessUI(ServerPlayerEntity player, int windowID) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_PLAYER_ACCESS);
        packet.writeInt(windowID);
        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessUIUpdate(ServerPlayerEntity player, PlayerEntity target) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_PLAYER_ACCESS_UPDATE);
        packet.writeString(target.getGameProfile().getName());
        packet.writePos(target.getPosition());
//        packet.writeInt(target.dimension.getId());
//        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessButton(int button) {
        PacketCustom packet = new PacketCustom(CHANNEL, S_PLAYER_ACCESS_BUTTON);
        packet.writeByte(button);
        packet.sendToServer();
    }

    public static void sendIndexedMessage(ServerPlayerEntity player, ITextComponent message, int index) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_INDEXED_MESSAGE);
        packet.writeTextComponent(message);
        packet.writeInt(index);
        packet.sendToPlayer(player);
    }




    public static void init() {
        netChannel = PacketCustomChannelBuilder.named(CHANNEL)
                .networkProtocolVersion(() -> "1")//
                .clientAcceptedVersions(e -> true)//
                .serverAcceptedVersions(e -> true)//
                .assignClientHandler(() -> ClientPacketHandler::new)//
                .assignServerHandler(() -> ServerPacketHandler::new)//
                .build();
    }
}
