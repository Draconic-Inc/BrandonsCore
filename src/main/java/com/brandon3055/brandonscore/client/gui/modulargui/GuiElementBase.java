package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public class GuiElementBase extends Gui {

    public int xPos;
    public int yPos;
    public int xSize;
    public int ySize;
    private boolean enabled = true;
    public IModularGui parentGui;
    /**
     * An id that is unique to this element
     */
    public String id = null;
    private List<String> groups = new ArrayList<String>();
    public List<GuiElementBase> childElements = new LinkedList<GuiElementBase>();
    private List<GuiElementBase> toRemove = new ArrayList<GuiElementBase>();

    public GuiElementBase(IModularGui parentGui) {
        this.parentGui = parentGui;
    }

    public GuiElementBase(IModularGui parentGui, int xPos, int yPos) {
        this(parentGui);
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public GuiElementBase(IModularGui parentGui, int xPos, int yPos, int xSize, int ySize) {
        this(parentGui, xPos, yPos);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void initElement() {
        for (GuiElementBase element : childElements) {
            element.initElement();
        }
    }

    //region Child Elements

    public GuiElementBase addChild(GuiElementBase element) {
        childElements.add(element);
        return this;
    }

    public GuiElementBase removeChild(GuiElementBase element) {
        if (childElements.contains(element)) {
            toRemove.add(element);
        }
        return this;
    }

    public GuiElementBase removeChildByID(String id) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.id != null && element.id.equals(id)) {
                toRemove.add(element);
                return this;
            }
        }
        return this;
    }

    public GuiElementBase removeChildByGroup(String group) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.isInGroup(group)) {
                toRemove.add(element);
            }
        }
        return this;
    }

    public GuiElementBase setChildIDEnabled(String id, boolean enabled) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.id != null && element.id.equals(id)) {
                element.enabled = enabled;
                return this;
            }
        }
        return this;
    }

    public GuiElementBase setChildGroupEnabled(String group, boolean enabled) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.isInGroup(group)) {
                element.enabled = enabled;
            }
        }
        return this;
    }

    //endregion

    //region Group & ID Stuff

    public GuiElementBase addToGroup(String group) {
        groups.add(group);
        return this;
    }

    public GuiElementBase removeFromGroup(String group) {
        if (groups.contains(group)) {
            groups.remove(group);
        }
        return this;
    }

    public GuiElementBase removeFromAllGroups() {
        groups.clear();
        return this;
    }

    public boolean isInGroup(String group) {
        return groups.contains(group);
    }

    public List<String> getGroups() {
        return groups;
    }

    //endregion

    //region Enable

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    //endregion

    //region Mouse

    public boolean isMouseOver(int mouseX, int mouseY) {
        return GuiHelper.isInRect(xPos, yPos, xSize, ySize, mouseX, mouseY);
    }

    /**
     * Called when the element is clicked. Return true to prevent further processing.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.mouseReleased(mouseX, mouseY, state);
            }
        }
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            }
        }
    }

    public void handleMouseInput() {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.handleMouseInput();
            }
        }
    }

    //endregion

    //region Keyboard

    /**
     * Called whenever a key is typed. Return true to cancel further processing.
     */
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //region Update

    public void onUpdate() {
        if (!toRemove.isEmpty()) {
            childElements.removeAll(toRemove);
            toRemove.clear();
        }

        for (GuiElementBase element : childElements) {
            element.onUpdate();
        }
    }

    //endregion

    //region Render

    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderForegroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    //endregion

    //region Misc

    public void setZLevel(float zLevel) {
        this.zLevel = zLevel;
    }

    public GuiScreen getScreen() {
        return parentGui.getScreen();
    }

    //endregion
}
