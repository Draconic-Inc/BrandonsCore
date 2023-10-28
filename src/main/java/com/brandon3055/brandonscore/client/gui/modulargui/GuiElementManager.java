//package com.brandon3055.brandonscore.client.gui.modulargui;
//
//import com.brandon3055.brandonscore.client.CursorHelper;
//import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui.JEITargetAdapter;
//import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
//import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventDispatcher;
//import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
//import com.brandon3055.brandonscore.utils.DataUtils;
//import com.brandon3055.brandonscore.utils.LogHelperBC;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.Rect2i;
//import net.minecraft.resources.ResourceLocation;
//
//import java.awt.*;
//import java.util.List;
//import java.util.*;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
///**
// * Created by brandon3055 on 31/08/2016.
// */
//public class GuiElementManager implements IGuiParentElement<GuiElementManager> {
//
//    protected LinkedList<GuiElement> elements = new LinkedList<GuiElement>();
//    protected LinkedList<GuiElement> actionList = new LinkedList<GuiElement>();
//    private boolean requiresReSort = false;
//    private boolean initialized = false;
//    private IModularGui parentGui;
//    private Minecraft mc;
//    private int width;
//    private int height;
//    private List<GuiElement> toRemove = new ArrayList<GuiElement>();
//    private Supplier<List<GuiElement<?>>> jeiExclusions = null;
//    private List<JEITargetAdapter> jeiGhostTargets = new ArrayList<>();
//    private ResourceLocation newCursor = null;
//    private boolean mousePressed = false;
//    private Runnable onTick = null;
//
//    public GuiElementManager(IModularGui parentGui) {
//        this.parentGui = parentGui;
//    }
//
//    public void onGuiInit(Minecraft mc, int width, int height) {
//        setWorldAndResolution(mc, width, height);
//        if (!initialized) {
//            parentGui.addElements(this);
//            initialized = true;
//        }
//    }
//
//    public void reinitialize(Minecraft mc, int width, int height) {
//        elements.clear();
//        actionList.clear();
//        toRemove.clear();
//        initialized = false;
//        onGuiInit(mc, width, height);
//    }
//
//    public void reloadElements() {
//        for (GuiElement element : elements) {
//            element.reloadElement();
//        }
//    }
//
//    public boolean isInitialized() {
//        return initialized;
//    }
//
//    public void setCursor(ResourceLocation cursor) {
//        this.newCursor = cursor;
//    }
//
//    public Runnable onTick(Runnable onTick) {
//        this.onTick = onTick;
//        return onTick;
//    }
//
//    //region Elements
//
//    /**
//     * Adds a new element to the manager with the given display level.
//     *
//     * @param element       The element to add.
//     * @param displayZLevel The display level for this element.
//     *                      Elements with higher display levels will be in front of manager with lower display levels.
//     *                      This also applies to mouse and key events.
//     * @param first         if true the element will be added to the start of the element array placing it under/before all other elements.
//     * @return The Element.
//     */
//    //TODO When i re-write this i need to nuke the entire display level system and implement something better that
//    // supports not only offsetting the position of an element but its child elements as well. It should probably
//    // also be a separate system not tied to GuiElementManager (if i even keep GuiElementManager)
//    public <C extends GuiElement> C addChild(C element, int displayZLevel, boolean first) {
//        if (displayZLevel >= 950) {
//            LogHelperBC.error("ModularGui Display Level Out Of Bounds! Can not be greater than 950 " + displayZLevel);
//        }
//        toRemove.remove(element);
//        element.applyGeneralElementData(parentGui, mc, width, height, BCFontRenderer.convert(mc.font));
//        element.displayZLevel = displayZLevel;
//        if (first) {
//            elements.addFirst(element);
//        } else {
//            elements.add(element);
//        }
//        if (!element.isElementInitialized()) {
//            element.addChildElements();
//            element.setElementInitialized();
//        }
//        requiresReSort = true;
//
//        if (element instanceof IGuiEventDispatcher && ((IGuiEventDispatcher) element).getListener() == null && parentGui instanceof IGuiEventListener) {
//            ((IGuiEventDispatcher) element).setListener((IGuiEventListener) parentGui);
//        }
//
//        element.reloadElement();
//
//        return element;
//    }
//
//    @Override
//    public <C extends GuiElement> boolean hasChild(C child) {
//        return elements.contains(child);
//    }
//
//    /**
//     * Adds a new element to the manager with a display level of 0.<br>
//     * Note: Adding an element automatically calls that element's addElements method.
//     *
//     * @param element The element to add.
//     * @return The Element.
//     */
//    @Override
//    public <C extends GuiElement> C addChild(C element) {
//        return addChild(element, 0, false);
//    }
//
//    @Override
//    public <C extends GuiElement> C addBackGroundChild(C element) {
//        return addChild(element, 0, true);
//    }
//
//    @Override
//    public GuiElementManager addChildren(Collection<? extends GuiElement> elements) {
//        return null;
//    }
//
//    @Override
//    public <C extends GuiElement> C removeChild(C element) {
//        if (elements.contains(element)) {
//            toRemove.add(element);
//            requiresReSort = true;
//            return element;
//        }
//        return null;
//    }
//
//    @Override
//    public GuiElementManager removeChildByID(String id) {
//        for (GuiElement element : elements) {
//            if (element.id != null && element.id.equals(id)) {
//                toRemove.add(element);
//                requiresReSort = true;
//                return this;
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public GuiElementManager removeChildByGroup(String group) {
//        for (GuiElement element : elements) {
//            if (element.isInGroup(group)) {
//                toRemove.add(element);
//                requiresReSort = true;
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public GuiElementManager setChildIDEnabled(String id, boolean enabled) {
//        for (GuiElement element : elements) {
//            if (element.id != null && element.id.equals(id)) {
//                element.setEnabled(enabled);
//                return this;
//            }
//        }
//        return this;
//    }
//
//    public GuiElementManager setChildGroupEnabled(String group, boolean enabled) {
//        for (GuiElement element : elements) {
//            if (element.isInGroup(group)) {
//                element.setEnabled(enabled);
//            }
//        }
//        return this;
//    }
//
//    public List<GuiElement> getElements() {
//        return elements;
//    }
//
//    public void clear() {
//        elements.clear();
//        requiresReSort = true;
//    }
//
//    //endregion
//
//    //region Mouse & Key
//
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        mousePressed = true;
//        int clickedDisplay = -100;
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && clickedDisplay > -100 && element.displayZLevel < clickedDisplay) {
//                continue;
//            }
//
//            if (element.isEnabled() && element.isMouseOver(mouseX, mouseY)) {
//                clickedDisplay = element.displayZLevel;
//            }
//
//            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, button)) {
//                globalClick(mouseX, mouseY, button);
//                return true;
//            }
//        }
//        globalClick(mouseX, mouseY, button);
//        return false;
//    }
//
//    public void globalClick(double mouseX, double mouseY, int button) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled()) {
//                element.globalClick(mouseX, mouseY, button);
//            }
//        }
//    }
//
//    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        mousePressed = false;
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.mouseReleased(mouseX, mouseY, button)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void mouseMoved(double mouseX, double mouseY) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.mouseMoved(mouseX, mouseY)) {
//                return;
//            }
//        }
//    }
//
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.keyPressed(keyCode, scanCode, modifiers)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.keyReleased(keyCode, scanCode, modifiers)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean charTyped(char charTyped, int charCode) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.charTyped(charTyped, charCode)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollAmount)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
////    public boolean handleMouseInput() throws IOException {
////        for (GuiElement element : actionList) {
////            if (element.isEnabled() && element.handleMouseInput()) {
////                return true;
////            }
////        }
////        return false;
////    }
//
//    /**
//     * Returns a list of all elements who's bounds contain the given position.
//     * This can for example be used to find all elements that are currently under the mouse cursor.
//     *
//     * @param posX The x position.
//     * @param posY The y position.
//     * @return a list of elements who's {@link GuiElement#isMouseOver} method returns true with the given position.
//     */
//    public List<GuiElement> getElementsAtPosition(double posX, double posY) {
//        List<GuiElement> list = new LinkedList<>();
//
//        for (GuiElement element : elements) {
//            element.getElementsAtPosition(posX, posY, list);
//        }
//
//        return list;
//    }
//
//    /**
//     * Similar to {@link #getElementsAtPosition(double, double)} except only returns elements that are an instance of the specified class.
//     * Note: This method is a little inefficient so probably not something you want to be doing say every render frame.
//     */
//    public <C extends GuiElement> List<C> getElementsAtPosition(double posX, double posY, Class<C> clazz) {
//        List<GuiElement> list = getElementsAtPosition(posX, posY);
//        List<C> matches = new LinkedList<>();
//        DataUtils.forEachMatch(list, element -> clazz.isAssignableFrom(element.getClass()), element -> matches.add(clazz.cast(element)));
//        return matches;
//    }
//
////    public void passClick() {
////        this.passClick = true;
////    }
//
//    //endregion
//
//    //region Render
//
//    public void renderElements(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//        for (GuiElement element : elements) {
//            if (element.isEnabled()) {
//                parentGui.setZLevel(element.displayZLevel);
//                element.preDraw(mc, mouseX, mouseY, partialTicks);
//                element.renderElement(mc, mouseX, mouseY, partialTicks);
//                element.postDraw(mc, mouseX, mouseY, partialTicks);
//                parentGui.setZLevel(0); //Reset this so that the final value does not inadvertently affect the overlay layer
//            }
//        }
//    }
//
//    public boolean renderOverlayLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//        int renderDisplay = -100;
//        for (GuiElement element : actionList) {
//            if (element.isEnabled() && renderDisplay > -100 && element.displayZLevel < renderDisplay) {
//                return true;
//            }
//
//            if (element.isEnabled() && element.isMouseOver(mouseX, mouseY)) {
//                renderDisplay = element.displayZLevel;
//            }
//
//            if (element.isEnabled() && element.renderOverlayLayer(mc, mouseX, mouseY, partialTicks)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * Returns true if the specified area is partially or fully obstructed by another element on a higher zLevel.
//     */
//    public boolean isAreaUnderElement(int posX, int posY, int xSize, int ySize, int zLevel) {
//        for (GuiElement element : elements) {
//            if (element.isEnabled() && element.displayZLevel >= zLevel && element.getRect().intersects(posX, posY, xSize, ySize)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Returns true if the specified point is under another element on a higher zLevel.
//     */
//    public boolean isPointUnderElement(int posX, int posY, int zLevel) {
//        for (GuiElement element : elements) {
//            if (element.isEnabled() && element.displayZLevel >= zLevel && element.getRect().contains(posX, posY)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    //endregion
//
//    //region Update
//
//    public void onUpdate() {
//        newCursor = null;
//        if (!toRemove.isEmpty()) {
//            elements.removeAll(toRemove);
//            toRemove.clear();
//        }
//
//        if (onTick != null) onTick.run();
//
//        for (GuiElement element : elements) {
//            if (element.onUpdate()) {
//                break;
//            }
//        }
//
//        if (requiresReSort) {
//            sort();
//        }
//
//        if (!mousePressed) {
//            CursorHelper.setCursor(newCursor);
//        }
//    }
//
//    public void setWorldAndResolution(Minecraft mc, int width, int height) {
//        this.mc = mc;
//        this.width = width;
//        this.height = height;
//        for (GuiElement element : elements) {
//            element.applyGeneralElementData(parentGui, mc, width, height, BCFontRenderer.convert(mc.font));
//        }
//        reloadElements();
//    }
//
//    //endregion
//
//    //region Sorting
//
//    /**
//     * When rendering elements need to be rendered in order of lowest first so that elements on higher z levels actually render on top.
//     */
//    private static Comparator<GuiElement> renderSorter = (o1, o2) -> o1.displayZLevel < o2.displayZLevel ? -1 : o1.displayZLevel > o2.displayZLevel ? 1 : 0;
//
//    /**
//     * When checking for element clicks we need the reverse of the renderSorter because we want to first check the upper most elements for clicks before
//     * passing the click to lower potentially hidden elements.
//     */
//    private static Comparator<GuiElement> actionSorter = (o1, o2) -> o1.displayZLevel < o2.displayZLevel ? 1 : o1.displayZLevel > o2.displayZLevel ? -1 : 0;
//
//    private void sort() {
//        Collections.sort(elements, renderSorter);
//        actionList.clear();
//        actionList.addAll(elements);
//        Collections.sort(actionList, actionSorter);
//    }
//
//    public IModularGui getParentGui() {
//        return parentGui;
//    }
//
//    public void setJeiExclusions(Supplier<List<GuiElement<?>>> exclusions) {
//        this.jeiExclusions = exclusions;
//    }
//
//    public List<Rect2i> getJeiExclusions() {
//        if (jeiExclusions == null) {
//            return Collections.emptyList();
//        }
//        return jeiExclusions.get().stream()
//                .filter(GuiElement::isEnabled)
//                .map(elementBase -> {
//                    Rectangle rect = elementBase.getRect();
//                    return new Rect2i(rect.x, rect.y, rect.width, rect.height);
//                })
//                .collect(Collectors.toList());
//    }
//
//    public List<JEITargetAdapter> getJeiGhostTargets() {
//        return jeiGhostTargets;
//    }
//
//    //endregion
//}
