package com.brandon3055.brandonscore.api.render;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.netty.util.internal.UnstableApi;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.GuiUtils;

/**
 * Created by brandon3055 on 12/8/21
 * This class will focus on properly implementing the new rendering system in its functions and will eventually completely replace {@link GuiHelperOld}
 * Everything in here should also use a MatrixStack.
 */
@UnstableApi //Warning this class is likely to see frequent additions and changes. 
public class GuiHelper {

    /**
     * Used to draw any solid colour elements that need to support transparency.
     */
    public static final RenderType transColourType = RenderType.create("ghv2_trans_colour", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
//            .setShadeModelState(RenderStateShard.SMOOTH_SHADE)
//            .setTexturingState(new RenderStateShard.TexturingStateShard("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
                    .createCompositeState(false)
    );


    //#######################################################################
    //# Backgrounds
    //#######################################################################

    /**
     * Draws a rectangle / background with a style matching vanilla tool tips.
     */
    public static void drawHoverRect(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height, int backgroundColor, int borderColorStart, int borderColorEnd, boolean empty) {
        //@formatter:off
        drawGradient(getter, mStack, x + 1,           y,                width - 2, 1,   backgroundColor, backgroundColor);             // Top
        drawGradient(getter, mStack, x + 1,           y + height - 1,   width - 2, 1,   backgroundColor, backgroundColor);             // Bottom
        drawGradient(getter, mStack, x,               y + 1,            1,              height - 2, backgroundColor, backgroundColor);   // Left
        drawGradient(getter, mStack, x + width - 1, y + 1,              1,              height - 2, backgroundColor, backgroundColor);   // Right
        if (!empty){
            drawGradient(getter, mStack, x + 1,           y + 1,            width - 2,      height - 2, backgroundColor, backgroundColor);   // Fill
        }
        drawGradient(getter, mStack, x + 1,           y + 1,            1,              height - 2, borderColorStart, borderColorEnd);   // Left Accent
        drawGradient(getter, mStack, x + width - 2, y + 1,              1,              height - 2, borderColorStart, borderColorEnd);   // Right Accent
        drawGradient(getter, mStack, x + 2,           y + 1,            width - 4, 1,   borderColorStart, borderColorStart);           // Top Accent
        drawGradient(getter, mStack, x + 2,           y + height - 2,   width - 4, 1,   borderColorEnd, borderColorEnd);               // Bottom Accent
        //@formatter:on
    }

    public static void drawHoverRect(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height, int backgroundColor, int borderColor, boolean empty) {
        int borderColorEnd = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;
        drawHoverRect(getter, mStack, x, y, width, height, backgroundColor, borderColor, borderColorEnd, empty);
    }

    public static void drawHoverRect(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height, boolean empty) {
        drawHoverRect(getter, mStack, x, y, width, height, GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, empty);
    }

    public static void drawHoverRect(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height, int backgroundColor, int borderColor) {
        int borderColorEnd = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;
        drawHoverRect(getter, mStack, x, y, width, height, backgroundColor, borderColor, borderColorEnd, false);
    }

    public static void drawHoverRect(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height) {
        drawHoverRect(getter, mStack, x, y, width, height, GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, false);
    }

    //#######################################################################
    //# Simple solids and gradients
    //#######################################################################

    public static void drawGradient(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height, int startColor, int endColor) {
        drawGradientRect(getter, mStack, x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawRect(MultiBufferSource getter, PoseStack mStack, double x, double y, double width, double height, int colour) {
        drawGradientRect(getter, mStack, x, y, x + width, y + height, colour, colour);
    }

    public static void drawGradientRect(MultiBufferSource getter, PoseStack mStack, double left, double top, double right, double bottom, int startColor, int endColor) {
        if (startColor == endColor && endColor == 0) return;
        //@formatter:off
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float)(startColor       & 255) / 255.0F;
        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(endColor         & 255) / 255.0F;

        VertexConsumer builder = new TransformingVertexConsumer(getter.getBuffer(transColourType), mStack);
        builder.vertex(right,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        builder.vertex( left,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        builder.vertex( left, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        builder.vertex(right, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        //@formatter:on
    }

    public static void drawMultiPassGradientRect(MultiBufferSource getter, PoseStack mStack, double left, double top, double right, double bottom, int startColor, int endColor, int layers) {
        if (startColor == endColor && endColor == 0) return;
        //@formatter:off
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float)(startColor       & 255) / 255.0F;
        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(endColor         & 255) / 255.0F;

        VertexConsumer builder = new TransformingVertexConsumer(getter.getBuffer(transColourType), mStack);
        for (int i = 0; i < layers; i++) {
            builder.vertex(right,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            builder.vertex( left,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            builder.vertex( left, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
            builder.vertex(right, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        }
        //@formatter:on
    }

    public static void drawShadedRect(MultiBufferSource getter, PoseStack poseStack, double x, double y, double width, double height, double borderWidth, int fill, int topLeftColour, int bottomRightColour, int cornerMixColour) {
        //Fill
        drawRect(getter, poseStack, x + borderWidth, y + borderWidth, width - borderWidth * 2, height - borderWidth * 2, fill);
        //Top
        drawRect(getter, poseStack, x, y, width - borderWidth, borderWidth, topLeftColour);
        //Left
        drawRect(getter, poseStack, x, y + borderWidth, borderWidth, height - borderWidth * 2, topLeftColour);
        //Bottom
        drawRect(getter, poseStack, x + borderWidth, y + height - borderWidth, width - borderWidth, borderWidth, bottomRightColour);
        //Right
        drawRect(getter, poseStack, x + width - borderWidth, y + borderWidth, borderWidth, height - borderWidth * 2, bottomRightColour);
        //Top Right Corner
        drawRect(getter, poseStack, x + width - borderWidth, y, borderWidth, borderWidth, cornerMixColour);
        //Bottom Left Corner
        drawRect(getter, poseStack, x, y + height - borderWidth, borderWidth, borderWidth, cornerMixColour);
    }

    //#######################################################################
    //# Sprites
    //#######################################################################

    public static void drawSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite) {
        //@formatter:off
        builder.vertex(x,          y + height, 0).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y + height, 0).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y,          0).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(x,          y,          0).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV0()).endVertex();
        //@formatter:on
    }

    public static void drawSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite, int colour) {
        //@formatter:off
        float alpha = (float)(colour >> 24 & 255) / 255.0F;
        float red   = (float)(colour >> 16 & 255) / 255.0F;
        float green = (float)(colour >>  8 & 255) / 255.0F;
        float blue  = (float)(colour       & 255) / 255.0F;
        drawSprite(builder, x, y, width, height, sprite, red, green, blue, alpha);
        //@formatter:on
    }

    public static void drawSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
        //@formatter:off
        builder.vertex(x,          y + height, 0).color(red, green, blue, alpha).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y + height, 0).color(red, green, blue, alpha).uv(sprite.getU1(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y,          0).color(red, green, blue, alpha).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(x,          y,          0).color(red, green, blue, alpha).uv(sprite.getU0(), sprite.getV0()).endVertex();
        //@formatter:on
    }

    public static void drawPartialSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV) {
        float sW = sprite.getU1() - sprite.getU0();
        float sH = sprite.getV1() - sprite.getV0();

        float uMin = sprite.getU0() + (sW * (float) minU);
        float uMax = sprite.getU0() + (sW * (float) maxU);
        float vMin = sprite.getV0() + (sH * (float) minV);
        float vMax = sprite.getV0() + (sH * (float) maxV);

        //@formatter:off
        builder.vertex(x,          y + height, 0).color(1F, 1F, 1F, 1F).uv(uMin, vMax).endVertex();
        builder.vertex(x + width,  y + height, 0).color(1F, 1F, 1F, 1F).uv(uMax, vMax).endVertex();
        builder.vertex(x + width,  y,          0).color(1F, 1F, 1F, 1F).uv(uMax, vMin).endVertex();
        builder.vertex(x,          y,          0).color(1F, 1F, 1F, 1F).uv(uMin, vMin).endVertex();
        //@formatter:on
    }

    public static void drawPartialSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV, double texWidth, double texHeight) {
        drawPartialSprite(builder, x, y, width, height, sprite, minU / texHeight, minV / texHeight, maxU / texWidth, maxV / texHeight);
    }

    public static void drawPartialSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV, float red, float green, float blue, float alpha) {
        float sW = sprite.getU1() - sprite.getU0();
        float sH = sprite.getV1() - sprite.getV0();

        float uMin = sprite.getU0() + (sW * (float) minU);
        float uMax = sprite.getU0() + (sW * (float) maxU);
        float vMin = sprite.getV0() + (sH * (float) minV);
        float vMax = sprite.getV0() + (sH * (float) maxV);

        //@formatter:off
        builder.vertex(x,          y + height, 0).color(red, green, blue, alpha).uv(uMin, vMax).endVertex();
        builder.vertex(x + width,  y + height, 0).color(red, green, blue, alpha).uv(uMax, vMax).endVertex();
        builder.vertex(x + width,  y,          0).color(red, green, blue, alpha).uv(uMax, vMin).endVertex();
        builder.vertex(x,          y,          0).color(red, green, blue, alpha).uv(uMin, vMin).endVertex();
        //@formatter:on
    }

    public static void drawPartialSprite(VertexConsumer builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV, double texWidth, double texHeight, int colour) {
        float alpha = (float) (colour >> 24 & 255) / 255.0F;
        float red = (float) (colour >> 16 & 255) / 255.0F;
        float green = (float) (colour >> 8 & 255) / 255.0F;
        float blue = (float) (colour & 255) / 255.0F;
        drawPartialSprite(builder, x, y, width, height, sprite, minU / texHeight, minV / texHeight, maxU / texWidth, maxV / texHeight, red, green, blue, alpha);
    }

    //#######################################################################
    //# Texture Rendering
    //#######################################################################

    public static void drawTexture(VertexConsumer builder, double x, double y, double width, double height) {
        //@formatter:off
        builder.vertex(x,          y + height, 0).color(1F, 1F, 1F, 1F).uv(0, 1).endVertex();
        builder.vertex(x + width,  y + height, 0).color(1F, 1F, 1F, 1F).uv(1, 1).endVertex();
        builder.vertex(x + width,  y,          0).color(1F, 1F, 1F, 1F).uv(1, 0).endVertex();
        builder.vertex(x,          y,          0).color(1F, 1F, 1F, 1F).uv(0, 0).endVertex();
        //@formatter:on
    }

//    public static void drawTexture(IVertexBuilder builder, double x, double y, double width, double height, double uMin, double vMin, double uMax, double vMax, int texWidth, int texHeight) {
//        double uInc = 1D / texWidth;
//        double vInc = 1D / texHeight;
//        uMin *= uInc;
//        vMin *= vInc;
//        uMax *= uInc;
//        vMax *= vInc;
//        //@formatter:off
//        builder.vertex(x,          y + height, 0).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV1()).endVertex();
//        builder.vertex(x + width,  y + height, 0).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV1()).endVertex();
//        builder.vertex(x + width,  y,          0).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV0()).endVertex();
//        builder.vertex(x,          y,          0).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV0()).endVertex();
//        //@formatter:on
//    }

//    public static void drawTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
//        float zLevel = getRenderZLevel();
//        float f = 1.0F / textureWidth;
//        float f1 = 1.0F / textureHeight;
//        RenderSystem.enableBlend();
//        RenderSystem.disableAlphaTest();
//        RenderSystem.defaultBlendFunc();
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder buffer = tessellator.getBuilder();
//        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
//        buffer.vertex(x, y + height, zLevel).uv(u * f, (v + height) * f1).endVertex();
//        buffer.vertex(x + width, y + height, zLevel).uv((u + width) * f, (v + height) * f1).endVertex();
//        buffer.vertex(x + width, y, zLevel).uv((u + width) * f, v * f1).endVertex();
//        buffer.vertex(x, y, zLevel).uv(u * f, v * f1).endVertex();
//        tessellator.end();
//        RenderSystem.disableBlend();
//        RenderSystem.enableAlphaTest();
//    }

    //#######################################################################
    //# Progress Rendering
    //#######################################################################

    public static void drawPieProgress(MultiBufferSource getter, PoseStack mStack, double x, double y, double diameter, double progress, double offsetAngle, int colour) {
        float alpha = (float) (colour >> 24 & 255) / 255.0F;
        float red = (float) (colour >> 16 & 255) / 255.0F;
        float green = (float) (colour >> 8 & 255) / 255.0F;
        float blue = (float) (colour & 255) / 255.0F;
        double radius = diameter / 2;
        VertexConsumer builder = new TransformingVertexConsumer(getter.getBuffer(GuiHelperOld.FAN_TYPE), mStack);
        builder.vertex(x + radius, y + radius, 0).color(0, 255, 255, 64).endVertex();
        for (double d = 0; d <= 1; d += 1D / 30D) {
            double angle = (d * progress) + 0.5 - progress;
            angle *= Math.PI * 2;
            angle += MathHelper.torad * offsetAngle;
            double vertX = x + radius + Math.sin(angle) * radius;
            double vertY = y + radius + Math.cos(angle) * radius;
            builder.vertex(vertX, vertY, 0).color(red, green, blue, alpha).endVertex();
        }
    }

    //#######################################################################
    //# Utils
    //#######################################################################

    public static boolean isInRect(int x, int y, int xSize, int ySize, double mouseX, double mouseY) {
        return ((mouseX >= x && mouseX < x + xSize) && (mouseY >= y && mouseY < y + ySize));
    }

    public static boolean isInRect(double x, double y, double xSize, double ySize, double mouseX, double mouseY) {
        return ((mouseX >= x && mouseX < x + xSize) && (mouseY >= y && mouseY < y + ySize));
    }

    public static RenderType guiTextureType(ResourceLocation resource) {
        return RenderType.create("gui_resource", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(resource, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
//                .setAlphaState(RenderStateShard.DEFAULT_ALPHA)
//                .setTexturingState(new RenderStateShard.TexturingStateShard("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
                        .createCompositeState(false)
        );
    }
}
