package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import net.minecraft.client.Minecraft;

import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class MGuiSlider extends MGuiElementBase {

    public IMGuiListener listener;
    public double position = 0;
    public boolean isVertical = false;
    private boolean isDraging = false;
    public int barColour = 0xFF000000;
    public int sliderColour = 0xFFFFFFFF;
    public int backFillColour = 0x00000000;
    public int backBorderColour = 0x00000000;
    public double backBorderWidth = 1;

    public MGuiSlider(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiSlider(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiSlider(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
    }

    @Override
    public void initElement() {
        super.initElement();
    }

    //region Render

    //endregion

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }


    //region Interact & Setters

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
    }

    public MGuiSlider setListener(IMGuiListener listener) {
        this.listener = listener;
        return this;
    }

    public MGuiSlider setColours(int barColour, int sliderColour) {
        this.barColour = barColour;
        this.sliderColour = sliderColour;
        return this;
    }

    public MGuiSlider setBackground(int backFillColour, int backBorderColour, int backBourderWidth) {
        this.backFillColour = backFillColour;
        this.backBorderColour = backBorderColour;
        this.backBorderWidth = backBourderWidth;
        return this;
    }

    public MGuiSlider setVertical(boolean vertical) {
        isVertical = vertical;
        return this;
    }

    //endregion
}
