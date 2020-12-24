package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.handlers.HandHelper;
import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.Pair;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.InventoryUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.io.IOUtils;


import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

/**
 * Created by brandon3055 on 23/06/2017.
 */
//TODO Test all these commands!
public class BCUtilCommands {

    private static Random rand = new Random();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("bcore_util")
                        .then(registerNBT())
//                        .then(registerRegenChunk())
                        .then(registerNoClip())
                        .then(registerUUID())
//                        .then(registerPlayerAccess())
                        .then(registerDumpEvents())
                        .then(registerEggify())
        );
    }


    private static ArgumentBuilder<CommandSource, ?> registerNBT() {
        return Commands.literal("nbt")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(context -> functionNBT(context.getSource()));
    }

    private static ArgumentBuilder<CommandSource, ?> registerRegenChunk() {
        return Commands.literal("regenchunk")
                .requires(cs -> cs.hasPermissionLevel(3))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 32))
                        .executes(ctx -> regenChunk(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))
                );
    }

    private static ArgumentBuilder<CommandSource, ?> registerNoClip() {
        return Commands.literal("noclip")
                .requires(cs -> cs.hasPermissionLevel(3))
                .executes(context -> toggleNoClip(context.getSource()));
    }

    private static ArgumentBuilder<CommandSource, ?> registerUUID() {
        return Commands.literal("uuid")
                .requires(cs -> cs.hasPermissionLevel(0))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> getUUID(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                )
                .executes(ctx -> getUUID(ctx.getSource(), ctx.getSource().asPlayer()));
    }

    private static ArgumentBuilder<CommandSource, ?> registerPlayerAccess() {
        return Commands.literal("player_access")
                .requires(cs -> cs.hasPermissionLevel(3))
                .then(Commands.argument("target", reader -> StringArgumentType.string())
                        .suggests((context, builder) -> ISuggestionProvider.suggest(accessiblePlayers(context.getSource()).values().stream().map(GameProfile::getName), builder))
                        .executes(context -> playerAccess(context.getSource(), context.getArgument("target", String.class)))
                )
                .then(Commands.literal("list")
                        .executes(context -> playerAccess(context.getSource(), null))
                );
    }

    private static ArgumentBuilder<CommandSource, ?> registerDumpEvents() {
        return Commands.literal("dump_event_listeners")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(ctx -> dumpEventListeners(ctx.getSource()));
    }

    private static ArgumentBuilder<CommandSource, ?> registerEggify() {
        return Commands.literal("eggify")
                .requires(cs -> cs.hasPermissionLevel(3))
                .then(Commands.argument("target", EntityArgument.entities())
                        .executes(ctx -> eggify(ctx, EntityArgument.getEntity(ctx, "target"))));
    }

//    private void help(ICommandSource sender) {
//        ChatHelper.message(sender, "The following are a list of Brandon's Core Utility Commands", new Style().setColor(TextFormatting.AQUA).setUnderlined(true));
//        ChatHelper.message(sender, "/bcore_util nbt", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-Prints the NBT tag of the stack you are holding to chat and to the console.", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_util regenchunk [radius]", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-Regenerates the chunk(s) at your position.", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_util noclip", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-Toggles noclip allowing you to fly through blocks as if in spectator mode... Or fall into the void if you dont have flight", TextFormatting.GRAY);
////        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
////        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//    }

    private static int functionNBT(CommandSource source) throws CommandException, CommandSyntaxException {
        PlayerEntity player = source.asPlayer();
        ItemStack stack = HandHelper.getMainFirst(player);
        if (stack.isEmpty()) {
            throw new CommandException(new StringTextComponent("You are not holding an item!"));
        } else if (!stack.hasTag()) {
            throw new CommandException(new StringTextComponent("That stack has no NBT tag!"));
        }

        CompoundNBT compound = stack.getTag();
        LogHelperBC.logNBT(compound);
        LogHelperBC.info(compound);
        StringBuilder builder = new StringBuilder();
        LogHelperBC.buildNBT(builder, compound, "", "Tag", false);
        String[] lines = builder.toString().split("\n");
        DataUtils.forEach(lines, s -> ChatHelper.sendMessage(player, new StringTextComponent(s).mergeStyle(TextFormatting.GOLD)));
        return 0;
    }

    private static int regenChunk(CommandSource source, int rad) throws CommandException, CommandSyntaxException {
//        LogHelperBC.dev(rad);

        for (int xOffset = -rad; xOffset <= rad; xOffset++) {
            for (int yOffset = -rad; yOffset <= rad; yOffset++) {
                ServerWorld world = (ServerWorld) source.getWorld();
                PlayerEntity player = source.asPlayer();
                int chunkX = (int) player.chunkCoordX + xOffset;
                int chunkZ = (int) player.chunkCoordZ + yOffset;

                Chunk oldChunk = world.getChunk(chunkX, chunkZ);
                ServerChunkProvider chunkProviderServer = world.getChunkProvider();
                ChunkGenerator chunkGenerate = chunkProviderServer.getChunkGenerator();

//                chunkGenerate.generateSurface(oldChunk);
//                chunkGenerate.generateBiomes(oldChunk);
//                chunkGenerate.generateStructureStarts(world, oldChunk);
//                Chunk newChunk = chunkGenerate.generateChunk(chunkX, chunkZ);
//
//                for (int x = 0; x < 16; x++) {
//                    for (int z = 0; z < 16; z++) {
//                        for (int y = 0; y < world.getHeight(); y++) {
//                            BlockPos chunkPos = new BlockPos(x, y, z);
//                            BlockPos absPos = new BlockPos(x + (chunkX * 16), y, z + (chunkZ * 16));
//                            BlockState newState = newChunk.getBlockState(chunkPos);
//                            world.setBlockState(absPos, newState);
//
//                            TileEntity tileEntity = newChunk.getTileEntity(chunkPos, Chunk.CreateEntityType.IMMEDIATE);
//                            if (tileEntity != null) {
//                                world.setTileEntity(absPos, tileEntity);
//                            }
//                        }
//                    }
//                }
//
//                oldChunk.setTerrainPopulated(false);
//                oldChunk.populate(chunkProviderServer, chunkGenerate);
//
//                PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
//                if (playerChunkMap == null) {
//                    return;
//                }
//
//                oldChunk.setModified(true);
//                oldChunk.generateSkylightMap();
//
//                PlayerChunkMapEntry watcher = playerChunkMap.getEntry(oldChunk.x, oldChunk.z);
//                if (watcher != null) {
//                    watcher.sendPacket(new SPacketChunkData(oldChunk, 65535));
//                }
            }
        }
        return 0;
    }

    private static int toggleNoClip(CommandSource source) throws CommandException, CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        boolean enabled = BCEventHandler.noClipPlayers.contains(player.getUniqueID());

        if (enabled) {
            BCEventHandler.noClipPlayers.remove(player.getUniqueID());
            BCoreNetwork.sendNoClip(player, false);
            source.sendFeedback(new StringTextComponent("NoClip Disabled!"), true);
        } else {
            BCEventHandler.noClipPlayers.add(player.getUniqueID());
            BCoreNetwork.sendNoClip(player, true);
            source.sendFeedback(new StringTextComponent("NoClip Enabled!"), true);
        }
        return 0;
    }

    private static int getUUID(CommandSource source, ServerPlayerEntity player) throws CommandException {
        StringTextComponent comp = new StringTextComponent(player.getName() + "'s UUID: " + TextFormatting.UNDERLINE + player.getUniqueID());
        Style style = Style.EMPTY;
        style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, player.getUniqueID().toString()));
        style.setHoverEvent(new HoverEvent(SHOW_TEXT, new StringTextComponent("Click to get text")));
        comp.setStyle(style);
        source.sendFeedback(comp, true);
        return 0;
    }

    //region Dump Event Handlers

    public static int dumpEventListeners(CommandSource source) throws CommandException {
        Map<String, Map<Class<?>, List<Pair<EventPriority, Method>>>> eventListenerMap = new HashMap<>();
        dumpBus("EVENT_BUS", (EventBus) MinecraftForge.EVENT_BUS, eventListenerMap);
//        dumpBus("ORE_GEN_BUS", MinecraftForge.ORE_GEN_BUS, eventListenerMap);
//        dumpBus("TERRAIN_GEN_BUS", MinecraftForge.TERRAIN_GEN_BUS, eventListenerMap);

        StringBuilder builder = new StringBuilder("\n");
        for (String bus : eventListenerMap.keySet()) {
            builder.append("Dumping listeners for bus: ").append(bus).append("\n");
            Map<Class<?>, List<Pair<EventPriority, Method>>> busListeners = eventListenerMap.get(bus);
            List<Class<?>> sortedClasses = Lists.newArrayList(busListeners.keySet());
            sortedClasses.sort(Comparator.comparing(Class::getName));
            for (Class<?> eventClass : sortedClasses) {
                List<Pair<EventPriority, Method>> listenerList = busListeners.get(eventClass);
                listenerList.sort(Comparator.comparingInt(value -> value.key().ordinal()));
                builder.append("    Handlers for event: ").append(eventClass).append("\n");
                for (Pair<EventPriority, Method> listener : listenerList) {
                    Method m = listener.value();
                    builder.append("        ").append(listener.key()).append(" ").append(m.getDeclaringClass().getName()).append(" ").append(m.getName()).append("(").append(separateWithCommas(m.getParameterTypes())).append(")\n");
                }
                builder.append("\n");
            }
        }

        LogHelperBC.info(builder.toString());
        for (String s : builder.toString().split("\n")) {
            source.sendFeedback(new StringTextComponent(s), true);
        }
        return 0;
    }

    private static String separateWithCommas(Class<?>[] types) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < types.length; j++) {
            sb.append(types[j].getTypeName());
            if (j < (types.length - 1)) sb.append(",");
        }
        return sb.toString();
    }

    private static void dumpBus(String name, EventBus bus, Map<String, Map<Class<?>, List<Pair<EventPriority, Method>>>> baseMap) throws CommandException {
        Map<Class<?>, List<Pair<EventPriority, Method>>> map = baseMap.computeIfAbsent(name, eventBus -> new HashMap<>());

        try {
            ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = ObfuscationReflectionHelper.getPrivateValue(EventBus.class, bus, "listeners");
            for (Object obj : listeners.keySet()) {
                for (Method method : obj.getClass().getMethods()) {
                    SubscribeEvent anno;
                    if ((anno = method.getAnnotation(SubscribeEvent.class)) != null) {
                        for (Class<?> parameter : method.getParameterTypes()) {
                            if (Event.class.isAssignableFrom(parameter)) {
                                map.computeIfAbsent(parameter, aClass -> new ArrayList<>()).add(new Pair<>(anno.priority(), method));
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new CommandException(new StringTextComponent(e.getMessage()));
        }
    }

    //endregion

    private static int eggify(CommandContext<CommandSource> ctx, Entity target) throws CommandException, CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        Entity entity = target;

        if (entity == null) {
            player.sendMessage(new StringTextComponent("You must be looking at an entity!"), Util.DUMMY_UUID);
            return 1;
        }

        ItemStack spawnEgg = new ItemStack(SpawnEggItem.getEgg(entity.getType()));
        CompoundNBT data = entity.serializeNBT();
//        data.putString("id", String.valueOf(EntityList.getKey(entity)));
        spawnEgg.setTagInfo("EntityTag", data);

        data.remove("Pos");
        data.remove("Motion");
        data.remove("Rotation");
        data.remove("FallDistance");
        data.remove("Fire");
        data.remove("Air");
        data.remove("OnGround");
        data.remove("Dimension");
        data.remove("Invulnerable");
        data.remove("PortalCooldown");
        data.remove("UUID");

        InventoryUtils.givePlayerStack(player, spawnEgg);
        return 0;
    }

    @Nullable
    protected static Entity traceEntity(PlayerEntity player) {
        Entity entity = null;
//        List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getBoundingBox().grow(20.0D));
//        double d0 = 0.0D;
//
//        Vector3d start = new Vector3d(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
//        Vector3d look = player.getLookVec();
//        Vector3d end = new Vector3d(player.getPosX() + (look.x * 20), player.getPosY() + player.getEyeHeight() + (look.y * 20), player.getPosZ() + (look.z * 20));
//
//        for (int i = 0; i < list.size(); ++i) {
//            Entity entity1 = list.get(i);
//            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(0.2);
//            RayTraceResult raytraceresult = AxisAlignedBB.rayTrace(Collections.singleton(axisalignedbb), start, end, player.getPosition());//axisalignedbb.calculateIntercept(start, end);
//
//            if (raytraceresult != null) {
//                double d1 = start.squareDistanceTo(raytraceresult.getHitVec());
//
//                if (d1 < d0 || d0 == 0.0D) {
//                    entity = entity1;
//                    d0 = d1;
//                }
//            }
//        }
//

        return entity;
    }


    //region Player Access Command

    private static Map<UUID, GameProfile> accessiblePlayers(CommandSource source) throws CommandException {
//        PlayerProfileCache cache = source.getServer().getPlayerProfileCache();
//
//        File playersFolder = new File(source.getServer().getWorld(World.OVERWORLD).getSaveHandler().getWorldDirectory(), "playerdata");
//        File[] playerArray = playersFolder.listFiles((dir, name) -> name.endsWith(".dat"));
//        if (playerArray == null) {
//            throw new CommandException(new StringTextComponent("There are no players in the playerdata folder"));
//        }
//
//        Map<String, File> playerFiles = new HashMap<>();
//        for (File file : playerArray) {
//            playerFiles.put(file.getName().replace(".dat", ""), file);
//        }

        Map<UUID, GameProfile> playerMap = new HashMap<>();
//        for (String stringId : playerFiles.keySet()) {
//            try {
//                UUID uuid = UUID.fromString(stringId);
//                GameProfile profile = cache.getProfileByUUID(uuid);
//                playerMap.put(uuid, profile);
//            }
//            catch (Throwable e) {
//                source.sendErrorMessage(new StringTextComponent("Detected possible non-playerdata file in playerdata folder: " + playerFiles.get(stringId) + ". Skipping").setStyle(new Style().setColor(TextFormatting.RED)));
//            }
//        }
        return playerMap;
    }


    private static int playerAccess(CommandSource source, String target) throws CommandException, CommandSyntaxException {
        PlayerProfileCache cache = source.getServer().getPlayerProfileCache();
//
//        File playersFolder = new File(source.getServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), "playerdata");
//        File[] playerArray = playersFolder.listFiles((dir, name) -> name.endsWith(".dat"));
//        if (playerArray == null) {
//            throw new CommandException(new StringTextComponent("There are no players in the playerdata folder"));
//        }
//
//        Map<String, File> playerFiles = new HashMap<>();
//        for (File file : playerArray) {
//            playerFiles.put(file.getName().replace(".dat", ""), file);
//        }

        Map<UUID, GameProfile> playerMap = accessiblePlayers(source);//new HashMap<>();
//        for (String stringId : playerFiles.keySet()) {
//            try {
//                UUID uuid = UUID.fromString(stringId);
//                GameProfile profile = cache.getProfileByUUID(uuid);
//                playerMap.put(uuid, profile);
//            }
//            catch (Throwable e) {
//                source.sendErrorMessage(new StringTextComponent("Detected possible non-playerdata file in playerdata folder: " + playerFiles.get(stringId) + ". Skipping").setStyle(new Style().setColor(TextFormatting.RED)));
//            }
//        }

        if (target == null) {
            source.sendFeedback(new StringTextComponent("################## All Known Players ##################"), false);
            for (UUID uuid : playerMap.keySet()) {
                GameProfile profile = playerMap.get(uuid);

                boolean online = false;
                for (PlayerEntity player : source.getServer().getPlayerList().getPlayers()) {
                    if (player.getGameProfile().getId().equals(uuid)) {
                        online = true;
                        break;
                    }
                }

                TextComponent message = new StringTextComponent((online ? TextFormatting.GREEN + "[Online]: " : TextFormatting.GRAY + "[Offline]: ") + profile.getName());

                boolean offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getName()).getBytes(Charsets.UTF_8)).equals(uuid);
                if (offline) {
                    message.append(new StringTextComponent(" (Offline Account)").mergeStyle(TextFormatting.RED));
                }

                ITextComponent messageHover = new StringTextComponent("Last Seen: " + "\n") //
                        .append(new StringTextComponent(TextFormatting.GRAY + "UUID: " + uuid + "\n")) //
                        .append(new StringTextComponent(TextFormatting.GOLD + "-Click to access player."));

                Style msgStyle = Style.EMPTY;
                msgStyle.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bcore_util player_access " + uuid));
                msgStyle.setHoverEvent(new HoverEvent(SHOW_TEXT, messageHover));
                message.setStyle(msgStyle);
                source.sendFeedback(message, false);
            }
            return 0;
        }
        target = target.toLowerCase();

        GameProfile profile = null;
        if (cache.usernameToProfileEntryMap.containsKey(target)) {
            profile = cache.getGameProfileForUsername(target);
            target = profile.getId().toString();
        } else {
            try {
                profile = cache.getProfileByUUID(UUID.fromString(target));
            }
            catch (IllegalArgumentException ignored) {
            }

            if (profile == null) {
                throw new CommandException(new StringTextComponent("Could not find the specified player name or uuid!"));
            }
        }

        //Access Player
        ServerPlayerEntity playerSender = source.asPlayer();
        PlayerEntity targetPlayer = source.getServer().getPlayerList().getPlayerByUUID(profile.getId());
        if (targetPlayer == null) {
            File playerFile = getPlayerFile(source.getServer(), target);
//            targetPlayer = new OfflinePlayer(playerSender, source.getServer().getWorld(World.OVERWORLD), profile, playerFile);
        }

        if (playerSender == targetPlayer) {
            throw new CommandException(new StringTextComponent("This command only works on other players!"));
        }
        openPlayerAccessUI(source.getServer(), playerSender, targetPlayer);
        return 0;
    }

    public static File getPlayerFile(MinecraftServer server, String uuid) throws CommandException {
//        File playerFolder = new File(server.getWorld(World.OVERWORLD).getSaveHandler().getWorldDirectory(), "playerdata");
//        File[] playerArray = playerFolder.listFiles();
//        if (playerArray == null) {
//            throw new CommandException(new StringTextComponent("There are no players in the playerdata folder"));
//        }

//        for (File file : playerArray) {
//            if (file.getName().replace(".dat", "").equals(uuid)) {
//                return file;
//            }
//        }

        throw new CommandException(new StringTextComponent("Could not find a data file for the specified player!"));
    }

    public static CompoundNBT readPlayerCompound(File playerData) throws CommandException {
        DataInputStream is = null;
        try {
            is = new DataInputStream(new GZIPInputStream(new FileInputStream(playerData)));
            CompoundNBT compound = CompressedStreamTools.read(is);
            IOUtils.closeQuietly(is);
            return compound;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(new StringTextComponent(e.toString()));
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void writePlayerCompound(File playerFile, CompoundNBT playerCompound) throws IOException {
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(playerFile)));
            CompressedStreamTools.write(playerCompound, os);
            IOUtils.closeQuietly(os);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        finally {
            IOUtils.closeQuietly(os);
        }
    }

    public static void openPlayerAccessUI(MinecraftServer server, ServerPlayerEntity player, PlayerEntity playerAccess) {
        player.getNextWindowId();
        player.closeContainer();
        int windowId = player.currentWindowId;
        BCoreNetwork.sendOpenPlayerAccessUI(player, windowId);
        BCoreNetwork.sendPlayerAccessUIUpdate(player, playerAccess);
        player.openContainer(new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("Player Access");
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                ContainerPlayerAccess access = new ContainerPlayerAccess(id, playerInventory, playerAccess, server);
                return access;
            }
        });
//        player.openContainer = new ContainerPlayerAccess(player, playerAccess, server);
//        player.openContainer.windowId = windowId;
//        player.openContainer.addListener(player);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.openContainer));
    }

//    public static class OfflinePlayer extends PlayerEntity {
//
//        private final PlayerEntity accessedBy;
//        private final File playerFile;
//        private CompoundNBT playerCompound;
//
//        public OfflinePlayer(PlayerEntity accessedBy, World worldIn, GameProfile gameProfileIn, File playerFile) throws CommandException {
//            super(worldIn, gameProfileIn);
//            this.accessedBy = accessedBy;
//            this.playerFile = playerFile;
//            inventory = new PlayerInventory(this) {
//                @Override
//                public void markDirty() {
//                    saveOfflinePlayer();
//                }
//
//                @Override
//                public void clear() {
//                    super.clear();
//                    saveOfflinePlayer();
//                }
//            };
//            playerCompound = readPlayerCompound(playerFile);
//            read(playerCompound);
//        }
//
//        public void tpTo(PlayerEntity player) {
//            posX = player.getPosX();
//            posY = player.getPosY();
//            posZ = player.getPosZ();
//            dimension = player.dimension;
//            saveOfflinePlayer();
//        }
//
//        @Override
//        public boolean isSpectator() {
//            return false;
//        }
//
//        @Override
//        public boolean isCreative() {
//            return false;
//        }
//
//        public void saveOfflinePlayer() {
//            playerCompound = serializeNBT();
//            try {
//                writePlayerCompound(playerFile, playerCompound);
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//                accessedBy.sendMessage(new StringTextComponent("An error occurred while saving the player's inventory!\n" + e.toString() + "\nFull error is in server console."));
//            }
//        }
//    }

    //endregion
}
