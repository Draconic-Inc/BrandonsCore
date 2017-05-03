package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.lib.StackReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiStackIcon extends MGuiElementBase {
    public static WeakHashMap<Integer, ItemStack> stackCache = new WeakHashMap<Integer, ItemStack>();

    public boolean drawCount = true;
    public boolean drawToolTip = true;
    public boolean drawHoverHighlight = false;
    private MGuiElementBase background = null;
    protected List<String> toolTipOverride = null;
    private StackReference stackReference;
    public int xOffset = 0;
    public int yOffset = 0;

    public MGuiStackIcon(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, StackReference stackReference) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.stackReference = stackReference;
    }

    public MGuiStackIcon(IModularGui modularGui, int xPos, int yPos, String stackRegName, int metadata, StackReference stackReference) {
        super(modularGui, xPos, yPos);
        this.stackReference = stackReference;
        this.xSize = this.ySize = 18;
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
        if (drawHoverHighlight && isMouseOver(mouseX, mouseY)) {
            drawColouredRect(xPos, yPos, xSize, ySize, -2130706433);
        }
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();

        double scaledWidth = xSize / 18D;
        double scaledHeight = ySize / 18D;

        GlStateManager.translate(xPos + scaledWidth + xOffset, yPos + scaledHeight + yOffset, getRenderZLevel() - 80);
        GlStateManager.scale(scaledWidth, scaledHeight, 1);
        minecraft.getRenderItem().renderItemIntoGUI(getStack(), 0, 0);

        if (drawCount && getStack().stackSize > 1) {
            String s = getStack().stackSize + "";
            GlStateManager.translate(0, 0, -(getRenderZLevel() - 80));
            zOffset = 45;
            drawString(minecraft.fontRendererObj, s, xSize - (minecraft.fontRendererObj.getStringWidth(s)) - 1, minecraft.fontRendererObj.FONT_HEIGHT, 0xFFFFFF, true);
            zOffset = 0;
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOver(mouseX - xOffset, mouseY - yOffset) && (drawToolTip || toolTipOverride != null)) {
            List<String> list = toolTipOverride != null ? toolTipOverride : getStack().getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
            drawHoveringText(list, mouseX, mouseY, minecraft.fontRendererObj, modularGui.screenWidth(), modularGui.screenHeight());
            return true;
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    public MGuiStackIcon setStack(StackReference stackReference) {
        this.stackReference = stackReference;
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

    public ItemStack getStack() {
        int hash = stackReference.hashCode();
        if (!stackCache.containsKey(hash)) {
            ItemStack stack = stackReference.createStack();
            if (stack == null) {
                stack = new ItemStack(Blocks.BARRIER);
                toolTipOverride = new ArrayList<String>();
                toolTipOverride.add("Failed to load Item Stack");
                toolTipOverride.add("This may mean the mod the stack belongs to is not installed");
                toolTipOverride.add("Or its just broken...");
            }
            stackCache.put(hash, stack);
        }
        ItemStack stack = stackCache.get(hash);

        if (stack == null) {
            stack = new ItemStack(Blocks.BARRIER);
            stackCache.remove(hash);
        }

        return stack;
    }

    public MGuiStackIcon setDrawHoverHighlight(boolean drawHoverHighlight) {
        this.drawHoverHighlight = drawHoverHighlight;
        return this;
    }
}
