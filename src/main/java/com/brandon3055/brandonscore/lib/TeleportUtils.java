package com.brandon3055.brandonscore.lib;

import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.common.ForgeHooks;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 6/12/2016.
 * <p>
 * This is a universal class for handling teleportation. Simply tell it where to send an entity and it just works!
 * Also has support for teleporting mounts.
 */
//TODO Dig through the vanilla teleport code again and re write this.
public class TeleportUtils {

    public static Entity teleportEntity(Entity entity, Entity destination) {
        return teleportEntity(entity, destination.level.dimension(), destination.getX(), destination.getY(), destination.zOld, destination.yRot, destination.xRot);
    }

    /**
     * Universal method for teleporting entities of all shapes and sizes!
     * This method will teleport an entity and any entity it is riding or that are ring it recursively. If riding the riding entity will be re mounted on the other side.
     * <p>
     * Note: When teleporting riding entities it is the rider that must be teleported and the mount will follow automatically.
     * As long as you teleport the rider you should not need wo worry about the mount.
     *
     * @return the entity. This may be a new instance so be sure to keep that in mind.
     */
    public static Entity teleportEntity(Entity entity, RegistryKey<World> dimension, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity == null || entity.level.isClientSide) {
            return entity;
        }

        MinecraftServer server = entity.getServer();
        RegistryKey<World> sourceDim = entity.level.dimension();

        if (!entity.isVehicle() && !entity.isPassenger()) {
            return handleEntityTeleport(entity, server, sourceDim, dimension, xCoord, yCoord, zCoord, yaw, pitch);
        }

        Entity rootEntity = entity.getRootVehicle();
        PassengerHelper passengerHelper = new PassengerHelper(rootEntity);
        PassengerHelper rider = passengerHelper.getPassenger(entity);
        if (rider == null) {
            LogHelperBC.error("RiddenEntity: This error should not be possible");
            return entity;
        }
        passengerHelper.teleport(server, sourceDim, dimension, xCoord, yCoord, zCoord, yaw, pitch);
        passengerHelper.remountRiders();
        passengerHelper.updateClients();

        return rider.entity;
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> dimension, Vector3 pos, float yaw, float pitch) {
        return teleportEntity(entity, dimension, pos.x, pos.y, pos.z, yaw, pitch);
    }

    /**
     * Convenience method that does not require pitch and yaw.
     */
    public static Entity teleportEntity(Entity entity, RegistryKey<World> dimension, double xCoord, double yCoord, double zCoord) {
        return teleportEntity(entity, dimension, xCoord, yCoord, zCoord, entity.yRot, entity.xRot);
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> dimension, Vector3 pos) {
        return teleportEntity(entity, dimension, pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
    }

    /**
     * This is the base teleport method that figures out how to handle the teleport and makes it happen!
     */
    private static Entity handleEntityTeleport(Entity entity, MinecraftServer server, RegistryKey<World> sourceDim, RegistryKey<World> targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity == null || entity.level.isClientSide || targetDim == null) {
            return entity;
        }

        boolean interDimensional = sourceDim != targetDim;

        if (interDimensional && !ForgeHooks.onTravelToDimension(entity, sourceDim)) {
            return entity;
        }

        if (interDimensional) {
            if (entity instanceof ServerPlayerEntity) {
                return teleportPlayerInterdimentional((ServerPlayerEntity) entity, server, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            } else {
                return teleportEntityInterdimentional(entity, server, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            }
        } else {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.connection.teleport(xCoord, yCoord, zCoord, yaw, pitch);
                player.setYHeadRot(yaw);
            } else {
                entity.moveTo(xCoord, yCoord, zCoord, yaw, pitch);
                entity.setYHeadRot(yaw);
            }
        }

        return entity;
    }

    private static Entity teleportEntityInterdimentional(Entity entity, MinecraftServer server, RegistryKey<World> targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        ServerWorld targetWorld = server.getLevel(targetDim);
        if (!entity.isAlive() || targetWorld == null) {
            return null;
        }

        Entity movedEntity = entity.changeDimension(targetWorld);
        if (movedEntity != null) {
            movedEntity.moveTo(xCoord, yCoord, zCoord, yaw, pitch);
            return movedEntity;
        }

        entity.unRide();
        movedEntity = entity.getType().create(targetWorld);
        if (movedEntity != null) {
            movedEntity.restoreFrom(entity);
            movedEntity.moveTo(xCoord, yCoord, zCoord, yaw, pitch);
            targetWorld.addFromAnotherDimension(movedEntity);
            entity.remove(false);
            ((ServerWorld) entity.level).resetEmptyTime();
            targetWorld.resetEmptyTime();
            return movedEntity;
        }


        return entity;

//
//        DimensionType sourceType =DimensionType.getById(sourceDim);
//        DimensionType targetType =DimensionType.getById(targetDim);
//        if (sourceType == null || targetType == null) {
//            return null;
//        }
//
//        ServerWorld sourceWorld = server.getWorld(sourceType);
//        ServerWorld targetWorld = server.getWorld(targetType);
//
//        if (entity.isAlive() && entity instanceof ContainerMinecartEntity) {
//            ((ContainerMinecartEntity) entity).dropContentsWhenDead(false);
//        }
//
//        entity.dimension = targetType;
//
//        sourceWorld.removeEntity(entity);
//        entity.revive();
//        entity.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
//        sourceWorld.updateEntity(entity);
//
//        Entity newEntity = EntityList.newEntity(entity.getClass(), targetWorld);
//        if (newEntity != null) {
//            newEntity.copyDataFromOld(entity);
//            newEntity.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
//            boolean flag = newEntity.forceSpawn;
//            newEntity.forceSpawn = true;
//            targetWorld.spawnEntity(newEntity);
//            newEntity.forceSpawn = flag;
//            targetWorld.updateEntityWithOptionalForce(newEntity, false);
//        }
//
//        entity.isDead = true;
//        sourceWorld.resetUpdateEntityTick();
//        targetWorld.resetUpdateEntityTick();
//
//        return newEntity;
    }

    /**
     * This is the black magic responsible for teleporting players between dimensions!
     */
    private static PlayerEntity teleportPlayerInterdimentional(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        ServerWorld originWorld = player.getLevel();
        ServerWorld targetWorld = server.getLevel(targetDim);
        if (!player.isAlive() || targetWorld == null) {
            return player;
        }
//        if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, targetWorld.dimension())) return player;
        player.isChangingDimension = true;

//        IWorldInfo worldInfo = targetWorld.getLevelData();
//        player.connection.send(new SRespawnPacket(targetWorld.dimensionType(), targetWorld.dimension(), BiomeManager.obfuscateSeed(targetWorld.getSeed()), player.gameMode.getGameModeForPlayer(), player.gameMode.getPreviousGameModeForPlayer(), targetWorld.isDebug(), targetWorld.isFlat(), true));
//        player.connection.send(new SServerDifficultyPacket(worldInfo.getDifficulty(), worldInfo.isDifficultyLocked()));
//        PlayerList playerlist = player.server.getPlayerList();
//        playerlist.sendPlayerPermissionLevel(player);
//        originWorld.removeEntity(player, true); //Forge: the player entity is moved to the new world, NOT cloned. So keep the data alive with no matching invalidate call.
//        player.revive();
//
//        player.setLevel(targetWorld);
//        targetWorld.addDuringPortalTeleport(player);
//        player.absMoveTo(xCoord, yCoord, zCoord, yaw, pitch);
//        player.setYHeadRot(yaw);
//        player.connection.resetPosition();
//
//        player.gameMode.setLevel(targetWorld);
//        player.connection.send(new SPlayerAbilitiesPacket(player.abilities));
//        playerlist.sendLevelInfo(player, targetWorld);
//        playerlist.sendAllPlayerInfo(player);
//
//        for (EffectInstance effectinstance : player.getActiveEffects()) {
//            player.connection.send(new SPlayEntityEffectPacket(player.getId(), effectinstance));
//        }

        player.teleportTo(targetWorld, xCoord, yCoord, zCoord, yaw, pitch);

        player.lastSentExp = -1;
        player.lastSentHealth = -1.0F;
        player.lastSentFood = -1;

        //Fixes issue where creative flight is reset client side after teleport
        player.onUpdateAbilities();
//        net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerChangedDimensionEvent(player, originWorld.dimension(), targetWorld.dimension());

        return player;
    }

    public static Entity getHighestRidingEntity(Entity mount) {
        Entity entity;

        for (entity = mount; entity.getPassengers().size() > 0; entity = entity.getPassengers().get(0)) ;

        return entity;
    }

    private static class PassengerHelper {
        public Entity entity;
        public LinkedList<PassengerHelper> passengers = new LinkedList<>();
        public double offsetX, offsetY, offsetZ;

        /**
         * Creates a new passenger helper for the given entity and recursively adds all of the entities passengers.
         *
         * @param entity The root entity. If you have multiple stacked entities this would be the one at the bottom of the stack.
         */
        public PassengerHelper(Entity entity) {
            this.entity = entity;
            if (entity.isPassenger()) {
                offsetX = entity.getX() - entity.getVehicle().getX();
                offsetY = entity.getY() - entity.getVehicle().getY();
                offsetZ = entity.getZ() - entity.getVehicle().getZ();
            }
            for (Entity passenger : entity.getPassengers()) {
                passengers.add(new PassengerHelper(passenger));
            }
        }

        /**
         * Recursively teleports the entity and all of its passengers after dismounting them.
         *
         * @param server    The minecraft server.
         * @param sourceDim The source dimension.
         * @param targetDim The target dimension.
         * @param xCoord    The target x position.
         * @param yCoord    The target y position.
         * @param zCoord    The target z position.
         * @param yaw       The target yaw.
         * @param pitch     The target pitch.
         */
        public void teleport(MinecraftServer server, RegistryKey<World> sourceDim, RegistryKey<World> targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
            entity.ejectPassengers();
            entity = handleEntityTeleport(entity, server, sourceDim, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            for (PassengerHelper passenger : passengers) {
                passenger.teleport(server, sourceDim, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            }
        }

        /**
         * Recursively remounts all of this entities riders and offsets their position relative to their position before teleporting.
         */
        public void remountRiders() {
            //If the mount was dead before teleporting then entity will be null.
            if (entity == null) {
                return;
            }
            if (entity.isPassenger()) {
                entity.moveTo(entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ, entity.yRot, entity.xRot);
            }
            for (PassengerHelper passenger : passengers) {
                passenger.entity.startRiding(entity, true);
                passenger.remountRiders();
            }
        }

        /**
         * This method sends update packets to any players that were teleported with the entity stack.
         */
        public void updateClients() {
            if (entity instanceof ServerPlayerEntity) {
                updateClient((ServerPlayerEntity) entity);
            }
            for (PassengerHelper passenger : passengers) {
                passenger.updateClients();
            }
        }

        /**
         * This is the method that is responsible for actually sending the update to each client.
         *
         * @param playerMP The Player.
         */
        private void updateClient(ServerPlayerEntity playerMP) {
            if (entity.isVehicle()) {
                playerMP.connection.send(new SSetPassengersPacket(entity));
            }
            for (PassengerHelper passenger : passengers) {
                passenger.updateClients();
            }
        }

        /**
         * This method returns the helper for a specific entity in the stack.
         *
         * @param passenger The passenger you are looking for.
         * @return The passenger helper for the specified passenger.
         */
        public PassengerHelper getPassenger(Entity passenger) {
            if (this.entity == passenger) {
                return this;
            }

            for (PassengerHelper rider : passengers) {
                PassengerHelper re = rider.getPassenger(passenger);
                if (re != null) {
                    return re;
                }
            }

            return null;
        }
    }
}
