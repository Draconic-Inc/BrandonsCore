package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * Created by brandon3055 on 10/09/2016. <br>
 * On its own this element does absolutely nothing<br>
 * But it makes it very easy to create a simple popup dialog that is bound to another static element.<br>
 * Simply add all the elements you need as child elements then call show() to display the dialog.<br>
 * The dialog will automatically show 10 display level above its parent element to avoid any potential zLevel issues.<br><br>
 * <p>
 * The popup can automatically close if the player clicks outside the popup or if you call the close() method.<br>
 * setting canDrag to true makes the window draggable by clicking and dragging withing the top 10 pixels of the popup (adjustable)
 */
public class GuiPopUpDialogBase<E extends GuiElement<E>> extends GuiElement<E> {

    protected int dragXOffset = 0;
    protected int dragYOffset = 0;
    protected boolean dragging = false;
    protected boolean closeOnOutsideClick = true;
    protected boolean closeOnScroll = false;
    protected boolean isVisible = false;
    protected boolean blockOutsideClicks = false;

    /**
     * Will close the dialog if one of its child elements capture a mouse click.
     * e.g. if a child button or button in a child list is pressed the dialog will close.
     * Useful for popup lists where you want the dialog to close after a selection is made.
     */
    protected boolean closeOnCapturedClick = false;
    protected Rectangle dragZone = null;
    protected Runnable escapeCallback = null;
    protected Runnable closeCallback = null;

    public GuiPopUpDialogBase(GuiElement parent) {
        this.setParent(parent);
        if (parent.modularGui == null) {
            throw new RuntimeException("GuiPopUpDialogBase parent has must be initialized!");
        }
        applyGeneralElementData(parent.modularGui, parent.mc, parent.screenWidth, parent.screenHeight, parent.fontRenderer);
    }

    public GuiPopUpDialogBase(int xPos, int yPos, GuiElement parent) {
        super(xPos, yPos);
        this.setParent(parent);
        applyGeneralElementData(parent.modularGui, parent.mc, parent.screenWidth, parent.screenHeight, parent.fontRenderer);
    }

    public GuiPopUpDialogBase(int xPos, int yPos, int xSize, int ySize, GuiElement parent) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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
            dragXOffset = (int)mouseX - xPos();
            dragYOffset = (int)mouseY - yPos();
        }

        return captured || blockOutsideClicks;
    }

    @Override
    public boolean allowMouseOver(GuiElement elementRequesting, double mouseX, double mouseY) {
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        if (dragging) {
            double xMove = (mouseX - dragXOffset) - xPos();
            double yMove = (mouseY - dragYOffset) - yPos();
            translate((int)xMove, (int)yMove);
        }

        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDirection) {
        if (closeOnScroll) {
            close();
        }
        return super.handleMouseScroll(mouseX, mouseY, scrollDirection) || blockOutsideClicks;
    }

    @Override
    protected boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 1) {
            close();
            if (escapeCallback != null) {
                escapeCallback.run();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int charCode) {
        return super.charTyped(charTyped, charCode);
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

    public E setBlockOutsideClicks(boolean blockOutsideClicks) {
        this.blockOutsideClicks = blockOutsideClicks;
        return (E) this;
    }

    public E setEscapeCallback(Runnable escapeCallback) {
        this.escapeCallback = escapeCallback;
        return (E) this;
    }

    public E setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
        return (E) this;
    }

    /**
     * Display this popup with the given zOffset.
     */
    public void show(int displayZLevel) {
        modularGui.getManager().addChild(this, displayZLevel, false);
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
    public void showCenter(GuiElement centerOn, int displayZLevel) {
        show(displayZLevel);
        setXPos(centerOn.xPos() + (centerOn.xSize() / 2) - (xSize() / 2));
        setYPos(centerOn.yPos() + (centerOn.ySize() / 2) - (ySize() / 2));
    }

    public void showCenter(GuiElement centerOn) {
        int pz = getParent() == null ? 100 : getParent().displayZLevel;
        showCenter(centerOn, pz >= 500 ? pz + 50 : 500);
    }

    /**
     * Close this popup. Popup can be reopened by calling show() again.
     */
    public void close() {
        modularGui.getManager().removeChild(this);
        if (getParent() != null && getParent().hasChild(this)) {
            getParent().removeChild(this);
        }
        isVisible = false;
        if (closeCallback != null) {
            closeCallback.run();
        }
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

    /**
     * This implementation exists for the sole purpose oof being able to use the raw GuiPopUpDialogBase without broken generics.
     * */
    public static class PopoutDialog extends GuiPopUpDialogBase<PopoutDialog> {
        public PopoutDialog(GuiElement parent) {
            super(parent);
        }

        public PopoutDialog(int xPos, int yPos, GuiElement parent) {
            super(xPos, yPos, parent);
        }

        public PopoutDialog(int xPos, int yPos, int xSize, int ySize, GuiElement parent) {
            super(xPos, yPos, xSize, ySize, parent);
        }
    }
}
