package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiBorderedRect extends MGuiElementBase {

    public int fillColour = 0xFF000000;
    public int borderColour = 0xFFFFFFFF;
    public int borderWidth = 1;

    public MGuiBorderedRect(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiBorderedRect(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiBorderedRect(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
        drawBorderedRect(xPos, yPos, xSize, ySize, borderWidth, fillColour, borderColour);
    }

    public MGuiBorderedRect setFillColour(int fillColour) {
        this.fillColour = fillColour;
        return this;
    }

    public MGuiBorderedRect setBorderColour(int borderColour) {
        this.borderColour = borderColour;
        return this;
    }

    public MGuiBorderedRect setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }
}
