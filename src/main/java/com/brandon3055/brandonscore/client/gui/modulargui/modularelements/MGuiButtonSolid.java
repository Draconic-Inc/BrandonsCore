package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiButtonSolid extends MGuiButton {

    public int fillColour = 0xFF000000;
    public int borderColour = 0xFF555555;
    public int borderColourHover = 0xFF777777;

    public MGuiButtonSolid(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiButtonSolid(IModularGui gui, int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, buttonId, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiButtonSolid(IModularGui gui, String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, buttonName, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiButtonSolid(IModularGui gui, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize, buttonText);
    }


    @Override
    public void renderBackgroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        drawBorderedRect(xPos, yPos, xSize, ySize, 1, getFillColour(hovered), getBorderColour(hovered));

        drawCenteredString(mc.fontRendererObj, displayString, xPos + xSize / 2, yPos + (ySize / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), 0xFFFFFF, false);
    }
    
    protected int getFillColour(boolean hovering) {
        return fillColour;
    }

    protected int getBorderColour(boolean hovering) {
        return hovering ? borderColourHover : borderColour;
    }

    public MGuiButtonSolid setColours(int fillColour, int borderColour, int borderColourHover) {
        this.fillColour = fillColour;
        this.borderColour = borderColour;
        this.borderColourHover = borderColourHover;
        return this;
    }
}
