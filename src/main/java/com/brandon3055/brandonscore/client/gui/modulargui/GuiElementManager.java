package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventDispatcher;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 31/08/2016.
 */
public class GuiElementManager implements IGuiParentElement<GuiElementManager> {

    protected LinkedList<MGuiElementBase> elements = new LinkedList<MGuiElementBase>();
    protected LinkedList<MGuiElementBase> actionList = new LinkedList<MGuiElementBase>();
    private boolean requiresReSort = false;
    private boolean initialized = false;
    private IModularGui parentGui;
    private Minecraft mc;
    private int width;
    private int height;
    private List<MGuiElementBase> toRemove = new ArrayList<MGuiElementBase>();
    private Supplier<List<MGuiElementBase>> jeiExclusions = null;

    public GuiElementManager(IModularGui parentGui) {
        this.parentGui = parentGui;
    }

    public void onGuiInit(Minecraft mc, int width, int height) {
        setWorldAndResolution(mc, width, height);
        if (!initialized) {
            parentGui.addElements(this);
            initialized = true;
        }
    }

    public void reinitialize(Minecraft mc, int width, int height) {
        elements.clear();
        actionList.clear();
        toRemove.clear();
        initialized = false;
        onGuiInit(mc, width, height);
    }

    public void reloadElements() {
        for (MGuiElementBase element : elements) {
            element.reloadElement();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    //region Elements

    /**
     * Adds a new element to the manager with the given display level.
     * @param element The element to add.
     * @param displayZLevel The display level for this element.
     * Elements with higher display levels will be in front of manager with lower display levels.
     * This also applies to mouse and key events.
     * @param first if true the element will be added to the start of the element array placing it under/before all other elements.
     * @return The Element.
     */
    public <C extends MGuiElementBase> C addChild(C element, int displayZLevel, boolean first) {
        if (displayZLevel >= 950) {
            LogHelperBC.error("ModularGui Display Level Out Of Bounds! Can not be greater than 950 " + displayZLevel);
        }
        element.applyGeneralElementData(parentGui, mc, width, height, BCFontRenderer.convert(mc.fontRenderer));
        element.displayZLevel = displayZLevel;
        if (first) {
            elements.addFirst(element);
        }
        else {
            elements.add(element);
        }
        if (!element.isElementInitialized()) {
            element.addChildElements();
        }
        requiresReSort = true;

        if (element instanceof IGuiEventDispatcher && ((IGuiEventDispatcher) element).getListener() == null && parentGui instanceof IGuiEventListener) {
            ((IGuiEventDispatcher) element).setListener((IGuiEventListener) parentGui);
        }

        element.reloadElement();

        return element;
    }

    /**
     * Adds a new element to the manager with a display level of 0.<br>
     * Note: Adding an element automatically calls that element's addElements method.
     * @param element The element to add.
     * @return The Element.
     */
    @Override
    public <C extends MGuiElementBase> C addChild(C element) {
        return addChild(element, 0, false);
    }

    @Override
    public <C extends MGuiElementBase> C addChildFirst(C element) {
        return addChild(element, 0, true);
    }

    @Override
    public GuiElementManager addChildren(Collection<? extends MGuiElementBase> elements) {
        return null;
    }

    @Override
    public <C extends MGuiElementBase> C removeChild(C element) {
        if (elements.contains(element)) {
            toRemove.add(element);
            requiresReSort = true;
            return element;
        }
        return null;
    }

    @Override
    public GuiElementManager removeChildByID(String id) {
        for (MGuiElementBase element : elements) {
            if (element.id != null && element.id.equals(id)) {
                toRemove.add(element);
                requiresReSort = true;
                return this;
            }
        }
        return this;
    }

    @Override
    public GuiElementManager removeChildByGroup(String group) {
        for (MGuiElementBase element : elements) {
            if (element.isInGroup(group)) {
                toRemove.add(element);
                requiresReSort = true;
            }
        }
        return this;
    }

    @Override
    public GuiElementManager setChildIDEnabled(String id, boolean enabled) {
        for (MGuiElementBase element : elements) {
            if (element.id != null && element.id.equals(id)) {
                element.setEnabled(enabled);
                return this;
            }
        }
        return this;
    }

    public GuiElementManager setChildGroupEnabled(String group, boolean enabled) {
        for (MGuiElementBase element : elements) {
            if (element.isInGroup(group)) {
                element.setEnabled(enabled);
            }
        }
        return this;
    }

    public List<MGuiElementBase> getElements() {
        return elements;
    }

    public void clear() {
        elements.clear();
        requiresReSort = true;
    }

    //endregion

    //region Mouse & Key

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int clickedDisplay = -100;
        for (MGuiElementBase element : actionList) {
            if (element.isEnabled() && clickedDisplay > -100 && element.displayZLevel < clickedDisplay) {
                return true;
            }

            if (element.isEnabled() && element.isMouseOver(mouseX, mouseY)) {
                clickedDisplay = element.displayZLevel;
            }

            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        for (MGuiElementBase element : actionList) {
            if (element.isEnabled() && element.mouseReleased(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (MGuiElementBase element : actionList) {
            if (element.isEnabled() && element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        for (MGuiElementBase element : actionList) {
            if (element.isEnabled() && element.keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean handleMouseInput() throws IOException {
        for (MGuiElementBase element : actionList) {
            if (element.isEnabled() && element.handleMouseInput()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all elements who's bounds contain the given position.
     * This can for example be used to find all elements that are currently under the mouse cursor.
     * @param posX The x position.
     * @param posY The y position.
     * @return a list of elements who's {@link MGuiElementBase#isMouseOver} method returns true with the given position.
     */
    public List<MGuiElementBase> getElementsAtPosition(int posX, int posY) {
        List<MGuiElementBase> list = new LinkedList<>();

        for (MGuiElementBase element : elements) {
            element.getElementsAtPosition(posX, posY, list);
        }

        return list;
    }

    /**
     * Similar to {@link #getElementsAtPosition(int, int)} except only returns elements that are an instance of the specified class.
     * Note: This method is a little inefficient so probably not something you want to be doing say every render frame.
     */
    public <C extends MGuiElementBase> List<C> getElementsAtPosition(int posX, int posY, Class<C> clazz) {
        List<MGuiElementBase> list = getElementsAtPosition(posX, posY);
        List<C> matches = new LinkedList<>();
        DataUtils.forEachMatch(list, element -> clazz.isAssignableFrom(element.getClass()), element -> matches.add(clazz.cast(element)));
        return matches;
    }

    //endregion

    //region Render

    public void renderElements(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        for (MGuiElementBase element : elements) {
            if (element.isEnabled()) {
                parentGui.setZLevel(element.displayZLevel);
                element.preDraw(mc, mouseX, mouseY, partialTicks);
                element.renderElement(mc, mouseX, mouseY, partialTicks);
                element.postDraw(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    public boolean renderOverlayLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int renderDisplay = -100;
        for (MGuiElementBase element : actionList) {
            if (element.isEnabled() && renderDisplay > -100 && element.displayZLevel < renderDisplay) {
                return true;
            }

            if (element.isEnabled() && element.isMouseOver(mouseX, mouseY)) {
                renderDisplay = element.displayZLevel;
            }

            if (element.isEnabled() && element.renderOverlayLayer(mc, mouseX, mouseY, partialTicks)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the specified area is partially or fully obstructed by another element on a higher zLevel.
     */
    public boolean isAreaUnderElement(int posX, int posY, int xSize, int ySize, int zLevel) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.displayZLevel >= zLevel && element.getRect().intersects(posX, posY, xSize, ySize)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the specified point is under another element on a higher zLevel.
     */
    public boolean isPointUnderElement(int posX, int posY, int zLevel) {
        for (MGuiElementBase element : elements) {
            if (element.isEnabled() && element.displayZLevel >= zLevel && element.getRect().contains(posX, posY)) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //region Update

    public void onUpdate() {
        if (!toRemove.isEmpty()) {
            elements.removeAll(toRemove);
            toRemove.clear();
        }

        for (MGuiElementBase element : elements) {
            if (element.onUpdate()) {
                break;
            }
        }

        if (requiresReSort) {
            sort();
        }
    }

    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        this.width = width;
        this.height = height;
        for (MGuiElementBase element : elements) {
            element.applyGeneralElementData(parentGui, mc, width, height, BCFontRenderer.convert(mc.fontRenderer));
        }
        reloadElements();
    }

    //endregion

    //region Sorting

    /**
     * When rendering elements need to be rendered in order of lowest first so that elements on higher z levels actually render on top.
     */
    private static Comparator<MGuiElementBase> renderSorter = (o1, o2) -> o1.displayZLevel < o2.displayZLevel ? -1 : o1.displayZLevel > o2.displayZLevel ? 1 : 0;

    /**
     * When checking for element clicks we need the reverse of the renderSorter because we want to first check the upper most elements for clicks before
     * passing the click to lower potentially hidden elements.
     */
    private static Comparator<MGuiElementBase> actionSorter = (o1, o2) -> o1.displayZLevel < o2.displayZLevel ? 1 : o1.displayZLevel > o2.displayZLevel ? -1 : 0;

    private void sort() {
        Collections.sort(elements, renderSorter);
        actionList.clear();
        actionList.addAll(elements);
        Collections.sort(actionList, actionSorter);
    }

    public IModularGui getParentGui() {
        return parentGui;
    }

    public void setJeiExclusions(Supplier<List<MGuiElementBase>> exclusions) {
        this.jeiExclusions = exclusions;
    }

    public List<Rectangle> getJeiExclusions() {
        if (jeiExclusions == null) {
            return Collections.emptyList();
        }
        return jeiExclusions.get().stream().map(elementBase -> new Rectangle(elementBase.getRect())).collect(Collectors.toList());
    }

    //endregion
}
