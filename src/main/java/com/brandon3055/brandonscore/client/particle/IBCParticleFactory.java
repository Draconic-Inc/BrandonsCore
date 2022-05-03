package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;

/**
 * Created by brandon3055 on 23/4/2016.
 */
@Deprecated
public interface IBCParticleFactory {

    Particle getEntityFX(int particleID, Level world, Vec3D pos, Vec3D speed, int... args);
}
