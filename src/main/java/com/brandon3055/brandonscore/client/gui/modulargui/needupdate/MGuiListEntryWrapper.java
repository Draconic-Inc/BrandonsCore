package com.brandon3055.brandonscore.client.gui.modulargui.needupdate;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

/**
 * Created by brandon3055 on 3/09/2016.
 * This is a simple MGuiListEntry designed to wrap an MGuiElement.
 */
@Deprecated
public class MGuiListEntryWrapper extends MGuiListEntry {
    private final MGuiElementBase element;
    private boolean lockXPos = false;

    public MGuiListEntryWrapper(MGuiElementBase element) {
        this.element = element;
        addChild(element);
        setPos(element);
    }

    @Override
    public int getEntryHeight() {
        return element.ySize();
    }

    @Override
    public void moveEntry(int newXPos, int newYPos) {
        if (lockXPos) {
            newXPos = element.xPos();
        }

        this.translate(newXPos - xPos(), newYPos - yPos());
    }

    /**
     * Locks the xPosition of the element.
     */
    public MGuiListEntryWrapper setLockXPos(boolean lockXPos) {
        this.lockXPos = lockXPos;
        return this;
    }
}
