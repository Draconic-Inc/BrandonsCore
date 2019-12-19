package com.brandon3055.brandonscore.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Created by brandon3055 on 19/12/19.
 */
public class ClientConfigDefinitions {

    public ClientConfigDefinitions(ForgeConfigSpec.Builder builder) {
        builder
                .comment("Some Comment").translation("trans.key").worldRestart().define("test", false);
    }
}
