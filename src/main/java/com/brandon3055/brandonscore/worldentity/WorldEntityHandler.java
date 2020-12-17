package com.brandon3055.brandonscore.worldentity;

import codechicken.lib.util.SneakyUtils;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

/**
 * Created by brandon3055 on 15/12/20
 */
@Mod.EventBusSubscriber(modid = BrandonsCore.MODID, bus = FORGE)
public class WorldEntityHandler {
    public static ForgeRegistry<WorldEntityType<?>> REGISTRY;
    private static final Map<UUID, WorldEntity> ID_ENTITY_MAP = new HashMap<>();
    private static final Map<RegistryKey<World>, List<WorldEntity>> WORLD_ENTITY_MAP = new HashMap<>();
    private static final Map<RegistryKey<World>, List<ITickableWorldEntity>> TICKING_ENTITY_MAP = new HashMap<>();
    private static final Map<RegistryKey<World>, List<WorldEntity>> ADDED_WORLD_ENTITIES = new HashMap<>();

    public static void createRegistry(RegistryEvent.NewRegistry event) {
        REGISTRY = (ForgeRegistry<WorldEntityType<?>>) new RegistryBuilder<>()
                .setName(new ResourceLocation(BrandonsCore.MODID, "world_entity"))
                .setType(SneakyUtils.unsafeCast(WorldEntityType.class))
                .disableSaving()
                .disableSync()
                .create();
    }

    @SubscribeEvent
    public static void worldLoad(WorldEvent.Load event) {
        if (!(event.getWorld() instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) event.getWorld();
        RegistryKey<World> key = world.getDimensionKey();

        //If the world was unloaded properly then this should always be null. But better safe
        List<WorldEntity> oldEntities = WORLD_ENTITY_MAP.remove(key);
        TICKING_ENTITY_MAP.remove(key);
        if (oldEntities != null) {
            LogHelperBC.warn("Detected stray world entities for world " + key.toString() + ". These should have been removed when the world unloaded.");
            oldEntities.forEach(e -> ID_ENTITY_MAP.remove(e.getUniqueID()));
            WORLD_ENTITY_MAP.remove(key);
        }

        WorldEntitySaveData data = world.getSavedData().getOrCreate(WorldEntitySaveData::new, WorldEntitySaveData.ID);
        data.setSaveCallback(() -> handleSave(data, key));
        for (WorldEntity entity : data.getEntities()) {
            addWorldEntity(world, entity);
        }
    }

    private static void handleSave(WorldEntitySaveData data, RegistryKey<World> key) {
        List<WorldEntity> worldEntities = WORLD_ENTITY_MAP.get(key);
        data.updateEntities(worldEntities);
    }

    @SubscribeEvent
    public static void worldUnload(WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) event.getWorld();
        RegistryKey<World> key = world.getDimensionKey();
        TICKING_ENTITY_MAP.remove(key);
        List<WorldEntity> removed = WORLD_ENTITY_MAP.get(key);
        if (removed != null) {
            removed.forEach(e -> ID_ENTITY_MAP.remove(e.getUniqueID()));
        }
    }

    public static void serverStopped() {
        WORLD_ENTITY_MAP.clear();
        TICKING_ENTITY_MAP.clear();
        ID_ENTITY_MAP.clear();
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (!(event.world instanceof ServerWorld)) return;
        World world = event.world;
        RegistryKey<World> key = world.getDimensionKey();

        //Clear dead entities
        ID_ENTITY_MAP.entrySet().removeIf(entry -> {
            WorldEntity entity = entry.getValue();
            if (entity.isRemoved()) {
                RegistryKey<World> removeKey = entity.world.getDimensionKey();
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

    public static void addWorldEntity(World world, WorldEntity entity) {
        if (!(world instanceof ServerWorld)) return;
        RegistryKey<World> key = world.getDimensionKey();
        ADDED_WORLD_ENTITIES.computeIfAbsent(key, e -> new ArrayList<>()).add(entity);
        entity.setWorld(world);
    }

    @Nullable
    public static WorldEntity getWorldEntity(World world, UUID id) {
        WorldEntity entity = ID_ENTITY_MAP.get(id);
        if (entity == null && ADDED_WORLD_ENTITIES.containsKey(world.getDimensionKey())) {
            entity = ADDED_WORLD_ENTITIES.get(world.getDimensionKey()).stream().filter(e -> e.getUniqueID().equals(id)).findAny().orElse(null);
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
        RegistryKey<World> key = entity.getWorld().getDimensionKey();
        if (ADDED_WORLD_ENTITIES.containsKey(key)) {
            ADDED_WORLD_ENTITIES.get(key).remove(entity);
        }
    }
}
