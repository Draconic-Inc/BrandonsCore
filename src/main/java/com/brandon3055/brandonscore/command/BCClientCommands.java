package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.ClientOnly;
import com.brandon3055.brandonscore.client.gui.ContributorConfigGui;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiToolkitTest;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiTest;
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
        if (BrandonsCore.inDev) {
            builder.then(testGui());
        }

        dispatcher.register(builder);
    }


    private static ArgumentBuilder<CommandSourceStack, ?> testGui() {
        return Commands.literal("testui")
                .requires(cs -> cs.hasPermission(0))
                .then(Commands.literal("mgui")
                        .executes(context -> {
                            DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new ModularGuiTest(new TextComponent("Test"))));
                            return 0;
                        }))
                .then(Commands.literal("toolkit")
                        .executes(context -> {
                            DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new GuiToolkitTest(new TextComponent("Test"))));
                            return 0;
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> contributor() {
        return Commands.literal("contributor")
                .requires(cs -> cs.hasPermission(0))
                .executes(context -> {
                    Player player = ClientOnly.getClientPlayer();
                    ContributorProperties props = ContributorHandler.getProps(player);
                    if (!props.isLoadComplete()) {
                        player.sendMessage(new TextComponent("Your contributor status has not yet been determined. Please wait a few seconds and try again."), Util.NIL_UUID);
                        return 0;
                    }
                    if (!props.isContributor()) {
                        player.sendMessage(new TextComponent("This command allows Draconic Evolution contributors to configure their contributor perks.").withStyle(GREEN), Util.NIL_UUID);
                        player.sendMessage(new TextComponent("Contributor perks are purely aesthetic features offered to those who support Draconic Evolution.").withStyle(GREEN), Util.NIL_UUID);
                        player.sendMessage(new TextComponent("You can find more information on my patreon page: ").withStyle(GREEN), Util.NIL_UUID);
                        MutableComponent link = new TextComponent("www.patreon.com/brandon3055").setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/brandon3055"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to open or copy link"))));
                        player.sendMessage(link.withStyle(BLUE, UNDERLINE), Util.NIL_UUID);
                        player.sendMessage(new TextComponent(""), Util.NIL_UUID);
                        MutableComponent notLinked = new TextComponent("Please Click Here").setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bcore_client contributor help"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click for help"))));
                        player.sendMessage(new TextComponent("Already a contributor? ").withStyle(LIGHT_PURPLE).append(notLinked.withStyle(BLUE)), Util.NIL_UUID);
                        return 0;
                    }

                    DelayedTask.client(10, () -> Minecraft.getInstance().setScreen(new ContributorConfigGui(player, props)));
                    return 0;
                })
                .then(Commands.literal("help")
                        .executes(context -> {
                            Player player = ClientOnly.getClientPlayer();
                            player.sendMessage(new TextComponent("If you are already a contributor but you you cant access your perks there are several possible causes.").withStyle(YELLOW), Util.NIL_UUID);
                            player.sendMessage(new TextComponent("Your contributor status might not have have been linked to your Minecraft user id. " +
                                    "If you are a patron then you should have received a message with instructions when you signed up. " +
                                    "If you are playing in offline mode your contributor status can not be verified. " +
                                    "Or your client might not be able to contact the contributor API for some reason.").withStyle(GRAY), Util.NIL_UUID);
                            MutableComponent link = new TextComponent("DE Discord").setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/e2HBEtF"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to open or copy link"))));
                            player.sendMessage(new TextComponent("If you need help you can request assistance via the ").withStyle(GREEN).append(link.withStyle(BLUE, UNDERLINE)), Util.NIL_UUID);
                            return 0;
                        })
                ).then(Commands.literal("reload")
                        .executes(context -> {
                            if (System.currentTimeMillis() - lastReload < 30000 && !BrandonsCore.inDev) {
                                Player player = ClientOnly.getClientPlayer();
                                player.sendMessage(new TextComponent("Please wait at least 30 seconds before running this command again").withStyle(RED), Util.NIL_UUID);
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
                                        player.sendMessage(new TextComponent("Please wait at least 30 seconds before running this command again").withStyle(RED), Util.NIL_UUID);
                                    } else {
                                        String linkCode = StringArgumentType.getString(context, "link_code");
                                        ContributorHandler.linkUser(player, linkCode, error -> {
                                            if (error == -1) {
                                                player.sendMessage(new TextComponent("Link Successful!").withStyle(ChatFormatting.GREEN), Util.NIL_UUID);
                                            } else {
                                                player.sendMessage(new TextComponent("An error occurred.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                                                player.sendMessage(new TextComponent(error == 404 ? "Invalid Link Code" : ("Unknown error code: " + error)).withStyle(ChatFormatting.RED), Util.NIL_UUID);
                                            }
                                        });
                                        lastLink = System.currentTimeMillis();
                                    }
                                    return 0;
                                }))
                );
    }


//    public static LiteralHiddenArgumentBuilder<CommandSourceStack> literalHidden(String literal) {
//        return LiteralHiddenArgumentBuilder.literal(literal);
//    }
//
//    public static class LiteralHiddenArgumentBuilder<S> extends ArgumentBuilder<S, LiteralHiddenArgumentBuilder<S>> {
//        private final String literal;
//
//        protected LiteralHiddenArgumentBuilder(final String literal) {
//            this.literal = literal;
//        }
//
//        public static <S> LiteralHiddenArgumentBuilder<S> literal(final String name) {
//            return new LiteralHiddenArgumentBuilder<>(name);
//        }
//
//        @Override
//        protected LiteralHiddenArgumentBuilder<S> getThis() {
//            return this;
//        }
//
//        public String getLiteral() {
//            return literal;
//        }
//
//        @Override
//        public LiteralCommandNode<S> build() {
//            final LiteralCommandNode<S> result = new LiteralCommandNode<>(getLiteral(), getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork()){
//                @Override
//                public CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
//                    return Suggestions.empty();
//                }
//
//                @Override
//                public LiteralHiddenArgumentBuilder<S> createBuilder() {
//                    final LiteralHiddenArgumentBuilder<S> builder = LiteralHiddenArgumentBuilder.literal(literal);
//                    builder.requires(getRequirement());
//                    builder.forward(getRedirect(), getRedirectModifier(), isFork());
//                    if (getCommand() != null) {
//                        builder.executes(getCommand());
//                    }
//                    return builder;
//                }
//            };
//
//            for (final CommandNode<S> argument : getArguments()) {
//                result.addChild(argument);
//            }
//
//            return result;
//        }
//    }
}