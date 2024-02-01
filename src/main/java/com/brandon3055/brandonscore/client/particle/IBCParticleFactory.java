package com.brandon3055.brandonscore.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Created by brandon3055 on 23/4/2016.
 */
@Deprecated
public interface IBCParticleFactory {

    Particle getEntityFX(int particleID, Level world, Vec3 pos, Vec3 speed, int... args);
}
