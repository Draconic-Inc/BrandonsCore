package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * This command has all of the functionality of the vanilla tp command but with the ability
 * to teleport across dimensions,
 * <p>
 * Created by brandon3055 on 23/12/2017.
 */
public class HudConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("draconic_hud_config")
                        .executes(HudConfigCommand::openGui)
        );
    }

    private static int openGui(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BCoreNetwork.sendOpenHudConfig(ctx.getSource().getPlayerOrException());
        return 0;
    }

}