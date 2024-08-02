package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.render.BlockEntityRendererTransparent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "setupAndRender", at = @At("RETURN"))
    private static <T extends BlockEntity> void bc$addBEToList(BlockEntityRenderer<T> p_112285_, T p_112286_, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_, CallbackInfo ci) {
        if (p_112285_ instanceof BlockEntityRendererTransparent) {
            BCClientEventHandler.addTranslucentBlockEntity(p_112286_);
        }
    }
}
