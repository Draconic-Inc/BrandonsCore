package com.brandon3055.brandonscore;

import codechicken.lib.config.*;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 19/12/19.
 */
//@Mod.EventBusSubscriber(modid = BrandonsCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCConfig {

    private static ConfigCategory config;
    private static ConfigCategory clientTag;
    private static ConfigCategory serverTag;

    public static void load() {
        config = new ConfigFile(BrandonsCore.MODID)
                .path(Paths.get("./config/brandon3055/BrandonsCore.cfg"))
                .load();
        loadServer();
        loadClient();
        config.runSync(ConfigCallback.Reason.MANUAL);
        config.save();
    }

    public static boolean enable_tpx;
    public static boolean clientPermissionVerification;

    private static void loadServer() {
        serverTag = config.getCategory("Server");
        serverTag.getValue("enable_tpx")
                .setComment("Allows you to disable the tpx command.")
                .setDefaultBoolean(!ModList.get().isLoaded("mystcraft"))
                .onSync((tag, type) -> enable_tpx = tag.getBoolean());

        serverTag.getValue("clientPermissionVerification")
                .setComment("Uses the right click block event to verify that players have permission to interact with BC / DE blocks.",
                        "This ensures there is no possible way a player can interact with a BC block if a protection system is blocking the interaction",
                        "In theory not even a modified client sending raw packets will be able to bypass this.",
                        "I have added the ability to disable this feature because it seems in rare cases it blocks players who should have access and i have no idea why.")
                .setDefaultBoolean(true)
                .onSync((tag, type) -> clientPermissionVerification = tag.getBoolean());
    }


    @Deprecated
    public static boolean devLog;
    public static boolean darkMode;
    public static boolean useShaders;

    private static void loadClient() {
        clientTag = config.getCategory("Client");
        clientTag.getValue("darkMode")
                .setComment("Enable / Disable dark mode in my GUI's. (This can also be toggled in game from any gui that supports dark mode)")
                .setDefaultBoolean(true)
                .onSync((tag, type) -> darkMode = tag.getBoolean());
        clientTag.getValue("useShaders")
                .setComment("Set this to false if your system can not handle the awesomeness that is shaders! (Warning: Will make cool things look much less cool!)")
                .setDefaultBoolean(true)
                .onSync((tag, type) -> useShaders = tag.getBoolean());
    }

    private static void modifyProperty(String name, Consumer<ConfigValue> modifyCallback, String... groupPath) {
        ConfigCategory parent = config;
        for (String group : groupPath) {
            parent = parent.getCategory(group);
        }
        ConfigValue tag = parent.getValue(name);
        modifyCallback.accept(tag);
        tag.runSync(ConfigCallback.Reason.MANUAL);
        tag.save();
    }

    public static void modifyClientProperty(String name, Consumer<ConfigValue> modifyCallback, String... groupPath) {
        modifyProperty(name, modifyCallback, ArrayUtils.addAll(new String[]{"Client"}, groupPath));
    }

    public static void modifyServerProperty(String name, Consumer<ConfigValue> modifyCallback, String... groupPath) {
        modifyProperty(name, modifyCallback, ArrayUtils.addAll(new String[]{"Server"}, groupPath));
    }

    public static void modifyCommonProperty(String name, Consumer<ConfigValue> modifyCallback, String... groupPath) {
        modifyProperty(name, modifyCallback, ArrayUtils.addAll(new String[]{"Common"}, groupPath));
    }

    //            use_shaders = builder
//                    .comment("")
//                    .translation(BrandonsCore.MODID + ".config.use_shaders")
//                    .define("use_shaders", true);
//            assignStatic(use_shaders, e -> BCConfig.useShaders = e);
////            useShaders = use_shaders.get();
//            dark_mode = builder
//                    .comment("")
//                    .translation(BrandonsCore.MODID + ".config.dark_mode")
//                    .define("dark_mode", false);
//            assignStatic(dark_mode, e -> BCConfig.darkMode = e);
////            darkMode = dark_mode.get();


//    public static final ForgeConfigSpec CLIENT_SPEC;
//    public static final ForgeConfigSpec SERVER_SPEC;
//    public static final ForgeConfigSpec COMMON_SPEC;
//    public static final Client CLIENT;
//    public static final Server SERVER;
//    public static final Common COMMON;
//
//    static {
//        final Pair<Client, ForgeConfigSpec> cSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
//        CLIENT = cSpecPair.getLeft();
//        CLIENT_SPEC = cSpecPair.getRight();
//
//        final Pair<Server, ForgeConfigSpec> sSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
//        SERVER = sSpecPair.getLeft();
//        SERVER_SPEC = sSpecPair.getRight();
//
//        final Pair<Common, ForgeConfigSpec> comSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
//        COMMON = comSpecPair.getLeft();
//        COMMON_SPEC = comSpecPair.getRight();
//    }
//
//    //Static Access (Fields that need to be accessed frequently are mapped here for improved efficiency)
//
//
//    //TODO figure out how setting/saving works
//    // I may need to hold on to the ModConfig instances from the events.
//
//    /**
//     * These are the underlying config fields that are managed by forge. If you need to modify a config value runtime
//     * these are the values you must modify. If a value needs to be accessed frequently it should be assigned to a
//     * more efficient static field in {@link BCConfig}
//     * <p>
//     * {@link ModConfig.Type#CLIENT}
//     * (Not Synchronized)
//     */
//    public static class Client {
//        private List<Runnable> accessAssigners = new ArrayList<>();
//
//        public final ForgeConfigSpec.BooleanValue use_shaders;
//        public final ForgeConfigSpec.BooleanValue dark_mode;
//
//        public Client(ForgeConfigSpec.Builder builder) {
////            builder.push("General");
//            use_shaders = builder
//                    .comment("Set this to false if your system can not handle the awesomeness that is shaders! (Warning: Will make cool things look much less cool!)")
//                    .translation(BrandonsCore.MODID + ".config.use_shaders")
//                    .define("use_shaders", true);
//            assignStatic(use_shaders, e -> BCConfig.useShaders = e);
////            useShaders = use_shaders.get();
//            dark_mode = builder
//                    .comment("Enable gui dark mode in GUI's. (This can also be toggled in game from any gui that supports dark mode)")
//                    .translation(BrandonsCore.MODID + ".config.dark_mode")
//                    .define("dark_mode", false);
//            assignStatic(dark_mode, e -> BCConfig.darkMode = e);
////            darkMode = dark_mode.get();
//
////            builder.pop();
//        }
//
//        private <T, C extends ForgeConfigSpec.ConfigValue<T>> void assignStatic(C config, Consumer<T> assigner) {
//            accessAssigners.add(() -> assigner.accept(config.get()));
//        }
//    }
//
//    /**
//     * These are the underlying config fields that are managed by forge. If you need to modify a config value runtime
//     * these are the values you must modify. If a value needs to be accessed frequently it should be assigned to a
//     * more efficient static field in {@link BCConfig}
//     * <p>
//     * {@link ModConfig.Type#SERVER}
//     * (Per-Server-Instance Config. Synchronized to players on connections)
//     */
//    public static class Server {
//        private List<Runnable> accessAssigners = new ArrayList<>();
//
//        public final ForgeConfigSpec.BooleanValue enable_tpx;
//        public final ForgeConfigSpec.BooleanValue disable_invasive_gui;
//
//        public Server(ForgeConfigSpec.Builder builder) {
////            builder.push("Misc");
//            enable_tpx = builder
//                    .worldRestart()
//                    .comment("Allows you to disable the tpx command.")
//                    .translation(BrandonsCore.MODID + ".config.enable_tpx")
//                    .define("enable_tpx", !ModList.get().isLoaded("mystcraft"));
//            disable_invasive_gui = builder
//                    .comment("This disables the gui that is shown to clients if a server side config that cant be hot swapped has changed.\nIt is replaced by a chat message that has an option to open the gui")
//                    .translation(BrandonsCore.MODID + ".config.invasive_gui")
//                    .define("disable_invasive_gui", true);
////            builder.pop();
//        }
//
//        private <T, C extends ForgeConfigSpec.ConfigValue<T>> void assignStatic(C config, Consumer<T> assigner) {
//            accessAssigners.add(() -> assigner.accept(config.get()));
//        }
//    }
//
//    /**
//     * These are the underlying config fields that are managed by forge. If you need to modify a config value runtime
//     * these are the values you must modify. If a value needs to be accessed frequently it should be assigned to a
//     * more efficient static field in {@link BCConfig}
//     * <p>
//     * {@link ModConfig.Type#COMMON}
//     * (Not Synchronized)
//     */
//    public static class Common {
//        private List<Runnable> accessAssigners = new ArrayList<>();
//
//        protected final ForgeConfigSpec.BooleanValue dev_log;
//
//
//        public Common(ForgeConfigSpec.Builder builder) {
////            builder.push("Misc");
//            dev_log = builder
//                    .comment("Enable DEV log output.")
//                    .translation(BrandonsCore.MODID + ".config.dev_log")
//                    .define("dev_log", false);
//            assignStatic(dev_log, e -> BCConfig.devLog = e);
//
////            builder.pop();
//        }
//
//        private <T, C extends ForgeConfigSpec.ConfigValue<T>> void assignStatic(C config, Consumer<T> assigner) {
//            accessAssigners.add(() -> assigner.accept(config.get()));
//        }
//    }
//
//
//    /**
//     * This method will be called by Forge when a config changes.
//     */
//    @SubscribeEvent
//    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
////        LogHelperBC.dev("Config");
//        final ModConfig config = event.getConfig();
//        // Reassign the configs when they change
//        if (config.getSpec() == CLIENT_SPEC) {
//            CLIENT.accessAssigners.forEach(Runnable::run);
//            LogHelperBC.debug("Assigned client config");
//        } else if (config.getSpec() == SERVER_SPEC) {
//            SERVER.accessAssigners.forEach(Runnable::run);
//            LogHelperBC.debug("Assigned server config");
//        } else if (config.getSpec() == COMMON_SPEC) {
//            COMMON.accessAssigners.forEach(Runnable::run);
//            LogHelperBC.debug("Assigned common config");
//        }
//    }

}
