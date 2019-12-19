package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.lib.StackReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED;
import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class GuiStackIcon extends GuiElement<GuiStackIcon> implements IModularGui.JEITargetAdapter {
    public static Map<Integer, ItemStack> stackCache = new HashMap<>();

    public boolean drawCount = true;
    public boolean drawToolTip = true;
    public boolean drawHoverHighlight = false;
    private GuiElement background = null;
    protected List<String> toolTipOverride = null;
    private StackReference stackReference;
    private Runnable clickListener = null;
    private Consumer<Object> ingredientDropListener = null;

    public GuiStackIcon(StackReference stackReference) {
        this.stackReference = stackReference;
        setSize(18, 18);
    }

    public GuiStackIcon(int xPos, int yPos, StackReference stackReference) {
        super(xPos, yPos);
        this.stackReference = stackReference;
        setSize(18, 18);
    }

    public GuiStackIcon(int xPos, int yPos, int xSize, int ySize, StackReference stackReference) {
        super(xPos, yPos, xSize, ySize);
        this.stackReference = stackReference;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        if (drawHoverHighlight && isMouseOver(mouseX, mouseY)) {
            drawColouredRect(xPos(), yPos(), xSize(), ySize(), -2130706433);
        }

        GlStateManager.pushMatrix();
        renderStack(minecraft);
        GlStateManager.popMatrix();
    }


    private void renderStack(Minecraft minecraft) {
        RenderHelper.enableGUIStandardItemLighting();

        double scaledWidth = xSize() / 18D;
        double scaledHeight = ySize() / 18D;

        GlStateManager.translate(xPos() + scaledWidth + getInsets().left, yPos() + scaledHeight + getInsets().top, getRenderZLevel() - 80);
        GlStateManager.scale(scaledWidth, scaledHeight, 1);
        minecraft.getRenderItem().renderItemIntoGUI(getStack(), 0, 0);

        if (getStack().getItem().showDurabilityBar(getStack())) {
            double health = getStack().getItem().getDurabilityForDisplay(getStack());
            int rgbfordisplay = getStack().getItem().getRGBDurabilityForDisplay(getStack());
            int i = Math.round(13.0F - (float) health * 13.0F);

            GlStateManager.translate(0, 0, -(getRenderZLevel() - 80));
            zOffset += 45;
            drawColouredRect(2, 13, 13, 2, 0xFF000000);
            drawColouredRect(2, 13, i, 1, rgbfordisplay | 0xFF000000);
            zOffset -= 45;
            GlStateManager.translate(0, 0, (getRenderZLevel() - 80));
        }

        if (drawCount && getStack().getCount() > 1) {
            String s = getStack().getCount() + "";
            GlStateManager.translate(0, 0, -(getRenderZLevel() - 80));
            zOffset += 45;
            drawString(fontRenderer, s, (float) (xSize() / scaledWidth) - (fontRenderer.getStringWidth(s)) - 1, fontRenderer.FONT_HEIGHT, 0xFFFFFF, true);
            zOffset -= 45;
        }

        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (getInsetRect().contains(mouseX, mouseY) && (drawToolTip || toolTipOverride != null) && stackReference != null) {
            List<String> list = toolTipOverride != null ? toolTipOverride : getStack().getTooltip(minecraft.player, minecraft.gameSettings.advancedItemTooltips ? ADVANCED : NORMAL);
            drawHoveringText(list, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
            return true;
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (clickListener != null && isMouseOver(mouseX, mouseY)) {
            clickListener.run();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void setClickListener(Runnable clickListener) {
        this.clickListener = clickListener;
    }

    public GuiStackIcon setStack(StackReference stackReference) {
        this.stackReference = stackReference;
        return this;
    }

    /**
     * Add an element to be used as the background for this stack.<br>
     * Recommend {@link GuiSlotRender} or {@link GuiBorderedRect}<br>
     * But really you can use any element base including buttons which will make the element function as a button.<br>
     * When you add a background element its size and position will automatically be adjusted to match the stack icon.
     *
     * @param background a MGuiElementBase object.
     * @return the MGuiStackIcon
     */
    public GuiStackIcon setBackground(GuiElement background) {
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
            background.setPos(this);
            background.setSize(this);
            addChild(background);
        }

        return this;
    }

    /**
     * Will render the normal item tool tip when you hover over the stack.
     *
     * @return the MGuiStackIcon
     */
    public GuiStackIcon setToolTip(boolean drawToolTip) {
        this.drawToolTip = drawToolTip;
        return this;
    }

    public ItemStack getStack() {
        if (stackReference == null) {
            return ItemStack.EMPTY;
        }
        int hash = stackReference.hashCode();
        if (!stackCache.containsKey(hash)) {
            ItemStack stack = stackReference.createStack();
            if (stack.isEmpty()) {
                stack = new ItemStack(Blocks.BARRIER);
                toolTipOverride = new ArrayList<>();
                toolTipOverride.add("Failed to load Item Stack");
                toolTipOverride.add("This may mean the mod the stack belongs to is not installed");
                toolTipOverride.add("Or its just broken...");
            }
            stackCache.put(hash, stack);
        }
        ItemStack stack = stackCache.get(hash);

        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.BARRIER);
            stackCache.remove(hash);
        }

        return stack;
    }

    public GuiStackIcon setToolTipOverride(List<String> toolTipOverride) {
        this.toolTipOverride = toolTipOverride;
        return this;
    }

    public GuiStackIcon setDrawHoverHighlight(boolean drawHoverHighlight) {
        this.drawHoverHighlight = drawHoverHighlight;
        return this;
    }

    public GuiStackIcon addSlotBackground() {
        setBackground(null);
        addChild(background = new GuiSlotRender().setPos(this).setSizeModifiers((guiSlotRender, integer) -> GuiStackIcon.this.xSize(), (guiSlotRender, integer) -> GuiStackIcon.this.ySize()));
        return this;
    }

    public GuiElement getBackground() {
        return background;
    }

    @Override
    public Rectangle getArea() {
        return getRect();
    }

    public void setIngredientDropListener(Consumer<Object> ingredientDropListener) {
        this.ingredientDropListener = ingredientDropListener;
    }

    @Override
    public void accept(Object ingredient) {
        if (ingredientDropListener != null) {
            ingredientDropListener.accept(ingredient);
        }
    }
}
