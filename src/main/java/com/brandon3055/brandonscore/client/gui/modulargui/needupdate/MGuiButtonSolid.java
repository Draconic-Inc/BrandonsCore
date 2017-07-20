package com.brandon3055.brandonscore.client.gui.modulargui.needupdate;

import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 3/09/2016.
 */
@Deprecated //This is now built in to GuiButton
public class MGuiButtonSolid extends GuiButton {

    public int fillColour = 0xFF000000;
    public int borderColour = 0xFF555555;
    public int borderColourHover = 0xFF777777;

    public MGuiButtonSolid() {}

    public MGuiButtonSolid(int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(buttonId, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiButtonSolid(String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(xPos, yPos, xSize, ySize, buttonText);
        setButtonName(buttonName);
    }

    public MGuiButtonSolid(int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(xPos, yPos, xSize, ySize, buttonText);
    }


    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        String displayString = getDisplayString();
        if (trim && fontRenderer.getStringWidth(displayString) > xSize() - 4) {
            displayString = fontRenderer.trimStringToWidth(displayString, xSize() - 8) + "..";
        }

        boolean hovered = isMouseOver(mouseX, mouseY);
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, getFillColour(hovered, disabled), getBorderColour(hovered, disabled));

        int l = getTextColour(hovered, disabled);

        if (alignment == GuiAlign.CENTER) {
            drawCenteredString(fontRenderer, displayString, xPos() + xSize() / 2, yPos() + (ySize() - 8) / 2, l, dropShadow);
        }
        else {
            int buffer = 1 + ((ySize() - fontRenderer.FONT_HEIGHT) / 2);
            if (alignment == GuiAlign.LEFT) {
                drawString(fontRenderer, displayString, xPos() + buffer, yPos() + (ySize() - 8) / 2, l, dropShadow);
            }
            else {
                drawString(fontRenderer, displayString, ((xPos() + xSize()) - buffer) - fontRenderer.getStringWidth(displayString), yPos() + (ySize() - 8) / 2, l, dropShadow);
            }
        }
        GlStateManager.color(1, 1, 1, 1);
    }

    public int getFillColour(boolean hovering, boolean disabled) {
        return fillColour;
    }

    public int getBorderColour(boolean hovering, boolean disabled) {
        return hovering ? borderColourHover : borderColour;
    }

    public MGuiButtonSolid setColours(int fillColour, int borderColour, int borderColourHover) {
        this.fillColour = fillColour;
        this.borderColour = borderColour;
        this.borderColourHover = borderColourHover;
        return this;
    }
}
