package com.brandon3055.brandonscore.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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

/**
 * Created by brandon3055 on 6/12/2016.
 *
 * This is a universal class for handling teleportation. Simply tell it where to send an entity and it just works!
 * Also has support for teleporting mounts.
 */
public class TeleportUtils {

    /**
     * Universal method for teleporting entities of all shapes and sizes!
     * This method will teleport an entity and any entity it is riding recursively. If riding the riding entity will be re mounted on the other side.
     *
     * Note: When teleporting riding entities it is the rider that must be teleported and the mount will follow automatically.
     * As long as you teleport the rider you should not need wo worry about the mount.
     *
     * @return the entity. This may be a new instance so be sure to keep that in mind.
     */
    public static Entity teleportEntity(Entity entity, int dimension, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity == null || entity.worldObj.isRemote || entity.isBeingRidden()) {
            return entity;
        }

        MinecraftServer server = entity.getServer();
        int sourceDim = entity.worldObj.provider.getDimension();

        Entity mount = null;

        if (entity.isRiding()) {
            mount = entity.getRidingEntity();
            double mountXOffset = entity.posX - mount.posX;
            double mountYOffset = entity.posY - mount.posY;
            double mountZOffset = entity.posZ - mount.posZ;
            entity.dismountRidingEntity();
            mount = teleportEntity(mount, dimension, xCoord, yCoord, zCoord, yaw, pitch);
            xCoord += mountXOffset;
            yCoord += mountYOffset;
            zCoord += mountZOffset;
        }

        entity = handleEntityTeleport(entity, server, sourceDim, dimension, xCoord, yCoord, zCoord, yaw, pitch);
        entity.fallDistance = 0;

        if (mount != null) {
            entity.startRiding(mount);
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).connection.sendPacket(new SPacketSetPassengers(mount));
            }
            entity.startRiding(mount);
            mount.setPosition(mount.posX, mount.posY, mount.posZ);
        }

        return entity;
    }

    /**
     * Convenience method that dose not require pitch and yaw.
     */
    public static Entity teleportEntity(Entity entity, int dimension, double xCoord, double yCoord, double zCoord) {
        return teleportEntity(entity, dimension, xCoord, yCoord, zCoord, entity.rotationYaw, entity.rotationPitch);
    }

    /**
     * This is the base teleport method that figures out how to handle the teleport and makes it happen!
     */
    private static Entity handleEntityTeleport(Entity entity, MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity == null || entity.worldObj.isRemote) {
            return entity;
        }

        boolean interDimensional = sourceDim != targetDim;

        if (interDimensional && !net.minecraftforge.common.ForgeHooks.onTravelToDimension(entity, targetDim)) {
            return entity;
        }

        if (interDimensional) {
            if (entity instanceof EntityPlayerMP) {
                return teleportPlayerInternational((EntityPlayerMP) entity, server, sourceDim, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
            }
            else {
                return teleportEntityInternational(entity, server, sourceDim, targetDim, xCoord, yCoord, zCoord, yaw, pitch);
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

    private static Entity teleportEntityInternational(Entity entity, MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        if (entity.isDead) {
            return null;
        }

        WorldServer sourceWorld = server.worldServerForDimension(sourceDim);
        WorldServer targetWorld = server.worldServerForDimension(targetDim);
        entity.dimension = targetDim;

        sourceWorld.removeEntity(entity);
        entity.isDead = false;
        entity.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
        sourceWorld.updateEntityWithOptionalForce(entity, false);

        Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), targetWorld);
        if (newEntity != null) {
            newEntity.copyDataFromOld(entity);
            newEntity.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
            boolean flag = newEntity.forceSpawn;
            newEntity.forceSpawn = true;
            targetWorld.spawnEntityInWorld(newEntity);
            newEntity.forceSpawn = flag;
            targetWorld.updateEntityWithOptionalForce(newEntity, false);
        }

        entity.isDead = true;
        sourceWorld.resetUpdateEntityTick();
        targetWorld.resetUpdateEntityTick();

        return newEntity;
    }

    /**
     * This is the black magic responsible for teleporting players between dimensions!
     */
    private static EntityPlayer teleportPlayerInternational(EntityPlayerMP player, MinecraftServer server, int sourceDim, int targetDim, double xCoord, double yCoord, double zCoord, float yaw, float pitch) {
        WorldServer sourceWorld = server.worldServerForDimension(sourceDim);
        WorldServer targetWorld = server.worldServerForDimension(targetDim);
        PlayerList playerList = server.getPlayerList();

        player.dimension = targetDim;
        player.connection.sendPacket(new SPacketRespawn(player.dimension, targetWorld.getDifficulty(), targetWorld.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
        playerList.updatePermissionLevel(player);
        sourceWorld.removeEntityDangerously(player);
        player.isDead = false;

        //region Transfer to world

        player.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);
        player.connection.setPlayerLocation(xCoord, yCoord, zCoord, yaw, pitch);
        targetWorld.spawnEntityInWorld(player);
        targetWorld.updateEntityWithOptionalForce(player, false);
        player.setWorld(targetWorld);

        //endregion

        playerList.preparePlayer(player, sourceWorld);
        player.connection.setPlayerLocation(xCoord, yCoord, zCoord, yaw, pitch);
        player.interactionManager.setWorld(targetWorld);
        player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));

        playerList.updateTimeAndWeatherForPlayer(player, targetWorld);
        playerList.syncPlayerInventory(player);

        for (PotionEffect potioneffect : player.getActivePotionEffects()) {
            player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, sourceDim, targetDim);
        player.setLocationAndAngles(xCoord, yCoord, zCoord, yaw, pitch);

        return player;
    }
}
