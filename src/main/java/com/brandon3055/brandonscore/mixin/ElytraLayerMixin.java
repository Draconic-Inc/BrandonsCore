package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {

    private PlayerModel getThis() {
        return (PlayerModel) (Object) this;
    }

    @Inject(
            method = "shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void shouldRender(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ContributorHandler.shouldCancelElytra(entity)) {
            cir.setReturnValue(false);
        }
    }
}
