package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class MGuiSlider extends MGuiElementBase {

    public IMGuiListener listener;
    public double position = 0;
    public boolean horizontal = false;    
    private boolean isDragging = false;
    public int barColour = 0xFF000000;
    public int sliderColour = 0xFFFFFFFF;
    public int backFillColour = 0x00000000;
    public int backBorderColour = 0x00000000;
    public double backBorderWidth = 1;
    protected double increment = 0.01;
    protected double shiftIncrement = 0.1;
    protected double barSize = 1;
    
    public MGuiSlider(IModularGui modularGui) {
        super(modularGui);
        barSize = horizontal ? xSize / 15D : ySize / 15D;
    }

    public MGuiSlider(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
        barSize = horizontal ? xSize / 15D : ySize / 15D;
    }

    public MGuiSlider(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
        barSize = horizontal ? xSize / 15D : ySize / 15D;
    }

    @Override
    public void initElement() {
        super.initElement();
    }

    //region Render

    //endregion

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (horizontal) {
            drawColouredRect(xPos, yPos + (ySize / 2D) - 0.5, xSize, 1, barColour);
            drawColouredRect(xPos, yPos, 1, ySize, barColour);
            drawColouredRect(xPos + xSize - 1, yPos, 1, ySize, barColour);
        }
        else {
            drawColouredRect(xPos + (xSize / 2D) - 0.5, yPos, 1, ySize, barColour);
            drawColouredRect(xPos, yPos, xSize, 1, barColour);
            drawColouredRect(xPos, yPos + ySize - 1, xSize, 1, barColour);
        }

        double barWidth = horizontal ? barSize : xSize;
        double barHeight = horizontal ? ySize : barSize;
        double barXPos = horizontal ? xPos + 1 + position * (xSize - 2 - barWidth) : xPos;
        double barYPos = horizontal ? yPos : yPos + 1 + position * (ySize - 2 - barHeight);

        drawBorderedRect(barXPos, barYPos, barWidth, barHeight, 0.5, sliderColour, mixColours(sliderColour, 0x00404040, true));
    }

    //region Interact & Setters

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        if (isMouseOver(mouseX, mouseY)) {
            double increment = GuiScreen.isShiftKeyDown() ? shiftIncrement : this.increment;
            setPos(position + (scrollDirection > 0 ? -increment : increment));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            double barWidth = horizontal ? barSize : xSize - 2;
            double barHeight = horizontal ? ySize - 2 : barSize;
            double barXPos = horizontal ? xPos + 1 + position * (xSize - 2 - barWidth) : xPos + 1;
            double barYPos = horizontal ? yPos + 1 : yPos + 1 + position * (ySize - 2 - barHeight);
            if (GuiHelper.isInRect((int)barXPos, (int)barYPos, (int)barWidth, (int)barHeight, mouseX, mouseY)) {
                isDragging = true;
            }
            else {
                if (horizontal) {
                    double pos = mouseX - xPos - (barWidth / 2);
                    setPos(pos / xSize);
                }
                else {
                    double pos = mouseY - yPos - (barHeight / 2);
                    setPos(pos / ySize);
                }
                isDragging = true;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDragging) {
            double barWidth = horizontal ? barSize : xSize - 2;
            double barHeight = horizontal ? ySize - 2 : barSize;
            if (horizontal) {
                double pos = mouseX - xPos - (barWidth / 2);
                setPos(pos / (xSize - barWidth));
            }
            else {
                double pos = mouseY - yPos - (barHeight / 2);
                setPos(pos / (ySize - barHeight));
            }
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, state);
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
    
    public MGuiSlider setIncrements(double increment, double shiftIncrement) {
        this.increment = increment;
        this.shiftIncrement = shiftIncrement;
        return this;
    }

    public MGuiSlider setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
        barSize = horizontal ? xSize / 15D : ySize / 15D;
        return this;
    }

    public MGuiSlider setBarSize(double barSize) {
        this.barSize = barSize;
        return this;
    }

    /**
     * @return the position of the scroll bar between (0 to 1)
     */
    public double getPos() {
        return position;
    }

    /**
     * Sets the position of the scroll bar.
     * @param scrollPos position between 0 and 1
     */
    public void setPos(double scrollPos) {
        if (scrollPos > 1) {
            scrollPos = 1;
        }
        else if (scrollPos < 0) {
            scrollPos = 0;
        }

        this.position = scrollPos;

        if (listener != null) {
            listener.onMGuiEvent("SLIDER_MOVE", this);
        }
    }

    //endregion
}
