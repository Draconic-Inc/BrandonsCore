package com.brandon3055.brandonscore.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Created by brandon3055 on 19/11/2022
 */
public interface EquippedItemModel {

    void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, ItemStack stack, int packedLight, int packedOverlay, float partialTicks);

}
