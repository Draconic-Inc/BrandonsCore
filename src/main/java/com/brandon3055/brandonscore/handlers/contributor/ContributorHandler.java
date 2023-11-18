package com.brandon3055.brandonscore.handlers.contributor;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingElytraCompat;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
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
    private static final ContributorFetcher FETCHER = new ContributorFetcher();
    private static final String PROJECT_KEY = "3055";

    public static void init() {
        LOCK.lock();
        FETCHER.init();
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
        if (event.getEntity() instanceof ServerPlayer player) {
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

    public static void getContributorStatus(UUID playerId, String username, Consumer<Map<String, String>> flagsCallback) {
        FETCHER.fetchContributorFlags(playerId, username, PROJECT_KEY, flagsCallback);
    }

    /**
     * Retrieve the players ContributorProperties creating them if they do not already exist.
     * An API query is cued automatically when ContributorProperties is created.
     * <p>
     * Note: when using this method there is no guarantee that the player's contributor status
     * has finished being verified.
     */
    public static ContributorProperties getProps(UUID playerId, String username) {
        return CONTRIBUTOR_MAP.computeIfAbsent(playerId, uuid -> new ContributorProperties(uuid, username));
    }

    /**
     * @see #getProps(UUID, String)
     */
    public static ContributorProperties getProps(Player player) {
        return getProps(player.getUUID(), player.getGameProfile().getName());
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
     * Retrieve the players ContributorProperties creating them if they do not already exist.
     * An API query is cued automatically when ContributorProperties is created.
     * <p>
     * The properties are then returned via a callback when the API query finishes and the player
     * is confirmed to be a contributor.
     * <p>
     * {@link #getPropsCallback(Player, Consumer)} is the preferred method as it takes online status into account.
     */
    public static void getPropsCallback(UUID playerID, Consumer<ContributorProperties> callBack) {
        getProps(playerID, null).onContributorLoaded(callBack);
    }

    public static void handleSettingsFromClient(ServerPlayer sender, PacketCustom packet) {
//        BrandonsCore.LOGGER.info("handleSettingsFromClient: Received Client Settings: " + sender);
        ContributorConfig newConfig = ContributorConfig.deSerialize(packet);
        getPropsCallback(sender, props -> {
            //Fixes some bugyness in the config gui when playing in single-player
            if (sender.server.isDedicatedServer() || !sender.server.isSingleplayerOwner(sender.getGameProfile())) {
                props.setConfig(newConfig);
//                BrandonsCore.LOGGER.info("handleSettingsFromClient: Accepted Client Settings: " + sender);
            }
            BCoreNetwork.sentToAllExcept(BCoreNetwork.contributorConfigToClient(props), sender);
        });
    }

    public static void handleSettingsFromServer(PacketCustom packet) {
        UUID uuid = packet.readUUID();
        ContributorConfig newConfig = ContributorConfig.deSerialize(packet);
//        BrandonsCore.LOGGER.info("handleSettingsFromServer: Received config from server");
        if (!FETCHER.hasUser(uuid)) {
//            BrandonsCore.LOGGER.info("handleSettingsFromServer: Not in fetcher, Reload");
            reload();
        }
        getPropsCallback(uuid, e -> {
//            BrandonsCore.LOGGER.info("handleSettingsFromServer: Apply config from server");
            e.setConfig(newConfig);
        });
    }

    public static void linkUser(Player player, String linkCode, Consumer<Integer> callback) {
        FETCHER.linkUser(player, linkCode, errorCode -> {
            if (errorCode == -1) linkSuccessful(player);
            callback.accept(errorCode);
        });
    }

    //Client side
    public static void linkSuccessful(Player player) {
        BCoreNetwork.sendContribLinkToServer();
        reload();
//        BrandonsCore.LOGGER.info("linkSuccessful: Link Successful");
        getPropsCallback(player, props -> sendWelcomeMessage(player, props));
    }

    private static long lastClientReload = 0;

    public static void handleClientLink(ServerPlayer sender) {
//        BrandonsCore.LOGGER.info("handleClientLink: Client Link Received");
        if (sender.server.isDedicatedServer() && !FETCHER.hasUser(sender.getUUID()) && System.currentTimeMillis() - lastClientReload > 60000) {
            reload();
            lastClientReload = System.currentTimeMillis();
//            BrandonsCore.LOGGER.info("handleClientLink: Client Link Accepted!");
        }
    }

    public static void reload() {
        FETCHER.reload();
        CONTRIBUTOR_MAP.clear();
    }

    private static void sendWelcomeMessage(Player player, ContributorProperties props) {
        if (!props.getConfig().showWelcome()) return;
        if (props.isDev()) {
            player.sendSystemMessage(Component.literal("DE Dev status confirmed.").withStyle(AQUA));
            player.sendSystemMessage(Component.literal("All contributor features are available.").withStyle(AQUA));
        } else if (props.isPatron()) {
            player.sendSystemMessage(Component.literal("Thank you for supporting Draconic Evolution!").withStyle(AQUA));
        } else {
            player.sendSystemMessage(Component.literal("Draconic Evolution contributor features enabled!").withStyle(AQUA));
        }

        MutableComponent configCommand = Component.literal("/bcore_client contributor").withStyle(BLUE);
        configCommand.setStyle(configCommand.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bcore_client contributor"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to run command"))));
        player.sendSystemMessage(Component.literal("Run ")
                .append(configCommand)
                .append(Component.literal(" to access contributor settings.")));
        player.sendSystemMessage(Component.literal("You will also find an option to disable this message."));
    }

    public static boolean shouldCancelElytra(LivingEntity entity) {
        if (entity instanceof Player player) {
            ContributorProperties props = ContributorHandler.getProps(player);
            if (!props.hasWings()) return false;
            ContributorConfig config = props.getConfig();
            if (config.getWingsTier() == null && props.getAnim().hideDecay() == 1) return false;
            return (props.getConfig().getWingsElytra() == WingElytraCompat.HIDE_ELYTRA || props.getConfig().getWingsElytra() == WingElytraCompat.REPLACE);
        }
        return false;
    }
}
