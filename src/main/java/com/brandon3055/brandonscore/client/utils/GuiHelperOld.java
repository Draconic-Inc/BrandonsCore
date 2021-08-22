package com.brandon3055.brandonscore.client.utils;

import codechicken.lib.colour.ColourARGB;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.buffer.TransformingVertexBuilder;
import codechicken.lib.util.SneakyUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by Brandon on 28/06/2014.
 */
@Deprecated //If possible convert to new rendering system and move to GuiHelperV2
public class GuiHelperOld {
    public static final RenderType TRANS_TYPE = RenderType.create("gui_trans_colour", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256, RenderType.State.builder()
            .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
            .setAlphaState(RenderState.NO_ALPHA)
            .setTexturingState(new RenderState.TexturingState("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
            .createCompositeState(false)
    );

    //Triangle Fan Type
    public static RenderType FAN_TYPE = RenderType.create("tri_fan_type", DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_FAN, 256, RenderType.State.builder()
            .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
            .setAlphaState(RenderState.NO_ALPHA)
            .setTexturingState(new RenderState.TexturingState("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
            .createCompositeState(false)
    );

    public static final float PXL128 = 0.0078125F;
    public static final float PXL256 = 0.00390625F;

    public static boolean isInRect(int x, int y, int xSize, int ySize, double mouseX, double mouseY) {
        return ((mouseX >= x && mouseX < x + xSize) && (mouseY >= y && mouseY < y + ySize));
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
        drawTexturedRect(x, y, width, height, u, v, width, height, 0, PXL256);
    }

    public static void drawTexturedRect(float x, float y, float width, float height, int u, int v, int uSize, int vSize, float zLevel, float pxl) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.vertex(x, y + height, zLevel).uv(u * pxl, (v + vSize) * pxl).endVertex();
        vertexBuffer.vertex(x + width, y + height, zLevel).uv((u + uSize) * pxl, (v + vSize) * pxl).endVertex();
        vertexBuffer.vertex(x + width, y, zLevel).uv((u + uSize) * pxl, v * pxl).endVertex();
        vertexBuffer.vertex(x, y, zLevel).uv(u * pxl, v * pxl).endVertex();
        tessellator.end();
    }

    public static void drawTiledTextureRectWithTrim(int xPos, int yPos, int xSize, int ySize, int topTrim, int leftTrim, int bottomTrim, int rightTrim, int texU, int texV, int texWidth, int texHeight, double zLevel) {
        int trimWidth = texWidth - leftTrim - rightTrim;
        int trimHeight = texHeight - topTrim - bottomTrim;
        if (xSize <= texWidth) trimWidth = Math.min(trimWidth, xSize - rightTrim);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(0x07, DefaultVertexFormats.POSITION_TEX);

        for (int x = 0; x < xSize; ) {
            int rWidth = Math.min(xSize - x, trimWidth);
            int trimU = x == 0 ? texU : x + texWidth <= xSize ? texU + leftTrim : texU + (texWidth - (xSize - x));

            //Top & Bottom trim
            bufferTexturedModalRect(buffer, xPos + x, yPos, trimU, texV, rWidth, topTrim, zLevel);
            bufferTexturedModalRect(buffer, xPos + x, yPos + ySize - bottomTrim, trimU, texV + texHeight - bottomTrim, rWidth, bottomTrim, zLevel);


            rWidth = Math.min(xSize - x - leftTrim - rightTrim, trimWidth);
            for (int y = 0; y < ySize; ) {
                int rHeight = Math.min(ySize - y - topTrim - bottomTrim, trimHeight);
                int trimV = y + texHeight <= ySize ? texV + topTrim : texV + (texHeight - (ySize - y));

                //Left & Right trim
                if (x == 0) {
                    bufferTexturedModalRect(buffer, xPos, yPos + y + topTrim, texU, trimV, leftTrim, rHeight, zLevel);
                    bufferTexturedModalRect(buffer, xPos + xSize - rightTrim, yPos + y + topTrim, trimU + texWidth - rightTrim, trimV, rightTrim, rHeight, zLevel);
                }

                //Core
                bufferTexturedModalRect(buffer, xPos + x + leftTrim, yPos + y + topTrim, texU + leftTrim, texV + topTrim, rWidth, rHeight, zLevel);
                y += trimHeight;
            }
            x += trimWidth;
        }

        tessellator.end();
    }

    private static void bufferTexturedModalRect(BufferBuilder buffer, int x, int y, int textureX, int textureY, int width, int height, double zLevel) {
        buffer.vertex(x, y + height, zLevel).uv( ((float) (textureX) * 0.00390625F),  ((float) (textureY + height) * 0.00390625F)).endVertex();
        buffer.vertex(x + width, y + height, zLevel).uv( ((float) (textureX + width) * 0.00390625F),  ((float) (textureY + height) * 0.00390625F)).endVertex();
        buffer.vertex(x + width, y, zLevel).uv( ((float) (textureX + width) * 0.00390625F),  ((float) (textureY) * 0.00390625F)).endVertex();
        buffer.vertex(x, y, zLevel).uv( ((float) (textureX) * 0.00390625F),  ((float) (textureY) * 0.00390625F)).endVertex();
    }

//    public static void drawHoveringText(List list, int x, int y, FontRenderer font, int guiWidth, int guiHeight) {
//        net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(list, x, y, guiWidth, guiHeight, -1, font);
//    }

    public static void drawHoveringTextScaled(List list, int mouseX, int mouseY, FontRenderer font, float fade, double scale, int guiWidth, int guiHeight) {
        MatrixStack matrixstack = new MatrixStack();
        if (!list.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.disableRescaleNormal();
            RenderHelper.turnOff();
            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            RenderSystem.scaled(scale, scale, 1);
            mouseX = (int) (mouseX / scale);
            mouseY = (int) (mouseY / scale);

            int tooltipTextWidth = 0;

            for (Object aList : list) {
                String s = (String) aList;
                int l = font.width(s);

                if (l > tooltipTextWidth) {
                    tooltipTextWidth = l;
                }
            }

            int tooltipX = mouseX + 12;
            int tooltipY = mouseY - 12;
            int tooltipHeight = 6;

            if (list.size() > 1) {
                tooltipHeight += 2 + (list.size() - 1) * 10;
            }

            if (tooltipX + tooltipTextWidth > (int) (guiWidth / scale)) {
                tooltipX -= 28 + tooltipTextWidth;
            }

            if (tooltipY + tooltipHeight + 6 > (int) (guiHeight / scale)) {
                tooltipY = (int) (guiHeight / scale) - tooltipHeight - 6;
            }

            int backgroundColor = -267386864;
            drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor, fade, scale);
            drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor, fade, scale);
            drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, fade, scale);
            drawGradientRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, fade, scale);
            drawGradientRect(tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, fade, scale);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            drawGradientRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, k1, l1, fade, scale);
            drawGradientRect(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, k1, l1, fade, scale);
            drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, k1, k1, fade, scale);
            drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, l1, l1, fade, scale);

            int i2 = 0;
            while (i2 < list.size()) {
                String s1 = (String) list.get(i2);
                RenderSystem.enableBlend();
                RenderSystem.disableAlphaTest();
                RenderSystem.blendFuncSeparate(770, 771, 1, 0);
                font.drawShadow(matrixstack, s1, tooltipX, tooltipY, ((int) (fade * 240F) + 0x10 << 24) | 0x00FFFFFF);
                RenderSystem.enableAlphaTest();
                tooltipY += 10;
                ++i2;
            }

            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();
            RenderHelper.turnBackOn();
            RenderSystem.enableRescaleNormal();
            RenderSystem.popMatrix();
        }
    }

    @Deprecated
    public static void drawGradientRect(float left, float top, float right, float bottom, int colour1, int colour2, float fade, double zLevel) {
        float f = ((colour1 >> 24 & 255) / 255.0F) * fade;
        float f1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float f2 = (float) (colour1 >> 8 & 255) / 255.0F;
        float f3 = (float) (colour1 & 255) / 255.0F;
        float f4 = ((colour2 >> 24 & 255) / 255.0F) * fade;
        float f5 = (float) (colour2 >> 16 & 255) / 255.0F;
        float f6 = (float) (colour2 >> 8 & 255) / 255.0F;
        float f7 = (float) (colour2 & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.vertex(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.vertex(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.vertex(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.vertex(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.end();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

//    /**
//     * Draws a simple vertical energy bar with no tool tip
//     */
//    public static void drawEnergyBar(AbstractGui gui, int posX, int posZ, int size, long energy, long maxEnergy) {
//        drawEnergyBar(gui, posX, posZ, size, false, energy, maxEnergy, false, 0, 0);
//    }

//    /**
//     * Draws an energy bar in a gui at the given position
//     *
//     * @param size       is the length of the energy bar.
//     * @param horizontal will rotate the bar clockwise 90 degrees.
//     */
//    @SuppressWarnings("all")
//    public static void drawEnergyBar(AbstractGui gui, int posX, int posY, int size, boolean horizontal, long energy, long maxEnergy, boolean toolTip, int mouseX, int mouseY) {
//        ResourceHelperBC.bindTexture("textures/gui/energy_gui.png");
//        int draw = (int) ((double) energy / (double) maxEnergy * (size - 2));
//
//        boolean inRect = isInRect(posX, posY, size, 14, mouseX, mouseY);
//
//        if (horizontal) {
//            int x = posY;
//            posY = posX;
//            posX = x;
//            RenderSystem.pushMatrix();
//            RenderSystem.translated(size + (posY * 2), 0, 0);
//            RenderSystem.rotated(90, 0, 0, 1);
//        }
//
//        RenderSystem.color3f(1F, 1F, 1F);
//        gui.blit(posX, posY, 0, 0, 14, size);
//        gui.blit(posX, posY + size - 1, 0, 255, 14, 1);
//        gui.blit(posX + 1, posY + size - draw - 1, 14, size - draw, 12, draw);
//
//        if (horizontal) {
//            RenderSystem.popMatrix();
//        }
//
//        if (toolTip && inRect) {
//            List<String> list = new ArrayList<String>();
//            list.add(InfoHelper.ITC() + I18n.format("gui.de.energyStorage.txt"));
//            list.add(InfoHelper.HITC() + Utils.formatNumber(energy) + " / " + Utils.formatNumber(maxEnergy));
//            list.add(TextFormatting.GRAY + "[" + Utils.addCommas(energy) + " RF]");
//            drawHoveringText(list, mouseX, mouseY, Minecraft.getInstance().fontRenderer, Minecraft.getInstance().getMainWindow().getWidth(), Minecraft.getInstance().getMainWindow().getHeight());
//        }
//    }

    public static void drawGuiBaseBackground(AbstractGui gui, int posX, int posY, int xSize, int ySize) {
        ResourceHelperBC.bindTexture("textures/gui/light/background_dynamic.png");
        RenderSystem.color3f(1F, 1F, 1F);
        MatrixStack matrixstack = new MatrixStack();
        gui.blit(matrixstack, posX, posY, 0, 0, xSize - 4, ySize - 4);
        gui.blit(matrixstack, posX + xSize - 4, posY, 252, 0, 4, ySize - 4);
        gui.blit(matrixstack, posX, posY + ySize - 4, 0, 252, xSize - 4, 4);
        gui.blit(matrixstack, posX + xSize - 4, posY + ySize - 4, 252, 252, 4, 4);
    }

    /**
     * Draws the players inventory slots into the gui.
     * note. X-Size is 162
     */
    @Deprecated
    public static void drawPlayerSlots(AbstractGui gui, int posX, int posY, boolean center) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        RenderMaterial mat = BCSprites.getThemed("slot");
        ResourceHelperBC.bindTexture(mat.atlasLocation());
        IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

        if (center) {
            posX -= 81;
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                drawSprite(mat.buffer(getter, BCSprites::makeType), posX + x * 18, posY + y * 18, 18, 18, mat.sprite(), 0);
            }
        }

        for (int x = 0; x < 9; x++) {
            drawSprite(mat.buffer(getter, BCSprites::makeType), posX + x * 18, posY + 58, 18, 18, mat.sprite(), 0);
        }
        getter.endBatch();
    }

    @Deprecated
    public static void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color, boolean dropShadow) {
        MatrixStack matrixstack = new MatrixStack();
        if (dropShadow) {
            fontRenderer.drawShadow(matrixstack, text, (float) (x - fontRenderer.width(text) / 2), (float) y, color);
        } else {
            fontRenderer.draw(matrixstack, text, (float) (x - fontRenderer.width(text) / 2), (float) y, color);
        }
    }

    public static void drawCenteredString(FontRenderer fontRenderer, MatrixStack matrixstack, String text, int x, int y, int color, boolean dropShadow) {
        if (dropShadow) {
            fontRenderer.drawShadow(matrixstack, text, (float) (x - fontRenderer.width(text) / 2), (float) y, color);
        } else {
            fontRenderer.draw(matrixstack, text, (float) (x - fontRenderer.width(text) / 2), (float) y, color);
        }
    }

    public static void drawBackgroundString(IVertexBuilder vertexBuilder, FontRenderer font, String text, float x, float y, int color, int background, int padding, boolean shadow, boolean centered) {
        MatrixStack matrixstack = new MatrixStack();
        int width = font.width(text);
        x = centered ? x - width / 2F : x;
        drawColouredRect(vertexBuilder, x - padding, y - padding, width + padding * 2, font.lineHeight - 2 + padding * 2, background, 0);
        if (shadow) {
            font.drawShadow(matrixstack, text, x, y, color);
        } else {
            font.draw(matrixstack, text, x, y, color);
        }
    }


//    public static void drawCenteredSplitString(FontRenderer fontRenderer, String str, int x, int y, int wrapWidth, int color, boolean dropShadow) {
//        for (String s : fontRenderer.listFormattedStringToWidth(str, wrapWidth)) {
//            drawCenteredString(fontRenderer, s, x, y, color, dropShadow);
//            y += fontRenderer.FONT_HEIGHT;
//        }
//    }
//
//    public static void drawStack2D(ItemStack stack, Minecraft mc, int x, int y, float scale) {
//        if (stack.isEmpty()) {
//            return;
//        }
////        RenderHelper.enableGUIStandardItemLighting();
//        RenderSystem.translatef(0.0F, 0.0F, 32.0F);
//        //this.zLevel = 200.0F;
//        mc.getItemRenderer().zLevel = 200.0F;
//        FontRenderer font = mc.fontRenderer;
//        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);
//        String count = stack.getCount() > 1 ? String.valueOf(stack.getCount()) : "";
//        mc.getItemRenderer().renderItemOverlayIntoGUI(font, stack, x, y, count);
//        //this.zLevel = 0.0F;
//        mc.getItemRenderer().zLevel = 0.0F;
//    }

//    public static void drawStack(ItemStack stack, Minecraft mc, int x, int y, float scale) {
//        if (stack.isEmpty()) {
//            return;
//        }
//        RenderSystem.pushMatrix();
//        RenderSystem.translated(x, y, 300);
//        RenderSystem.scaled(scale, scale, scale);
//        RenderSystem.rotated(180, 1, 0, 0);
//
//        mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
//
//        RenderSystem.popMatrix();
//    }

    public static void drawGradientRect(int posX, int posY, int xSize, int ySize, int colour, int colour2) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour2, 1F, 0);
    }

    @Deprecated
    public static void drawColouredRect(int posX, int posY, int xSize, int ySize, int colour) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour, 1F, 0);
    }

    @Deprecated
    public static void drawBorderedRect(int posX, int posY, int xSize, int ySize, int borderWidth, int fillColour, int borderColour) {
        drawColouredRect(posX, posY, xSize, borderWidth, borderColour);
        drawColouredRect(posX, posY + ySize - borderWidth, xSize, borderWidth, borderColour);

        drawColouredRect(posX, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);
        drawColouredRect(posX + xSize - borderWidth, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);

        drawColouredRect(posX + borderWidth, posY + borderWidth, xSize - (2 * borderWidth), ySize - (2 * borderWidth), fillColour);
    }

    public static void renderCuboid(Cuboid6 cuboid, float r, float g, float b, float a) {
        MatrixStack stack = new MatrixStack();
        IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        Matrix4 mat = new Matrix4(stack);
        IVertexBuilder builder = new TransformingVertexBuilder(getter.getBuffer(RenderType.lines()), mat);
        RenderUtils.bufferCuboidOutline(builder, cuboid.copy().expand(0.0020000000949949026D), r, g, b, a);
    }






    //New Stuff

    public static void drawShadedRect(IVertexBuilder builder, double x, double y, double width, double height, double borderWidth, int fill, int topLeftColour, int bottomRightColour, int cornerMixColour, double zLevel) {
        //Fill
        drawColouredRect(builder, x + borderWidth, y + borderWidth, width - borderWidth * 2, height - borderWidth * 2, fill, zLevel);
        //Top
        drawColouredRect(builder, x, y, width - borderWidth, borderWidth, topLeftColour, zLevel);
        //Left
        drawColouredRect(builder, x, y + borderWidth, borderWidth, height - borderWidth * 2, topLeftColour, zLevel);
        //Bottom
        drawColouredRect(builder, x + borderWidth, y + height - borderWidth, width - borderWidth, borderWidth, bottomRightColour, zLevel);
        //Right
        drawColouredRect(builder, x + width - borderWidth, y + borderWidth, borderWidth, height - borderWidth * 2, bottomRightColour, zLevel);
        //Top Right Corner
        drawColouredRect(builder, x + width - borderWidth, y, borderWidth, borderWidth, cornerMixColour, zLevel);
        //Bottom Left Corner
        drawColouredRect(builder, x, y + height - borderWidth, borderWidth, borderWidth, cornerMixColour, zLevel);
    }

    public static void drawColouredRect(IVertexBuilder builder, double posX, double posY, double xSize, double ySize, int colour, double zLevel) {
        drawGradientRect(builder, posX, posY, posX + xSize, posY + ySize, colour, colour, zLevel);
    }

    public static void drawGradientRect(IVertexBuilder builder, double left, double top, double right, double bottom, int startColor, int endColor, double zLevel) {
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
        builder.vertex(right,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        builder.vertex( left,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        builder.vertex( left, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        builder.vertex(right, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        //@formatter:on
    }

    public static void drawBorderedRect(IVertexBuilder builder, double posX, double posY, double xSize, double ySize, double borderWidth, int fillColour, int borderColour, double zLevel) {
        drawColouredRect(builder, posX, posY, xSize, borderWidth, borderColour, zLevel);
        drawColouredRect(builder, posX, posY + ySize - borderWidth, xSize, borderWidth, borderColour, zLevel);
        drawColouredRect(builder, posX, posY + borderWidth, borderWidth, ySize - 2 * borderWidth, borderColour, zLevel);
        drawColouredRect(builder, posX + xSize - borderWidth, posY + borderWidth, borderWidth, ySize - 2 * borderWidth, borderColour, zLevel);
        drawColouredRect(builder, posX + borderWidth, posY + borderWidth, xSize - 2 * borderWidth, ySize - 2 * borderWidth, fillColour, zLevel);
    }

    public static void drawDynamicSprite(IVertexBuilder builder, TextureAtlasSprite tex, int xPos, int yPos, int xSize, int ySize, int topTrim, int leftTrim, int bottomTrim, int rightTrim, int colour, double zLevel) {
        int texWidth = tex.getWidth();
        int texHeight = tex.getHeight();
        int trimWidth = texWidth - leftTrim - rightTrim;
        int trimHeight = texHeight - topTrim - bottomTrim;
        if (xSize <= texWidth) trimWidth = Math.min(trimWidth, xSize - rightTrim);
        if (xSize <= 0 || ySize <= 0 || trimWidth <= 0 || trimHeight <= 0) return;

        for (int x = 0; x < xSize; ) {
            int rWidth = Math.min(xSize - x, trimWidth);
            int trimU = 0;
            if (x != 0) {
                if (x + leftTrim + trimWidth <= xSize) {
                    trimU = leftTrim;
                } else {
                    trimU = (texWidth - (xSize - x));
                }
            }

            //Top & Bottom trim
            bufferTexturedModalRect(builder, tex, xPos + x, yPos, trimU, 0, rWidth, topTrim, colour, zLevel);
            bufferTexturedModalRect(builder, tex, xPos + x, yPos + ySize - bottomTrim, trimU, texHeight - bottomTrim, rWidth, bottomTrim, colour, zLevel);


            rWidth = Math.min(xSize - x - leftTrim - rightTrim, trimWidth);
            for (int y = 0; y < ySize; ) {
                int rHeight = Math.min(ySize - y - topTrim - bottomTrim, trimHeight);
                int trimV;
                if (y + (texHeight - topTrim - bottomTrim) <= ySize) {
                    trimV = topTrim;
                } else {
                    trimV = texHeight - (ySize - y);
                }

                //Left & Right trim
                if (x == 0 && y + topTrim < ySize - bottomTrim) {
                    bufferTexturedModalRect(builder, tex, xPos, yPos + y + topTrim, 0, trimV, leftTrim, rHeight, colour, zLevel);
                    bufferTexturedModalRect(builder, tex, xPos + xSize - rightTrim, yPos + y + topTrim, trimU + texWidth - rightTrim, trimV, rightTrim, rHeight, colour, zLevel);
                }

                //Core
                if (y + topTrim < ySize - bottomTrim && x + leftTrim < xSize - rightTrim) {
                    bufferTexturedModalRect(builder, tex, xPos + x + leftTrim, yPos + y + topTrim, leftTrim, topTrim, rWidth, rHeight, colour, zLevel);
                }
                y += trimHeight;
            }
            x += trimWidth;
        }
    }

    private static void bufferTexturedModalRect(IVertexBuilder builder, TextureAtlasSprite tex, int x, int y, double textureX, double textureY, int width, int height, int colour, double zLevel) {
        int w = tex.getWidth();
        int h = tex.getHeight();
        int[] colours = ColourARGB.unpack(colour);
        //@formatter:off
        builder.vertex(x,         y + height, zLevel).color(colours[1], colours[2], colours[3], colours[0]).uv(tex.getU((textureX / w) * 16D),          tex.getV(((textureY + height) / h) * 16)).endVertex();
        builder.vertex(x + width, y + height, zLevel).color(colours[1], colours[2], colours[3], colours[0]).uv(tex.getU(((textureX + width) / w) * 16), tex.getV(((textureY + height) / h) * 16)).endVertex();
        builder.vertex(x + width, y,          zLevel).color(colours[1], colours[2], colours[3], colours[0]).uv(tex.getU(((textureX + width) / w) * 16), tex.getV(((textureY) / h) * 16)).endVertex();
        builder.vertex(x,         y,          zLevel).color(colours[1], colours[2], colours[3], colours[0]).uv(tex.getU((textureX / w) * 16),           tex.getV(((textureY) / h) * 16)).endVertex();
        //@formatter:on
    }

    public static void drawSprite(IVertexBuilder builder, float x, float y, float width, float height, TextureAtlasSprite sprite, double zLevel) {
        //@formatter:off
        builder.vertex(x,          y + height, zLevel).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y + height, zLevel).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV1()).endVertex();
        builder.vertex(x + width,  y,          zLevel).color(1F, 1F, 1F, 1F).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(x,          y,          zLevel).color(1F, 1F, 1F, 1F).uv(sprite.getU0(), sprite.getV0()).endVertex();
        //@formatter:on
    }
}
