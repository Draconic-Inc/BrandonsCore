package com.brandon3055.brandonscore.command;

import codechicken.lib.reflect.ObfMapping;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.handlers.HandHelper;
import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.PairKV;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.InventoryUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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

    private static Random rand = new Random();

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

            if (function.equals("nbt")) {
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
            else if (function.equals("light") && !ObfMapping.obfuscated) {
//                new LightTest((WorldServer) sender.getEntityWorld()).runTest(new BlockPos(getCommandSenderAsPlayer(sender)));
            }
            else if (function.equals("eggify")) {
                eggify(server, sender, args);
            }
            else if (function.equals("pingblock")) {
                pingBlock(server, sender, args);
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
        else if (args.length == 2 && args[0].equals("pingblock")) {
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        }
        return getListOfStringsMatchingLastWord(args, "nbt", "regenchunk", "noclip", "uuid", "player_access", "dump_event_listeners", "eggify", "pingblock");
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
            if (j < (types.length - 1)) sb.append(",");
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

    private void eggify(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Entity entity = traceEntity(player);

        if (entity == null) {
            player.sendMessage(new TextComponentString("You must be looking at an entity!"));
            return;
        }

        ItemStack spawnEgg = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound data = entity.writeToNBT(new NBTTagCompound());
        data.setString("id", String.valueOf(EntityList.getKey(entity)));
        spawnEgg.setTagInfo("EntityTag", data);

        data.removeTag("Pos");
        data.removeTag("Motion");
        data.removeTag("Rotation");
        data.removeTag("FallDistance");
        data.removeTag("Fire");
        data.removeTag("Air");
        data.removeTag("OnGround");
        data.removeTag("Dimension");
        data.removeTag("Invulnerable");
        data.removeTag("PortalCooldown");
        data.removeTag("UUID");
        data.removeTag("UUIDLeast");
        data.removeTag("UUIDMost");

        InventoryUtils.givePlayerStack(player, spawnEgg);
    }

    private void pingBlock(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2)
        {
            throw new WrongUsageException("Usage: /bcore_util pingblock <block> [meta] [rad]");
        }
        Block block = CommandBase.getBlockByText(sender, args[1]);
        int meta = args.length > 2 ? parseInt(args[2], 0, 15) : -1;
        int range = args.length > 3 ? parseInt(args[3], 3, 128) : 32;

        sender.sendMessage(new TextComponentString("Searching for first " + args[1] + " within " + range + " Blocks"));
        Iterable<BlockPos> positions = BlockPos.getAllInBox(sender.getPosition().add(-range, -range, -range), sender.getPosition().add(+range, +range, +range));

        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : positions) {
            IBlockState state = sender.getEntityWorld().getBlockState(pos);
            if (state.getBlock() == block && (meta == -1 || meta == block.getMetaFromState(state))) {
                found.add(pos);
            }
        }

        sender.sendMessage(new TextComponentString("Found " + found.size() + " matches"));
        found.forEach(blockPos -> {
            sender.sendMessage(new TextComponentString("Match At " + blockPos));
        });
    }

    @Nullable
    protected Entity traceEntity(EntityPlayer player) {
        Entity entity = null;
        List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(20.0D));
        double d0 = 0.0D;

        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d look = player.getLookVec();
        Vec3d end = new Vec3d(player.posX + (look.x * 20), player.posY + player.getEyeHeight() + (look.y * 20), player.posZ + (look.z * 20));

        for (int i = 0; i < list.size(); ++i) {
            Entity entity1 = list.get(i);
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.2);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(start, end);

            if (raytraceresult != null) {
                double d1 = start.squareDistanceTo(raytraceresult.hitVec);

                if (d1 < d0 || d0 == 0.0D) {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        return entity;
    }

//    private static class LightTest {
//        private HashSet<Chunk> modifiedChunks = new HashSet<>();
//        private WorldServer world;
//
//        private Object2IntMap<BlockPos> skyLight = new Object2IntOpenHashMap<>();
//        private Object2IntMap<BlockPos> blockLight = new Object2IntOpenHashMap<>();
//        private Object2IntMap<BlockPos> LEBs = new Object2IntOpenHashMap<>();
//
//
//        private LightTest(WorldServer serverWorld) {
//            this.world = serverWorld;
//        }
//
//        private void runTest(BlockPos origin) {
//            IBlockState[] randStates = new IBlockState[]{Blocks.AIR.getDefaultState(), Blocks.STONE.getDefaultState(), Blocks.GLASS.getDefaultState()};
//            Map<BlockPos, IBlockState> blockList = new HashMap<>();
//
//            origin = new BlockPos(origin.getX(), 90, origin.getZ());
//            Iterable<BlockPos> blocks = BlockPos.getAllInBox(origin.add(-80, -40, -80), origin.add(80, 40, 80));
//
//            LogHelperBC.dev("Calc Blocks");
//            int i = 0;
//            for (BlockPos blockPos : blocks) {
//                i++;
//                IBlockState randState = randStates[rand.nextInt(randStates.length)];
//                if (rand.nextInt(100) == 0) {
//                    randState = Blocks.GLOWSTONE.getDefaultState();
//                }
//                blockList.put(blockPos, randState);
//            }
//
//            LogHelperBC.dev("Place " + i + " Blocks");
//            long start = System.currentTimeMillis();
//
//            for (BlockPos pos : blockList.keySet()) {
////                world.setBlockState(pos, blockList.get(pos), 0);
//                changeBlock(pos, blockList.get(pos));
////                world.setBlockToAir(pos);
//            }
//
//            LogHelperBC.dev("Blocks Placed in " + ((System.currentTimeMillis() - start) / 1000D) + " seconds");
//
//            start = System.currentTimeMillis();
//            doLightingMagics();
//            LogHelperBC.dev("Light Calculated in " + ((System.currentTimeMillis() - start) / 1000D) + " seconds");
//
//            start = System.currentTimeMillis();
//            sendChanges();
//            LogHelperBC.dev("Changes Sent in " + ((System.currentTimeMillis() - start) / 1000D) + " seconds");
//        }
//
//        private void doLightingMagics() {
//            HashSet<Chunk> lightChunks = new HashSet<>(modifiedChunks);
//            for (Chunk chunk : modifiedChunks) {
//                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
//                    Chunk adjacent = world.getChunkFromChunkCoords(chunk.x + facing.getFrontOffsetX(), chunk.z + facing.getFrontOffsetZ());
//                    lightChunks.add(adjacent);
//                }
//            }
//
//            long start = System.currentTimeMillis();
//            for (Chunk chunk : lightChunks) {
//                runPreCalculation(chunk); //Light in .88 before block search
//            }
//
//            LogHelperBC.dev(" - Pre-calculation Completed in " + ((System.currentTimeMillis() - start) / 1000D) + " seconds");
//            start = System.currentTimeMillis();
//
//            for (BlockPos pos : LEBs.keySet()) {
//                //Around ~7 seconds with vanilla calc and is a little broken.
////                world.checkLightFor(EnumSkyBlock.BLOCK, pos);
//                propagateBlockLight(pos, LEBs.get(pos), true);
//            }
//
//            LogHelperBC.dev(" - Block Light Calculated in " + ((System.currentTimeMillis() - start) / 1000D) + " seconds");
//        }
//
//        //Generates sky light map and finds all light emitting blocks
//        private void runPreCalculation(Chunk chunk) {
//            int topFilled = chunk.getTopFilledSegment();
////            chunk.heightMapMinimum = Integer.MAX_VALUE;
//
//            for (int x = 0; x < 16; ++x) {
//                for (int z = 0; z < 16; ++z) {
//                    chunk.precipitationHeightMap[x + (z << 4)] = -999;
//
//                    for (int y = topFilled + 16; y > 0; --y) {
//                        if (getBlockLightOpacity(chunk, x, y - 1, z) != 0) {
//                            chunk.heightMap[z << 4 | x] = y;
//
////                            if (y < chunk.heightMapMinimum)
////                            {
////                                chunk.heightMapMinimum = y;
////                            }
//
//                            break;
//                        }
//                    }
//
//                    if (chunk.getWorld().provider.hasSkyLight()) {
//                        //Start at the top "Empty block" where the light level will always ne 15
//                        //Go down block by block and subtract each blocks opacity from the light level then apply the remaining light
//                        //once the light level reaches 0 stop (essentially a ray trace)
//                        int lightLevel = 15;
//                        int offsetYPos = topFilled + 16 - 1;
//
//                        while (true) {
//                            ExtendedBlockStorage storage = chunk.storageArrays[offsetYPos >> 4];
//                            if (lightLevel > 0) {
//                                int opacityAtOffset = getBlockLightOpacity(chunk, x, offsetYPos, z);
//
//                                if (opacityAtOffset == 0 && lightLevel != 15) {
//                                    opacityAtOffset = 1;
//                                }
//
//                                lightLevel -= opacityAtOffset;
//
//                                if (lightLevel > 0) {
//                                    if (storage != NULL_BLOCK_STORAGE) {
//                                        skyLight.put(new BlockPos((chunk.x << 4) + x, offsetYPos, (chunk.z << 4) + z), lightLevel);
//                                        storage.setSkyLight(x, offsetYPos & 15, z, lightLevel);
////                                        chunk.getWorld().notifyLightSet(new BlockPos((chunk.x << 4) + x, offsetYPos, (chunk.z << 4) + z)); //Dont need this because it just sends changes to clients. We already do this with chunk packets
//                                    }
//                                }
//                            }
//
//                            if (storage != null) {
//                                int blockEmittedLight = getBlockLightLevel(chunk, x, offsetYPos, z);
//                                if (blockEmittedLight > 0) {
//                                    LEBs.put(new BlockPos(chunk.x << 4 | x & 15, offsetYPos, chunk.z << 4 | z & 15), blockEmittedLight);
//                                }
//
//                                storage.setBlockLight(x, offsetYPos & 15, z, blockEmittedLight); //This way i don't have to worry about updating blocks when a light source is removed. Though may cause other issues... Actually it will... Fuck this is hard!
//                            }
//
//
//                            --offsetYPos;
//
//                            if (offsetYPos <= 0) {
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        private void propagateBlockLight(BlockPos pos, int light, boolean source) {
//            if (!world.isValid(pos)) return;
//
//            int opacity = getBlockLightOpacity(pos);
//            if (opacity == 0 && light != 15) {
//                opacity = 1;
//            }
//
//            if (!source){
//                light -= opacity;
//            }
//
//            if (light <= 0) {
//                return;
//            }
//
//            Integer current = blockLight.get(pos);
//
//            if (current != null && current >= light) {
//                return;
//            }
//
//            blockLight.put(pos, light);
//            setBlockLight(pos, light);
//
//            for (EnumFacing facing : EnumFacing.values()) {
//                BlockPos adjacent = pos.offset(facing);
//                propagateBlockLight(adjacent, light - 1, false);
//            }
//        }
//
//        private int getBlockLightOpacity(Chunk chunk, int x, int y, int z) {
//            IBlockState state = chunk.getBlockState(x, y, z);
//            return !chunk.loaded ? state.getLightOpacity() : state.getLightOpacity(world, new BlockPos(chunk.x << 4 | x & 15, y, chunk.z << 4 | z & 15));
//        }
//
//        private int getBlockLightOpacity(BlockPos pos) {
//            Chunk chunk = world.getChunkFromBlockCoords(pos);
//            IBlockState state = chunk.getBlockState(pos);
//            return !chunk.loaded ? state.getLightOpacity() : state.getLightOpacity(world, pos);
//        }
//
//
//        private int getBlockLightLevel(Chunk chunk, int x, int y, int z) {
//            IBlockState state = chunk.getBlockState(x, y, z);
//            return !chunk.loaded ? state.getLightValue() : state.getLightValue(world, new BlockPos(chunk.x << 4 | x & 15, y, chunk.z << 4 | z & 15));
//        }
//
//        private void setBlockLight(BlockPos pos, int light) {
//            ExtendedBlockStorage storage = getBlockStorage(pos);
//            if (storage != null) {
//                storage.setBlockLight(pos.getX() & 0xf, pos.getY() & 0xf, pos.getZ() & 0xf, light);
//            }
//        }
//
////        public void setBlockLight(BlockPos pos, int value) {
////            if (!world.isValid(pos)) return;
////            Chunk chunk = world.getChunkFromBlockCoords(pos);
////            int i = pos.getX() & 15;
////            int j = pos.getY();
////            int k = pos.getZ() & 15;
////            ExtendedBlockStorage extendedblockstorage = chunk.storageArrays[j >> 4];
////
////            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
////                return;
////                extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
////                chunk.storageArrays[j >> 4] = extendedblockstorage;
////                chunk.generateSkylightMap();
////            }
////
////            extendedblockstorage.setBlockLight(i, j & 15, k, value);
////        }
//
//        private void changeBlock(BlockPos pos, IBlockState state) {
//            Chunk chunk = getChunk(pos);
//            IBlockState oldState = chunk.getBlockState(pos);
//
//            if (oldState.getBlock().hasTileEntity(oldState)) {
//                world.setBlockToAir(pos);
//
//                PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
//                if (playerChunkMap != null) {
//                    PlayerChunkMapEntry watcher = playerChunkMap.getEntry(pos.getX() >> 4, pos.getZ() >> 4);
//                    if (watcher != null) {
//                        watcher.sendPacket(new SPacketBlockChange(world, pos));
//                    }
//                }
//
//                return;
//            }
//
//            ExtendedBlockStorage storage = getBlockStorage(pos);
//            if (storage != null) {
//                storage.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
//            }
//            setChunkModified(pos);
//        }
//
//        private ExtendedBlockStorage getBlockStorage(BlockPos pos) {
//            Chunk chunk = getChunk(pos);
//            ExtendedBlockStorage storage = chunk.storageArrays[pos.getY() >> 4];
//            if (storage == null) {
//                storage = new ExtendedBlockStorage(pos.getY() >> 4 << 4, world.provider.hasSkyLight());
//                chunk.storageArrays[pos.getY() >> 4] = storage;
//            }
//            return storage;
//        }
//
//        private HashMap<ChunkPos, Chunk> chunkCache = new HashMap<>();
//
//        private Chunk getChunk(BlockPos pos) {
//            ChunkPos cp = new ChunkPos(pos);
//            if (!chunkCache.containsKey(cp)) {
//                chunkCache.put(cp, world.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4));
//            }
//
//            return chunkCache.get(cp);
//        }
//
//        public void setChunkModified(BlockPos blockPos) {
//            Chunk chunk = getChunk(blockPos);
//            setChunkModified(chunk);
//        }
//
//        public void setChunkModified(Chunk chunk) {
//            modifiedChunks.add(chunk);
//        }
//
//        public void sendChanges() {
//            PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
//            if (playerChunkMap == null) {
//                return;
//            }
//
//            for (Chunk chunk : modifiedChunks) {
//                chunk.setModified(true);
////                chunk.runPreCalculation(); //This is where this falls short. It can calculate basic sky lighting for blocks exposed to the sky but thats it.
//
//                PlayerChunkMapEntry watcher = playerChunkMap.getEntry(chunk.x, chunk.z);
//                if (watcher != null) {//TODO Change chunk mask to only the sub chunks changed.
//                    watcher.sendPacket(new SPacketChunkData(chunk, 65535));
//                }
//            }
//
//            modifiedChunks.clear();
//        }
//    }

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
            catch (IllegalArgumentException ignored) {
            }

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
