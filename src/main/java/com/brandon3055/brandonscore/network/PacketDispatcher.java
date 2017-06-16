package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class PacketDispatcher {

    public static final int C_TILE_DATA_MANAGER = 1;
    public static final int C_TILE_MESSAGE = 2;
    public static final int C_SERVER_CONFIG_SYNC = 3;

    public static final int S_TILE_MESSAGE = 1;

    public static void sendConfigToClient(EntityPlayerMP player) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, C_SERVER_CONFIG_SYNC);
        ModConfigParser.writeConfigForSync(packet);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending Config To Client: " + player);
    }
}
