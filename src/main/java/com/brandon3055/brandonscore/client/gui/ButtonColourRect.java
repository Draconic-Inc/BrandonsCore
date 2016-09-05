package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 4/07/2016.
 */
public class ButtonColourRect extends GuiButton{
    private final int backColour;
    private final int borderColourInactive;
    private final int borderColourActive;

    public ButtonColourRect(int buttonId, String text, int x, int y, int width, int height, int backColour, int borderColourInactive, int borderColourActive) {
        super(buttonId, x, y, width, height, text);
        this.backColour = backColour;
        this.borderColourInactive = borderColourInactive;
        this.borderColourActive = borderColourActive;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);

            GuiHelper.drawColouredRect(xPosition + 1, yPosition + 1, width - 2, height - 2, backColour);
            int border = hovered ? borderColourActive : borderColourInactive;
            GuiHelper.drawColouredRect(xPosition, yPosition, width, 1, border);
            GuiHelper.drawColouredRect(xPosition, yPosition + height - 1, width, 1, border);
            GuiHelper.drawColouredRect(xPosition, yPosition, 1, height, border);
            GuiHelper.drawColouredRect(xPosition + width - 1, yPosition, 1, height, border);

            GuiHelper.drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2, yPosition + (height / 2) - (mc.fontRendererObj.FONT_HEIGHT / 2), 0xFFFFFF, false);

            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        super.playPressSound(soundHandlerIn);
    }
}
