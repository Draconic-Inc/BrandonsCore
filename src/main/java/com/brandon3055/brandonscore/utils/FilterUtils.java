package com.brandon3055.brandonscore.utils;

import com.google.common.base.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 4/07/2016.
 */
public class FilterUtils {

    public static final Predicate<Entity> IS_PLAYER = new Predicate<Entity>()
    {
        public boolean apply(@Nullable Entity entity)
        {
            return entity instanceof Player && entity.isAlive();
        }
    };

}
