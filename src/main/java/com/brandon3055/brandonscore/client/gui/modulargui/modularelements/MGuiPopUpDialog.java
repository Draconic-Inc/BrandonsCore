package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.utils.GuiHelper;

import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class MGuiPopUpDialog extends MGuiElementBase {

    public final MGuiElementBase parent;
    public boolean closeOnOutsideClick = true;
    public boolean dragging = false;
    public int dragXOffset = 0;
    public int dragYOffset = 0;
    public boolean canDrag = false;
    public int dragZoneSize = 10;

    public MGuiPopUpDialog(IModularGui modularGui, MGuiElementBase parent) {
        super(modularGui);
        this.parent = parent;
        this.mc = parent.mc;
        this.fontRenderer = parent.fontRenderer;
    }

    public MGuiPopUpDialog(IModularGui modularGui, int xPos, int yPos, MGuiElementBase parent) {
        super(modularGui, xPos, yPos);
        this.parent = parent;
        this.mc = parent.mc;
        this.fontRenderer = parent.fontRenderer;
    }

    public MGuiPopUpDialog(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.parent = parent;
        this.mc = parent.mc;
        this.fontRenderer = parent.fontRenderer;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (closeOnOutsideClick && !isMouseOver(mouseX, mouseY)) {
            close();
            return true;
        }

        if (canDrag && GuiHelper.isInRect(xPos, yPos, xSize, dragZoneSize, mouseX, mouseY)) {
            dragging = true;
            dragXOffset = mouseX - xPos;
            dragYOffset = mouseY - yPos;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
//         true;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            int xMove = (mouseX - dragXOffset) - xPos;
            int yMove = (mouseY - dragYOffset) - yPos;

            moveBy(xMove, yMove);
        }


        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;

        return super.mouseReleased(mouseX, mouseY, state);
    }

    public MGuiPopUpDialog setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        this.closeOnOutsideClick = closeOnOutsideClick;
        return this;
    }

    public void show() {
        modularGui.getManager().add(this, parent.displayLevel + 1);
    }

    public void close() {
        modularGui.getManager().remove(this);
//        parent.removeChild(this);
    }
}
