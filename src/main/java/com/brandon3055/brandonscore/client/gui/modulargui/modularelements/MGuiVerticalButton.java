package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.EnumAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 5/09/2016.
 */
public class MGuiVerticalButton extends MGuiButtonSolid {

    public boolean rotateBottomToTop = true;

    public MGuiVerticalButton(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiVerticalButton(IModularGui gui, int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, buttonId, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiVerticalButton(IModularGui gui, String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, buttonName, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiVerticalButton(IModularGui gui, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize, buttonText);
    }

    @Override
    public void renderBackgroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        FontRenderer fontrenderer = mc.fontRendererObj;
        boolean hovered = isMouseOver(mouseX, mouseY);
        drawBorderedRect(xPos, yPos, xSize, ySize, 1, getFillColour(hovered, disabled), getBorderColour(hovered, disabled));

        GlStateManager.pushMatrix();

        GlStateManager.translate(xPos + xSize / 2, yPos + (ySize / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), 0);
        GlStateManager.rotate(rotateBottomToTop ? -90 : 90, 0, 0, 1);
        GlStateManager.translate(-(xPos + xSize / 2), -(yPos + (ySize / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2)), 0);

        int translation = mc.fontRendererObj.FONT_HEIGHT / 2;

        GlStateManager.translate(rotateBottomToTop ? -translation : translation, -translation, 0);

        int l = getTextColour(hovered, disabled);

        if (alignment == EnumAlignment.CENTER) {
            drawCenteredString(mc.fontRendererObj, displayString, xPos + xSize / 2, yPos + (ySize / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), l, dropShadow);
        }
        else {
            int buffer = 1 + ((xSize - fontrenderer.FONT_HEIGHT) / 2);
            if (alignment == EnumAlignment.LEFT) {
                drawString(mc.fontRendererObj, displayString, xPos + xSize / 2 - ySize / 2 + buffer, yPos + (ySize / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), l, dropShadow);
            }
            else {
                drawString(mc.fontRendererObj, displayString, xPos + xSize / 2 + ySize / 2 - fontrenderer.getStringWidth(displayString) - buffer, yPos + (ySize / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), l, dropShadow);
            }
        }

        GlStateManager.popMatrix();
    }
}
