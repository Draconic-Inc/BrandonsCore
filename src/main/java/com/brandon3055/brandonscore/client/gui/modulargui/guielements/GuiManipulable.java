package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.CursorHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.function.Supplier;

public class GuiManipulable extends GuiElement<GuiManipulable> {

    protected int dragXOffset = 0;
    protected int dragYOffset = 0;
    protected boolean isDragging = false;
    protected boolean dragPos = false;
    protected boolean dragTop = false;
    protected boolean dragLeft = false;
    protected boolean dragBottom = false;
    protected boolean dragRight = false;
    protected boolean enableCursors = false;
    protected Dimension minSize = new Dimension(50, 50);
    protected Dimension maxSize = new Dimension(256, 256);
    protected Supplier<Boolean> canDrag = () -> true;
    protected Supplier<Boolean> canResizeV = () -> true;
    protected Supplier<Boolean> canResizeH = () -> true;
    protected PositionValidator dragZone = (x, y) -> GuiHelper.isInRect(xPos(), yPos(), xSize(), 20, x, y);
    protected PositionValidator topResizeZone = null;
    protected PositionValidator leftResizeZone = null;
    protected PositionValidator bottomResizeZone = null;
    protected PositionValidator rightResizeZone = null;
    protected Runnable onMovedCallback = null;
    protected Runnable onResizedCallback = null;
    protected PositionRestraint positionRestraint = GuiElement::normalizePosition;

    public void setCanDrag(Supplier<Boolean> canDrag) {
        this.canDrag = canDrag;
    }

    public void setCanResizeH(Supplier<Boolean> canResizeH) {
        this.canResizeH = canResizeH;
    }

    public void setCanResizeV(Supplier<Boolean> canResizeV) {
        this.canResizeV = canResizeV;
    }

    public void setDragBarHeight(int dragBarHeight) {
        dragZone = (x, y) -> GuiHelper.isInRect(xPos(), yPos(), xSize(), dragBarHeight, x, y);
    }

    public void setDragZone(PositionValidator dragZone) {
        this.dragZone = dragZone;
    }

    public void setOnMovedCallback(Runnable onMovedCallback) {
        this.onMovedCallback = onMovedCallback;
    }

    public void setOnResizedCallback(Runnable onResizedCallback) {
        this.onResizedCallback = onResizedCallback;
    }

    public void setPositionRestraint(PositionRestraint positionRestraint) {
        this.positionRestraint = positionRestraint;
    }

    public void setEnableCursors(boolean enableCursors) {
        this.enableCursors = enableCursors;
    }

    public void startDragging() {
        double mouseX = getMouseX();
        double mouseY = getMouseY();
        dragXOffset = (int) mouseX - xPos();
        dragYOffset = (int) mouseY - yPos();
        isDragging = true;
        dragPos = true;
        onStartMove(mouseX, mouseY);
        onStartManipulation(mouseX, mouseY);
    }

    public void setMinSize(Dimension minSize) {
        this.minSize = minSize;
    }

    public void setMaxSize(Dimension maxSize) {
        this.maxSize = maxSize;
    }

    public Dimension getMinSize() {
        return minSize;
    }

    public Dimension getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean captured = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (captured) return true;

        boolean posFlag = dragZone != null && dragZone.validate(mouseX, mouseY) && canDrag.get();
        boolean topFlag = topResizeZone != null && topResizeZone.validate(mouseX, mouseY) && canResizeV.get();
        boolean leftFlag = leftResizeZone != null && leftResizeZone.validate(mouseX, mouseY) && canResizeH.get();
        boolean bottomFlag = bottomResizeZone != null && bottomResizeZone.validate(mouseX, mouseY) && canResizeV.get();
        boolean rightFlag = rightResizeZone != null && rightResizeZone.validate(mouseX, mouseY) && canResizeH.get();

        if (posFlag || topFlag || leftFlag || bottomFlag || rightFlag) {
            dragXOffset = (int) mouseX - xPos();
            dragYOffset = (int) mouseY - yPos();
            isDragging = true;
            if (posFlag) {
                dragPos = true;
                onStartMove(mouseX, mouseY);
            } else {
                dragTop = topFlag;
                dragLeft = leftFlag;
                dragBottom = bottomFlag;
                dragRight = rightFlag;
                onStartResized(mouseX, mouseY);
            }
            onStartManipulation(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        if (isDragging) {
            double xMove = (mouseX - dragXOffset) - xPos();
            double yMove = (mouseY - dragYOffset) - yPos();
            if (dragPos) {
                Rectangle previous = new Rectangle(getRect());
                translate((int) xMove, (int) yMove);
                validatePosition();
                validateMove(previous, (int) mouseX, (int) mouseY);
                onMoved();
            } else {
                Dimension min = getMinSize();
                Dimension max = getMaxSize();
                if (dragTop) {
                    int newHeight = (int) MathHelper.clip(ySize() - yMove, min.height, max.height);
                    setYPos(maxYPos() - newHeight, true);
                }
                if (dragLeft) {
                    int newWidth = (int) MathHelper.clip(xSize() - xMove, min.width, max.width);
                    setXPos(maxXPos() - newWidth, true);
                }
                if (dragBottom) {
                    int newHeight = (int) MathHelper.clip(yPos() + (dragYOffset + yMove - yPos()), min.height, max.height);
                    setYSize(newHeight);
                }
                if (dragRight) {
                    int newWidth = (int) MathHelper.clip(xPos() + (dragXOffset + xMove - xPos()), min.width, max.width);
                    setXSize(newWidth);
                }
                onResized();
            }
            onManipulated(mouseX, mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    protected void onMoved() {
        if (onMovedCallback != null) {
            onMovedCallback.run();
        }
    }

    protected void onResized() {
        if (onResizedCallback != null) {
            onResizedCallback.run();
        }
    }

    protected void onManipulated(double mouseX, double mouseY) {}

    protected void onStartManipulation(double mouseX, double mouseY) {}

    protected void onFinishManipulation(double mouseX, double mouseY) {}

    protected void onStartMove(double mouseX, double mouseY) {}

    protected void onStartResized(double mouseX, double mouseY) {}

    protected void onFinishMove(double mouseX, double mouseY) {}

    protected void onFinishResized(double mouseX, double mouseY) {}

    protected void validateMove(Rectangle previous, double mouseX, double mouseY) {
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (isDragging) {
            validatePosition();
            if (dragPos) {
                onFinishMove(mouseX, mouseY);
            } else {
                onFinishResized(mouseX, mouseY);
            }
            onFinishManipulation(mouseX, mouseY);
        }
        isDragging = dragPos = dragTop = dragLeft = dragBottom = dragRight = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        if (xSize() > 0 && ySize() > 0) {
            validatePosition();
        }
    }

    @Override
    public boolean onUpdate() {
        double x = getMouseX();
        double y = getMouseY();

        if (enableCursors) {
            boolean posFlag = dragZone != null && dragZone.validate(x, y) && canDrag.get();
            boolean topFlag = topResizeZone != null && topResizeZone.validate(x, y) && canResizeV.get();
            boolean leftFlag = leftResizeZone != null && leftResizeZone.validate(x, y) && canResizeH.get();
            boolean bottomFlag = bottomResizeZone != null && bottomResizeZone.validate(x, y) && canResizeV.get();
            boolean rightFlag = rightResizeZone != null && rightResizeZone.validate(x, y) && canResizeH.get();
            boolean any = posFlag || topFlag || leftFlag || bottomFlag || rightFlag;

            if (any && !modularGui.getManager().isAreaUnderElement((int) x, (int) y, 1, 1, displayZLevel + 1)) {
                if (posFlag) {
                    modularGui.getManager().setCursor(CursorHelper.DRAG);
                } else if ((topFlag && leftFlag) || (bottomFlag && rightFlag)) {
                    modularGui.getManager().setCursor(CursorHelper.RESIZE_TLBR);
                } else if ((topFlag && rightFlag) || (bottomFlag && leftFlag)) {
                    modularGui.getManager().setCursor(CursorHelper.RESIZE_TRBL);
                } else if (topFlag || bottomFlag) {
                    modularGui.getManager().setCursor(CursorHelper.RESIZE_V);
                } else {
                    modularGui.getManager().setCursor(CursorHelper.RESIZE_H);
                }
            }
        }

        return super.onUpdate();
    }

    protected void validatePosition() {
        int x = xPos();
        int y = yPos();
        positionRestraint.restrainPosition(this);
        if ((x != xPos() || y != yPos()) && onMovedCallback != null) {
            onMovedCallback.run();
        }
    }

    public GuiManipulable setHResizeBorders(int width) {
        leftResizeZone = (x, y) -> GuiHelper.isInRect(xPos(), yPos(), width, ySize(), x, y);
        rightResizeZone = (x, y) -> GuiHelper.isInRect(maxXPos() - width, yPos(), width, ySize(), x, y);
        return this;
    }

    public GuiManipulable setVResizeBorders(int width) {
        topResizeZone = (x, y) -> GuiHelper.isInRect(xPos(), yPos(), xSize(), width, x, y);
        bottomResizeZone = (x, y) -> GuiHelper.isInRect(xPos(), maxYPos() - width, xSize(), width, x, y);
        return this;
    }

    public GuiManipulable setResizeBorders(int width) {
        setHResizeBorders(width);
        setVResizeBorders(width);
        return this;
    }

    public interface PositionRestraint {
        void restrainPosition(GuiManipulable draggable);
    }

    public interface PositionValidator {
        boolean validate(double x, double y);
    }
}