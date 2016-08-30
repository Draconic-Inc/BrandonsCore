package com.brandon3055.brandonscore.client.gui.guicomponentsold;

import net.minecraft.inventory.Container;

import java.io.IOException;

/**
 * Created by Brandon on 6/03/2015.
 */
public abstract class GUIScrollingBase extends GUIBase {

    public int scrollOffset;
    public int pageLength;
    public int scrollLimit;
    public int barPosition;
    public boolean disableScrollBar = true;

    public GUIScrollingBase(Container container, int xSize, int ySize) {
        super(container, xSize, ySize);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = org.lwjgl.input.Mouse.getEventDWheel();
        if (i != 0) {
            handleScrollInput(i > 0 ? -1 : 1);

            for (ComponentBase c : collection.getComponents()) {
                if (c instanceof ComponentScrollingBase && c.isEnabled())
                    ((ComponentScrollingBase) c).handleScrollInput(i > 0 ? -1 : 1);
            }
        }
    }

    public abstract void handleScrollInput(int direction);

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
    }

    protected int clickedY = 0;
    protected boolean scrollPressed = false;

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if (disableScrollBar) return;
        clickedY = y;
        if ((x - guiLeft + 17) > 0 && (x - guiLeft + 17) < 17 && clickedY - guiTop - 20 > barPosition && clickedY - guiTop - 20 < barPosition + 38)
            scrollPressed = true;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        scrollPressed = false;
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long time) {
        super.mouseClickMove(x, y, button, time);
        if (!scrollPressed || disableScrollBar) return;
        barPosition += y - clickedY;
        if (barPosition < 0) barPosition = 0;
        else if (barPosition > 247) barPosition = 247;
        if (barPosition != 0 && barPosition != 247) clickedY = y;
        barMoved((double) barPosition / 247D * 1D);
    }

    /**
     * @param position New bar position between 0 (top) and 1 (bottom)
     */
    public abstract void barMoved(double position);
}
