package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

/**
 * Created by brandon3055 on 3/09/2016.
 * This is a base MGuiListEntry which should ideally be extended to make custom entries.<br>
 * Alternatively it can be used to wrap an element.
 */
public abstract class MGuiListEntry extends MGuiElementBase {
    protected MGuiList list;

    public MGuiListEntry(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiListEntry(IModularGui modularGui, int ySize) {
        super(modularGui, 0, 0, 0, ySize);
    }

    public abstract int getEntryHeight();

    @Override
    public MGuiElementBase setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        list.schedualUpdate();
        return this;
    }

    /**
     * Called whenever the entry is moved within the list. Use this to update the position of any child elements.
     * @param newXPos The new xPosition of the entry.
     * @param newYPos The new yPosition of the entry.
     */
    public abstract void moveEntry(int newXPos, int newYPos);

    public void setList(MGuiList list) {
        this.list = list;
    }
}
