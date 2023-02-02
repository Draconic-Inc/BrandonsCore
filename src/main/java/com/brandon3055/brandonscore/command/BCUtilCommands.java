package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.handlers.HandHelper;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.Pair;
import com.brandon3055.brandonscore.lib.StringyStacks;
import com.brandon3055.brandonscore.multiblock.MultiBlockManager;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.InventoryUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.Utils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT;

;

/**
 * Created by brandon3055 on 23/06/2017.
 */
//TODO Test all these commands!
public class BCUtilCommands {

    private static Random rand = new Random();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("bcore_util")
                .then(registerNBT())
                .then(registerStackString())
//                        .then(registerRegenChunk())
                .then(registerNoClip())
                .then(registerUUID())
//                        .then(registerPlayerAccess())
                .then(registerDumpEvents())
                .then(registerEggify())
                .then(registerPlaceMultiBlock())
                .then(reloadContributors());
        if (BrandonsCore.inDev) {
            builder.then(registerDev1());
            builder.then(registerDev2());
        }

        dispatcher.register(builder);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> reloadContributors() {
        return Commands.literal("reset_contrib_handler")
                .requires(cs -> cs.hasPermission(3))
                .executes(context -> {
                    ContributorHandler.reload();
                    context.getSource().sendSuccess(new TextComponent("Reset complete"), false);
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerPlaceMultiBlock() {
        return Commands.literal("place_multiblock")
                .requires(cs -> cs.hasPermission(3))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("multiblock", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(MultiBlockManager.getRegisteredIds(), builder))
                                .executes(context -> MultiBlockManager.placeCommand(context.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(context, "pos"), ResourceLocationArgument.getId(context, "multiblock"), Vec3.ZERO))
                                .then(Commands.argument("rotation", Vec3Argument.vec3(false))
                                        .executes(context -> MultiBlockManager.placeCommand(context.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(context, "pos"), ResourceLocationArgument.getId(context, "multiblock"), Vec3Argument.getVec3(context, "rotation")))
                                )
                        )
                );
    }


    private static ArgumentBuilder<CommandSourceStack, ?> registerNBT() {
        return Commands.literal("nbt")
                .requires(cs -> cs.hasPermission(0))
                .executes(context -> functionNBT(context.getSource()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerStackString() {
        return Commands.literal("stack_string")
                .then(Commands.literal("from_string")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.argument("give-to", EntityArgument.player())
                                .then(Commands.argument("stack-string", StringArgumentType.greedyString())
                                        .executes(ctx -> functionFromStackString(ctx.getSource(), EntityArgument.getPlayer(ctx, "give-to"), StringArgumentType.getString(ctx, "stack-string")))
                                )))

                .then(Commands.literal("to_string")
                        .requires(cs -> cs.hasPermission(0))
                        .then(Commands.literal("id_only")
                                .executes(context -> functionToStackString(context.getSource(), false, false, false)))
                        .then(Commands.literal("id_nbt")
                                .executes(context -> functionToStackString(context.getSource(), false, true, false)))
                        .then(Commands.literal("id_count")
                                .executes(context -> functionToStackString(context.getSource(), true, false, false)))
                        .then(Commands.literal("id_nbt_count")
                                .executes(context -> functionToStackString(context.getSource(), true, true, false)))
                        .then(Commands.literal("id_nbt_capabilities")
                                .executes(context -> functionToStackString(context.getSource(), false, true, true)))
                        .executes(context -> functionToStackString(context.getSource(), true, true, true))
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerRegenChunk() {
        return Commands.literal("regenchunk")
                .requires(cs -> cs.hasPermission(3))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 32))
                        .executes(ctx -> regenChunk(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerNoClip() {
        return Commands.literal("noclip")
                .requires(cs -> cs.hasPermission(3))
                .executes(context -> toggleNoClip(context.getSource()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerUUID() {
        return Commands.literal("uuid")
                .requires(cs -> cs.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> getUUID(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                )
                .executes(ctx -> getUUID(ctx.getSource(), ctx.getSource().getPlayerOrException()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerPlayerAccess() {
        return Commands.literal("player_access")
                .requires(cs -> cs.hasPermission(3))
                .then(Commands.argument("target", reader -> StringArgumentType.string())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(accessiblePlayers(context.getSource()).values().stream().map(GameProfile::getName), builder))
                        .executes(context -> playerAccess(context.getSource(), context.getArgument("target", String.class)))
                )
                .then(Commands.literal("list")
                        .executes(context -> playerAccess(context.getSource(), null))
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerDumpEvents() {
        return Commands.literal("dump_event_listeners")
                .requires(cs -> cs.hasPermission(0))
                .executes(ctx -> dumpEventListeners(ctx.getSource()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerEggify() {
        return Commands.literal("eggify")
                .requires(cs -> cs.hasPermission(3))
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

    private static int functionNBT(CommandSourceStack source) throws CommandRuntimeException, CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = HandHelper.getMainFirst(player);
        if (stack.isEmpty()) {
            throw new CommandRuntimeException(new TextComponent("You are not holding an item!"));
        } else if (!stack.hasTag()) {
            throw new CommandRuntimeException(new TextComponent("That stack has no NBT tag!"));
        }

        CompoundTag compound = stack.getTag();
        LogHelperBC.logNBT(compound);
        LogHelperBC.info(compound);
        StringBuilder builder = new StringBuilder();
        LogHelperBC.buildNBT(builder, compound, "", "Tag", false);
        String[] lines = builder.toString().split("\n");
        DataUtils.forEach(lines, s -> ChatHelper.sendMessage(player, new TextComponent(s).withStyle(ChatFormatting.GOLD)));
        return 0;
    }

    private static int functionToStackString(CommandSourceStack source, boolean count, boolean nbt, boolean caps) throws CommandRuntimeException, CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = HandHelper.getMainFirst(player);
        if (stack.isEmpty()) {
            throw new CommandRuntimeException(new TextComponent("You are not holding an item!"));
        }

        String returnString = StringyStacks.toString(stack, nbt, count, caps);
        ChatHelper.sendMessage(player, new TextComponent("# The following is stack string for the held stack (click to copy) #").withStyle(ChatFormatting.BLUE));
        MutableComponent textComponent = returnString.length() > 64 ? new TextComponent(returnString.substring(0, 64) + "... ").withStyle(ChatFormatting.GOLD).append(new TextComponent("(Mouseover for full)").withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.UNDERLINE)) : new TextComponent(returnString).withStyle(ChatFormatting.GOLD);
        textComponent.setStyle(textComponent.getStyle().withHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponent("Click to copy to clipboard").withStyle(ChatFormatting.BLUE).append(new TextComponent("\n" + returnString).withStyle(ChatFormatting.GRAY)))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, returnString)));
        ChatHelper.sendMessage(player, textComponent);
        StringyStacks.LOGGER.info(returnString);
        return 0;
    }

    private static int functionFromStackString(CommandSourceStack source, ServerPlayer player, String stackString) throws CommandRuntimeException, CommandSyntaxException {
        ItemStack stack = StringyStacks.fromString(stackString, null);
        if (stack == null) {
            throw new CommandRuntimeException(new TextComponent("Invalid item string. You may find more details in the server console."));
        }

        boolean flag = player.getInventory().add(stack);
        if (flag && stack.isEmpty()) {
            stack.setCount(1);
            player.level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.inventoryMenu.broadcastChanges();
        } else {
            ItemEntity itementity = player.drop(stack, false);
            if (itementity != null) {
                itementity.setNoPickUpDelay();
                itementity.setOwner(player.getUUID());
            }
        }
        return 0;
    }

    private static int regenChunk(CommandSourceStack source, int rad) throws CommandRuntimeException, CommandSyntaxException {
//        LogHelperBC.dev(rad);

        for (int xOffset = -rad; xOffset <= rad; xOffset++) {
            for (int yOffset = -rad; yOffset <= rad; yOffset++) {
                ServerLevel world = (ServerLevel) source.getLevel();
                Player player = source.getPlayerOrException();
//                int chunkX = (int) player.xChunk + xOffset;
//                int chunkZ = (int) player.zChunk + yOffset;

//                LevelChunk oldChunk = world.getChunk(chunkX, chunkZ);
                ServerChunkCache chunkProviderServer = world.getChunkSource();
                ChunkGenerator chunkGenerate = chunkProviderServer.getGenerator();

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

    private static int toggleNoClip(CommandSourceStack source) throws CommandRuntimeException, CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean enabled = BCEventHandler.noClipPlayers.contains(player.getUUID());

        if (enabled) {
            BCEventHandler.noClipPlayers.remove(player.getUUID());
            BCoreNetwork.sendNoClip(player, false);
            source.sendSuccess(new TextComponent("NoClip Disabled!"), true);
        } else {
            BCEventHandler.noClipPlayers.add(player.getUUID());
            BCoreNetwork.sendNoClip(player, true);
            source.sendSuccess(new TextComponent("NoClip Enabled!"), true);
        }
        return 0;
    }

    private static int getUUID(CommandSourceStack source, ServerPlayer player) throws CommandRuntimeException {
        TextComponent comp = new TextComponent(player.getName().getString() + "'s UUID: " + ChatFormatting.UNDERLINE + player.getUUID());
        comp.setStyle(comp.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, player.getUUID().toString())));
        comp.setStyle(comp.getStyle().withHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponent("Click to copy to clipboard"))));
        source.sendSuccess(comp, true);
        return 0;
    }

    //region Dump Event Handlers

    public static int dumpEventListeners(CommandSourceStack source) throws CommandRuntimeException {
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
            source.sendSuccess(new TextComponent(s), true);
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

    private static void dumpBus(String name, EventBus bus, Map<String, Map<Class<?>, List<Pair<EventPriority, Method>>>> baseMap) throws CommandRuntimeException {
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
        } catch (Throwable e) {
            e.printStackTrace();
            throw new CommandRuntimeException(new TextComponent(e.getMessage()));
        }
    }

    //endregion

    private static int eggify(CommandContext<CommandSourceStack> ctx, Entity target) throws CommandRuntimeException, CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Entity entity = target;

        if (entity == null) {
            player.sendMessage(new TextComponent("You must be looking at an entity!"), Util.NIL_UUID);
            return 1;
        }

        ItemStack spawnEgg = new ItemStack(SpawnEggItem.byId(entity.getType()));
        CompoundTag data = entity.serializeNBT();
//        data.putString("id", String.valueOf(EntityList.getKey(entity)));
        spawnEgg.addTagElement("EntityTag", data);

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
    protected static Entity traceEntity(Player player) {
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

    private static Map<UUID, GameProfile> accessiblePlayers(CommandSourceStack source) throws CommandRuntimeException {
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


    private static int playerAccess(CommandSourceStack source, String target) throws CommandRuntimeException, CommandSyntaxException {
        GameProfileCache cache = source.getServer().getProfileCache();
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
            source.sendSuccess(new TextComponent("################## All Known Players ##################"), false);
            for (UUID uuid : playerMap.keySet()) {
                GameProfile profile = playerMap.get(uuid);

                boolean online = false;
                for (Player player : source.getServer().getPlayerList().getPlayers()) {
                    if (player.getGameProfile().getId().equals(uuid)) {
                        online = true;
                        break;
                    }
                }

                BaseComponent message = new TextComponent((online ? ChatFormatting.GREEN + "[Online]: " : ChatFormatting.GRAY + "[Offline]: ") + profile.getName());

                boolean offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getName()).getBytes(Charsets.UTF_8)).equals(uuid);
                if (offline) {
                    message.append(new TextComponent(" (Offline Account)").withStyle(ChatFormatting.RED));
                }

                Component messageHover = new TextComponent("Last Seen: " + "\n") //
                        .append(new TextComponent(ChatFormatting.GRAY + "UUID: " + uuid + "\n")) //
                        .append(new TextComponent(ChatFormatting.GOLD + "-Click to access player."));

                Style msgStyle = Style.EMPTY;
                msgStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bcore_util player_access " + uuid));
                msgStyle.withHoverEvent(new HoverEvent(SHOW_TEXT, messageHover));
                message.setStyle(msgStyle);
                source.sendSuccess(message, false);
            }
            return 0;
        }
        target = target.toLowerCase(Locale.ENGLISH);

        GameProfile profile = null;
        if (cache.get(target).isPresent()) {
            profile = cache.get(target).get();
            target = profile.getId().toString();
        } else {
            try {
                profile = cache.get(UUID.fromString(target)).orElse(null);
            } catch (IllegalArgumentException ignored) {
            }

            if (profile == null) {
                throw new CommandRuntimeException(new TextComponent("Could not find the specified player name or uuid!"));
            }
        }

        //Access Player
        ServerPlayer playerSender = source.getPlayerOrException();
        Player targetPlayer = source.getServer().getPlayerList().getPlayer(profile.getId());
        if (targetPlayer == null) {
            File playerFile = getPlayerFile(source.getServer(), target);
//            targetPlayer = new OfflinePlayer(playerSender, source.getServer().getWorld(World.OVERWORLD), profile, playerFile);
        }

        if (playerSender == targetPlayer) {
            throw new CommandRuntimeException(new TextComponent("This command only works on other players!"));
        }
        openPlayerAccessUI(source.getServer(), playerSender, targetPlayer);
        return 0;
    }

    public static File getPlayerFile(MinecraftServer server, String uuid) throws CommandRuntimeException {
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

        throw new CommandRuntimeException(new TextComponent("Could not find a data file for the specified player!"));
    }

    public static CompoundTag readPlayerCompound(File playerData) throws CommandRuntimeException {
        DataInputStream is = null;
        try {
            is = new DataInputStream(new GZIPInputStream(new FileInputStream(playerData)));
            CompoundTag compound = NbtIo.read(is);
            IOUtils.closeQuietly(is);
            return compound;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandRuntimeException(new TextComponent(e.toString()));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void writePlayerCompound(File playerFile, CompoundTag playerCompound) throws IOException {
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(playerFile)));
            NbtIo.write(playerCompound, os);
            IOUtils.closeQuietly(os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    public static void openPlayerAccessUI(MinecraftServer server, ServerPlayer player, Player playerAccess) {
        player.nextContainerCounter();
        player.doCloseContainer();
        int windowId = player.containerCounter;
        BCoreNetwork.sendOpenPlayerAccessUI(player, windowId);
        BCoreNetwork.sendPlayerAccessUIUpdate(player, playerAccess);
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("Player Access");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                ContainerPlayerAccess access = new ContainerPlayerAccess(id, playerInventory, playerAccess, server);
                return access;
            }
        });
//        player.openContainer = new ContainerPlayerAccess(player, playerAccess, server);
//        player.openContainer.windowId = windowId;
//        player.openContainer.addListener(player);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.containerMenu));
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


    private static ArgumentBuilder<CommandSourceStack, ?> registerDev1() {
        return Commands.literal("dev1")
                .requires(cs -> cs.hasPermission(3))
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    int radius = 90;
                    BlockPos origin = player.blockPosition().offset(0, 88 - player.blockPosition().getY(), 0);

                    BlockPos.betweenClosed(origin.offset(-radius, -20, -radius), origin.offset(radius, 20, radius)).forEach(blockPos -> {
                        if (Utils.getDistance(blockPos.getX(), blockPos.getZ(), origin.getX(), origin.getZ()) > radius) return;
                        BlockState state = level.getBlockState(blockPos);
                        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                            level.removeBlock(blockPos, false);
                        }
                    });

                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerDev2() {
        return Commands.literal("dev2")
                .requires(cs -> cs.hasPermission(3))
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

//                    level

                    return 0;
                });
    }
}
