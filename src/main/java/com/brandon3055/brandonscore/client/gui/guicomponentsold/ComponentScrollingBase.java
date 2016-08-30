package com.brandon3055.brandonscore.client.gui.guicomponentsold;

/**
 * Created by Brandon on 6/03/2015.
 */
public abstract class ComponentScrollingBase extends ComponentBase {

    protected GUIScrollingBase gui;

    public ComponentScrollingBase(int x, int y, GUIScrollingBase gui) {
        super(x, y);
        this.gui = gui;
    }

    public abstract void handleScrollInput(int direction);

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return super.isMouseOver(mouseX, mouseY + gui.scrollOffset);
    }

}
