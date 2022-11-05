package com.brandon3055.brandonscore.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created by brandon3055 on 23/10/2022
 */
public interface BlockEntityRendererTransparent<T extends BlockEntity> extends BlockEntityRenderer<T> {

    void renderTransparent(T tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay);

}
