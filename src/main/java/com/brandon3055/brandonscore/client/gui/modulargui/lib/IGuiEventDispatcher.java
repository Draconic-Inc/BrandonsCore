package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 3/07/2017.
 * Any element that implements this will have its parent GuiElementBase or IModularGui assigned as its listener if that parent is a listener and the element implementing this does not already have a listener.
 * The parent is assigned as the listener when the child is added to the parent.
 */
@Deprecated
public interface IGuiEventDispatcher {

    <E extends GuiElement> E setListener(IGuiEventListener listener);

    @Nullable
    IGuiEventListener getListener();
}
