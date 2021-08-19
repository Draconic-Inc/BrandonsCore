package com.brandon3055.brandonscore.api.render;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexBuilder;
import codechicken.lib.util.SneakyUtils;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.util.internal.UnstableApi;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

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
    public static final RenderType transColourType = RenderType.create("ghv2_trans_colour", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256, RenderType.State.builder()
            .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderState.NO_CULL)
            .setShadeModelState(RenderState.SMOOTH_SHADE)
            .setTexturingState(new RenderState.TexturingState("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
            .createCompositeState(false)
    );

    //#######################################################################
    //# Backgrounds
    //#######################################################################

    /**
     * Draws a rectangle / background with a style matching vanilla tool tips.
     */
    public static void drawHoverRect(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height, int backgroundColor, int borderColorStart, int borderColorEnd, boolean empty) {
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

    public static void drawHoverRect(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height, int backgroundColor, int borderColor, boolean empty) {
        int borderColorEnd = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;
        drawHoverRect(getter, mStack, x, y, width, height, backgroundColor, borderColor, borderColorEnd, empty);
    }

    public static void drawHoverRect(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height, boolean empty) {
        drawHoverRect(getter, mStack, x, y, width, height, GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, empty);
    }

    public static void drawHoverRect(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height, int backgroundColor, int borderColor) {
        int borderColorEnd = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;
        drawHoverRect(getter, mStack, x, y, width, height, backgroundColor, borderColor, borderColorEnd, false);
    }

    public static void drawHoverRect(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height) {
        drawHoverRect(getter, mStack, x, y, width, height, GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, false);
    }

    //#######################################################################
    //# Simple solids and gradients
    //#######################################################################

    public static void drawGradient(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height, int startColor, int endColor) {
        drawGradientRect(getter, mStack, x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawRect(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double width, double height, int colour) {
        drawGradientRect(getter, mStack, x, y, x + width, y + height, colour, colour);
    }

    public static void drawGradientRect(IRenderTypeBuffer getter, MatrixStack mStack, double left, double top, double right, double bottom, int startColor, int endColor) {
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

        IVertexBuilder builder = new TransformingVertexBuilder(getter.getBuffer(transColourType), mStack);
        builder.vertex(right,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        builder.vertex( left,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        builder.vertex( left, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        builder.vertex(right, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        //@formatter:on
    }

    //#######################################################################
    //# Sprites
    //#######################################################################

    public static void drawSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite) {
        //@formatter:off
        builder.vertex(x,          y + height, 0).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y + height, 0).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y,          0).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(x,          y,          0).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV0()).endVertex();
        //@formatter:on
    }

    public static void drawSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite, int colour) {
        //@formatter:off
        float alpha = (float)(colour >> 24 & 255) / 255.0F;
        float red   = (float)(colour >> 16 & 255) / 255.0F;
        float green = (float)(colour >>  8 & 255) / 255.0F;
        float blue  = (float)(colour       & 255) / 255.0F;
        drawSprite(builder, x, y, width, height, sprite, red, green, blue, alpha);
        //@formatter:on
    }

    public static void drawSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
        //@formatter:off
        builder.vertex(x,          y + height, 0).color(red, green, blue, alpha).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y + height, 0).color(red, green, blue, alpha).uv(sprite.getU1(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y,          0).color(red, green, blue, alpha).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(x,          y,          0).color(red, green, blue, alpha).uv(sprite.getU0(), sprite.getV0()).endVertex();
        //@formatter:on
    }

    public static void drawPartialSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV) {
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

    public static void drawPartialSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV, double texWidth, double texHeight) {
        drawPartialSprite(builder, x, y, width, height, sprite, minU / texHeight, minV / texHeight, maxU / texWidth, maxV / texHeight);
    }

    public static void drawPartialSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV, float red, float green, float blue, float alpha) {
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

    public static void drawPartialSprite(IVertexBuilder builder, double x, double y, double width, double height, TextureAtlasSprite sprite, double minU, double minV, double maxU, double maxV, double texWidth, double texHeight, int colour) {
        float alpha = (float)(colour >> 24 & 255) / 255.0F;
        float red   = (float)(colour >> 16 & 255) / 255.0F;
        float green = (float)(colour >>  8 & 255) / 255.0F;
        float blue  = (float)(colour       & 255) / 255.0F;
        drawPartialSprite(builder, x, y, width, height, sprite, minU / texHeight, minV / texHeight, maxU / texWidth, maxV / texHeight, red, green, blue, alpha);
    }

    //#######################################################################
    //# Progress Rendering
    //#######################################################################

    public static void drawPieProgress(IRenderTypeBuffer getter, MatrixStack mStack, double x, double y, double diameter, double progress, double offsetAngle, int colour) {
        float alpha = (float)(colour >> 24 & 255) / 255.0F;
        float red   = (float)(colour >> 16 & 255) / 255.0F;
        float green = (float)(colour >>  8 & 255) / 255.0F;
        float blue  = (float)(colour       & 255) / 255.0F;
        double radius = diameter / 2;
        IVertexBuilder builder = new TransformingVertexBuilder(getter.getBuffer(GuiHelperOld.FAN_TYPE), mStack);
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
}
