package com.brandon3055.brandonscore.worldentity;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.collect.ImmutableList;
import net.covers1624.quack.util.CrashLock;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

/**
 * Created by brandon3055 on 15/12/20
 */
public class WorldEntityHandler {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static IForgeRegistry<WorldEntityType<?>> REGISTRY;
    private static final Map<UUID, WorldEntity> ID_ENTITY_MAP = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<WorldEntity>> WORLD_ENTITY_MAP = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<ITickableWorldEntity>> TICKING_ENTITY_MAP = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<WorldEntity>> ADDED_WORLD_ENTITIES = new HashMap<>();

    public static void createRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<WorldEntityType<?>>()
                        .setName(new ResourceLocation(BrandonsCore.MODID, "world_entity"))
                        //.setType(SneakyUtils.unsafeCast(WorldEntityType.class)) // TODO [FoxMcloud5655]: This is surely incorrect.
                        .disableSaving()
                        .disableSync(),
                ts -> REGISTRY = ts);
    }

    public static void init() {
        LOCK.lock();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(WorldEntityHandler::createRegistry);

        MinecraftForge.EVENT_BUS.addListener(WorldEntityHandler::worldLoad);
        MinecraftForge.EVENT_BUS.addListener(WorldEntityHandler::worldUnload);
        MinecraftForge.EVENT_BUS.addListener(WorldEntityHandler::onServerStop);
        MinecraftForge.EVENT_BUS.addListener(WorldEntityHandler::worldTick);
    }

    public static void worldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        ServerLevel world = (ServerLevel) event.getLevel();
        ResourceKey<Level> key = world.dimension();

        //If the world was unloaded properly then this should always be null. But better safe
        List<WorldEntity> oldEntities = WORLD_ENTITY_MAP.remove(key);
        TICKING_ENTITY_MAP.remove(key);
        if (oldEntities != null) {
            LogHelperBC.warn("Detected stray world entities for world " + key.toString() + ". These should have been removed when the world unloaded.");
            oldEntities.forEach(e -> ID_ENTITY_MAP.remove(e.getUniqueID()));
            WORLD_ENTITY_MAP.remove(key);
        }

        WorldEntitySaveData data = world.getDataStorage().computeIfAbsent(WorldEntitySaveData::load,WorldEntitySaveData::new, WorldEntitySaveData.FILE_ID);
        data.setSaveCallback(() -> handleSave(data, key));
        for (WorldEntity entity : data.getEntities()) {
            addWorldEntity(world, entity);
        }
    }

    private static void handleSave(WorldEntitySaveData data, ResourceKey<Level> key) {
        List<WorldEntity> worldEntities = WORLD_ENTITY_MAP.get(key);
        data.updateEntities(worldEntities);
    }

    public static void worldUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        ServerLevel world = (ServerLevel) event.getLevel();
        ResourceKey<Level> key = world.dimension();
        TICKING_ENTITY_MAP.remove(key);
        List<WorldEntity> removed = WORLD_ENTITY_MAP.get(key);
        if (removed != null) {
            removed.forEach(e -> ID_ENTITY_MAP.remove(e.getUniqueID()));
        }
    }

    public static void onServerStop(ServerStoppedEvent event) {
        WORLD_ENTITY_MAP.clear();
        TICKING_ENTITY_MAP.clear();
        ID_ENTITY_MAP.clear();
    }

    public static void worldTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel)) return;
        Level world = event.level;
        ResourceKey<Level> key = world.dimension();

        //Clear dead entities
        ID_ENTITY_MAP.entrySet().removeIf(entry -> {
            WorldEntity entity = entry.getValue();
            if (entity.isRemoved()) {
                ResourceKey<Level> removeKey = entity.world.dimension();
                if (WORLD_ENTITY_MAP.containsKey(removeKey)) {
                    WORLD_ENTITY_MAP.get(removeKey).remove(entity);
                }
                if (entity instanceof ITickableWorldEntity && TICKING_ENTITY_MAP.containsKey(removeKey)) {
                    TICKING_ENTITY_MAP.get(removeKey).remove(entity);
                }
                return true;
            }
            return false;
        });

        //Tick Tickable Entities
        if (TICKING_ENTITY_MAP.containsKey(key)) {
            TICKING_ENTITY_MAP.get(key).forEach(e -> {
                if (e.getPhase() == event.phase) {
                    e.tick();
                }
            });
        }

        //Add New Entities
        if (event.phase == TickEvent.Phase.END && ADDED_WORLD_ENTITIES.containsKey(key)) {
            List<WorldEntity> newEntities = ADDED_WORLD_ENTITIES.get(key);
            if (!newEntities.isEmpty()) {
                List<WorldEntity> worldEntities = WORLD_ENTITY_MAP.computeIfAbsent(key, e -> new ArrayList<>());
                List<ITickableWorldEntity> worldTickingEntities = TICKING_ENTITY_MAP.computeIfAbsent(key, e -> new ArrayList<>());
                for (WorldEntity entity : newEntities) {
                    if (entity.isRemoved()) {
                        worldEntities.remove(entity);
                        if (entity instanceof ITickableWorldEntity) {
                            worldTickingEntities.remove(entity);
                        }
                        continue;
                    }
                    ID_ENTITY_MAP.put(entity.getUniqueID(), entity);
                    if (!worldEntities.contains(entity)) {
                        worldEntities.add(entity);
                    }
                    if (entity instanceof ITickableWorldEntity && !worldTickingEntities.contains(entity)) {
                        worldTickingEntities.add((ITickableWorldEntity) entity);
                    }
                    if (entity.getWorld() != world) {
                        entity.setWorld(world);
                    }
                    entity.onLoad();
                }
            }
            ADDED_WORLD_ENTITIES.remove(key);
        }
    }

    public static void addWorldEntity(Level world, WorldEntity entity) {
        if (!(world instanceof ServerLevel)) return;
        ResourceKey<Level> key = world.dimension();
        ADDED_WORLD_ENTITIES.computeIfAbsent(key, e -> new ArrayList<>()).add(entity);
        entity.setWorld(world);
    }

    @Nullable
    public static WorldEntity getWorldEntity(Level world, UUID id) {
        WorldEntity entity = ID_ENTITY_MAP.get(id);
        if (entity == null && ADDED_WORLD_ENTITIES.containsKey(world.dimension())) {
            entity = ADDED_WORLD_ENTITIES.get(world.dimension()).stream().filter(e -> e.getUniqueID().equals(id)).findAny().orElse(null);
        }
        return entity;
    }

    public static List<WorldEntity> getWorldEntities() {
        if (ADDED_WORLD_ENTITIES.isEmpty()) {
            return ImmutableList.copyOf(ID_ENTITY_MAP.values());
        }
        Set<WorldEntity> set = new HashSet<>();
        set.addAll(ID_ENTITY_MAP.values());
        set.addAll(ADDED_WORLD_ENTITIES.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));
        return ImmutableList.copyOf(set);
    }

    protected static void onEntityRemove(WorldEntity entity) {
        ResourceKey<Level> key = entity.getWorld().dimension();
        if (ADDED_WORLD_ENTITIES.containsKey(key)) {
            ADDED_WORLD_ENTITIES.get(key).remove(entity);
        }
    }
}
