package com.brandon3055.brandonscore.handlers.contributor;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.BLUE;

/**
 * Server side property loading is lazy.
 * When a contributor connects they send their settings to the server.
 * The server will just accept the settings packet and wait for the contributor to be verified
 * before relaying the settings to all other clients.
 * <p>
 * Created by brandon3055 on 21/11/2022
 */
public class ContributorHandler {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");
    private static final Map<UUID, ContributorProperties> CONTRIBUTOR_MAP = new HashMap<>();

    public static void init() {
        LOCK.lock();
        ContributorFetcher.init();
        MinecraftForge.EVENT_BUS.addListener(ContributorHandler::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ContributorHandler::onPlayerLogin);
    }

    //ClientPlayerNetworkEvent.LoggedInEvent, Registered in ClientInit.
    public static void onClientLogin(Player player) {
        getPropsCallback(player, props -> {
            BCoreNetwork.sendContributorConfigToServer(props);
            sendWelcomeMessage(player, props);
        });
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            CONTRIBUTOR_MAP.values()
                    .stream()
                    .filter(ContributorProperties::isContributor)
                    .filter(e -> !e.getUserID().equals(player.getUUID()))
                    .forEach(e -> BCoreNetwork.contributorConfigToClient(e).sendToPlayer(player));
        }
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        CONTRIBUTOR_MAP.values().forEach(ContributorProperties::clientTick);
    }

    /**
     * Retrieve the players ContributorProperties creating them if they do not already exist.
     * An API query is cued automatically when ContributorProperties is created.
     * <p>
     * Note: when using this method there is no guarantee that the player's contributor status
     * has finished being verified.
     */
    public static ContributorProperties getProps(Player player) {
        return CONTRIBUTOR_MAP.computeIfAbsent(player.getUUID(), uuid -> new ContributorProperties(player));
    }

    /**
     * Retrieve the players ContributorProperties creating them if they do not already exist.
     * An API query is cued automatically when ContributorProperties is created.
     * <p>
     * The properties are then returned via a callback if and when the API query finishes and the player
     * is confirmed to be a contributor.
     */
    public static void getPropsCallback(Player player, Consumer<ContributorProperties> callBack) {
        getProps(player).onContributorLoaded(callBack);
    }

    /**
     * Retrieves the contributor properties for the specified player if they exist.
     * The given callback is then attached via {@link ContributorProperties#onContributorLoaded(Consumer)}
     * where it will be called if and when the player is confirmed to be a contributor.
     * <p>
     * This method is not able to initialize a contributor if they do not already been initialized.
     */
    public static void getPropsCallback(UUID playerID, Consumer<ContributorProperties> callBack) {
        if (CONTRIBUTOR_MAP.containsKey(playerID)) {
            CONTRIBUTOR_MAP.get(playerID).onContributorLoaded(callBack);
        }
    }

    public static void handleSettingsFromClient(ServerPlayer sender, PacketCustom packet) {
        ContributorConfig newConfig = ContributorConfig.deSerialize(packet);
        getPropsCallback(sender, props -> {
            //Fixes some bugyness in the config gui when playing in single-player
            if (sender.server.isDedicatedServer() || !sender.server.isSingleplayerOwner(sender.getGameProfile())) {
                props.setConfig(newConfig);
            }
            BCoreNetwork.sentToAllExcept(BCoreNetwork.contributorConfigToClient(props), sender);
        });
    }

    public static void handleSettingsFromServer(PacketCustom packet) {
        ContributorConfig newConfig = ContributorConfig.deSerialize(packet);
        ContributorHandler.getPropsCallback(packet.readUUID(), e -> e.setConfig(newConfig));
    }

    public static void reset() {
        CONTRIBUTOR_MAP.clear();
    }

    private static void sendWelcomeMessage(Player player, ContributorProperties props) {
        if (!props.getConfig().showWelcome()) return;
        if (props.isDev()) {
            player.sendMessage(new TextComponent("DE Dev status confirmed.").withStyle(AQUA), Util.NIL_UUID);
            player.sendMessage(new TextComponent("All contributor features are available.").withStyle(AQUA), Util.NIL_UUID);
        } else if (props.isPatron()) {
            player.sendMessage(new TextComponent("Thank you for supporting Draconic Evolution!").withStyle(AQUA), Util.NIL_UUID);
        } else {
            player.sendMessage(new TextComponent("Draconic Evolution contributor features enabled!").withStyle(AQUA), Util.NIL_UUID);
        }

        MutableComponent configCommand = new TextComponent("/bcore_client contributor").withStyle(BLUE);
        configCommand.setStyle(configCommand.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bcore_client contributor"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to run command"))));
        player.sendMessage(new TextComponent("Run ")
                .append(configCommand)
                .append(new TextComponent(" to access contributor settings.")), Util.NIL_UUID);
        player.sendMessage(new TextComponent("You will also find an option to disable this message."), Util.NIL_UUID);
    }

    public static boolean shouldCancelElytra(LivingEntity entity) {
        if (entity instanceof Player player) {
            ContributorProperties props = ContributorHandler.getProps(player);
            return props.hasWings() && props.getConfig().getWingsElytra() == ContributorConfig.WingElytraCompat.HIDE_ELYTRA;
        }
        return false;
    }
}
