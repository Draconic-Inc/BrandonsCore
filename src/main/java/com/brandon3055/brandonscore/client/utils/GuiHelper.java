package com.brandon3055.brandonscore.client.utils;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 28/06/2014.
 */
public class GuiHelper {
    public static final double PXL128 = 0.0078125;
    public static final double PXL256 = 0.00390625;

    public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY) {
        return ((mouseX >= x && mouseX <= x + xSize) && (mouseY >= y && mouseY <= y + ySize));
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
        drawTexturedRect(x, y, width, height, u, v, width, height, 0, PXL256);
    }

    public static void drawTexturedRect(double x, double y, double width, double height, int u, int v, int uSize, int vSize, double zLevel, double pxl) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.pos(x, y + height, zLevel).tex(u * pxl, (v + vSize) * pxl).endVertex();
        vertexBuffer.pos(x + width, y + height, zLevel).tex((u + uSize) * pxl, (v + vSize) * pxl).endVertex();
        vertexBuffer.pos(x + width, y, zLevel).tex((u + uSize) * pxl, v * pxl).endVertex();
        vertexBuffer.pos(x, y, zLevel).tex(u * pxl, v * pxl).endVertex();
        tessellator.draw();
    }

    public static void drawHoveringText(List list, int x, int y, FontRenderer font, int guiWidth, int guiHeight) {
        net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(list, x, y, guiWidth, guiHeight, -1, font);
    }

    public static void drawHoveringTextScaled(List list, int mouseX, int mouseY, FontRenderer font, float fade, double scale, int guiWidth, int guiHeight) {
        if (!list.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.scale(scale, scale, 1);
            mouseX = (int) (mouseX / scale);
            mouseY = (int) (mouseY / scale);

            int tooltipTextWidth = 0;

            for (Object aList : list) {
                String s = (String) aList;
                int l = font.getStringWidth(s);

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
                GlStateManager.enableBlend();
                GlStateManager.disableAlpha();
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                font.drawStringWithShadow(s1, tooltipX, tooltipY, ((int) (fade * 240F) + 0x10 << 24) | 0x00FFFFFF);
                GlStateManager.enableAlpha();
                tooltipY += 10;
                ++i2;
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int colour1, int colour2, float fade, double zLevel) {
        float f = ((colour1 >> 24 & 255) / 255.0F) * fade;
        float f1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float f2 = (float) (colour1 >> 8 & 255) / 255.0F;
        float f3 = (float) (colour1 & 255) / 255.0F;
        float f4 = ((colour2 >> 24 & 255) / 255.0F) * fade;
        float f5 = (float) (colour2 >> 16 & 255) / 255.0F;
        float f6 = (float) (colour2 >> 8 & 255) / 255.0F;
        float f7 = (float) (colour2 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double) right, (double) top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double) left, (double) top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double) left, (double) bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos((double) right, (double) bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /**
     * Draws a simple vertical energy bar with no tool tip
     */
    public static void drawEnergyBar(Gui gui, int posX, int posZ, int size, long energy, long maxEnergy) {
        drawEnergyBar(gui, posX, posZ, size, false, energy, maxEnergy, false, 0, 0);
    }

    /**
     * Draws an energy bar in a gui at the given position
     *
     * @param size       is the length of the energy bar.
     * @param horizontal will rotate the bar clockwise 90 degrees.
     */
    @SuppressWarnings("all")
    public static void drawEnergyBar(Gui gui, int posX, int posY, int size, boolean horizontal, long energy, long maxEnergy, boolean toolTip, int mouseX, int mouseY) {
        ResourceHelperBC.bindTexture("textures/gui/energy_gui.png");
        int draw = (int) ((double) energy / (double) maxEnergy * (size - 2));

        boolean inRect = isInRect(posX, posY, size, 14, mouseX, mouseY);

        if (horizontal) {
            int x = posY;
            posY = posX;
            posX = x;
            GlStateManager.pushMatrix();
            GlStateManager.translate(size + (posY * 2), 0, 0);
            GlStateManager.rotate(90, 0, 0, 1);
        }

        GlStateManager.color(1F, 1F, 1F);
        gui.drawTexturedModalRect(posX, posY, 0, 0, 14, size);
        gui.drawTexturedModalRect(posX, posY + size - 1, 0, 255, 14, 1);
        gui.drawTexturedModalRect(posX + 1, posY + size - draw - 1, 14, size - draw, 12, draw);

        if (horizontal) {
            GlStateManager.popMatrix();
        }

        if (toolTip && inRect) {
            List<String> list = new ArrayList<String>();
            list.add(InfoHelper.ITC() + I18n.translateToLocal("gui.de.energyStorage.txt"));
            list.add(InfoHelper.HITC() + Utils.formatNumber(energy) + " / " + Utils.formatNumber(maxEnergy));
            list.add(TextFormatting.GRAY + "[" + Utils.addCommas(energy) + " RF]");
            drawHoveringText(list, mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        }
    }

    public static void drawGuiBaseBackground(Gui gui, int posX, int posY, int xSize, int ySize) {
        ResourceHelperBC.bindTexture("textures/gui/base_gui.png");
        GlStateManager.color(1F, 1F, 1F);
        gui.drawTexturedModalRect(posX, posY, 0, 0, xSize - 3, ySize - 3);
        gui.drawTexturedModalRect(posX + xSize - 3, posY, 253, 0, 3, ySize - 3);
        gui.drawTexturedModalRect(posX, posY + ySize - 3, 0, 253, xSize - 3, 3);
        gui.drawTexturedModalRect(posX + xSize - 3, posY + ySize - 3, 253, 253, 3, 3);
    }

    /**
     * Draws the players inventory slots into the gui.
     * note. X-Size is 162
     */
    public static void drawPlayerSlots(Gui gui, int posX, int posY, boolean center) {
        ResourceHelperBC.bindTexture("textures/gui/bc_widgets.png");

        if (center) {
            posX -= 81;
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                gui.drawTexturedModalRect(posX + x * 18, posY + y * 18, 138, 0, 18, 18);
            }
        }

        for (int x = 0; x < 9; x++) {
            gui.drawTexturedModalRect(posX + x * 18, posY + 58, 138, 0, 18, 18);
        }
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color, boolean dropShadow) {
        fontRenderer.drawString(text, (float) (x - fontRenderer.getStringWidth(text) / 2), (float) y, color, dropShadow);
    }

    public static void drawCenteredSplitString(FontRenderer fontRenderer, String str, int x, int y, int wrapWidth, int color, boolean dropShadow) {
        for (String s : fontRenderer.listFormattedStringToWidth(str, wrapWidth)) {
            drawCenteredString(fontRenderer, s, x, y, color, dropShadow);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    public static void drawStack2D(ItemStack stack, Minecraft mc, int x, int y, float scale) {
        if (stack == null || stack.getItem() == null) {
            return;
        }
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        //this.zLevel = 200.0F;
        mc.getRenderItem().zLevel = 200.0F;
        FontRenderer font = mc.fontRendererObj;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        String count = stack.stackSize > 1 ? String.valueOf(stack.stackSize) : "";
        mc.getRenderItem().renderItemOverlayIntoGUI(font, stack, x, y, count);
        //this.zLevel = 0.0F;
        mc.getRenderItem().zLevel = 0.0F;
    }

    public static void drawStack(ItemStack stack, Minecraft mc, int x, int y, float scale) {
        if (stack == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 300);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180, 1, 0, 0);

        mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);

        GlStateManager.popMatrix();
    }

    public static void drawGradientRect(int posX, int posY, int xSize, int ySize, int colour, int colour2) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour2, 1F, 0);
    }

    public static void drawColouredRect(int posX, int posY, int xSize, int ySize, int colour) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour, 1F, 0);
    }

    public static void drawBorderedRect(int posX, int posY, int xSize, int ySize, int borderWidth, int fillColour, int borderColour) {
        drawColouredRect(posX, posY, xSize, borderWidth, borderColour);
        drawColouredRect(posX, posY + ySize - borderWidth, xSize, borderWidth, borderColour);

        drawColouredRect(posX, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);
        drawColouredRect(posX + xSize - borderWidth, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);

        drawColouredRect(posX + borderWidth, posY + borderWidth, xSize - (2 * borderWidth), ySize - (2 * borderWidth), fillColour);
    }
}
