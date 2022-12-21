package com.brandon3055.brandonscore.handlers.contributor;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.covers1624.quack.net.HttpResponseException;
import net.covers1624.quack.net.java.JavaDownloadAction;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * The soul responsibility of this class is to access the contributor API and report back if a specified player is a contributor.
 * Because web requests are not instant this uses a callback system.
 * <p>
 * Created by brandon3055 on 21/11/2022
 */
public class ContributorFetcher {
    private static final String CONTRIBUTOR_URL = "http://localhost:8080/api/contributor";
    private static final String HASHES_URL = "http://localhost:8080/api/hashes";
    private static final String LINK_URL = "http://localhost:8080/api/link";
    private static final Map<UUID, String> HASH_CACHE = new HashMap<>();
    private static final HashFunction SHA = Hashing.sha256();
    private static final Gson GSON = new GsonBuilder().create();
    private static final Type FLAGS_TYPE = new TypeToken<HashMap<String, HashMap<String, String>>>() {
    }.getType();
    private static final Type HASHES_TYPE = new TypeToken<HashSet<String>>() {
    }.getType();

    private static final org.slf4j.Logger POOL_LOGGER = LogUtils.getLogger();
    private static final Logger LOGGER = LogManager.getLogger();
    private final CrashLock LOCK = new CrashLock("Already Initialized.");
    private final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder()
            .setNameFormat("DE Contributor Fetcher #%d")
            .setDaemon(true)
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(POOL_LOGGER))
            .build());

    private boolean apiError = false;

    private final List<ThreadedTask> TASK_QUE = new ArrayList<>();
    private final List<Future<ThreadedTask>> FUTURE_TASKS = new ArrayList<>();
    private final Map<UUID, Map<String, Map<String, String>>> CACHED_FLAGS = new HashMap<>();
    private final List<Runnable> waitingForHashes = new ArrayList<>();

    private Set<String> contributorHashes = null;

    public void init() {
        LOCK.lock();                                              //I can do this because ClientTickEvent is server safe.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> onTick(event)));
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> onTick(event)));
        downloadHashes();
    }

    private int tick;

    private void onTick(TickEvent event) {
        if (!TASK_QUE.isEmpty()) {
            TASK_QUE.forEach(task -> FUTURE_TASKS.add(THREAD_POOL.submit(task)));
            TASK_QUE.clear();
        }

        if (tick++ % 20 != 0 || FUTURE_TASKS.isEmpty()) return;

        List<Future<ThreadedTask>> complete = new ArrayList<>();
        for (Future<ThreadedTask> task : FUTURE_TASKS) {
            if (task.isDone()) {
                try {
                    task.get().finish();
                } catch (Throwable e) {
                    LOGGER.warn("An error occurred while finalizing contributor status", e);
                }
                complete.add(task);
            }
        }
        FUTURE_TASKS.removeAll(complete);
    }

    /**
     * If this player's uuid is in the hashes file. This will Query the API to check if this player is a contributor.
     * If the player is a contributor the callback will be fired with the flags returned by the server.
     * Otherwise, the callback will be fired with null.
     * <p>
     *
     * @param playerId      The player's UUID
     * @param userName      Needed to check if the player is in online mode. If null will assume online mode. (Probably get this from player.getGameProfile just in case some mod is overriding player display names)
     * @param projectKey    The project key linked to the mod requesting the flags.
     * @param flagsCallback This callback will always be called. But the supplied map will be empty if the player is not a contributor.
     */
    public void fetchContributorFlags(UUID playerId, @Nullable String userName, String projectKey, Consumer<Map<String, String>> flagsCallback) {
        if (!isOnline(playerId, userName)) {
            flagsCallback.accept(null);
            return; //Player is in offline mode. It is not possible to validate their UUID.
        }

        if (apiError) {
            flagsCallback.accept(null);
            return;
        }

        if (CACHED_FLAGS.containsKey(playerId)) {
            flagsCallback.accept(CACHED_FLAGS.get(playerId).get(projectKey));
        } else {
            //Hashes have not been downloaded yet!
            if (contributorHashes == null) {
                waitingForHashes.add(() -> checkAndFetchUser(playerId, map -> flagsCallback.accept(map == null ? null : map.get(projectKey))));
            } else {
                checkAndFetchUser(playerId, map -> flagsCallback.accept(map == null ? null : map.get(projectKey)));
            }
        }
    }

    private void checkAndFetchUser(UUID uuid, Consumer<Map<String, Map<String, String>>> flagsCallback) {
        if (contributorHashes != null && contributorHashes.contains(userHash(uuid))) {
//            LOGGER.info("checkAndFetchUser: User is in hash file");
            queTask(new FetcherTask(uuid, flagsCallback));
        } else {
            flagsCallback.accept(null);
        }
    }

    public void linkUser(Player player, String linkCode, Consumer<Integer> callback) {
        if (!isOnline(player.getUUID(), player.getGameProfile().getName())) {
            player.sendMessage(new TextComponent("You must be playing in online mode to link your account.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
        } else {
            queTask(new LinkTask(player.getUUID(), linkCode, callback));
        }
    }

    public String userHash(UUID uuid) {
        return HASH_CACHE.computeIfAbsent(uuid, e -> SHA.hashBytes(e.toString().getBytes(StandardCharsets.UTF_8)).toString());
    }

    public boolean hasUser(UUID uuid) {
        return contributorHashes != null && !contributorHashes.isEmpty() && contributorHashes.contains(userHash(uuid));
    }

    private void queTask(ThreadedTask task) {
        TASK_QUE.add(task);
    }

    public void reload() {
        contributorHashes = null;
        apiError = false;
        CACHED_FLAGS.clear();
        downloadHashes();
    }

    private void downloadHashes() {
        File hashFile = new File(FileHandler.brandon3055Folder, "contributors.json");
        DownloadTask downloadTask = new DownloadTask(hashFile, HASHES_URL);
        downloadTask.onFinished(downloader -> {
            try {
                if (downloader.errored) {
                    apiError = true;
                } else {
                    try (FileReader reader = new FileReader(downloader.file)) {
                        contributorHashes = GSON.fromJson(reader, HASHES_TYPE);
//                        LOGGER.info("Hashes Downloaded");
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("An error occurred while processing contributor hashes", e);
            }
            waitingForHashes.forEach(Runnable::run);
            waitingForHashes.clear();
        });
        queTask(downloadTask);
    }

    private boolean isOnline(UUID playerId, @Nullable String username) {
        return username == null || !playerId.equals(Player.createPlayerUUID(username));
    }

    private static class DownloadTask implements ThreadedTask {
        private final File file;
        private final String url;
        private boolean errored = false;
        private Consumer<DownloadTask> finishedCallback;

        public DownloadTask(File file, String url) {
            this.file = file;
            this.url = url;
        }

        public DownloadTask onFinished(Consumer<DownloadTask> finishedCallback) {
            this.finishedCallback = finishedCallback;
            return this;
        }

        @Override
        public DownloadTask call() {
            try {
                new JavaDownloadAction()
                        .setUrl(url)
                        .setDest(file)
                        .setUseETag(true)
                        .execute();
            } catch (Throwable e) {
                LOGGER.warn("Error occurred when attempting to download file: {}", url, e);
                errored = true;
            }
            return this;
        }

        @Override
        public void finish() {
            if (finishedCallback != null) {
                finishedCallback.accept(this);
            }
        }
    }

    private static class FetcherTask implements ThreadedTask {
        private final UUID playerID;
        private final Consumer<Map<String, Map<String, String>>> flagsCallback;
        /** Result will be null if user is not a contributor */
        private Map<String, Map<String, String>> result = null;

        public FetcherTask(UUID playerID, Consumer<Map<String, Map<String, String>>> flagsCallback) {
            this.playerID = playerID;
            this.flagsCallback = flagsCallback;
        }

        @Override
        public FetcherTask call() throws Exception {
            String url = CONTRIBUTOR_URL + "?uuid=" + playerID.toString();
            try (StringWriter writer = new StringWriter()) {
                new JavaDownloadAction()
                        .setUrl(url)
                        .setDest(writer)
                        .execute();
                result = GSON.fromJson(writer.toString(), FLAGS_TYPE);
            } catch (Throwable e) {
                LOGGER.warn("Error occurred when attempting to download contributor flags: {}", url, e);
            }
            if (result != null && result.isEmpty()) {
                result = null;
            }
            return this;
        }

        public void finish() {
            flagsCallback.accept(result);
        }
    }

    private static class LinkTask implements ThreadedTask {
        private final UUID playerId;
        private final String linkCode;
        private Consumer<Integer> onFinished;
        private int errorCode = -1;

        public LinkTask(UUID playerId, String linkCode, Consumer<Integer> onFinished) {
            this.playerId = playerId;
            this.linkCode = linkCode;
            this.onFinished = onFinished;
        }

        @Override
        public LinkTask call() {
            String url = LINK_URL + "?uuid=" + playerId.toString() + "&link_code=" + linkCode;
            try (StringWriter writer = new StringWriter()) {
                JavaDownloadAction action = new JavaDownloadAction()
                        .setUrl(url)
                        .setDest(writer)
                        .setUseETag(true);
                action.execute();
            } catch (HttpResponseException e) {
                errorCode = e.code;
            } catch (IOException e) {
                LOGGER.warn("Error occurred when attempting link account", e);
            }
            return this;
        }

        @Override
        public void finish() {
            onFinished.accept(errorCode);
        }
    }

    private interface ThreadedTask extends Callable<ThreadedTask> {
        void finish();
    }
}
