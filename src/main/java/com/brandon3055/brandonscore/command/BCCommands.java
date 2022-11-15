package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BCConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * Created by brandon3055 on 15/11/2022
 */
public class BCCommands {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.addListener(BCCommands::registerServerCommands);
        MinecraftForge.EVENT_BUS.addListener(BCCommands::registerClientCommands);
    }

    private static void registerServerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        BCUtilCommands.register(dispatcher);
        HudConfigCommand.register(dispatcher);
        if (BCConfig.enable_tpx) {
            CommandTPX.register(dispatcher);
        }
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        BCClientCommands.register(dispatcher);
    }
}
