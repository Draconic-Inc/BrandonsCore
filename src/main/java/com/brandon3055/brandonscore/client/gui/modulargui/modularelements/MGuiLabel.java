package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.EnumAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is button based on GuiButton with similar appearance and functionality.
 * Extend this if you want to create a custom button element that calls elementButtonAction in your IModularGui
 */
public class MGuiLabel extends MGuiElementBase {

    public String displayString = "";
    public EnumAlignment alignment = EnumAlignment.CENTER;
    public boolean dropShadow = true;
    public int textColour = 0xFFFFFF;
    private int rotation = 0;
    public boolean wrap = false;
    public boolean trim = false;

    public MGuiLabel(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiLabel(IModularGui gui, int xPos, int yPos, int xSize, int ySize, String displayString) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = displayString;
    }

    @Override
    public void renderBackgroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.renderBackgroundLayer(mc, mouseX, mouseY, partialTicks);
        FontRenderer fontrenderer = mc.fontRendererObj;
        int colour = getTextColour();
        int fh = mc.fontRendererObj.FONT_HEIGHT;
        String text = getDisplayString();

        if (rotation == 0) {
            int buffer = 5;
            if (trim && fontrenderer.getStringWidth(text) > xSize - 4) {
                text = fontrenderer.trimStringToWidth(text, xSize - 8) + "...";
            }

            if (alignment == EnumAlignment.CENTER) {
                if (wrap) {
                    float wrapCount = fontrenderer.listFormattedStringToWidth(text, xSize - (buffer * 2)).size();
                    drawCenteredSplitString(fontrenderer, text, xPos + xSize / 2, yPos - (fh / 2) + (ySize / 2F) - ((fh * (wrapCount - 1)) / 2), xSize - (buffer * 2), colour, getDropShadow());
                }
                else {
                    drawCenteredString(fontrenderer, text, xPos + xSize / 2, yPos + (ySize - 8) / 2, colour, getDropShadow());
                }
            }
            else {
                if (alignment == EnumAlignment.LEFT) {
                    if (wrap) {
                        float wrapCount = fontrenderer.listFormattedStringToWidth(text, xSize - (buffer * 2)).size();
                        drawSplitString(fontrenderer, text, xPos + buffer, yPos - (fh / 2) + (ySize / 2F) - ((fh * (wrapCount - 1)) / 2), xSize - (buffer * 2), colour, getDropShadow());
                    }
                    else {
                        drawString(fontrenderer, text, xPos + buffer, yPos + (ySize - 8) / 2, colour, getDropShadow());
                    }
                }
                else {
                    drawString(fontrenderer, text, ((xPos + xSize) - buffer) - fontrenderer.getStringWidth(text), yPos + (ySize - 8) / 2, colour, getDropShadow());
                }
            }
        }
        else {
            if (trim && fontrenderer.getStringWidth(text) > ySize - 4) {
                text = fontrenderer.trimStringToWidth(text, ySize - 8) + "...";
            }
            boolean rotateBottomToTop = rotation == -1;
            GlStateManager.pushMatrix();

            GlStateManager.translate(xPos + xSize / 2, yPos + (ySize / 2) - (fh / 2), 0);
            GlStateManager.rotate(rotateBottomToTop ? -90 : 90, 0, 0, 1);
            GlStateManager.translate(-(xPos + xSize / 2), -(yPos + (ySize / 2) - (fh / 2)), 0);

            int translation = fh / 2;

            GlStateManager.translate(rotateBottomToTop ? -translation : translation, -translation, 0);

            if (alignment == EnumAlignment.CENTER) {
                drawCenteredString(mc.fontRendererObj, text, xPos + xSize / 2, yPos + (ySize / 2) - (fh / 2), colour, getDropShadow());
            }
            else {
                int buffer = 1 + ((xSize - fh) / 2);
                if (alignment == EnumAlignment.LEFT) {
                    drawString(mc.fontRendererObj, text, xPos + xSize / 2 - ySize / 2 + buffer, yPos + (ySize / 2) - (fh / 2), colour, getDropShadow());
                }
                else {
                    drawString(mc.fontRendererObj, text, xPos + xSize / 2 + ySize / 2 - fontrenderer.getStringWidth(text) - buffer, yPos + (ySize / 2) - (fh / 2), colour, getDropShadow());
                }
            }

            GlStateManager.popMatrix();
        }
        GlStateManager.color(1, 1, 1, 1);
    }

    public MGuiLabel setDisplayString(String displayString) {
        this.displayString = displayString;
        return this;
    }

    public String getDisplayString() {
        return displayString;
    }

    public MGuiLabel setAlignment(EnumAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public MGuiLabel setTextColour(int textColour) {
        this.textColour = textColour;
        return this;
    }

    public int getTextColour() {
        return textColour;
    }

    public MGuiLabel setShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public boolean getDropShadow() {
        return dropShadow;
    }

    public MGuiLabel setRotation(int rotation) {
        if (rotation > 1) {
            rotation = 1;
        }
        else if (rotation < -1) {
            rotation = -1;
        }
        this.rotation = rotation;
        return this;
    }

    /**
     * Dose not support rotated ot Right Aligned labels.
     */
    public MGuiLabel setWrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    public MGuiLabel setTrim(boolean trim) {
        this.trim = trim;
        return this;
    }
}
