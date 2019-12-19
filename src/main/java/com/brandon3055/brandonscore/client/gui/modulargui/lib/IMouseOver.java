package com.brandon3055.brandonscore.client.gui.modulargui.lib;

/**
 * Created by brandon3055 on 4/07/2017.
 * This should be a pretty self explanatory interface.
 * This is currently used by {@link com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl#setParentScrollable(IMouseOver)}
 */
public interface IMouseOver {

    boolean isMouseOver(double mouseX, double mouseY);
}
