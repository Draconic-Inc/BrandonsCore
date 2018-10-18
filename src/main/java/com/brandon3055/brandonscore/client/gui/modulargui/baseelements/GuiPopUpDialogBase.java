package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016. <br>
 * On its own this element does absolutely nothing<br>
 * But it makes it very easy to create a simple popup dialog that is bound to another static element.<br>
 * Simply add all the elements you need as child elements then call show() to display the dialog.<br>
 * The dialog will automatically show 10 display level above its parent element to avoid any potential zLevel issues.<br><br>
 *
 * The popup can automatically close if the player clicks outside the popup or if you call the close() method.<br>
 * setting canDrag to true makes the window draggable by clicking and dragging withing the top 10 pixels of the popup (adjustable)
 */
public class GuiPopUpDialogBase<E extends MGuiElementBase<E>> extends MGuiElementBase<E> {

    protected int dragXOffset = 0;
    protected int dragYOffset = 0;
    protected boolean dragging = false;
    protected boolean closeOnOutsideClick = true;
    protected boolean closeOnScroll = false;
    protected boolean isVisible = false;
    /**
     * Will close the dialog if one of its child elements capture a mouse click.
     * e.g. if a child button or button in a child list is pressed the dialog will close.
     * Useful for popup lists where you want the dialog to close after a selection is made.
     */
    protected boolean closeOnCapturedClick = false;
    protected Rectangle dragZone = null;

    public GuiPopUpDialogBase(MGuiElementBase parent) {
        this.setParent(parent);
        if (parent.modularGui == null) {
            throw new RuntimeException("GuiPopUpDialogBase parent has must be initialized!");
        }
        applyGeneralElementData(parent.modularGui, parent.mc, parent.screenWidth, parent.screenHeight, parent.fontRenderer);
    }

    public GuiPopUpDialogBase(int xPos, int yPos, MGuiElementBase parent) {
        super(xPos, yPos);
        this.setParent(parent);
        applyGeneralElementData(parent.modularGui, parent.mc, parent.screenWidth, parent.screenHeight, parent.fontRenderer);
    }

    public GuiPopUpDialogBase(int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(xPos, yPos, xSize, ySize);
        this.setParent(parent);
        applyGeneralElementData(parent.modularGui, parent.mc, parent.screenWidth, parent.screenHeight, parent.fontRenderer);
    }

    /**
     * Called once after creation, Use this to add any child elements.<br>
     * Note: the modularGui, mc, screenWidth and screenHeight fields *Should* be initialized at this point.
     * Assuming the parent modular gui is setup correctly.<br>
     * Also Note: In the case of GuiPopUpDialog's this will not be called until you
     */
    @Override
    public void addChildElements() {
        super.addChildElements();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (closeOnOutsideClick && !isMouseOver(mouseX, mouseY)) {
            close();
            return true;
        }

        boolean captured = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (captured && closeOnCapturedClick) {
            close();
        }

        if (!captured && dragZone != null && dragZone.contains(mouseX - xPos(), mouseY - yPos())) {
            dragging = true;
            dragXOffset = mouseX - xPos();
            dragYOffset = mouseY - yPos();
        }

        return captured;
    }

    @Override
    public boolean allowMouseOver(MGuiElementBase elementRequesting, int mouseX, int mouseY) {
        return true;
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

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        if (closeOnScroll) {
            close();
        }
        return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
    }

    @Override
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            close();
            return true;
        }
        return super.keyTyped(typedChar, keyCode);
    }

    /**
     * When set to true the this popup will close if the user clicks outside of its bounds.
     * Enabled by default.
     */
    public E setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        this.closeOnOutsideClick = closeOnOutsideClick;
        return (E) this;
    }

    /**
     * When set to true the this popup will close if the user scrolls
     * Disabled by default.
     */
    public E setCloseOnScroll(boolean closeOnScroll) {
        this.closeOnScroll = closeOnScroll;
        return (E) this;
    }

    /**
     * Define an area where the user can click to drag this element.
     * If null dragging is disabled.
     * Disabled by default.
     * Note: This is relative to this windows position so 0 xPos would be the this.xPos().
     */
    public E setDragZone(@Nullable Rectangle rectangle) {
        this.dragZone = rectangle;
        return (E) this;
    }

    /**
     * Define an area where the user can click to drag this element.
     * If null dragging is disabled.
     * Note: This is relative to this windows position so 0 xPos would be the this.xPos().
     */
    public E setDragZone(int x, int y, int width, int height) {
        this.dragZone = new Rectangle(x, y, width, height);
        return (E) this;
    }

    /**
     * Helper method for setting the drag zone to a bar of the specified height across the top of the popup.
     */
    public E setDragBar(int height) {
        return setDragZone(0, 0, xSize(), height);
    }

    /**
     * If set to true this popup will close if one of its child elements captures a mouse clock.
     * This can be useful for selection dialogs.
     */
    public E setCloseOnCapturedClick(boolean closeOnCapturedClick) {
        this.closeOnCapturedClick = closeOnCapturedClick;
        return (E) this;
    }

    /**
     * Display this popup with the given zOffset.
     */
    public void show(int displayZLevel) {
        modularGui.getManager().add(this, displayZLevel);
        isVisible = true;
    }

    /**
     * Display this popup with a zOffset of 500.
     */
    public void show() {
        int pz = getParent() == null ? 100 : getParent().displayZLevel;
        show(pz >= 500 ? pz + 50 : 500);
    }

    /**
     * Display this popup in the centre of the screen.
     */
    public void showCenter(int displayZLevel) {
        show(displayZLevel);
        setXPos((screenWidth / 2) - (xSize() / 2));
        setYPos((screenHeight / 2) - (ySize() / 2));
    }

    public void showCenter() {
        int pz = getParent() == null ? 100 : getParent().displayZLevel;
        showCenter(pz >= 500 ? pz + 50 : 500);
    }


    /**
     * Display this popup in the centre of the specified Element.
     */
    public void showCenter(MGuiElementBase centerOn, int displayZLevel) {
        show(displayZLevel);
        setXPos(centerOn.xPos() + (centerOn.xSize() / 2) - (xSize() / 2));
        setYPos(centerOn.yPos() + (centerOn.ySize() / 2) - (ySize() / 2));
    }

    public void showCenter(MGuiElementBase centerOn) {
        int pz = getParent() == null ? 100 : getParent().displayZLevel;
        showCenter(centerOn, pz >= 500 ? pz + 50 : 500);
    }

    /**
     * Close this popup. Popup can be reopened by calling show() again.
     */
    public void close() {
        modularGui.getManager().remove(this);
        isVisible = false;
    }

    public void toggleShown(boolean centre, int displayZLevel) {
        if (isVisible) close();
        else if (centre) {
            showCenter(displayZLevel);
        }
        else show(displayZLevel);
    }

    public void toggleShown(boolean centre) {
        toggleShown(centre, 500);
    }

    public void toggleShown() {
        toggleShown(true);
    }

    public boolean isVisible() {
        return isVisible;
    }
}
