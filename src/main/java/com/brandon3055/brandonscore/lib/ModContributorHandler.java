//package com.brandon3055.brandonscore.lib;
//
//import com.brandon3055.brandonscore.BrandonsCore;
//import com.brandon3055.brandonscore.handlers.FileHandler;
//
//import com.brandon3055.brandonscore.utils.LogHelperBC;
//import com.google.common.base.Charsets;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.google.gson.JsonPrimitive;
//import com.google.gson.internal.Streams;
//import com.google.gson.stream.JsonReader;
//import com.google.gson.stream.JsonWriter;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.ServerPlayerEntity;
//import net.minecraft.nbt.JsonToNBT;
//
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraftforge.common.MinecraftForge;
//
//import org.apache.commons.io.IOUtils;
//
//import javax.annotation.Nullable;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Created by brandon3055 on 11/06/2017.
// * <p>
// * This is a universal contributor handler that can be implemented by any mod.
// * The mod implementing this need only supply a direct link to their contributor json file.
// * This system allows you to add any valid json data to a contributor in the json and retrieve that data.
// * See the mod methods in this class to get a better idea of how to use this.
// * <p>
// * TODO Add some sort of config save that allows you to save flags for each contribute. e.g. if you want to give the player an option to disable
// * their perk that flag should be saved to disk server side and synced with all other clients.
// */
//public class ModContributorHandler {
//
//    public static final Map<String, ModContributorHandler> MOD_CONTRIBUTOR_HANDLERS = new HashMap<>();
//
//    private final String modid;
//    private final String url;
//    private boolean downloadComplete = false;
//    private boolean downloadFailed = false;
//    private ThreadFileDownloader downloader = null;
//    private File contributorFile = null;
//    private File fileDirectory;
//    /**
//     * Set this to true if you want to support clients running on offline mode.
//     * This means anyone who logs in to an offline server or singleplayer with the name of
//     * one of your contributors will be recognized as that contributor.
//     * This only works if you dont use UUID's as offline UUID's are not the same as online UUID's
//     */
//    public boolean allowOfflineMode = false;
//
//    /**
//     * This map contains the data for all contributors once downloaded.
//     */
//    private Map<String, ContributorData> nameContributorDataMap = new HashMap<>();
//    private Map<UUID, ContributorData> uuidContributorDataMap = new HashMap<>();
//
//    /**
//     * Caches user UUID -> data once the user has been validated.
//     * It will also contain UUID -> null for everyone who isnt a contributor or failed validation.
//     */
//    private Map<UUID, ContributorData> validatedUserCache = new HashMap<>();
//
//    /**
//     * To use this handler simply create an instance of it in a continent location,<br>
//     * call initialize during mod initialization and it will do the rest.
//     *
//     * @param modid Supply your mods mod id.
//     * @param url   The url of your contributors file. See the bottom of this class for an example file format. (https not supported)
//     */
//    public ModContributorHandler(String modid, String url) {
//        this.modid = modid;
//        this.url = url;
//        this.fileDirectory = new File(FileHandler.brandon3055Folder, "ContributorFiles");
//        MOD_CONTRIBUTOR_HANDLERS.put(modid, this);
//    }
//
//
//    //==========================================================================================================//
//    //Handler Methods, These are methods that a mod implementing this may use.
//    //==========================================================================================================//
//    //region Handler Methods
//
//    /**
//     * This can be called during initialization or post initialization.<br><br>
//     * <p>
//     * This method downloads and reads the contributors file. It does the download in a separate thread so it will not have any effect on load time
//     * or performance unless the system is already low on resources.
//     * <br><br>
//     * <p>
//     * If the world loads before the download finishes nothing bad will happen isPlayerContributor will just
//     * return false for all players until the contributor file is downloaded and read.
//     *
//     * @param fileDirectory        Allows you to specify a directory where you want to save the contributor and settings files. If null will default to config/brandon3055/ContributorFiles
//     * @param registerEventHandler This handler will be registered to the forge event bus. If you would like to use your own handler you can return false and manually call onPlayerLoggedIn and onPlayerLoggedOut from your event handler.
//     */
//    public void initialize(File fileDirectory, boolean registerEventHandler) {
//        if (fileDirectory != null) {
//            this.fileDirectory = fileDirectory;
//        }
//        if (registerEventHandler) {
//            MinecraftForge.EVENT_BUS.register(this);
//        }
//        contributorFile = new File(this.fileDirectory, modid + "-Contributors.json");
//        startContributorDownload();
//    }
//
//    /**
//     * @return true if the player is a contributor.
//     */
//    public boolean isPlayerContributor(PlayerEntity player) {
//        return getContributorData(player) != null;
//    }
//
//    /**
//     * @return the players contributor data if they are a contributor or null if they are not
//     */
//    @Nullable
//    public ContributorData getContributorData(PlayerEntity player) {
//        if (player == null || player.getGameProfile().getId() == null) {
//            return null;
//        }
//
//        UUID id = player.getGameProfile().getId();
//
//        return validatedUserCache.computeIfAbsent(id, uuid -> {
//            String username = player.getName();
//
//            //If we are allowing offline users for whatever reason the first just check for their name in the map.
//            if (allowOfflineMode && nameContributorDataMap.containsKey(username)) {
//                return nameContributorDataMap.get(username);
//            }
//            else {
//                if (uuidContributorDataMap.containsKey(id)) {
//                    return uuidContributorDataMap.get(id);
//                }
//                else if (nameContributorDataMap.containsKey(username)) {
//                    ContributorData data = nameContributorDataMap.get(username);
//                    //Figure out what the players uuid would be if running in offline mode.
//                    UUID offlineID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
//
//                    //If there uuid is not the same as their offline uuid then they are probably running in online mode.
//                    //This validation is not idea because if another mod tampers with uuids... Someone needs to kill it with fire!
//                    //But also it would break this validation.
//                    if (data.uuid == null && !offlineID.equals(uuid)) {
//                        return nameContributorDataMap.get(username);
//                    }
//                    else {
//                        return null;
//                    }
//                }
//                return null;
//            }
//        });
//    }
//
//    /**
//     * @return true if the contributor file download failed.
//     */
//    public boolean didDownloadFail() {
//        return downloadFailed;
//    }
//
//    /**
//     * @return true if the contributor file has been downloaded and read successfully.
//     */
//    public boolean isDownloadComplete() {
//        return downloadComplete;
//    }
//
//    //endregion
//    //==========================================================================================================//
//    //Non-Handler Methods, These are internal methods used by the handler. A mod should not need to access these
//    //==========================================================================================================//
//    //region Non handler methods
//
//    /**
//     * If the download failed or you need to redownload the file it is ok to run this again. But only if you have good reason
//     * if it failed the first time it will probably fail the second time aswell.
//     * This may be useful if you want to implement some sort of reload/retry button or something that can be used in the event the download fails.
//     * <p>
//     * Do not call this before initialization!
//     */
//    public void startContributorDownload() {
//        if (downloader != null && (!downloader.isFinished() && !downloader.downloadFailed())) {
//            LogHelperBC.warn("The mod " + modid + " attempted to re-download their contributors file while the previous download was still in progress!");
//            return;
//        }
//
//        downloadFailed = false;
//        downloadComplete = false;
//
//        downloader = new ThreadFileDownloader(modid + " Contributor Download Thread", url, contributorFile, (dlThread, file) -> {
//            if (file == null) {
//                downloadFailed = true;
//                LogHelperBC.warn("Contributor file download for " + modid + " failed! The reason for the failure should be contained in the stacktrace bellow.");
//                if (dlThread.getException() != null) {
//                    dlThread.getException().printStackTrace();
//                }
//                else {
//                    LogHelperBC.error("No error information could be found!");
//                }
//            }
//            else {
//                try {
//                    readContributorFile(file);
//                }
//                catch (FileNotFoundException e) {
//                    downloadFailed = true;
//                    LogHelperBC.error("Something went wrong while attempting to read the contributor file for " + modid);
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        downloader.start();
//    }
//
//    private void readContributorFile(File file) throws FileNotFoundException {
//        JsonParser parser = new JsonParser();
//        JsonReader reader = new JsonReader(new FileReader(file));
//        reader.setLenient(true);
//        JsonObject objectList = parser.parse(reader).getAsJsonObject();
//        nameContributorDataMap.clear();
//
//        for (Map.Entry<String, JsonElement> entry : objectList.entrySet()) {
//            if (entry.getValue().isJsonObject()) {
//                JsonObject object = entry.getValue().getAsJsonObject();
//                String key = entry.getKey();
//                UUID uuid = null;
//
//                if (object.has("uuid")) {
//                    uuid = UUID.fromString(object.get("uuid").getAsString());
//                }
//
//                ContributorData data = new ContributorData(object, entry.getKey(), uuid);
//                nameContributorDataMap.put(key, data);
//                if (uuid != null) {
//                    uuidContributorDataMap.put(uuid, data);
//                }
//            }
//        }
//
//        IOUtils.closeQuietly(reader);
//        downloadComplete = true;
//        loadConfig();
//    }
//
//    @SubscribeEvent
//    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
//        if (!event.player.world.isRemote && event.player instanceof ServerPlayerEntity) {
//            for (ContributorData data : nameContributorDataMap.values()) {
//                BrandonsCore.network.sendTo(new PacketContributor(modid, data.name, data.config), (ServerPlayerEntity) event.player);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
//        UUID id = event.player.getGameProfile().getId();
//        if (id != null) {
//            validatedUserCache.remove(id);
//        }
//    }
//
//    //region Config
//
//    public void handleConfigChange(ContributorData contributor, CompoundNBT config) {
//        contributor.config = config;
//        BrandonsCore.network.sendToAll(new PacketContributor(modid, contributor.name, config));
//        saveConfig();
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public void configReceivedClient(CompoundNBT config, String contributor) {
//        if (nameContributorDataMap.containsKey(contributor)) {
//            nameContributorDataMap.get(contributor).config = config;
//        }
//    }
//
//    public void configReceivedServer(CompoundNBT config, String contributor, ServerPlayerEntity sender) {
//        ContributorData data = nameContributorDataMap.get(contributor);
//        ContributorData seder = getContributorData(sender);
//        if (data != null && data == seder) {
//            handleConfigChange(data, config);
//        }
//    }
//
//    private void saveConfig() {
//        try {
//            File configFile = new File(this.fileDirectory, modid + "-ContributorConfig.json");
//            JsonWriter writer = new JsonWriter(new FileWriter(configFile));
//            writer.setLenient(true);
//            JsonObject jsonObject = new JsonObject();
//
//            for (ContributorData data : nameContributorDataMap.values()) {
//                jsonObject.addProperty(data.name, data.config.toString());
//            }
//
//            Streams.write(jsonObject, writer);
//            IOUtils.closeQuietly(writer);
//        }
//        catch (Exception e) {
//            LogHelperBC.error("Something went wrong while saving the contributor config file!");
//            e.printStackTrace();
//        }
//    }
//
//    private void loadConfig() {
//        try {
//            File configFile = new File(this.fileDirectory, modid + "-ContributorConfig.json");
//            JsonParser parser = new JsonParser();
//            JsonReader reader = new JsonReader(new FileReader(configFile));
//            reader.setLenient(true);
//            JsonObject objectList = parser.parse(reader).getAsJsonObject();
//
//            for (Map.Entry<String, JsonElement> entry : objectList.entrySet()) {
//                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
//                    String data = entry.getValue().getAsJsonPrimitive().getAsString();
//                    if (data == null) {
//                        continue;
//                    }
//
//                    try {
//                        if (nameContributorDataMap.containsKey(entry.getKey())) {
//                            nameContributorDataMap.get(entry.getKey()).config = JsonToNBT.getTagFromJson(data);;
//                        }
//                    }
//                    catch (NBTException nbte) {
//                        LogHelperBC.error("Something went wrong while reading the config for a contributor - " + entry.getKey());
//                        nbte.printStackTrace();
//                    }
//                }
//            }
//            IOUtils.closeQuietly(reader);
//        }
//        catch (Exception e) {
//            LogHelperBC.error("Something went wrong while loading the contributor config file!");
//            e.printStackTrace();
//        }
//    }
//
//    //endregion
//
//    public class ContributorData {
//        private JsonObject jsonObject;
//        private CompoundNBT config = new CompoundNBT();
//        public final String name;
//        public final UUID uuid;
//
//        public ContributorData(JsonObject jsonObject, String name, UUID uuid) {
//            this.jsonObject = jsonObject;
//            this.name = name;
//            this.uuid = uuid;
//        }
//
//        //region Data Getters
//
//        public JsonObject getRawData() {
//            return jsonObject;
//        }
//
//        public boolean hasPrimitive(String key) {
//            return jsonObject.has(key) && jsonObject.get(key).isJsonPrimitive();
//        }
//
//        public JsonPrimitive getPrimitive(String key) {
//            return jsonObject.get(key).getAsJsonPrimitive();
//        }
//
//        /**
//         * @param key the json field name to search for.
//         * @return true if there is a boolean with this name in the contributor data.
//         */
//        public boolean hasBoolean(String key) {
//            return hasPrimitive(key) && getPrimitive(key).isBoolean();
//        }
//
//        /**
//         * Checks for the existence of a Number with this key in the contributor data.
//         * If the number exists you can retrieve it as whatever primitive data type you like.
//         *
//         * @param key the json field name to search for.
//         * @return true if there is a Number with this name in the contributor data.
//         */
//        public boolean hasNumber(String key) {
//            return hasPrimitive(key) && getPrimitive(key).isBoolean();
//        }
//
//        /**
//         * @param key the json field name to search for.
//         * @return true if there is a String with this name in the contributor data.
//         */
//        public boolean hasString(String key) {
//            return hasPrimitive(key) && getPrimitive(key).isBoolean();
//        }
//
//        /**
//         * Use hasBoolean to confirm this field exists before calling this to avoid exceptions.
//         *
//         * @param key the json field name to retrieve.
//         * @return the boolean value of the specified field.
//         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
//         */
//        public boolean getBoolean(String key) {
//            return getPrimitive(key).getAsBoolean();
//        }
//
//        /**
//         * Use hasNumber to confirm this field exists before calling this to avoid exceptions.
//         *
//         * @param key the json field name to retrieve.
//         * @return the int value of the specified field.
//         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
//         */
//        public int getInt(String key) {
//            return getPrimitive(key).getAsInt();
//        }
//
//        /**
//         * Use hasNumber to confirm this field exists before calling this to avoid exceptions.
//         *
//         * @param key the json field name to retrieve.
//         * @return the double value of the specified field.
//         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
//         */
//        public double getDouble(String key) {
//            return getPrimitive(key).getAsDouble();
//        }
//
//        /**
//         * Use hasString to confirm this field exists before calling this to avoid exceptions.
//         *
//         * @param key the json field name to retrieve.
//         * @return the String value of the specified field.
//         * @throws IllegalStateException or ClassCastException if this field does not exist or is of an inconvertible type.
//         */
//        public String getString(String key) {
//            return getPrimitive(key).getAsString();
//        }
//
//        //endregion
//
//        //region Config
//
//        /**
//         * This config compound allow you to save local data for each contributor.
//         * This can be any data you want. e.g. say for example you put a chest on
//         * contributors heads and you want to give them the option to turn it off,
//         * You could save the on off state to this tag and the data will be saved
//         * to disk and loaded next startup.<br><br>
//         * <p>
//         * This data can be set from the server but more importantly it can be set
//         * from the client (but only by the contributor this data belongs to) and
//         * will be synchronized across all other clients on the server.<br><br>
//         * <p>
//         * Make sure you call saveAndSyncConfig() after modifying this tag to save it
//         * and sync it to all other clients.
//         *
//         * @return the config compound.
//         */
//        public CompoundNBT getConfigNBT() {
//            return config;
//        }
//
//        public void saveAndSyncConfig() {
//            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
//                BrandonsCore.network.sendToServer(new PacketContributor(modid, name, config));
//            }
//            else {
//                handleConfigChange(this, config);
//            }
//        }
//
//        //endregion
//    }
//
//    //endregion
//
//
//    /*
//    *
//    * {
//    *   "username": {
//    *       "uuid":"uuid", //Optional
//    *       //Whatever
//    *       //Data
//    *       //you
//    *       //want
//    *       //goes
//    *       //here
//    *   },
//    *   "username": {
//    *       "uuid":"uuid", //Optional
//    *       //Whatever
//    *       //Data
//    *       //you
//    *       //want
//    *       //goes
//    *       //here
//    *   }
//    * }
//    *
//    * */
//}
