package com.brandon3055.brandonscore.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by brandon3055 on 6/5/2016.
 */
public class TESRBase<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

    @Override
    public void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage) {

    }

    public void renderItem(ItemStack stack) {
        if (stack != null) {
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        }
    }

    private boolean isLightSet = false;
    private float lastBrightnessX = 0;
    private float lastBrightnessY = 0;

    public void setLighting(float light) {
        if (!isLightSet) {
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            isLightSet = true;
        }
        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light, light);
    }

    public void resetLighting() {
        if (isLightSet) {
            isLightSet = false;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
        GlStateManager.enableLighting();
    }

    public void translateScaleTranslate(double translate, double x, double y, double z) {
        GlStateManager.translate(translate, translate, translate);
        GlStateManager.scale(x, y, z);
        GlStateManager.translate(-translate, -translate, -translate);
    }

    public void translateRotateTranslate(double translate, float angle, float x, float y, float z) {
        GlStateManager.translate(translate, translate, translate);
        GlStateManager.rotate(angle, x, y, z);
        GlStateManager.translate(-translate, -translate, -translate);
    }

    public void preRenderFancy() {
        GlStateManager.glTexParameteri(3553, 10242, 10497);
        GlStateManager.glTexParameteri(3553, 10243, 10497);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    /**
     * Call before rendering transparent
     */
    public void midRenderFancy() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
    }

    public void postRenderFancy() {
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
    }


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
