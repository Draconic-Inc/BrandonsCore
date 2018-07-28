package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.handlers.HandHelper;
import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.PairKV;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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
public class BCUtilCommands extends CommandBase {

    @Override
    public String getName() {
        return "bcore_util";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/bcore_util help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            help(sender);
            return;
        }

        try {
            String function = args[0];

            if (function.toLowerCase().equals("nbt")) {
                functionNBT(server, sender, args);
            }
            else if (function.equals("regenchunk")) {
                regenChunk(server, sender, args);
            }
            else if (function.equals("noclip")) {
                toggleNoClip(server, sender, args);
            }
            else if (function.equals("uuid")) {
                getUUID(server, sender, args);
            }
            else if (function.equals("player_access")) {
                playerAccess(server, sender, args);
            }
            else if (function.equals("dump_event_listeners")) {
                dumpEventListeners(sender);
            }
            else {
                help(sender);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2 && args[0].equals("player_access")) {
            PlayerProfileCache cache = server.getPlayerProfileCache();
            List<String> list = new ArrayList<>();
            list.add("list");
            list.addAll(Lists.newArrayList(cache.getUsernames()));
            return getListOfStringsMatchingLastWord(args, list);
        }
        return getListOfStringsMatchingLastWord(args, "nbt", "regenchunk", "noclip", "uuid", "player_access", "dump_event_listeners");
    }

    private void help(ICommandSender sender) {
        ChatHelper.message(sender, "The following are a list of Brandon's Core Utility Commands", new Style().setColor(TextFormatting.AQUA).setUnderlined(true));
        ChatHelper.message(sender, "/bcore_util nbt", TextFormatting.BLUE);
        ChatHelper.message(sender, "-Prints the NBT tag of the stack you are holding to chat and to the console.", TextFormatting.GRAY);
        ChatHelper.message(sender, "/bcore_util regenchunk [radius]", TextFormatting.BLUE);
        ChatHelper.message(sender, "-Regenerates the chunk(s) at your position.", TextFormatting.GRAY);
        ChatHelper.message(sender, "/bcore_util noclip", TextFormatting.BLUE);
        ChatHelper.message(sender, "-Toggles noclip allowing you to fly through blocks as if in spectator mode... Or fall into the void if you dont have flight", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-", TextFormatting.GRAY);
    }

    private void functionNBT(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        ItemStack stack = HandHelper.getMainFirst(player);
        if (stack.isEmpty()) {
            throw new CommandException("You are not holding an item!");
        }
        else if (!stack.hasTagCompound()) {
            throw new CommandException("That stack has no NBT tag!");
        }

        NBTTagCompound compound = stack.getTagCompound();
        LogHelperBC.logNBT(compound);
        StringBuilder builder = new StringBuilder();
        LogHelperBC.buildNBT(builder, compound, "", "Tag", false);
        String[] lines = builder.toString().split("\n");
        DataUtils.forEach(lines, s -> ChatHelper.message(sender, s, TextFormatting.GOLD));
    }

    private void regenChunk(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int rad = 0;
        if (args.length > 1) {
            rad = parseInt(args[1]);
        }

        LogHelperBC.dev(rad);

        for (int xOffset = -rad; xOffset <= rad; xOffset++) {
            for (int yOffset = -rad; yOffset <= rad; yOffset++) {
                WorldServer world = (WorldServer) sender.getEntityWorld();
                EntityPlayer player = getCommandSenderAsPlayer(sender);
                int chunkX = (int) player.chunkCoordX + xOffset;
                int chunkZ = (int) player.chunkCoordZ + yOffset;

                Chunk oldChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                ChunkProviderServer chunkProviderServer = world.getChunkProvider();
                IChunkGenerator chunkGenerate = chunkProviderServer.chunkGenerator;

                Chunk newChunk = chunkGenerate.generateChunk(chunkX, chunkZ);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < world.getHeight(); y++) {
                            BlockPos chunkPos = new BlockPos(x, y, z);
                            BlockPos absPos = new BlockPos(x + (chunkX * 16), y, z + (chunkZ * 16));
                            IBlockState newState = newChunk.getBlockState(chunkPos);
                            world.setBlockState(absPos, newState);

                            TileEntity tileEntity = newChunk.getTileEntity(chunkPos, Chunk.EnumCreateEntityType.IMMEDIATE);
                            if (tileEntity != null) {
                                world.setTileEntity(absPos, tileEntity);
                            }
                        }
                    }
                }

                oldChunk.setTerrainPopulated(false);
                oldChunk.populate(chunkProviderServer, chunkGenerate);

                PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
                if (playerChunkMap == null) {
                    return;
                }

                oldChunk.setModified(true);
                oldChunk.generateSkylightMap();

                PlayerChunkMapEntry watcher = playerChunkMap.getEntry(oldChunk.x, oldChunk.z);
                if (watcher != null) {
                    watcher.sendPacket(new SPacketChunkData(oldChunk, 65535));
                }
            }
        }
    }

    private void toggleNoClip(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String name = sender.getName();
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        boolean enabled = BCEventHandler.noClipPlayers.contains(name);

        if (enabled) {
            BCEventHandler.noClipPlayers.remove(name);
            PacketDispatcher.sendNoClip(player, false);
            sender.sendMessage(new TextComponentString("NoClip Disabled!"));
        }
        else {
            BCEventHandler.noClipPlayers.add(name);
            PacketDispatcher.sendNoClip(player, true);
            sender.sendMessage(new TextComponentString("NoClip Enabled!"));
        }
    }

    private void getUUID(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (args.length == 2) {
            player = getPlayer(server, sender, args[1]);
        }

        TextComponentString comp = new TextComponentString(player.getName() + "'s UUID: " + TextFormatting.UNDERLINE + player.getUniqueID());
        Style style = new Style();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, player.getUniqueID().toString()));
        style.setHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponentString("Click to get text")));
        comp.setStyle(style);
        sender.sendMessage(comp);
    }

    //region Dump Event Handlers

    public static void dumpEventListeners(ICommandSender sender) throws CommandException {
        Map<String, Map<Class<?>, List<PairKV<EventPriority, Method>>>> eventListenerMap = new HashMap<>();
        dumpBus("EVENT_BUS", MinecraftForge.EVENT_BUS, eventListenerMap);
        dumpBus("ORE_GEN_BUS", MinecraftForge.ORE_GEN_BUS, eventListenerMap);
        dumpBus("TERRAIN_GEN_BUS", MinecraftForge.TERRAIN_GEN_BUS, eventListenerMap);

        StringBuilder builder = new StringBuilder("\n");
        for (String bus : eventListenerMap.keySet()) {
            builder.append("Dumping listeners for bus: ").append(bus).append("\n");
            Map<Class<?>, List<PairKV<EventPriority, Method>>> busListeners = eventListenerMap.get(bus);
            List<Class<?>> sortedClasses = Lists.newArrayList(busListeners.keySet());
            sortedClasses.sort(Comparator.comparing(Class::getName));
            for (Class<?> eventClass : sortedClasses) {
                List<PairKV<EventPriority, Method>> listenerList = busListeners.get(eventClass);
                listenerList.sort(Comparator.comparingInt(value -> value.getKey().ordinal()));
                builder.append("    Handlers for event: ").append(eventClass).append("\n");
                for (PairKV<EventPriority, Method> listener : listenerList) {
                    Method m = listener.getValue();
                    builder.append("        ").append(listener.getKey()).append(" ").append(m.getDeclaringClass().getName()).append(" ").append(m.getName()).append("(").append(separateWithCommas(m.getParameterTypes())).append(")\n");
                }
                builder.append("\n");
            }
        }

        LogHelperBC.info(builder.toString());
        for (String s : builder.toString().split("\n")) {
            sender.sendMessage(new TextComponentString(s));
        }
    }

    private static String separateWithCommas(Class<?>[] types) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < types.length; j++) {
            sb.append(types[j].getTypeName());
            if (j < (types.length - 1))
                sb.append(",");
        }
        return sb.toString();
    }

    private static void dumpBus(String name, EventBus bus, Map<String, Map<Class<?>, List<PairKV<EventPriority, Method>>>> baseMap) throws CommandException {
        Map<Class<?>, List<PairKV<EventPriority, Method>>> map = baseMap.computeIfAbsent(name, eventBus -> new HashMap<>());

        try {
            ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = ReflectionHelper.getPrivateValue(EventBus.class, bus, "listeners");
            for (Object obj : listeners.keySet()) {
                for (Method method : obj.getClass().getMethods()) {
                    SubscribeEvent anno;
                    if ((anno = method.getAnnotation(SubscribeEvent.class)) != null) {
                        for (Class<?> parameter : method.getParameterTypes()) {
                            if (Event.class.isAssignableFrom(parameter)) {
                                map.computeIfAbsent(parameter, aClass -> new ArrayList<>()).add(new PairKV<>(anno.priority(), method));
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new CommandException(e.getMessage());
        }
    }

    //endregion

    //region Player Access Command

    private void playerAccess(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("Please specify a player or use the list option to view all known players.");
        }

        String target = args[1];
        PlayerProfileCache cache = server.getPlayerProfileCache();

        File playersFolder = new File(server.getEntityWorld().getSaveHandler().getWorldDirectory(), "playerdata");
        File[] playerArray = playersFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (playerArray == null) {
            throw new PlayerNotFoundException("There are no players in the playerdata folder");
        }

        Map<String, File> playerFiles = new HashMap<>();
        for (File file : playerArray) {
            playerFiles.put(file.getName().replace(".dat", ""), file);
        }

        Map<UUID, GameProfile> playerMap = new HashMap<>();
        for (String stringId : playerFiles.keySet()) {
            try {
                UUID uuid = UUID.fromString(stringId);
                GameProfile profile = cache.getProfileByUUID(uuid);
                playerMap.put(uuid, profile);
            }
            catch (Throwable e) {
                sender.sendMessage(new TextComponentString("Detected possible non-playerdata file in playerdata folder: " + playerFiles.get(stringId) + ". Skipping").setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }

        if (target.equals("list")) {
            sender.sendMessage(new TextComponentString("################## All Known Players ##################"));
            for (UUID uuid : playerMap.keySet()) {
                GameProfile profile = playerMap.get(uuid);

                boolean online = false;
                for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.getGameProfile().getId().equals(uuid)) {
                        online = true;
                        break;
                    }
                }

                ITextComponent message = new TextComponentString((online ? TextFormatting.GREEN + "[Online]: " : TextFormatting.GRAY + "[Offline]: ") + profile.getName());

                boolean offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getName()).getBytes(Charsets.UTF_8)).equals(uuid);
                if (offline) {
                    message.appendSibling(new TextComponentString(" (Offline Account)").setStyle(new Style().setColor(TextFormatting.RED)));
                }

                ITextComponent messageHover = new TextComponentString("Last Seen: " + "\n") //
                        .appendSibling(new TextComponentString(TextFormatting.GRAY + "UUID: " + uuid + "\n")) //
                        .appendSibling(new TextComponentString(TextFormatting.GOLD + "-Click to access player."));

                Style msgStyle = new Style();
                msgStyle.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bcore_util player_access " + uuid));
                msgStyle.setHoverEvent(new HoverEvent(SHOW_TEXT, messageHover));
                message.setStyle(msgStyle);
                sender.sendMessage(message);
            }
            return;
        }

        GameProfile profile = null;
        if (cache.usernameToProfileEntryMap.containsKey(target)) {
            profile = cache.getGameProfileForUsername(target);
            target = profile.getId().toString();
        }
        else {
            try {
                profile = cache.getProfileByUUID(UUID.fromString(target));
            }
            catch (IllegalArgumentException ignored) {}

            if (profile == null) {
                throw new CommandException("Could not find the specified player name or uuid!");
            }
        }

        //Access Player
        EntityPlayerMP playerSender = getCommandSenderAsPlayer(sender);

        EntityPlayer targetPlayer = server.getPlayerList().getPlayerByUUID(profile.getId());
        //noinspection ConstantConditions
        if (targetPlayer == null) {
            File playerFile = getPlayerFile(server, target);
            targetPlayer = new OfflinePlayer(playerSender, server.getWorld(0), profile, playerFile);
        }

        if (playerSender == targetPlayer) {
            throw new CommandException("This command only works on other players!");
        }
        openPlayerAccessUI(server, playerSender, targetPlayer);
    }

    public static File getPlayerFile(MinecraftServer server, String uuid) throws CommandException {
        File playerFolder = new File(server.getEntityWorld().getSaveHandler().getWorldDirectory(), "playerdata");
        File[] playerArray = playerFolder.listFiles();
        if (playerArray == null) {
            throw new PlayerNotFoundException("There are no players in the playerdata folder");
        }

        for (File file : playerArray) {
            if (file.getName().replace(".dat", "").equals(uuid)) {
                return file;
            }
        }

        throw new PlayerNotFoundException("Could not find a data file for the specified player!");
    }

    public static NBTTagCompound readPlayerCompound(File playerData) throws CommandException {
        DataInputStream is = null;
        try {
            is = new DataInputStream(new GZIPInputStream(new FileInputStream(playerData)));
            NBTTagCompound compound = CompressedStreamTools.read(is);
            IOUtils.closeQuietly(is);
            return compound;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(e.toString());
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void writePlayerCompound(File playerFile, NBTTagCompound playerCompound) throws IOException {
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

    public static void openPlayerAccessUI(MinecraftServer server, EntityPlayerMP player, EntityPlayer playerAccess) {
        player.getNextWindowId();
        player.closeContainer();
        int windowId = player.currentWindowId;
        PacketDispatcher.sendOpenPlayerAccessUI(player, windowId);
        PacketDispatcher.sendPlayerAccessUIUpdate(player, playerAccess);
        player.openContainer = new ContainerPlayerAccess(player, playerAccess, server);
        player.openContainer.windowId = windowId;
        player.openContainer.addListener(player);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.openContainer));
    }

    public static class OfflinePlayer extends EntityPlayer {

        private final EntityPlayer accessedBy;
        private final File playerFile;
        private NBTTagCompound playerCompound;

        public OfflinePlayer(EntityPlayer accessedBy, World worldIn, GameProfile gameProfileIn, File playerFile) throws CommandException {
            super(worldIn, gameProfileIn);
            this.accessedBy = accessedBy;
            this.playerFile = playerFile;
            inventory = new InventoryPlayer(this) {
                @Override
                public void markDirty() {
                    saveOfflinePlayer();
                }

                @Override
                public void clear() {
                    super.clear();
                    saveOfflinePlayer();
                }
            };
            playerCompound = readPlayerCompound(playerFile);
            readFromNBT(playerCompound);
        }

        public void tpTo(EntityPlayer player) {
            posX = player.posX;
            posY = player.posY;
            posZ = player.posZ;
            dimension = player.dimension;
            saveOfflinePlayer();
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }

        public void saveOfflinePlayer() {
            writeToNBT(playerCompound);
            try {
                writePlayerCompound(playerFile, playerCompound);
            }
            catch (IOException e) {
                e.printStackTrace();
                accessedBy.sendMessage(new TextComponentString("An error occurred while saving the player's inventory!\n" + e.toString() + "\nFull error is in server console."));
            }
        }
    }

    //endregion
}
