package com.brandon3055.brandonscore.client.gui.modulargui;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.*;

/**
 * Created by brandon3055 on 31/08/2016.
 */
public class ModuleManager {

    protected List<MGuiElementBase> elements = new ArrayList<MGuiElementBase>();
    private boolean requiresReSort = false;
    private IModularGui parentGui;
    private List<MGuiElementBase> toRemove = new ArrayList<MGuiElementBase>();

    public ModuleManager(IModularGui parentGui) {
        this.parentGui = parentGui;
    }

    public void initElements() {
        for (MGuiElementBase element : elements) {
            element.initElement();
        }
    }

    //region Elements

    /**
     * Adds a new element to the manager with the given display level.
     * @param element The element to add.
     * @param displayLevel The display level for this element.
     * Elements with higher display levels will be in front of manager with lower display levels.
     * This also applies to mouse and key events.
     * @return The Element.
     */
    public MGuiElementBase add(MGuiElementBase element, int displayLevel) {
        element.displayLevel = displayLevel;
        elements.add(element);
        requiresReSort = true;
        return element;
    }

    /**
     * Adds a new element to the manager with a display level of 0.
     * @param element The element to add.
     * @return The Element.
     */
    public MGuiElementBase add(MGuiElementBase element) {
        return add(element, 0);
    }

    public void remove(MGuiElementBase element) {
        if (elements.contains(element)) {
            toRemove.add(element);
            requiresReSort = true;
        }
    }

    public void removeByID(String id) {
        Iterator<MGuiElementBase> i = elements.iterator();
        while (i.hasNext()) {
            MGuiElementBase element = i.next();
            if (element.id != null && element.id.equals(id)) {
                toRemove.add(element);
                requiresReSort = true;
                return;
            }
        }
    }

    public void removeByGroup(String group) {
        Iterator<MGuiElementBase> i = elements.iterator();
        while (i.hasNext()) {
            MGuiElementBase element = i.next();
            if (element.isInGroup(group)) {
                toRemove.add(element);
                requiresReSort = true;
            }
        }
    }

    public void setIDEnabled(String id, boolean enabled) {
        Iterator<MGuiElementBase> i = elements.iterator();
        while (i.hasNext()) {
            MGuiElementBase element = i.next();
            if (element.id != null && element.id.equals(id)) {
                element.setEnabled(enabled);
                return;
            }
        }
    }

    public void setGroupEnabled(String group, boolean enabled) {
        Iterator<MGuiElementBase> i = elements.iterator();
        while (i.hasNext()) {
            MGuiElementBase element = i.next();
            if (element.isInGroup(group)) {
                element.setEnabled(enabled);
            }
        }
    }

    public List<MGuiElementBase> getElements() {
        return elements;
    }

    public void clear() {
        elements.clear();
    }

    //endregion

    //region Mouse & Key

    protected boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    protected boolean mouseReleased(int mouseX, int mouseY, int state) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.mouseReleased(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }

    protected boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }

    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean handleMouseInput() throws IOException {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.handleMouseInput()) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //region Render

    public void renderBackgroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled()) {
                parentGui.setZLevel(element.displayLevel * 100);
                element.renderBackgroundLayer(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderForegroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled()) {
                parentGui.setZLevel(element.displayLevel * 100);
                element.renderForegroundLayer(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderOverlayLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled()) {
                parentGui.setZLevel(element.displayLevel * 100);
                element.renderOverlayLayer(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    //endregion

    //region Update

    public void onUpdate() {
        if (!toRemove.isEmpty()) {
            elements.removeAll(toRemove);
        }

        for (MGuiElementBase element : elements) {
            element.onUpdate();
        }

        if (requiresReSort) {
            sort();
        }
    }

    //endregion

    //region Sorting

    private static Comparator<MGuiElementBase> sorter = new Comparator<MGuiElementBase>() {
        @Override
        public int compare(MGuiElementBase o1, MGuiElementBase o2) { return o1.displayLevel < o2.displayLevel ? 1 : o1.displayLevel > o2.displayLevel ? -1 : 0; }
    };

    private void sort() {
        Collections.sort(elements, sorter);
    }

    //endregion
}
