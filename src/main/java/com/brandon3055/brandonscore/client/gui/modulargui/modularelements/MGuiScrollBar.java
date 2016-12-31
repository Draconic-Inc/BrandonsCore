package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IScrollListener;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiScrollBar extends MGuiElementBase {

    private double scrollPos = 0;
    public int backColour = 0xFF000000;
    public int borderColour = 0xFFFFFFFF;
    public int scrollColour = 0xFF777777;
    public boolean horizontal = false;
    private boolean isDragging = false;
    public MGuiElementBase parentScrollable = null;
    public IScrollListener listener = null;
    protected double increment = 0.1;
    protected double shiftIncrement = 0.2;
    public double barSizeRatio = 20;
    public int clickOffset = 0;
    public double clickPos = 0;

    public MGuiScrollBar(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiScrollBar(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiScrollBar(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawBorderedRect(xPos, yPos, xSize, ySize, 1, getBackColour(), getBorderColour());

        double barWidth = horizontal ? xSize / barSizeRatio : xSize - 2;
        double barHeight = horizontal ? ySize - 2 : ySize / barSizeRatio;
        double barXPos = horizontal ? xPos + 1 + scrollPos * (xSize - 2 - barWidth) : xPos + 1;
        double barYPos = horizontal ? yPos + 1 : yPos + 1 + scrollPos * (ySize - 2 - barHeight);

        drawColouredRect(barXPos, barYPos, barWidth, barHeight, getScrollColour());

    }

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        if (isMouseOver(mouseX, mouseY) || (parentScrollable != null && parentScrollable.isMouseOver(mouseX, mouseY))) {
            double increment = GuiScreen.isShiftKeyDown() ? shiftIncrement : this.increment;
            setScrollPos(scrollPos + (scrollDirection > 0 ? -increment : increment));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            double barWidth = horizontal ? xSize / barSizeRatio : xSize - 2;
            double barHeight = horizontal ? ySize - 2 : ySize / barSizeRatio;
            double barXPos = horizontal ? xPos + 1 + scrollPos * (xSize - 2 - barWidth) : xPos + 1;
            double barYPos = horizontal ? yPos + 1 : yPos + 1 + scrollPos * (ySize - 2 - barHeight);
            if (GuiHelper.isInRect((int)barXPos, (int)barYPos, (int)barWidth, (int)barHeight, mouseX, mouseY)) {
                isDragging = true;
                clickOffset = horizontal ? mouseX : mouseY;
                clickPos = scrollPos;
            }
            else {
                if (horizontal) {
                    double pos = mouseX - xPos;
                    setScrollPos(pos / xSize);
                }
                else {
                    double pos = mouseY - yPos;
                    setScrollPos(pos / ySize);
                }
                isDragging = true;
                clickOffset = horizontal ? mouseX : mouseY;
                clickPos = scrollPos;
            }
        }

        return false;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDragging) {
            double barWidth = horizontal ? xSize / barSizeRatio : xSize - 2;
            double barHeight = horizontal ? ySize - 2 : ySize / barSizeRatio;
            if (horizontal) {
                int movement = mouseX - clickOffset;

                double pos = clickPos + ((double) movement / (xSize - barWidth));
//                double pos = mouseX - xPos - (barWidth / 2);
                setScrollPos(pos);// / (xSize - barWidth));
            }
            else {
                int movement = mouseY - clickOffset;

                double pos = clickPos + ((double) movement / (ySize - barHeight));

                //double pos = (mouseY + clickOffset) - yPos - (barHeight / 2);
                setScrollPos(pos);// / (ySize - barHeight));
            }
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * @return the position of the scroll bar between (0 to 1)
     */
    public double getScrollPos() {
        return scrollPos;
    }

    /**
     * Sets the position of the scroll bar.
     * @param scrollPos position between 0 and 1
     */
    public MGuiScrollBar setScrollPos(double scrollPos) {
        if (scrollPos > 1) {
            scrollPos = 1;
        }
        else if (scrollPos < 0) {
            scrollPos = 0;
        }

        this.scrollPos = scrollPos;

        if (listener != null) {
            listener.scrollBarMoved(this.scrollPos);
        }
        return this;
    }

    public MGuiScrollBar setListener(IScrollListener listener) {
        this.listener = listener;
        return this;
    }

    public void setIncrements(double increment, double shiftIncrement) {
        this.increment = increment;
        this.shiftIncrement = shiftIncrement;
    }

    public int getBackColour() {
        return backColour;
    }

    public int getBorderColour() {
        return borderColour;
    }

    public int getScrollColour() {
        return scrollColour;
    }

    /**
     * This sets how large the scroll bar is.
     * To calculate this value simply divide the maximum ySize of your window bu the actual ySize of your window.
     * Replace ySize with xSize if your window scrolls sideways.
     *
     * By maximum ySize the total ySize of all of the MGuiListElements.
     */
    public MGuiScrollBar setBarSizeRatio(double barSizeRatio) {
        this.barSizeRatio = barSizeRatio;
        return this;
    }
}
