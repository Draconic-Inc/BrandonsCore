package com.brandon3055.brandonscore.proxy;

import net.minecraft.server.dedicated.DedicatedServer;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class SainProxyServer {

    public static boolean isDedicatedServer() {
        return SainProxyHandler.getMCServer() != null && SainProxyHandler.getMCServer() instanceof DedicatedServer;
    }
}
