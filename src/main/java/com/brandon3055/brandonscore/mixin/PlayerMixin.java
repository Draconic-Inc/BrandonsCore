package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.ElytraEnabledItem;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(Player.class)
public class PlayerMixin {

    private Player getThis() {
        return (Player) (Object) this;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(
            method = "tryToStartFallFlying",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void tryToStartFallFlying(CallbackInfoReturnable<Boolean> cir, ItemStack itemStack) {
        if (itemStack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(itemStack, getThis())) {
            getThis().startFallFlying();
            cir.setReturnValue(true);
        }
        if (BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e-> e.getItem() instanceof ElytraEnabledItem, getThis());
            if (stack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(stack, getThis())) {
                getThis().startFallFlying();
                cir.setReturnValue(true);
            }
        }
    }

}
