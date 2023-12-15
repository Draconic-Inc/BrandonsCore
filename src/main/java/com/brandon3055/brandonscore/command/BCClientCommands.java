package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.ClientOnly;
import com.brandon3055.brandonscore.client.gui.ContributorConfigGui;
import com.brandon3055.brandonscore.client.gui.HudConfigGui;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.brandon3055.brandonscore.init.ClientInit;
import com.brandon3055.brandonscore.lib.DelayedTask;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;

import static net.minecraft.ChatFormatting.*;

/**
 * Created by brandon3055 on 23/06/2017.
 */
public class BCClientCommands {

    private static long lastReload = 0;
    private static long lastLink = 0;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal("bcore_client");

        builder.then(contributor());
        builder.then(hudConfig());
        if (BrandonsCore.inDev) {
            //builder.then(testGui());
        }

        dispatcher.register(builder);
    }


//    private static ArgumentBuilder<CommandSourceStack, ?> testGui() {
//        return Commands.literal("testui")
//                .requires(cs -> cs.hasPermission(0))
//                .then(Commands.literal("mgui")
//                        .executes(context -> {
//                            DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new ModularGuiTest(Component.literal("Test"))));
//                            return 0;
//                        }))
//                .then(Commands.literal("toolkit")
//                        .executes(context -> {
//                            DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new GuiToolkitTest(Component.literal("Test"))));
//                            return 0;
//                        }));
//    }

    private static ArgumentBuilder<CommandSourceStack, ?> hudConfig() {
        return Commands.literal("hudconfig")
                .requires(cs -> cs.hasPermission(0))
                .executes(context -> {
                    DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new HudConfigGui.Screen()));
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> contributor() {
        return Commands.literal("contributor")
                .requires(cs -> cs.hasPermission(0))
                .executes(context -> {
                    Player player = ClientOnly.getClientPlayer();
                    ContributorProperties props = ContributorHandler.getProps(player);
                    if (!props.isLoadComplete()) {
                        player.sendSystemMessage(Component.literal("Your contributor status has not yet been determined. Please wait a few seconds and try again."));
                        return 0;
                    }
                    if (!props.isContributor()) {
                        player.sendSystemMessage(Component.literal("This command allows Draconic Evolution contributors to configure their contributor perks.").withStyle(GREEN));
                        player.sendSystemMessage(Component.literal("Contributor perks are purely aesthetic features offered to those who support Draconic Evolution.").withStyle(GREEN));
                        player.sendSystemMessage(Component.literal("You can find more information on my patreon page: ").withStyle(GREEN));
                        MutableComponent link = Component.literal("www.patreon.com/brandon3055").setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/brandon3055"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open or copy link"))));
                        player.sendSystemMessage(link.withStyle(BLUE, UNDERLINE));
                        player.sendSystemMessage(Component.literal(""));
                        MutableComponent notLinked = Component.literal("Please Click Here").setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bcore_client contributor help"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click for help"))));
                        player.sendSystemMessage(Component.literal("Already a contributor? ").withStyle(LIGHT_PURPLE).append(notLinked.withStyle(BLUE)));
                        return 0;
                    }

                    DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new ContributorConfigGui.Screen(player, props)));
                    return 0;
                })
                .then(Commands.literal("help")
                        .executes(context -> {
                            Player player = ClientOnly.getClientPlayer();
                            player.sendSystemMessage(Component.literal("If you are already a contributor but you you cant access your perks there are several possible causes.").withStyle(YELLOW));
                            player.sendSystemMessage(Component.literal("Your contributor status might not have have been linked to your Minecraft user id. " +
                                    "If you are a patron then you should have received a message with instructions when you signed up. " +
                                    "If you are playing in offline mode your contributor status can not be verified. " +
                                    "Or your client might not be able to contact the contributor API for some reason.").withStyle(GRAY));
                            MutableComponent link = Component.literal("DE Discord").setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/e2HBEtF"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open or copy link"))));
                            player.sendSystemMessage(Component.literal("If you need help you can request assistance via the ").withStyle(GREEN).append(link.withStyle(BLUE, UNDERLINE)));
                            return 0;
                        })
                ).then(Commands.literal("reload")
                        .executes(context -> {
                            if (System.currentTimeMillis() - lastReload < 30000 && !BrandonsCore.inDev) {
                                Player player = ClientOnly.getClientPlayer();
                                player.sendSystemMessage(Component.literal("Please wait at least 30 seconds before running this command again").withStyle(RED));
                            } else {
                                ContributorHandler.reload();
                                lastReload = System.currentTimeMillis();
                            }
                            return 0;
                        })
                ).then(Commands.literal("link")
                        .then(Commands.argument("link_code", StringArgumentType.string())
                                .executes(context -> {
                                    Player player = ClientOnly.getClientPlayer();
                                    if (System.currentTimeMillis() - lastLink < 30000 && !BrandonsCore.inDev) {
                                        player.sendSystemMessage(Component.literal("Please wait at least 30 seconds before running this command again").withStyle(RED));
                                    } else {
                                        String linkCode = StringArgumentType.getString(context, "link_code");
                                        ContributorHandler.linkUser(player, linkCode, error -> {
                                            if (error == -1) {
                                                player.sendSystemMessage(Component.literal("Link Successful!").withStyle(ChatFormatting.GREEN));
                                            } else {
                                                player.sendSystemMessage(Component.literal("An error occurred.").withStyle(ChatFormatting.RED));
                                                player.sendSystemMessage(Component.literal(error == 404 ? "Invalid Link Code" : ("Unknown error code: " + error)).withStyle(ChatFormatting.RED));
                                            }
                                        });
                                        lastLink = System.currentTimeMillis();
                                    }
                                    return 0;
                                }))
                );
    }
}