package com.brandon3055.brandonscore.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created by brandon3055 on 19/12/2022
 */
@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker
    void invokeSetSharedFlag(int flag, boolean value);
}
