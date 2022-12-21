package com.brandon3055.brandonscore.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The functonality of this interface is identical to what you would get if you were to implement elytra via
 * {@link net.minecraftforge.common.extensions.IForgeItem#canElytraFly(ItemStack, LivingEntity)} and {@link net.minecraftforge.common.extensions.IForgeItem#elytraFlightTick(ItemStack, LivingEntity, int)}
 * With one notable exception. This also works with curios.
 * <p>
 * Also you will need to add a custom elytra layer to the player to render your elytra.
 * Created by brandon3055 on 19/12/2022
 */
public interface ElytraEnabledItem {

    /**
     * Used to determine if the player can use Elytra flight.
     * This is called Client and Server side.
     *
     * @param stack  The ItemStack in the Chest slot of the entity.
     * @param entity The entity trying to fly.
     * @return True if the entity can use Elytra flight.
     */
    boolean canElytraFlyBC(ItemStack stack, LivingEntity entity);

    /**
     * Used to determine if the player can continue Elytra flight,
     * this is called each tick, and can be used to apply ItemStack damage,
     * consume Energy, or what have you.
     * For example the Vanilla implementation of this, applies damage to the
     * ItemStack every 20 ticks.
     *
     * @param stack       ItemStack in the Chest slot of the entity.
     * @param entity      The entity currently in Elytra flight.
     * @param flightTicks The number of ticks the entity has been Elytra flying for.
     * @return True if the entity should continue Elytra flight or False to stop.
     */
    boolean elytraFlightTickBC(ItemStack stack, LivingEntity entity, int flightTicks);

}
