package com.brandon3055.brandonscore.client.gui.modulargui.needupdate;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 5/09/2016.
 */
@Deprecated //This is now built in to GuiButton
public class MGuiVerticalButton extends MGuiButtonSolid {

    public boolean rotateBottomToTop = true;

    public MGuiVerticalButton() {}

    public MGuiVerticalButton(int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(buttonId, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiVerticalButton(String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(buttonName, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiVerticalButton(int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(xPos, yPos, xSize, ySize, buttonText);
    }

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, getFillColour(hovered, disabled), getBorderColour(hovered, disabled));

        GlStateManager.pushMatrix();

        GlStateManager.translate(xPos() + xSize() / 2, yPos() + (ySize() / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), 0);
        GlStateManager.rotate(rotateBottomToTop ? -90 : 90, 0, 0, 1);
        GlStateManager.translate(-(xPos() + xSize() / 2), -(yPos() + (ySize() / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2)), 0);

        int translation = mc.fontRendererObj.FONT_HEIGHT / 2;

        GlStateManager.translate(rotateBottomToTop ? -translation : translation, -translation, 0);

        int l = getTextColour(hovered, disabled);

        if (alignment == GuiAlign.CENTER) {
            drawCenteredString(fontRenderer, getDisplayString(), xPos() + xSize() / 2, yPos() + (ySize() / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), l, dropShadow);
        }
        else {
            int buffer = 1 + ((xSize() - fontRenderer.FONT_HEIGHT) / 2);
            if (alignment == GuiAlign.LEFT) {
                drawString(fontRenderer, getDisplayString(), xPos() + xSize() / 2 - ySize() / 2 + buffer, yPos() + (ySize() / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), l, dropShadow);
            }
            else {
                drawString(fontRenderer, getDisplayString(), xPos() + xSize() / 2 + ySize() / 2 - fontRenderer.getStringWidth(getDisplayString()) - buffer, yPos() + (ySize() / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), l, dropShadow);
            }
        }

        GlStateManager.popMatrix();
    }
}
