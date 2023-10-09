package com.brandon3055.brandonscore.api.render;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.brandon3055.brandonscore.utils.Utils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.netty.util.internal.UnstableApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

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

    public static void drawBorderedRect(MultiBufferSource getter, PoseStack poseStack, double x, double y, double width, double height, double borderWidth, int fillColour, int borderColour) {
        //Fill
        drawRect(getter, poseStack, x + borderWidth, y + borderWidth, width - borderWidth * 2, height - borderWidth * 2, fillColour);
        //Top (Full Width)
        drawRect(getter, poseStack, x, y, width, borderWidth, borderColour);
        //Left
        drawRect(getter, poseStack, x, y + borderWidth, borderWidth, height - borderWidth * 2, borderColour);
        //Bottom (Full Width)
        drawRect(getter, poseStack, x, y + height - borderWidth, width, borderWidth, borderColour);
        //Right
        drawRect(getter, poseStack, x + width - borderWidth, y + borderWidth, borderWidth, height - borderWidth * 2, borderColour);
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
    //# Stack Rendering
    //#######################################################################

    /**
     * Recommended standard z offset is +100
     */
    public static void renderGuiStack(ItemStack stack, PoseStack poseStack, double x, double y, double width, double height) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, null, null, 0);
        float xScale = (float) width / 16F;
        float yScale = (float) height / 16F;

        minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(x + (8.0D * xScale), y + (8.0D * yScale), 0);
        poseStack.scale(16.0F, -16.0F, 16.0F);
        poseStack.scale(xScale, yScale, 1F);
        pushModelViewPoseStack(poseStack);
        poseStack.popPose();

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean noBlockLight = !model.usesBlockLight();
        if (noBlockLight) {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, new PoseStack(), buffers, 0xf000f0, OverlayTexture.NO_OVERLAY, model);
        buffers.endBatch();
        RenderSystem.enableDepthTest();
        if (noBlockLight) {
            Lighting.setupFor3DItems();
        }

        popModelViewPoseStack(poseStack);
    }

    /**
     * Recommended standard z offset is +200
     */
    public static void renderStackOverlay(ItemStack stack, PoseStack poseStack, Font font, double x, double y, double width, double height, boolean drawCount) {
        float xScale = (float) width / 16F;
        float yScale = (float) height / 16F;
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            if (stack.getCount() != 1 && drawCount) {
                String s = String.valueOf(stack.getCount());
                poseStack.translate(x, y, 0);
                poseStack.scale(xScale, yScale, 1F);
                MultiBufferSource.BufferSource buffer = RenderUtils.getGuiBuffers();
                font.drawInBatch(s, (float) (19 - 2 - font.width(s)), (float) (6 + 3), 16777215, true, poseStack.last().pose(), buffer, false, 0, 15728880);
                buffer.endBatch();
            }

            if (stack.isBarVisible()) {
                MultiBufferSource getter = RenderUtils.getGuiBuffers();
                poseStack.translate(x, y, 0);
                poseStack.scale(xScale, yScale, 1F);
                int i = stack.getBarWidth();
                int colour = stack.getBarColor();
                GuiHelper.drawRect(getter, poseStack, 2, 13, 13, 2, 0xFF000000);
                GuiHelper.drawRect(getter, poseStack, 2, 13, i, 1, 0xFF000000 | colour);
                RenderUtils.endBatch(getter);
            }
            poseStack.popPose();
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

    //    /**
//     * @param x Rect xPos (Inclusive)
//     * @param y Rect yPos (Inclusive)
//     * @param xSize Rect Width (Exclusive)
//     * @param ySize Rect Height (Exclusive)
//     * @param mouseX Check x pos
//     * @param mouseY Check y pos
//     * @return distance.
//     */
    public static double distToRect(double x, double y, double xSize, double ySize, double mouseX, double mouseY) {
        if (isInRect(x, y, xSize, ySize, mouseX, mouseY)) {
            return 0;
        }
//        /*
//        * This is to deal with a quirk of rendering where...
//        * Say you render the rect x=0,y=0,width=10,height=10
//        * That renders from x=0,y=0 (Inclusive) to x=10,y=10 (Exclusive)
//        * But if you were to give that same rect to this method it would be treated as
//        * x=0,y=0 (Inclusive) to x=10,y=10 (Inclusive)
//        * Effectively adding a 1 pixel offset to the right and the bottom.
//        * To resolve this width and height are decremented by 1.
//        * */
////        xSize--;
////        ySize--;

        //Inside Height (Simply distance from Left or Right)
        if (mouseY >= y && mouseY < y + ySize) {
            return mouseX >= x + xSize ? mouseX - (x + xSize) : x - mouseX;
        }

        //Inside Width (Simply distance from Top or Bottom)
        if (mouseX >= x && mouseX < x + xSize) {
            return mouseY < y ? y - mouseY : mouseY - (y + ySize);
        }

        //Distance from corner
        double cx = mouseX < x ? x : x + xSize;
        double cy = mouseY < y ? y : y + ySize;
        return Utils.getDistance(cx, cy, mouseX, mouseY);
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

    public static int mixColours(int colour1, int colour2) {
        return mixColours(colour1, colour2, false);
    }

    public static int mixColours(int colour1, int colour2, boolean subtract) {
        int alpha1 = colour1 >> 24 & 255;
        int alpha2 = colour2 >> 24 & 255;
        int red1 = colour1 >> 16 & 255;
        int red2 = colour2 >> 16 & 255;
        int green1 = colour1 >> 8 & 255;
        int green2 = colour2 >> 8 & 255;
        int blue1 = colour1 & 255;
        int blue2 = colour2 & 255;

        int alpha = Mth.clamp(alpha1 + (subtract ? -alpha2 : alpha2), 0, 255);
        int red = Mth.clamp(red1 + (subtract ? -red2 : red2), 0, 255);
        int green = Mth.clamp(green1 + (subtract ? -green2 : green2), 0, 255);
        int blue = Mth.clamp(blue1 + (subtract ? -blue2 : blue2), 0, 255);

        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static double getMouseX() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
    }

    public static double getMouseY() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
    }

    /**
     * For cases where you need to apply your own PoseStack to the modelViewStack.
     * At time of writing this is really only needed for {@link #renderGuiStack(ItemStack, PoseStack, double, double, double, double)}
     * But it may be useful in other places.
     */
    public static void pushModelViewPoseStack(PoseStack poseStack) {
        PoseStack viewStack = RenderSystem.getModelViewStack();
        if (viewStack == poseStack) return;

        viewStack.pushPose();
        viewStack.last().pose().multiply(poseStack.last().pose());
        viewStack.last().normal().mul(poseStack.last().normal());
        RenderSystem.applyModelViewMatrix();
    }

    public static void popModelViewPoseStack(PoseStack poseStack) {
        PoseStack viewStack = RenderSystem.getModelViewStack();
        if (viewStack == poseStack) return;

        viewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
