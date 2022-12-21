package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.ElytraEnabledItem;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    public LocalPlayer getThis() {
        return SneakyUtils.unsafeCast(this);
    }

    @Inject(
            method = "aiStep()V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            )
    )
    public void aiStep(CallbackInfo ci) {
        ItemStack itemStack = getThis().getItemBySlot(EquipmentSlot.CHEST);
        if (itemStack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(itemStack, getThis()) && getThis().tryToStartFallFlying()) {
            getThis().connection.send(new ServerboundPlayerCommandPacket(getThis(), ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
        if (BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e-> e.getItem() instanceof ElytraEnabledItem, getThis());
            if (stack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(stack, getThis()) && getThis().tryToStartFallFlying()) {
                getThis().connection.send(new ServerboundPlayerCommandPacket(getThis(), ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }
    }

}
