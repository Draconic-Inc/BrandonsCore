package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class PacketDispatcher {

    public static final int C_TILE_DATA_MANAGER = 1;
    public static final int C_TILE_MESSAGE = 2;
    public static final int C_SERVER_CONFIG_SYNC = 3;
    public static final int C_NO_CLIP = 4;
    public static final int C_PLAYER_ACCESS = 5;
    public static final int C_PLAYER_ACCESS_UPDATE = 6;

    public static final int S_TILE_MESSAGE = 1;
    public static final int S_PLAYER_ACCESS_BUTTON = 2;

    public static void sendConfigToClient(EntityPlayerMP player) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, C_SERVER_CONFIG_SYNC);
        ModConfigParser.writeConfigForSync(packet);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending Config To Client: " + player);
    }

    public static void sendNoClip(EntityPlayerMP player, boolean enabled) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, C_SERVER_CONFIG_SYNC);
        packet.writeBoolean(enabled);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending NoClip update to player: " + player + " Enabled: " + enabled);
    }

    public static void sendOpenPlayerAccessUI(EntityPlayerMP player, int windowID) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, C_PLAYER_ACCESS);
        packet.writeInt(windowID);
        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessUIUpdate(EntityPlayerMP player, EntityPlayer target) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, C_PLAYER_ACCESS_UPDATE);
        packet.writeString(target.getName());
        packet.writePos(target.getPosition());
        packet.writeInt(target.dimension);
        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessButton(int button) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, S_PLAYER_ACCESS_BUTTON);
        packet.writeByte(button);
        packet.sendToServer();
    }
}
