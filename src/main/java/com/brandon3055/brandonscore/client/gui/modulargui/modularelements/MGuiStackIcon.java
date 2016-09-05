package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiStackIcon extends MGuiElementBase {
    private ItemStack stack;
    public boolean drawSlot = false;
    public boolean drawToolTip = true;
    private MGuiElementBase background = null;

    public MGuiStackIcon(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, ItemStack stack) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.stack = stack;
    }

    public MGuiStackIcon(IModularGui modularGui, int xPos, int yPos, ItemStack stack) {
        super(modularGui, xPos, yPos);
        this.stack = stack;
        this.xSize = this.ySize = 18;
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();

        double scaledWidth = xSize / 18D;
        double scaledHeight = ySize / 18D;

        GlStateManager.translate(xPos + scaledWidth, yPos + scaledHeight, getRenderZLevel() + 50);
        GlStateManager.scale(scaledWidth, scaledHeight, 1);

        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, 0, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlays(minecraft.fontRendererObj, stack, 0, 0);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOver(mouseX, mouseY) && drawToolTip) {
            List<String> list = stack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
            drawHoveringText(list, mouseX, mouseY, minecraft.fontRendererObj, modularGui.screenWidth(), modularGui.screenHeight());
        }
    }

    public MGuiStackIcon setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    /**
     * Add an element to be used as the background for this stack.<br>
     * Recommend {@link MGuiSlotRender} or {@link MGuiBorderedRect}<br>
     * But really you can use any element base including buttons which will make the element function as a button.<br>
     * When you add a background element its size and position will automatically be adjusted to match the stack icon.
     * @param background a MGuiElementBase object.
     * @return the MGuiStackIcon
     */
    public MGuiStackIcon setBackground(MGuiElementBase background) {
        if (background == null) {
            if (this.background != null) {
                removeChild(this.background);
                this.background = null;
            }
        }
        else {
            if (this.background != null) {
                removeChild(this.background);
            }
            this.background = background;
            background.xPos = xPos;
            background.yPos = yPos;
            background.xSize = xSize;
            background.ySize = ySize;
            addChild(background);
        }

        return this;
    }

    /**
     * Will render the normal item tool tip when you hover over the stack.
     * @return the MGuiStackIcon
     */
    public MGuiStackIcon setToolTip(boolean drawToolTip) {
        this.drawToolTip = drawToolTip;
        return this;
    }
}
