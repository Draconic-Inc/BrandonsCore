package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

/**
 * Created by brandon3055 on 4/09/2016.
 * TODO make this more flexible somehow... Not sure how yet
 */
@Deprecated
public interface IGuiEventListener {

    /**
     * All gui events can be handled by this method,
     * @param event An instance of GuiEvent. e.g. ButtonEvent
     * @param eventSource The element that fired the even
     */
    @Deprecated
    void onMGuiEvent(GuiEvent event, MGuiElementBase eventSource);
}
