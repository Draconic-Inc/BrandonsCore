package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.ElytraEnabledItem;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EntityAccessor{

    @Shadow public abstract boolean isFallFlying();

    @Shadow protected int fallFlyTicks;

    public LivingEntity getThis() {
        return SneakyUtils.unsafeCast(this);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(
            method = "updateFallFlying",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void updateFallFlying(CallbackInfo ci, boolean flag, ItemStack itemstack) {
        if (itemstack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(itemstack, getThis()) && item.elytraFlightTickBC(itemstack, getThis(), fallFlyTicks)) {
            ci.cancel();
        }

        if (BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e-> e.getItem() instanceof ElytraEnabledItem, getThis());
            if (stack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(stack, getThis()) && item.elytraFlightTickBC(stack, getThis(), fallFlyTicks)) {
                ci.cancel();
            }
        }
    }

}
