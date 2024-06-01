package com.brandon3055.brandonscore.client.gui.modulargui;

import codechicken.lib.gui.modular.elements.GuiManipulable;
import codechicken.lib.gui.modular.lib.geometry.GuiParent;
import org.jetbrains.annotations.NotNull;

import static codechicken.lib.gui.modular.lib.geometry.Constraint.dynamic;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.LEFT;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.TOP;

/**
 * Created by brandon3055 on 31/05/2024
 */
public class GuiDialogBase extends GuiManipulable {

    private boolean blockOutsideClicks = false;
    private boolean closeOnOutsideClick = false;

    public GuiDialogBase(@NotNull GuiParent<?> rootAccess) {
        super(rootAccess.getModularGui().getRoot());
    }

    public GuiDialogBase setBlockOutsideClicks(boolean blockOutsideClicks) {
        this.blockOutsideClicks = blockOutsideClicks;
        return this;
    }

    public GuiDialogBase setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        this.closeOnOutsideClick = closeOnOutsideClick;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean consumed) {
        if (closeOnOutsideClick && !getContentElement().getRectangle().contains(mouseX, mouseY)) {
            close();
        }
        return super.mouseClicked(mouseX, mouseY, button, consumed) || blockOutsideClicks;
    }

    public void close() {
        getParent().removeChild(this);
    }

    public GuiDialogBase setNormalizedPos(double x, double y) {
        constrain(LEFT, dynamic(() -> Math.min(Math.max(x, 0), scaledScreenWidth() - xSize())));
        constrain(TOP, dynamic(() -> Math.min(Math.max(y, 0), scaledScreenHeight() - ySize())));
        resetBounds();
        return this;
    }

    public GuiDialogBase placeCenter() {
        constrain(LEFT, dynamic(() -> (scaledScreenWidth() - xSize()) / 2D));
        constrain(TOP, dynamic(() -> (scaledScreenHeight() - ySize()) / 2D));
        resetBounds();
        return this;
    }
}
