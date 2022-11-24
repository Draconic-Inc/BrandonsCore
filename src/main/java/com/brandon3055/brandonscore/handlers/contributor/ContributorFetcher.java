package com.brandon3055.brandonscore.handlers.contributor;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * The soul responsibility of this class is to access the contributor API and report back if a specified player is a contributor.
 * Because web requests are not instant this uses a callback system.
 * <p>
 * Created by brandon3055 on 21/11/2022
 */
public class ContributorFetcher {
    static final org.slf4j.Logger POOL_LOGGER = LogUtils.getLogger();
    public static final Logger LOGGER = LogManager.getLogger(ContributorFetcher.class);
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");
    private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder()
            .setNameFormat("DE Contributor Fetcher #%d")
            .setDaemon(true)
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(POOL_LOGGER))
            .build());

    private static final List<Future<APIV2Fetcher>> APIv2_FETCHERS = new ArrayList<>();
    private static final Map<String, Consumer<Map<String, String>>> APIv1_QUE = new HashMap<>();
    private static Future<APIV1Fetcher> apiV1Fetcher = null;
    private static Map<String, Map<String, String>> apiV1Flags = null;
    private static int tick;

    public static void init() {
        LOCK.lock();                                              //I can do this because ClientTickEvent is server safe.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> onTick(event)));
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> onTick(event)));
    }

    private static void onTick(TickEvent event) {
        if (tick++ % 20 != 0) return; //You can just wait a second ok!

        APIv2_FETCHERS.removeIf(future -> {
            if (future.isDone()) {
                try {
                    future.get().finish();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });

        if (apiV1Fetcher != null && apiV1Fetcher.isDone()) {
            try {
                apiV1Fetcher.get().finish();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                apiV1Flags = new HashMap<>();
            }
            apiV1Fetcher = null;
        }

        if (apiV1Flags != null && !APIv1_QUE.isEmpty()) {
            APIv1_QUE.forEach((name, callback) -> callback.accept(apiV1Flags.getOrDefault(name, null)));
            APIv1_QUE.clear();
        }
    }

    /**
     * Queries the API to check if this player is a contributor.
     * If the player is a contributor the callback will be fired with the flags returned by the server.
     *
     * @param player        The player.
     * @param flagsCallback This callback will always be called. But the supplied map will be empty if the player is not a contributor.
     */
    public static void getContributorStatus(Player player, Consumer<Map<String, String>> flagsCallback) {
        GameProfile profile = player.getGameProfile();
        if (profile.getId() == null || profile.getId().equals(Player.createPlayerUUID(profile.getName()))) {
            flagsCallback.accept(null);
            return; //Player is in offline mode. It is not possible to validate their UUID.
        }

        APIv2_FETCHERS.add(THREAD_POOL.submit(new APIV2Fetcher(profile.getId(), profile.getName(), flagsCallback)));
    }

    public static void reset() {
        apiV1Flags = null;
    }

    private static class APIV2Fetcher implements Callable<APIV2Fetcher> {
        private final UUID playerID;
        private final String username;
        private final Consumer<Map<String, String>> flagsCallback;
        /** Result will be null if user is not a contributor */
        private Map<String, String> result = null;
        /**
         * Success here means the API call was successful
         * It does not mean the player is a contributor.
         */
        public boolean success;

        public APIV2Fetcher(UUID playerID, String username, Consumer<Map<String, String>> flagsCallback) {
            this.playerID = playerID;
            this.username = username;
            this.flagsCallback = flagsCallback;
        }

        @Override
        public APIV2Fetcher call() throws Exception {
            //TODO implement API v2
            // Also todo... build APIv2...
            success = false;
            return this;
        }

        public void finish() {
            if (success) {
                flagsCallback.accept(result);
            } else {
                APIv1_QUE.put(username, flagsCallback);
                if (apiV1Fetcher == null && apiV1Flags == null) {
                    LOGGER.info("DE Contributor APIv2 unavailable. Attempting to fall back to APIv1");
                    apiV1Fetcher = THREAD_POOL.submit(new APIV1Fetcher());
                }
            }
        }
    }

    /**
     * Note I can remap contribution level to tier in the old api.
     * It just means wyvern tier will be treated as draconic in older mod versions which is fine.
     *
     * TODO: Maybe include the UUID in APIv1 so i can stop requiring usernames for APIv1
     */
    private static class APIV1Fetcher implements Callable<APIV1Fetcher> {
        private Map<String, Map<String, String>> result = new HashMap<>();

        @Override
        public APIV1Fetcher call() throws Exception {
            try {
                File cFile = new File(FileHandler.brandon3055Folder, "contributors.json");
                FileHandler.downloadFile("http://www.brandon3055.com/json/DEContributors.json", cFile);
                JsonArray array = FileHandler.readArray(cFile);
                for (JsonElement element : array) {
                    JsonObject entry = element.getAsJsonObject();
                    Map<String, String> flags = new HashMap<>();
                    String name = entry.get("ign").getAsString();
                    int level = entry.get("contributionLevel").getAsInt();
                    switch (level) {
                        case 3 -> flags.put("wings", "chaotic");
                        case 2 -> flags.put("wings", "draconic");
                        case 1 -> flags.put("wings", "wyvern");
                        case 0 -> flags.put("wings", "draconium");
                    }
                    String contrib = entry.get("contribution").getAsString();
                    String details = entry.get("details").getAsString();
                    if (contrib.toLowerCase(Locale.ENGLISH).contains("lolnet") || details.toLowerCase(Locale.ENGLISH).contains("lolnet")) {
                        flags.put("lolnet", "");
                    }
                    if (contrib.toLowerCase(Locale.ENGLISH).contains("patreon")) {
                        switch (level) {
                            case 3 -> flags.put("patreon", "chaotic");
                            case 2 -> flags.put("patreon", "draconic");
                            case 1 -> flags.put("patreon", "wyvern");
                            case 0 -> flags.put("patreon", "draconium");
                        }
                    }
                    if (name.equals("brandon3055")) {
                        flags.put("dev", "");
                    }
                    result.put(name, flags);
//                    LOGGER.info("Contributor: " + name + ", Flags: " + flags);
                }
            } catch (Throwable e) {
                LOGGER.info("APIv1 is also unavailable.");
                e.printStackTrace();
            }
            return this;
        }

        public void finish() {
            apiV1Flags = result;
            if (!result.isEmpty()) {
                LOGGER.info("Successfully loaded contributors from APIv1.");
            }
        }
    }
}
