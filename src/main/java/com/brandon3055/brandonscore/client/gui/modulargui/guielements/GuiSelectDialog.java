package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventDispatcher;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 10/09/2016.
 * This is a select dialog which you give a list of values and an optional element base to be used to represent each one.
 * These items are displayed as a list which the user can choose from.
 *
 * Im not entirely happy with this implementation. There are a few caveats to look out for when implementing this system.
 */
public class GuiSelectDialog<T> extends GuiPopUpDialogBase<GuiSelectDialog<T>> implements IGuiEventDispatcher {

    protected T selectedItem = null;
    protected boolean noScrollBars = false;
    protected boolean closeOnSelection = false;
    protected Consumer<T> selectionListener = null;
    protected LinkedList<T> sectionItems = new LinkedList<>();
    protected GuiScrollElement scrollElement;
    protected IGuiEventListener listener = null;
    protected Map<T, MGuiElementBase> sectionElements = new HashMap<>();
    protected Predicate<T> selectionFilter = null;

    //There is probably a much cleaner wau to do this... But i cant think of it right now.
    protected Function<T, MGuiElementBase> rendererBuilder = t -> {
        GuiLabel label = new GuiLabel(String.valueOf(t)).setInsets(0, 2, 0, 2).setWrap(true).setShadow(false).setTextColGetter(hovering -> hovering ? 0x0000FF : 0);
        label.setYSizeMod((guiLabel, integer) -> guiLabel.fontRenderer.getWordWrappedHeight(label.getLabelText(), Math.max(10, guiLabel.xSize() - label.getInsets().left - label.getInsets().right)) + 6);
        label.addChild(GuiTexture.newVanillaGuiTexture(0, 0).setYSizeMod((guiTexture, integer) -> label.ySize()).setPos(label).bindSize(label, false));
        return label;
    };

    public GuiSelectDialog(MGuiElementBase parent) {
        super(parent);
    }

    public GuiSelectDialog(int xPos, int yPos, MGuiElementBase parent) {
        super(xPos, yPos, parent);
    }

    public GuiSelectDialog(int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(xPos, yPos, xSize, ySize, parent);
    }

    @Override
    public GuiSelectDialog setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    @Nullable
    @Override
    public IGuiEventListener getListener() {
        return listener;
    }

    //region setup and list logic

    @Override
    public void addChildElements() {
        super.addChildElements();
        if (scrollElement == null) {
            addChild(scrollElement = new GuiScrollElement());
            scrollElement.setPosAndSize(this.getInsetRect()).setStandardScrollBehavior();
            scrollElement.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
            if (noScrollBars) {
                scrollElement.getVerticalScrollBar().setHidden(true);
                scrollElement.getHorizontalScrollBar().setHidden(true);
            }
        }
        else if (!childElements.contains(scrollElement)) {
            addChild(scrollElement);
        }
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        scrollElement.setPosAndSize(this.getInsetRect());
        reloadSelectionList();
    }

    protected void reloadSelectionList() {
        if (scrollElement == null) {
            return;
        }
        scrollElement.clearElements();
        if (selectionFilter != null) {
            sectionItems.stream().filter(selectionFilter).forEach(t -> scrollElement.addElement(sectionElements.get(t)));
        }
        else {
            sectionItems.forEach(t -> scrollElement.addElement(sectionElements.get(t)));
        }
    }

    /**
     * Allows you to override the default scroll element and add your own.
     */
    public GuiSelectDialog<T> setScrollElement(GuiScrollElement scrollElement) {
        if (scrollElement != null) {
            removeChild(scrollElement);
        }
        this.scrollElement = scrollElement;
        addChild(scrollElement);
        if (isElementInitialized()) {
            reloadElement();
        }
        return this;
    }

    /**
     * This returns the GuiScrollElement. You can use this to customise the GuiScrollElement as needed.
     * You can also completely replace the scroll element using {@link #setScrollElement(GuiScrollElement)}
     * @return The GuiScrollElement used to render this items in this dialog.
     */
    public GuiScrollElement getScrollElement() {
        if (scrollElement == null) {
            scrollElement = new GuiScrollElement();
            scrollElement.applyGeneralElementData(modularGui, mc, screenWidth, screenHeight, fontRenderer);
            scrollElement.setPosAndSize(this.getInsetRect()).setStandardScrollBehavior();
            scrollElement.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
            if (noScrollBars) {
                scrollElement.getVerticalScrollBar().setHidden(true);
                scrollElement.getHorizontalScrollBar().setHidden(true);
            }
        }
        return scrollElement;
    }

    public GuiSelectDialog<T> setSelectionFilter(Predicate<T> selectionFilter) {
        this.selectionFilter = selectionFilter;
        return this;
    }

    //endregion

    //region Items

    /**
     * Add an item to this selection dialog. itemRenderer is the element that will be used to represent
     * this item in the list. If left null then the item will be represented by a simple label.
     * The text used by this label will be String.valueOf(item). You can override the default renderer
     * and add a custom renderer builder via setRendererBuilder().
     * Can also be called for items that have already been added to update their renderer.
     * @param item The item to be added to the list.
     * @param itemRenderer an element to represent this item in the list.
     */
    public GuiSelectDialog<T> addItem(T item, @Nullable MGuiElementBase itemRenderer) {
        if (!sectionItems.contains(item)){
            sectionItems.add(item);
        }
        sectionElements.put(item, itemRenderer == null ? rendererBuilder.apply(item) : itemRenderer);
        reloadSelectionList();
        return this;
    }

    /**
     * @see #addItem(Object, MGuiElementBase)
     */
    public GuiSelectDialog<T> addItems(Map<T, MGuiElementBase> itemMap) {
        sectionElements.putAll(itemMap);
        reloadSelectionList();
        return this;
    }

    /**
     * @see #addItem(Object)
     */
    public GuiSelectDialog<T> addItems(Collection<T> itemMap) {
        itemMap.forEach(t -> {
            if (!sectionItems.contains(t)) {
                sectionItems.add(t);
            }
            sectionElements.put(t, rendererBuilder.apply(t));
        });
        reloadSelectionList();
        return this;
    }

    /**
     * Adds an item at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public GuiSelectDialog<T> addItemAt(T item, int index, @Nullable MGuiElementBase itemRenderer) {
        sectionItems.add(index, item);
        sectionElements.put(item, itemRenderer == null ? rendererBuilder.apply(item) : itemRenderer);
        reloadSelectionList();
        return this;
    }

    /**
     * Removes the specified item and its associated renderer from this dialog.
     */
    public GuiSelectDialog<T> removeItem(T item) {
        sectionItems.remove(item);
        sectionElements.remove(item);
        reloadSelectionList();
        return this;
    }

    public GuiSelectDialog<T> clearItems() {
        sectionItems.clear();
        sectionElements.clear();
        reloadSelectionList();
        return this;
    }

    /**
     * Convenience method. Adds an item with a null renderer.
     * @see #addItem(Object, MGuiElementBase)
     */
    public GuiSelectDialog<T> addItem(T item) {
        addItem(item, null);
        return this;
    }

    /**
     * This allows you to override the default renderer builder. The renderer builder creates a
     * display element to represent elements that have not been assigned a display element.
     */
    public GuiSelectDialog<T> setRendererBuilder(Function<T, MGuiElementBase> rendererBuilder) {
        this.rendererBuilder = rendererBuilder;
        return this;
    }

    /**
     * @return the selection items list. This is an immutable copy of the internal list because you are not allowed to
     * directly modify the list.
     */
    public ImmutableList<T> getItems() {
        return ImmutableList.copyOf(sectionItems);
    }

    /**
     * @return the map of item to renderer element. This is an immutable copy of the internal map because you are not allowed to
     * directly modify the map.
     */
    public Map<T, MGuiElementBase> getSectionElements() {
        return ImmutableMap.copyOf(sectionElements);
    }

    //endregion

    //region Selection

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0)
        for (T item : sectionElements.keySet()) {
            if (sectionElements.get(item).isMouseOver(mouseX, mouseY)) {
                if (listener != null) {
                    listener.onMGuiEvent(new GuiEvent.SelectEvent(this, item, sectionElements.get(item)), this);
                }
                if (selectionListener != null) {
                    selectionListener.accept(item);
                }
                if (closeOnSelection) close();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Sets the selection listener that will be called when a selection is made bu the user.
     */
    public GuiSelectDialog<T> setSelectionListener(Consumer<T> selectionListener) {
        this.selectionListener = selectionListener;
        return this;
    }

    /**
     * If true this dialog will automatically close when a selection is made.
     * Disabled by default.
     */
    public GuiSelectDialog<T> setCloseOnSelection(boolean closeOnSelection) {
        this.closeOnSelection = closeOnSelection;
        return this;
    }

    /**
     * @return the item that was last selected or null if null if no item has been selected.
     */
    public T getSelectedItem() {
        return selectedItem;
    }

    //endregion

    //region Display

    /**
     * This "Hides" the scroll bar. The scroll bar still exists because it is actually required for scrolling but
     * in this mode it does not render and can not be clicked. The only way to scroll in this mode is with the mouse
     * scroll wheel.
     */
    public GuiSelectDialog<T> setNoScrollBar() {
        noScrollBars = true;
        if (scrollElement != null) {
            scrollElement.getVerticalScrollBar().setHidden(true);
            scrollElement.getHorizontalScrollBar().setHidden(true);
        }
        return this;
    }

    //endregion
}
