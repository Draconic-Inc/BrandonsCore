package com.brandon3055.brandonscore.multiblock;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.Quat;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.math.Quaternion;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 26/06/2022
 */
public class MultiBlockManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger(MultiBlockManager.class);
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    /**
     * These are the definitions loaded from disk locally, So local in this case could mean the local client or the dedicated server.
     */
    private static final Map<ResourceLocation, MultiBlockDefinition> LOCAL_DEFINITIONS = new HashMap<>();
    /**
     * These are definitions that have been sent from the server to the client. These override the clients local definitions.
     * So these will only ever be populated client side when the client is connected to a server.
     * This map will be cleared when the client disconnects.
     */
    private static final Map<ResourceLocation, MultiBlockDefinition> SERVER_DEFINITIONS = new HashMap<>();

    public MultiBlockManager() {
        super(GSON, "multiblocks");
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(MultiBlockManager::addReloadListeners);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(MultiBlockManager::onDisconnectFromServer));
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> MinecraftForge.EVENT_BUS.addListener(MultiBlockManager::onSendDataToClient));
    }

    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MultiBlockManager());
    }

    private static void onSendDataToClient(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            BCoreNetwork.sendMultiBlockDefinitions(event.getPlayer(), LOCAL_DEFINITIONS);
        } else {
            event.getPlayerList().getPlayers().forEach(player -> BCoreNetwork.sendMultiBlockDefinitions(player, LOCAL_DEFINITIONS));
        }
    }

    public static void receiveDefinitionsFromServer(PacketCustom packet) {
        int count = packet.readVarInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = packet.readResourceLocation();
            String jsonString = packet.readString();
            SERVER_DEFINITIONS.put(id, new MultiBlockDefinition(id, JsonParser.parseString(jsonString)));
        }
    }

    private static void onDisconnectFromServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        SERVER_DEFINITIONS.clear();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller filter) {
        LOCAL_DEFINITIONS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();
            try {
                LOCAL_DEFINITIONS.put(id, new MultiBlockDefinition(id, json));
            } catch (Throwable e) {
                LOGGER.error("An exception occurred while loading Multi-Block Definition from json.");
                e.printStackTrace();
            }
        }
    }

    public static Map<ResourceLocation, MultiBlockDefinition> getDefinitions() {
        if (SERVER_DEFINITIONS.isEmpty()) {
            return ImmutableMap.copyOf(LOCAL_DEFINITIONS);
        }
        return ImmutableMap.copyOf(SERVER_DEFINITIONS);
    }

    public static ImmutableList<ResourceLocation> getRegisteredIds() {
        return ImmutableList.copyOf(getDefinitions().keySet());
    }

    @Nullable
    public static MultiBlockDefinition getDefinition(ResourceLocation id) {
        return getDefinitions().getOrDefault(id, null);
    }

    public static int placeCommand(ServerLevel level, BlockPos placePos, ResourceLocation id, Vec3 rotation) {
        MultiBlockDefinition definition = getDefinition(id);

        if (definition != null) {
            for (Map.Entry<BlockPos, MultiBlockPart> entry : definition.getBlocksWithRotation(new Rotation(new Quat(new Quaternion((float) rotation.x, (float) rotation.y, (float) rotation.z, true)))).entrySet()) {
//            for (Map.Entry<BlockPos, MultiBlockPart> entry : definition.getBlocksWithRotation(new Rotation(1, new Vector3(rotation))).entrySet()) {
                BlockPos offset = entry.getKey();
                List<Block> blocks = Lists.newArrayList(entry.getValue().validBlocks());
                if (!blocks.isEmpty()) {
                    BlockPos pos = placePos.offset(offset);
                    level.setBlockAndUpdate(pos, blocks.get(0).defaultBlockState());
                }
            }

            return 0;
        }

        return 1;
    }
}
