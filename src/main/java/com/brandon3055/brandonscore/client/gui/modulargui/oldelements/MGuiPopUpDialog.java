package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.utils.GuiHelper;

import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016. <br>
 * On its own this element does absolutely nothing (visible to the user)<br>
 * But it makes it very easy to create a simple popup dialog that is bound to another static element.<br>
 * Simply add all the elements you need as child elements then call show() to display the dialog.<br>
 * The dialog will automatically show 1 display level above its parent element to avoid any potential zLevel issues.<br><br>
 *
 * The popup will automatically close if the player clicks outside the popup or if you call the close() method.<br>
 * setting canDrag to true makes the window draggable by clicking and dragging withing the top 10 pixels of the popup (adjustable)
 */
public class MGuiPopUpDialog extends MGuiElementBase {

    public final MGuiElementBase parent;
    public boolean closeOnOutsideClick = true;
    /**
     * Will close the dialog if one of its children capture a mouse click.
     * e.g. if a child button or button in a child list is pressed the dialog will close.
     * Very useful for popup lists where you want the dialog to close after a selection is made.
     */
    public boolean closeOnCapturedClick = false;
    public boolean dragging = false;
    public int dragXOffset = 0;
    public int dragYOffset = 0;
    public boolean canDrag = false;
    public int dragZoneSize = 10;

    public MGuiPopUpDialog(MGuiElementBase parent) {
        this.parent = parent;
        this.mc = parent.mc;
        this.fontRenderer = parent.fontRenderer;
    }

    public MGuiPopUpDialog(int xPos, int yPos, MGuiElementBase parent) {
        super(xPos, yPos);
        this.parent = parent;
        this.mc = parent.mc;
        this.fontRenderer = parent.fontRenderer;
    }

    public MGuiPopUpDialog(int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(xPos, yPos, xSize, ySize);
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

        if (canDrag && GuiHelper.isInRect(xPos(), yPos(), xSize(), dragZoneSize, mouseX, mouseY)) {
            dragging = true;
            dragXOffset = mouseX - xPos();
            dragYOffset = mouseY - yPos();
        }

        boolean flag = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (flag && closeOnCapturedClick) {
            close();
        }
        return flag;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            int xMove = (mouseX - dragXOffset) - xPos();
            int yMove = (mouseY - dragYOffset) - yPos();

            translate(xMove, yMove);
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

    public MGuiPopUpDialog setCanDrag(boolean canDrag) {
        this.canDrag = canDrag;
        return this;
    }

    public MGuiPopUpDialog setCloseOnCapturedClick(boolean closeOnCapturedClick) {
        this.closeOnCapturedClick = closeOnCapturedClick;
        return this;
    }

    public void show() {
        modularGui.getManager().add(this, parent.displayLevel + 1);
    }

    public void close() {
        modularGui.getManager().remove(this);
    }
}
