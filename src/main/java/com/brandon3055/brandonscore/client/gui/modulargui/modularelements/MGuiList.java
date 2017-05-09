package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IScrollListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiList extends MGuiElementBase implements IScrollListener {

    protected boolean scrollBarEnabled = true;
    protected MGuiScrollBar scrollBar;
    public int leftPadding = 1, rightPadding = 1, topPadding = 1, bottomPadding = 1;
    public boolean disableList = false;
    protected boolean updateRequired = true;
    public boolean allowOutsideClicks = false;
    public boolean lockScrollBar = false;

    public LinkedList<MGuiListEntry> listEntries = new LinkedList<MGuiListEntry>();
    public LinkedList<MGuiElementBase> nonListEntries = new LinkedList<MGuiElementBase>();
    private boolean scrollingEnabled = true;

    public MGuiList(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiList(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiList(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
    }

    @Override
    public void initElement() {
        initScrollBar();
        updateEntriesAndScrollBar();
        super.initElement();
    }

    protected void initScrollBar() {
        if (scrollBar != null) {
            removeChild(scrollBar);
        }
        if (!scrollingEnabled) {
            return;
        }
        scrollBar = new MGuiScrollBar(modularGui, xPos + xSize - 10, yPos + 1, 10, ySize - 2);
        addChild(scrollBar);
        scrollBar.setListener(this);
        scrollBar.parentScrollable = this;
    }

    //region List

    //TODO on re write. Do away with the hole "ListEntry" idea and find a way for this to work with MGuiElementBase
    public MGuiList addEntry(MGuiListEntry entry) {
        listEntries.add(entry);
        entry.setList(this);
        super.addChild(entry);
        updateRequired = true;
        return this;
    }

    @Override
    public MGuiElementBase addChild(MGuiElementBase element) {
        nonListEntries.add(element);
        return super.addChild(element);
    }

    public void clear() {
        toRemove.addAll(listEntries);
        listEntries.clear();
    }

    //endregion

    //region Render

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : nonListEntries) {
            if (element.isEnabled() && !listEntries.contains(element)) {
                element.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        cullList();

        if (disableList) {
            return;
        }

        for (MGuiElementBase element : listEntries) {
            if (element.isEnabled()) {
                element.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public void renderForegroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : nonListEntries) {
            if (element.isEnabled() && !listEntries.contains(element)) {
                element.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        cullList();

        if (disableList) {
            return;
        }

        for (MGuiElementBase element : listEntries) {
            if (element.isEnabled()) {
                element.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : nonListEntries) {
            if (element.isEnabled() && !listEntries.contains(element) && element.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks)) {
                return true;
            }
        }

        cullList();

        if (disableList) {
            return false;
        }

        for (MGuiElementBase element : listEntries) {
            if (element.isEnabled() && element.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks)) {
                return true;
            }
        }
        return false;
    }

    protected void cullList() {
        zOffset = 160;

//        GlStateManager.enableCull();
        //GlStateManager.enableDepth();
        GlStateManager.disableAlpha();
//        GlStateManager.disableDepth();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        double left = xPos;
        double top = 0;
        double right = xPos + xSize;
        double bottom = yPos + topPadding;
        double zLevel = getRenderZLevel();

        vertexbuffer.pos(left, bottom, zLevel).color(1F, 1F, 1F, 0F).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).color(1F, 1F, 1F, 0F).endVertex();
        vertexbuffer.pos(right, top, zLevel).color(1F, 1F, 1F, 0F).endVertex();
        vertexbuffer.pos(left, top, zLevel).color(1F, 1F, 1F, 0F).endVertex();

        top = yPos + ySize - bottomPadding;
        bottom = modularGui.screenHeight();

        vertexbuffer.pos(left, bottom, zLevel).color(1F, 1F, 1F, 0F).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).color(1F, 1F, 1F, 0F).endVertex();
        vertexbuffer.pos(right, top, zLevel).color(1F, 1F, 1F, 0F).endVertex();
        vertexbuffer.pos(left, top, zLevel).color(1F, 1F, 1F, 0F).endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
//        GlStateManager.enableDepth();
        zOffset = 0;
    }

    //endregion

    //region Update

    @Override
    public void scrollBarMoved(double pos) {
        if (!scrollingEnabled) {
            return;
        }
        if (scrollBar == null) {
            initScrollBar();
        }

        int maxMove = getListHeight() - (ySize - 1);
        scrollBar.setIncrements(50D / maxMove, 0.1D);
        updateEntriesAndScrollBar();
    }

    protected void updateEntriesAndScrollBar() {
        if (!scrollingEnabled) {
            return;
        }
        if (scrollBar == null) {
            initScrollBar();
        }

        double scrollPos = scrollBar == null ? 0 : scrollBar.getScrollPos();
        int yOffset = topPadding;

        int maxMove = getListHeight() - (ySize - 1);

        if (maxMove > 0 && scrollPos > 0) {
            yOffset = topPadding - (int) (scrollPos * maxMove);
        }

        for (MGuiListEntry entry : listEntries) {
            if (!entry.isEnabled()) {
                continue;
            }

            entry.moveEntry(xPos + leftPadding, yPos + yOffset);

            yOffset += entry.getEntryHeight();
        }

        boolean canScroll = maxMove > 0;

        if (!canScroll && lockScrollBar) {
            scrollBar.setEnabled((scrollBarEnabled = true));
            scrollBar.setBarSizeRatio(0);
        }
        else {
            scrollBar.setEnabled((scrollBarEnabled = canScroll));
            scrollBar.setBarSizeRatio((double) (maxMove + ySize) / (double) ySize);
        }
    }

    /**
     * @return the total height of all enabled list elements inclining top and bottom padding.
     */
    protected int getListHeight() {
        int height = 0;

        for (MGuiListEntry entry : listEntries) {
            if (!entry.isEnabled()) {
                continue;
            }

            height += entry.getEntryHeight();
        }

        return height + topPadding + bottomPadding;
    }

    /**
     * @return the total height of all enabled list elements NOT inclining top and bottom padding.
     */
    protected int getRawListHeight() {
        int height = 0;

        for (MGuiListEntry entry : listEntries) {
            if (!entry.isEnabled()) {
                continue;
            }

            height += entry.getEntryHeight();
        }

        return height;
    }

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
    }

    @Override
    public boolean onUpdate() {
        if (!toRemove.isEmpty()) {
            nonListEntries.removeAll(toRemove);
        }

        if (updateRequired) {
            updateRequired = false;
            updateEntriesAndScrollBar();
        }

        return super.onUpdate();
    }

    /**
     * Schedules an update to occur next tick that will recalculate the positions of all
     * entries and enable/disable the scroll bar as needed.
     */
    public void schedualUpdate() {
        updateRequired = true;
    }

    //endregion

    //region Misc

    public MGuiList setScrollBarEnabled(boolean scrollBarEnabled) {
        this.scrollBarEnabled = scrollBarEnabled;
        return this;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!isMouseOver(mouseX, mouseY) && !allowOutsideClicks) {
            return false;
        }

        for (MGuiElementBase element : nonListEntries) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }

        if (disableList) {
            return false;
        }

        for (MGuiElementBase element : listEntries) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If set to false scrolling will be completely disabled.
     * List sorting will also be disabled meaning you will need to ether manually arrange the component positions or use one of the built in sorting methods
     *
     * @param scrollingEnabled
     * @return
     */
    public MGuiList setScrollingEnabled(boolean scrollingEnabled) {
        this.scrollingEnabled = scrollingEnabled;
        return this;
    }

    //endregion

    //region Manual list sorting
    //TODO add new sorting methods as needed

    /**
     * This method will sort the list and arrange the elements so they are evenly spaces within the list as to use all available vertical space.
     * This should only be used with list entries of equal height.
     *
     * @param compress If true the entries will be compressed (if needed) to make them all fit within the list. Meaning elements will be allowed to overlap.
     * @return the list.
     */
    public MGuiList sortEvenSpacing(boolean compress) {
        int totalEntryHeight = getRawListHeight();
        int remainingSpace = ySize - totalEntryHeight;

        double y = yPos;
        if (remainingSpace >= 0) {
            //Initial element alignment.
            for (MGuiListEntry entry : listEntries) {
                entry.setXPos(xPos + ((xSize - entry.xSize) / 2));
                entry.setYPos((int) y);
                y += entry.ySize;
            }

            //Center Elements
            y = 0;
            for (MGuiListEntry entry : listEntries) {
                double eHeight = entry.getEntryHeight();
                double scale = eHeight / (double) totalEntryHeight;
                double offsetShare = (scale * remainingSpace) / 2;
                entry.moveBy(0, (int) (y + offsetShare));
                y += offsetShare * 2;
            }
        }
        else {
            int ySize = compress ? this.ySize : totalEntryHeight;
            int i = 0;
            for (MGuiListEntry entry : listEntries) {
                double eHeight = entry.getEntryHeight();
                double yAllocation = (double)(ySize) / (double)(listEntries.size());
                double overlap = eHeight - yAllocation;
                yAllocation = (ySize - overlap) / (double)(listEntries.size());

                entry.setXPos(xPos + ((xSize - entry.xSize) / 2));
                entry.setYPos(yPos + (int) (i * yAllocation));
                i++;
            }
        }

        return this;
    }

    //endregion
}
