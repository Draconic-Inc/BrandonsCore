package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 23/4/2016.
 */
public interface IBCParticleFactory {

    EntityFX getEntityFX(int particleID, World world, Vec3D pos, Vec3D speed, int... args);
}
