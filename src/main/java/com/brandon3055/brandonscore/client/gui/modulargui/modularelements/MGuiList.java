package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
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

    public LinkedList<MGuiListEntry> listEntries = new LinkedList<MGuiListEntry>();

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
        updateEntryPositions();
        super.initElement();
    }

    protected void initScrollBar() {
        scrollBar = new MGuiScrollBar(modularGui, xPos + xSize - 10, yPos + 1, 10, ySize - 2);
        addChild(scrollBar);
        scrollBar.setListener(this);
    }

    //region List

    public MGuiList addEntry(MGuiListEntry entry) {
        listEntries.add(entry);
        addChild(entry);
        return this;
    }

    public void clear() {
        toRemove.addAll(listEntries);
        listEntries.clear();
    }

    //endregion

    //region Render

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        cullList();
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderForegroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        cullList();
        super.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        cullList();
        super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    protected void cullList() {
        zOffset = 90;

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableAlpha();

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
        zOffset = 0;
    }

    //endregion

    //region Update

    @Override
    public void scrollBarMoved(double pos) {
        updateEntryPositions();
    }

    protected void updateEntryPositions() {
        double scrollPos = scrollBar == null ? 0 : scrollBar.getScrollPos();
        int yOffset = topPadding;

        int maxMove = getListHeight() - (ySize - 1);

        if (maxMove > 0 && scrollPos > 0) {
            yOffset = 1 -(int) (scrollPos * maxMove);
        }

        for (MGuiListEntry entry : listEntries) {
            if (!entry.isEnabled()) {
                continue;
            }

            entry.moveEntry(xPos + leftPadding, yPos + yOffset);

            yOffset += entry.getEntryHeight();
        }
    }

    protected int getListHeight() {
        int height = 0;

        for (MGuiListEntry entry : listEntries) {
            if (!entry.isEnabled()) {
                continue;
            }

            height += entry.getEntryHeight();
        }

        return height;
    }

    //endregion

    //region Misc

    public MGuiList setScrollBarEnabled(boolean scrollBarEnabled) {
        this.scrollBarEnabled = scrollBarEnabled;
        return this;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    //endregion
}
