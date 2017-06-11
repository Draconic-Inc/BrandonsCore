package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by brandon3055 on 11/06/2017.
 *
 * This is a universal contributor handler that can be implemented by any mod.
 * The mod implementing this need only supply a direct link to their contributor json file.
 * This system allows you to add any valid json data to a contributor in the json and retrieve that data.
 * See the mod methods in this class to get a better idea of how to use this.
 *
 * TODO Add some sort of config save that allows you to save flags for each contribute. e.g. if you want to give the player an option to disable
 * their perk that flag should be saved to disc server side and synced with all other clients.
 */
public class ModContributorHandler {

    private final String modid;
    private final String url;
    private boolean downloadComplete = false;
    private boolean downloadFailed = false;
    private ThreadFileDownloader downloader = null;
    private File contributorFile = null;
    private File fileDirectory;
    /**
     * Set this to true if you want to support clients running on offline mode.
     * This means anyone who logs in to an offline server or singleplayer with the name of
     * one of your contributors will be recognized as that contributor.
     * This only works if you dont use UUID's as offline UUID's are not the same as online UUID's
     */
    public boolean allowOfflineMode = false;

    /**
     * This map contains the data for all contributors once downloaded.
     * Object can be ether username or UUID depending on whether or not a uuid is specified in the contributors file.
     */
    private Map<Object, ContributorData> contributorData = new HashMap<>();

    /**
     * Caches user UUID -> data once the user has been validated.
     * It will also contain UUID -> null for everyone who isnt a contributor or failed validation.
     */
    private Map<UUID, ContributorData> validatedUserCache = new HashMap<>();

    /**
     * To use this handler simply create an instance of it in a continent location,<br>
     * call initialize during mod initialization and it will do the rest.
     *
     * @param modid This is used in the name of the contributor settings file and to correctly tag log errors in the event something breaks.
     * @param url The url of your contributors file. See the bottom of this class for an example file format. (https not supported)
     */
    public ModContributorHandler(String modid, String url) {
        this.modid = modid;
        this.url = url;
        this.fileDirectory = FileHandler.brandon3055Folder;
    }


    //==========================================================================================================//
    //Handler Methods, These are methods that a mod implementing this may use.
    //==========================================================================================================//

    /**
     * This can be called during initialization or post initialization.<br><br>
     *
     * This method downloads and reads the contributors file. It does the download in a separate thread so it will not have any effect on load time
     * or performance unless the system is already low on resources.
     * <br><br>
     *
     * If the world loads before the download finishes nothing bad will happen isPlayerContributor will just
     * return false for all players until the contributor file is downloaded and read.
     *
     * @param fileDirectory Allows you to specify a directory where you want to save the contributor and settings files. If null will default to config/brandon3055/ContributorFiles
     * @param registerEventHandler This handler will be registered to the forge event bus. If you would like to use your own handler you can return false and manually call onPlayerLoggedIn and onPlayerLoggedOut from your event handler.
     */
    public void initialize(File fileDirectory, boolean registerEventHandler) {
        if (fileDirectory != null) {
            this.fileDirectory = fileDirectory;
        }
        if (registerEventHandler) {
            MinecraftForge.EVENT_BUS.register(this);
        }
        contributorFile = new File(this.fileDirectory, "ContributorFiles/" + modid + "-Contributors.json");
        startContributorDownload();
    }

    /**
     * @return true if the player is a contributor.
     */
    public boolean isPlayerContributor(EntityPlayer player) {
        return getContributorData(player) != null;
    }

    /**
     * @return the players contributor data if they are a contributor or null if they are not
     */
    @Nullable
    public ContributorData getContributorData(EntityPlayer player) {
        if (player == null || player.getGameProfile().getId() == null) {
            return null;
        }

        UUID id = player.getGameProfile().getId();

        return validatedUserCache.computeIfAbsent(id, uuid -> {
            String username = player.getName();

            //If we are allowing offline users for whatever reason the first just check for their name in the map.
            if (allowOfflineMode && contributorData.containsKey(username)) {
                return contributorData.get(username);
            }
            else {
                //Figure out what the players uuid would be if running in offline mode.
                UUID offlineID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));

                //If this player has been specified by UUID directly there is no need for validation.
                if (contributorData.containsKey(uuid)) {
                    return contributorData.get(uuid);
                }
                else {
                    //If there uuid is not the same as their offline uuid then they are probably running in online mode.
                    //This validation is not idea because if another mod tampers with uuids... Someone needs to kill it with fire!
                    //But also it would break this validation.
                    if (!offlineID.equals(uuid) && contributorData.containsKey(username)) {
                        return contributorData.get(uuid);
                    }
                    else {
                        return null;
                    }
                }
            }
        });
    }

    /**
     * @return true if the contributor file download failed.
     */
    public boolean didDownloadFail() {
        return downloadFailed;
    }

    /**
     * @return true if the contributor file has been downloaded and read successfully.
     */
    public boolean isDownloadComplete() {
        return downloadComplete;
    }

    //==========================================================================================================//
    //Non-Handler Methods, These are internal methods used by the handler. A mod should not need to access these
    //==========================================================================================================//
    //region Non handler methods

    /**
     * If the download failed or you need to redownload the file it is ok to run this again. But only if you have good reason
     * if it failed the first time it will probably fail the second time aswell.
     * This may be useful if you want to implement some sort of reload/retry button or something that can be used in the event the download fails.
     *
     * Do not call this before initialization!
     */
    public void startContributorDownload() {
        if (downloader != null && (!downloader.isFinished() && !downloader.downloadFailed())) {
            LogHelperBC.warn("The mod "+modid+" attempted to re-download their contributors file while the previous download was still in progress!");
            return;
        }

        downloadFailed = false;
        downloadComplete = false;

        downloader = new ThreadFileDownloader(modid + " Contributor Download Thread", url, contributorFile, (dlThread, file) -> {
            if (file == null) {
                downloadFailed = true;
                LogHelperBC.warn("Contributor file download for " + modid + " failed! The reason for the failure should be contained in the stacktrace bellow.");
                if (dlThread.getException() != null) {
                    dlThread.getException().printStackTrace();
                }
                else {
                    LogHelperBC.error("No error information could be found!");
                }
            }
            else {
                try {
                    readContributorFile(file);
                }
                catch (FileNotFoundException e) {
                    downloadFailed = true;
                    LogHelperBC.error("Something went wrong while attempting to read the contributor file for " + modid);
                    e.printStackTrace();
                }
            }
        });

        downloader.start();
    }

    private void readContributorFile(File file) throws FileNotFoundException {
        JsonParser parser = new JsonParser();
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.setLenient(true);
        JsonArray array = parser.parse(reader).getAsJsonArray();
        contributorData.clear();

        array.forEach(jsonElement -> {
            if (jsonElement.isJsonObject()) {
                JsonObject object = jsonElement.getAsJsonObject();
                Object key = null;
                if (object.has("uuid")) {
                    key = UUID.fromString(object.get("uuid").getAsString());
                }
                else if (object.has("username")) {
                    key = UUID.fromString(object.get("username").getAsString());
                }

                if (key != null) {
                    contributorData.put(key, new ContributorData(object));
                }
                else {
                    LogHelperBC.warn("Error loading a contributor for " + modid + " No player username or uuid found");
                }
            }
        });

        downloadComplete = true;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.player.getGameProfile().getId();
        if (id != null) {
            validatedUserCache.remove(id);
        }
    }

    public class ContributorData {
        private JsonObject jsonObject;

        public ContributorData(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JsonObject getRawData() {
            return jsonObject;
        }

        public boolean hasPrimitive(String key) {
            return jsonObject.has(key) && jsonObject.get(key).isJsonPrimitive();
        }

        public JsonPrimitive getPrimitive(String key) {
            return jsonObject.get(key).getAsJsonPrimitive();
        }

        /**
         * @param key the json field name to search for.
         * @return true if there is a boolean with this name in the contributor data.
         */
        public boolean hasBoolean(String key) {
            return hasPrimitive(key) && getPrimitive(key).isBoolean();
        }

        /**
         * Checks for the existence of a Number with this key in the contributor data.
         * If the number exists you can retrieve it as whatever primitive data type you like.
         *
         * @param key the json field name to search for.
         * @return true if there is a Number with this name in the contributor data.
         */
        public boolean hasNumber(String key) {
            return hasPrimitive(key) && getPrimitive(key).isBoolean();
        }

        /**
         * @param key the json field name to search for.
         * @return true if there is a String with this name in the contributor data.
         */
        public boolean hasString(String key) {
            return hasPrimitive(key) && getPrimitive(key).isBoolean();
        }

        /**
         * Use hasBoolean to confirm this field exists before calling this to avoid exceptions.
         *
         * @param key the json field name to retrieve.
         * @return the boolean value of the specified field.
         *
         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
         */
        public boolean getBoolean(String key) {
            return getPrimitive(key).getAsBoolean();
        }

        /**
         * Use hasNumber to confirm this field exists before calling this to avoid exceptions.
         *
         * @param key the json field name to retrieve.
         * @return the int value of the specified field.
         *
         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
         */
        public int getInt(String key) {
            return getPrimitive(key).getAsInt();
        }

        /**
         * Use hasNumber to confirm this field exists before calling this to avoid exceptions.
         *
         * @param key the json field name to retrieve.
         * @return the double value of the specified field.
         *
         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
         */
        public double getDouble(String key) {
            return getPrimitive(key).getAsDouble();
        }

        /**
         * Use hasString to confirm this field exists before calling this to avoid exceptions.
         *
         * @param key the json field name to retrieve.
         * @return the String value of the specified field.
         *
         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
         */
        public String getString(String key) {
            return getPrimitive(key).getAsString();
        }
    }

    //endregion
}
