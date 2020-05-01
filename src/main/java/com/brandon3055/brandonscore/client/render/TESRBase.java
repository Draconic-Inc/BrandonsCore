package com.brandon3055.brandonscore.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 6/5/2016.
 */
@Deprecated //I probably dont need thsi any more
public class TESRBase<T extends TileEntity> extends TileEntityRenderer<T> {

    protected static Map<ItemStack, IBakedModel> itemModelCache = new HashMap<>();

    public TESRBase(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

    }

//    public void renderItem(ItemStack stack) {
//        if (!stack.isEmpty()) {
//            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
//        }
//    }

//    public static IBakedModel getStackModel(ItemStack stack) {
//        if (stack.isEmpty()) {
//            throw new IllegalArgumentException("BrandonsCore:TESRBase#getStackModel Someone attempted to get the model from a null itemstack! ");
//        }
//
//        return itemModelCache.computeIfAbsent(stack, stack1 -> Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack, null, null));
//    }

    private boolean isLightSet = false;
    private float lastBrightnessX = 0;
    private float lastBrightnessY = 0;
    
//    public void translateScaleTranslate(double translate, double x, double y, double z) {
//        RenderSystem.translated(translate, translate, translate);
//        RenderSystem.scaled(x, y, z);
//        RenderSystem.translated(-translate, -translate, -translate);
//    }
//
//    public void translateRotateTranslate(double translate, float angle, float x, float y, float z) {
//        RenderSystem.translated(translate, translate, translate);
//        RenderSystem.rotatef(angle, x, y, z);
//        RenderSystem.translated(-translate, -translate, -translate);
//    }

//    public void preRenderFancy() {
//        RenderSystem.glTexParameteri(3553, 10242, 10497);
//        RenderSystem.glTexParameteri(3553, 10243, 10497);
//        RenderSystem.disableCull();
//        RenderSystem.disableBlend();
//        RenderSystem.depthMask(true);
//        RenderSystem.tryBlendFuncSeparate(RenderSystem.SourceFactor.SRC_ALPHA, RenderSystem.DestFactor.ONE, RenderSystem.SourceFactor.ONE, RenderSystem.DestFactor.ZERO);
//    }

//    /**
//     * Call before rendering transparent
//     */
//    public void midRenderFancy() {
//        RenderSystem.enableBlend();
//        RenderSystem.tryBlendFuncSeparate(RenderSystem.SourceFactor.SRC_ALPHA, RenderSystem.DestFactor.ONE_MINUS_SRC_ALPHA, RenderSystem.SourceFactor.ONE, RenderSystem.DestFactor.ZERO);
//        RenderSystem.depthMask(false);
//    }
//
//    public void postRenderFancy() {
//        RenderSystem.enableTexture2D();
//        RenderSystem.depthMask(true);
//        RenderSystem.disableBlend();
//    }


//    GL_LINES = 0x1,
//    GL_LINE_LOOP = 0x2,
//    GL_LINE_STRIP = 0x3,
//    GL_TRIANGLES = 0x4,
//    GL_TRIANGLE_STRIP = 0x5,
//    GL_TRIANGLE_FAN = 0x6,
//    GL_QUADS = 0x7,
//    GL_QUAD_STRIP = 0x8,
//    GL_POLYGON = 0x9,
}
