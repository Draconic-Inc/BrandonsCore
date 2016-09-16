package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016.
 *///TODO Build on this and make a good flexible popup/context menu system system
public class MGuiPopUpDialog extends MGuiElementBase {

    public final MGuiElementBase parent;
    public boolean closeOnOutsideClick = true;

    public MGuiPopUpDialog(IModularGui modularGui, MGuiElementBase parent) {
        super(modularGui);
        this.parent = parent;
    }

    public MGuiPopUpDialog(IModularGui modularGui, int xPos, int yPos, MGuiElementBase parent) {
        super(modularGui, xPos, yPos);
        this.parent = parent;
    }

    public MGuiPopUpDialog(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.parent = parent;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (closeOnOutsideClick && !isMouseOver(mouseX, mouseY)) {
            close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public MGuiPopUpDialog setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        this.closeOnOutsideClick = closeOnOutsideClick;
        return this;
    }

    public void close() {
        parent.removeChild(this);
    }
}
