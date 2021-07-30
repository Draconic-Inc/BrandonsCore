package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 6/12/2016.
 * <p>
 * This is a universal class for handling teleportation. Simply tell it where to send an entity and it just works!
 * Also has support for teleporting mounts.
 */
public class TeleportUtils {

    /**
     * Universal method for teleporting entities of all shapes and sizes!
     * This method will teleport an entity and any entity it is riding or that are ring it recursively. If riding the riding entity will be re mounted on the other side.
     * <p>
     * Note: When teleporting riding entities it is the rider that must be teleported and the mount will follow automatically.
     * As long as you teleport the rider you should not need wo worry about the mount.
     *
     * @return the entity. This may be a new instance so be sure to keep that in mind.
     */
    public static Entity teleportEntity(Entity entity, int dimension, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity == null || entity.world.isRemote) {
            return entity;
        }

        MinecraftServer server = entity.getServer();
        int sourceDim = entity.world.provider.getDimension();

        if (!entity.isBeingRidden() && !entity.isRiding()) {
            return handleEntityTeleport(entity, server, sourceDim, dimension, xCoord, yCoord, zCoord, yaw, pitch);
        }

        Entity rootEntity = entity.getLowestRidingEntity();
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

    /**
     * Convenience method that does not require pitch and yaw.
     */
    public static Entity teleportEntity(Entity entity, int dimension, double xCoord, double yCoord, double zCoord) {
        return teleportEntity(entity, dimension, xCoord, yCoord, zCoord, entity.rotationYaw, entity.rotationPitch);
    }

    /**
     * This is the base teleport method that figures out how to handle the teleport and makes it happen!
     */
    private static Entity handleEntityTeleport(Entity entity, MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity == null || entity.world.isRemote) {
            return entity;
        }

        boolean interDimensional = sourceDim != targetDim;

        if (interDimensional && !ForgeHooks.onTravelToDimension(entity, targetDim)) {
            return entity;
        }

        if (interDimensional) {
            if (entity instanceof EntityPlayerMP) {
                return teleportPlayerInterdimentional((EntityPlayerMP) entity, server, sourceDim, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            }
            else {
                return teleportEntityInterdimentional(entity, server, sourceDim, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            }
        }
        else {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                player.connection.setPlayerLocation(xCoord, yCoord, zCoord, yaw, pitch);
                player.setRotationYawHead(yaw);
            }
            else {
                entity.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
                entity.setRotationYawHead(yaw);
            }
        }

        return entity;
    }

    private static Entity teleportEntityInterdimentional(Entity entity, MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (!entity.isEntityAlive()) {
            return null;
        }
        entity = entity.changeDimension(targetDim);
        if (entity != null) {
            entity.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
        }
        return entity;
    }

    /**
     * This is the black magic responsible for teleporting players between dimensions!
     */
    private static EntityPlayer teleportPlayerInterdimentional(EntityPlayerMP player, MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (!player.isEntityAlive()) {
            return player;
        }
        WorldServer worldServer = server.getWorld(targetDim);
        server.getWorld(targetDim).getMinecraftServer().getPlayerList().transferPlayerToDimension(player, targetDim, new BrandonTeleporter(worldServer, xCoord, yCoord, zCoord));
        // Logically, player cannot be null. We already called a method on the instance, and we are not reassigning to the instance.
        // if(player != null) {
            player.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
        // }
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
            if (entity.isRiding()) {
                offsetX = entity.posX - entity.getRidingEntity().posX;
                offsetY = entity.posY - entity.getRidingEntity().posY;
                offsetZ = entity.posZ - entity.getRidingEntity().posZ;
            }
            for (Entity passenger : entity.getPassengers()) {
                passengers.add(new PassengerHelper(passenger));
            }
        }

        /**
         * Recursively teleports the entity and all of its passengers after dismounting them.
         * @param server The minecraft server.
         * @param sourceDim The source dimension.
         * @param targetDim The target dimension.
         * @param xCoord The target x position.
         * @param yCoord The target y position.
         * @param zCoord The target z position.
         * @param yaw The target yaw.
         * @param pitch The target pitch.
         */
        public void teleport(MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
            entity.removePassengers();
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
            if (entity.isRiding()) {
                entity.setLocationAndAngles(entity.posX + offsetX, entity.posY + offsetY, entity.posZ + offsetZ, entity.rotationYaw, entity.rotationPitch);
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
            if (entity instanceof EntityPlayerMP) {
                updateClient((EntityPlayerMP) entity);
            }
            for (PassengerHelper passenger : passengers) {
                passenger.updateClients();
            }
        }

        /**
         * This is the method that is responsible for actually sending the update to each client.
         * @param playerMP The Player.
         */
        private void updateClient(EntityPlayerMP playerMP) {
            if (entity.isBeingRidden()) {
                playerMP.connection.sendPacket(new SPacketSetPassengers(entity));
            }
            for (PassengerHelper passenger : passengers) {
                passenger.updateClients();
            }
        }

        /**
         * This method returns the helper for a specific entity in the stack.
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
    private static class BrandonTeleporter extends Teleporter {
        private double x;
        private double y;
        private double z;


        public BrandonTeleporter(WorldServer world, double x, double y, double z) {
            super(world);
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public void placeInPortal(Entity pEntity, float rotationYaw) {
            pEntity.setPosition(this.x, this.y, this.z);
            pEntity.motionX = 0.0f;
            pEntity.motionY = 0.0f;
            pEntity.motionZ = 0.0f;
        }
    }
}
