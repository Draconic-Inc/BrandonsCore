package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is button based on GuiButton with similar appearance and functionality.
 * Extend this if you want to create a custom button element that calls elementButtonAction in your IModularGui
 */
public class GuiLabel extends MGuiElementBase<GuiLabel> {

//    public String displayString = "";
//    public GuiAlign alignment = GuiAlign.CENTER;
//    public boolean dropShadow = true;
//    public int textColour = 0xFFFFFF;
//    private int rotation = 0;
//    public boolean wrap = false;
//    public boolean trim = false;
//
//    public GuiLabel() {
//    }
//
//    public GuiLabel(int xPos, int yPos, int xSize, int ySize, String displayString) {
//        super(xPos, yPos, xSize, ySize);
//        this.displayString = displayString;
//    }
//
//    @Override
//    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//        int xPos = xPos();
//        int yPos = yPos();
//        int xSize = xSize();
//        int ySize = ySize();
//
//        super.renderElement(mc, mouseX, mouseY, partialTicks);
//        FontRenderer fontrenderer = mc.fontRendererObj;
//        int colour = getTextColour();
//        int fh = mc.fontRendererObj.FONT_HEIGHT;
//        String text = getDisplayString();
//
//        if (rotation == 0) {
//            int buffer = 5;
//            if (trim && fontrenderer.getStringWidth(text) > xSize - 4) {
//                text = fontrenderer.trimStringToWidth(text, xSize - 8) + "...";
//            }
//
//            if (alignment == GuiAlign.CENTER) {
//                if (wrap) {
//                    float wrapCount = fontrenderer.listFormattedStringToWidth(text, xSize - (buffer * 2)).size();
//                    drawCenteredSplitString(fontrenderer, text, xPos + xSize / 2, yPos - (fh / 2) + (ySize / 2F) - ((fh * (wrapCount - 1)) / 2), xSize - (buffer * 2), colour, getDropShadow());
//                }
//                else {
//                    drawCenteredString(fontrenderer, text, xPos + xSize / 2, yPos + (ySize - 8) / 2, colour, getDropShadow());
//                }
//            }
//            else {
//                if (alignment == GuiAlign.LEFT) {
//                    if (wrap) {
//                        float wrapCount = fontrenderer.listFormattedStringToWidth(text, xSize - (buffer * 2)).size();
//                        drawSplitString(fontrenderer, text, xPos + buffer, yPos - (fh / 2) + (ySize / 2F) - ((fh * (wrapCount - 1)) / 2), xSize - (buffer * 2), colour, getDropShadow());
//                    }
//                    else {
//                        drawString(fontrenderer, text, xPos + buffer, yPos + (ySize - 8) / 2, colour, getDropShadow());
//                    }
//                }
//                else {
//                    drawString(fontrenderer, text, ((xPos + xSize) - buffer) - fontrenderer.getStringWidth(text), yPos + (ySize - 8) / 2, colour, getDropShadow());
//                }
//            }
//        }
//        else {
//            if (trim && fontrenderer.getStringWidth(text) > ySize - 4) {
//                text = fontrenderer.trimStringToWidth(text, ySize - 8) + "...";
//            }
//            boolean rotateBottomToTop = rotation == -1;
//            GlStateManager.pushMatrix();
//
//            GlStateManager.translate(xPos + xSize / 2, yPos + (ySize / 2) - (fh / 2), 0);
//            GlStateManager.rotate(rotateBottomToTop ? -90 : 90, 0, 0, 1);
//            GlStateManager.translate(-(xPos + xSize / 2), -(yPos + (ySize / 2) - (fh / 2)), 0);
//
//            int translation = fh / 2;
//
//            GlStateManager.translate(rotateBottomToTop ? -translation : translation, -translation, 0);
//
//            if (alignment == GuiAlign.CENTER) {
//                drawCenteredString(mc.fontRendererObj, text, xPos + xSize / 2, yPos + (ySize / 2) - (fh / 2), colour, getDropShadow());
//            }
//            else {
//                int buffer = 1 + ((xSize - fh) / 2);
//                if (alignment == GuiAlign.LEFT) {
//                    drawString(mc.fontRendererObj, text, xPos + xSize / 2 - ySize / 2 + buffer, yPos + (ySize / 2) - (fh / 2), colour, getDropShadow());
//                }
//                else {
//                    drawString(mc.fontRendererObj, text, xPos + xSize / 2 + ySize / 2 - fontrenderer.getStringWidth(text) - buffer, yPos + (ySize / 2) - (fh / 2), colour, getDropShadow());
//                }
//            }
//
//            GlStateManager.popMatrix();
//        }
//        GlStateManager.color(1, 1, 1, 1);
//    }
//
//    public GuiLabel setDisplayString(String displayString) {
//        this.displayString = displayString;
//        return this;
//    }
//
//    public String getDisplayString() {
//        return displayString;
//    }
//
//    public GuiLabel setAlignment(GuiAlign alignment) {
//        this.alignment = alignment;
//        return this;
//    }
//
//    public GuiLabel setTextColour(int textColour) {
//        this.textColour = textColour;
//        return this;
//    }
//
//    public int getTextColour() {
//        return textColour;
//    }
//
//    public GuiLabel setShadow(boolean dropShadow) {
//        this.dropShadow = dropShadow;
//        return this;
//    }
//
//    public boolean getDropShadow() {
//        return dropShadow;
//    }
//
//    public GuiLabel setRotation(int rotation) {
//        if (rotation > 1) {
//            rotation = 1;
//        }
//        else if (rotation < -1) {
//            rotation = -1;
//        }
//        this.rotation = rotation;
//        return this;
//    }
//
//    /**
//     * Dose not support rotated ot Right Aligned labels.
//     */
//    public GuiLabel setWrap(boolean wrap) {
//        this.wrap = wrap;
//        return this;
//    }
//
//    public GuiLabel setTrim(boolean trim) {
//        this.trim = trim;
//        return this;
//    }
}
