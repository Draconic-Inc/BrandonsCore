package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.utils.GuiHelper;

import java.util.function.Supplier;

public class GuiDraggable extends GuiElement<GuiDraggable> {

    protected int dragXOffset = 0;
    protected int dragYOffset = 0;
    protected boolean dragging = false;
    protected Supplier<Boolean> canDrag = () -> true;
    protected PositionValidator dragZoneValidator = null;
    protected int dragBarHeight = 20;
    protected Runnable onMovedCallback = null;
    protected PositionRestraint positionRestraint = GuiElement::normalizePosition;

    public void setCanDrag(Supplier<Boolean> canDrag) {
        this.canDrag = canDrag;
    }

    public void setDragBarHeight(int dragBarHeight) {
        this.dragBarHeight = dragBarHeight;
    }

    public void setDragZoneValidator(PositionValidator dragZoneValidator) {
        this.dragZoneValidator = dragZoneValidator;
    }

    public void setOnMovedCallback(Runnable onMovedCallback) {
        this.onMovedCallback = onMovedCallback;
    }

    public void setPositionRestraint(PositionRestraint positionRestraint) {
        this.positionRestraint = positionRestraint;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean captured = super.mouseClicked(mouseX, mouseY, mouseButton);

        if (!captured && canDrag.get() && (dragZoneValidator != null ? dragZoneValidator.validate(mouseX, mouseY) : GuiHelper.isInRect(xPos(), yPos(), xSize(), dragBarHeight, mouseX, mouseY))) {
            dragging = true;
            dragXOffset = (int)mouseX - xPos();
            dragYOffset = (int)mouseY - yPos();
        }

        return captured;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        if (dragging) {
            double xMove = (mouseX - dragXOffset) - xPos();
            double yMove = (mouseY - dragYOffset) - yPos();
            translate((int)xMove, (int)yMove);

            validatePosition();

            if (onMovedCallback != null) {
                onMovedCallback.run();
            }
        }
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        if (xSize() > 0 && ySize() > 0) {
            validatePosition();
        }
    }

    private void validatePosition() {
        int x = xPos();
        int y = yPos();
        positionRestraint.restrainPosition(this);
        if ((x != xPos() || y != yPos()) && onMovedCallback != null) {
            onMovedCallback.run();
        }
    }

    public interface PositionRestraint {
        void restrainPosition(GuiDraggable draggable);
    }

    public interface PositionValidator {
        boolean validate(double x, double y);
    }
}