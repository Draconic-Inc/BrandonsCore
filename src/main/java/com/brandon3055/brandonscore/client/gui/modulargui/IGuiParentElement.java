package com.brandon3055.brandonscore.client.gui.modulargui;

import java.util.Collection;

/**
 * Created by brandon3055 on 9/7/19.
 */
public interface IGuiParentElement<E> {

    /**
     * Adds a new child element to this element.
     *
     * @return The child element that was added.
     */
    <C extends GuiElement> C addChild(C child);

    /**
     * Adds a new child element to this element at index 0 so it will render behind all other elements previously added.
     *
     * @return The child element that was added.
     */
    <C extends GuiElement> C addBackGroundChild(C child);

    /**
     * Adds a Collection of child elements to this element.
     *
     * @return self
     */
    E addChildren(Collection<? extends GuiElement> elements);

    /**
     * This schedules a child element to be removed at the start of the next update tick.
     * The reason this works this way is to avoid concurrent modification exceptions.
     * If this 1 tick delay is an issue then you can set disableOnRemove to true for the parent element
     * which disables all child elements the instant they are scheduled for removal by this method.
     *
     * @param child the child element to remove.
     * @return the element that will be removed or null if the element was not a child of this element.
     */
    <C extends GuiElement> C removeChild(C child);

    /**
     * Remove an element by its 'unique' id.
     * Please see setId before using this.
     *
     * @param id The id of the element to remove.
     */
    E removeChildByID(String id);

    /**
     * Remove all elements that are assigned to the specified group.
     *
     * @param group the name of the element group to remove.
     */
    E removeChildByGroup(String group);

    /**
     * Set en elements enabled state by its id.
     */
    E setChildIDEnabled(String id, boolean enabled);

    /**
     * Set the enabled state for all elements that are part of the specified group.
     */
    E setChildGroupEnabled(String group, boolean enabled);
}
