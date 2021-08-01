package com.brandon3055.brandonscore.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Created by brandon3055 on 1/8/21
 */
public class DummyTeleporter extends Teleporter {

    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;

    public DummyTeleporter(WorldServer world, double x, double y, double z, float pitch, float yaw) {
        super(world);
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public void placeEntity(World world, Entity entity, float suggestedYaw) {
        if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, yaw, pitch);
        } else {
            entity.setLocationAndAngles(x, y, z, yaw, pitch);
        }
        entity.setRotationYawHead(yaw);
    }
}


