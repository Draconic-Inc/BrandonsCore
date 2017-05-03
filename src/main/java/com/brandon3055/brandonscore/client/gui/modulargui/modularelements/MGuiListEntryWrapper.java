package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

/**
 * Created by brandon3055 on 3/09/2016.
 * This is a simple MGuiListEntry designed to wrap an MGuiElement.
 */
public class MGuiListEntryWrapper extends MGuiListEntry {
    private final MGuiElementBase element;
    private boolean lockXPos = false;

    public MGuiListEntryWrapper(IModularGui modularGui, MGuiElementBase element) {
        super(modularGui);
        this.element = element;
        addChild(element);
        this.xSize = element.xSize;
        this.ySize = element.ySize;
    }

    @Override
    public int getEntryHeight() {
        return element.ySize;
    }

    @Override
    public void moveEntry(int newXPos, int newYPos) {
        if (lockXPos) {
            newXPos = element.xPos;
        }

        this.moveBy(newXPos - xPos, newYPos - yPos);
//        element.moveBy(newXPos - element.xPos, newYPos - element.yPos);

//        if (!lockXPos) {
//            element.xPos = newXPos;
//        }
//        element.yPos = newYPos;
//
//        for (MGuiElementBase elementBase : element.childElements) {
//            if (!lockXPos) {
//                elementBase.xPos = newXPos;
//            }
//            elementBase.yPos = newYPos;
//        }
    }

    /**
     * Locks the xPosition of the element.
     */
    public MGuiListEntryWrapper setLockXPos(boolean lockXPos) {
        this.lockXPos = lockXPos;
        return this;
    }
}
