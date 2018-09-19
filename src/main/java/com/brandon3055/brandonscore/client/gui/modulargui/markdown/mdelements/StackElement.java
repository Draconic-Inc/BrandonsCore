package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.integration.JeiHelper;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.api.PiAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED;
import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class StackElement extends MDElementBase<StackElement> {

    private ItemStack[] stacks;
    public boolean drawSlot = false;

    public StackElement(String stackString) {
        this.enableTooltip = true;
        this.size = 16;

        boolean isOre = OreDictionary.doesOreNameExist(stackString);

        List<ItemStack> baseStacks = new ArrayList<>();

        if (isOre) {
            baseStacks.addAll(OreDictionary.getOres(stackString));
        }
        else {
            StackReference stackRef = StackReference.fromString(stackString);
            ItemStack stack;
            if (stackRef == null || (stack = stackRef.createStack()).isEmpty()) {
                error("[Broken Stack. Specified Item or Block could not be found!]");
                return;
            }
            baseStacks.add(stack);
        }

        NonNullList<ItemStack> finalStacks = NonNullList.create();
        for (ItemStack stack : baseStacks) {
            if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE && stack.getHasSubtypes()) {
                stack.getItem().getSubItems(CreativeTabs.SEARCH, finalStacks);
            }
            else {
                finalStacks.add(stack);
            }
        }

        stacks = finalStacks.toArray(new ItemStack[finalStacks.size()]);
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        setSize(size, size);
        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();

        if (drawSlot) {
            bindTexture(BCTextures.MODULAR_GUI);
            GlStateManager.color(1F, 1F, 1F, 1F);
            drawScaledCustomSizeModalRect(xPos(), yPos(), 0, 0, 18, 18, xSize(), ySize(), 255, 255);
        }

        double scale = size / 18D;
        ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate(xPos() + scale, yPos() + scale, getRenderZLevel() - 80);
        GlStateManager.scale(scale, scale, 1);
        minecraft.getRenderItem().renderItemIntoGUI(stack, 0, 0);

        if (stack.getCount() > 1) {
            String s = "" + Utils.SELECT + "f" + stack.getCount() + "" + Utils.SELECT + "f";
            GlStateManager.translate(0, 0, -(getRenderZLevel() - 80));
            zOffset += 45;
            drawString(fontRenderer, s, 18 - (fontRenderer.getStringWidth(s)) - 1, fontRenderer.FONT_HEIGHT, 0xFFFFFF, true);
            zOffset -= 45;
        }

        GlStateManager.color(fontRenderer.red, fontRenderer.blue, fontRenderer.green, 1);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (enableTooltip && isMouseOver(mouseX, mouseY)) {
            if (tooltip.isEmpty()) {
                ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
                List<String> list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ADVANCED : NORMAL);

                for (int i = 0; i < list.size(); ++i) {
                    if (i == 0) {
                        list.set(i, stack.getRarity().rarityColor + list.get(i));
                    }
                    else {
                        list.set(i, TextFormatting.GRAY + list.get(i));
                    }
                }

                GuiUtils.preItemToolTip(stack);
                this.drawHoveringText(stack, list, mouseX, mouseY, screenWidth, screenHeight, -1, fontRenderer);
                GuiUtils.postItemToolTip();
            }
            else {
                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
            }
            return true;
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY) && (mouseButton == 0 || mouseButton == 1)) {
            ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
            JeiHelper.openJEIRecipe(stack, mouseButton == 1);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        int mouseX = Mouse.getX() * screenWidth / this.mc.displayWidth;
        int mouseY = screenHeight - Mouse.getY() * screenHeight / this.mc.displayHeight - 1;

        if (isMouseOver(mouseX, mouseY)) {
            ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
            if (keyCode == JeiHelper.getRecipeKey(false)) {
                JeiHelper.openJEIRecipe(stack, false);
                return true;
            }
            else if (keyCode == JeiHelper.getRecipeKey(true)) {
                JeiHelper.openJEIRecipe(stack, true);
                return true;
            }
            else if (PiAPI.isAPIAvalible() && keyCode == PiAPI.getETGuiKey().getKeyCode()) {
                List<String> pages = PiAPI.getRelatedPages(stack);
                if (!pages.isEmpty()) {
                    PiAPI.openGui(modularGui.getScreen(), pages);
                    return true;
                }
            }
        }

        return super.keyTyped(typedChar, keyCode);
    }
}