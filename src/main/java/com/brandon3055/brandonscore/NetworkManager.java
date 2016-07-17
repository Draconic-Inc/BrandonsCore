package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.network.PacketSpawnParticle;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.network.PacketTileMessage;
import com.brandon3055.brandonscore.network.PacketUpdateMount;
import com.brandon3055.brandonscore.utils.LogHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class NetworkManager {

    private static SimpleNetworkWrapper network;

    public static SimpleNetworkWrapper getNetwork() {
        if (network == null) {
            LogHelper.error("WHAT THE FUCK IS GOING ON HERE!?!?!?!?!?!");

            throw new RuntimeException("The network is somehow null... THIS SHOULD NOT BE POSSIBLE!!!!!!!!");
        }

        return network;
    }

    public static void registerNetwork() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel("BrCoreNet");
        network.registerMessage(PacketSyncableObject.Handler.class, PacketSyncableObject.class, 0, Side.CLIENT);
        network.registerMessage(PacketTileMessage.Handler.class, PacketTileMessage.class, 1, Side.SERVER);
        network.registerMessage(PacketSpawnParticle.Handler.class, PacketSpawnParticle.class, 2, Side.CLIENT);
        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 3, Side.CLIENT);
        network.registerMessage(PacketUpdateMount.Handler.class, PacketUpdateMount.class, 4, Side.SERVER);
    }
}
