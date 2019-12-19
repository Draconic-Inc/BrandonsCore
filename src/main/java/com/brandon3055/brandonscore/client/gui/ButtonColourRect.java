package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 4/07/2016.
 */
@Deprecated //May keep these for when i need to inject a button onto a vanilla gui. But should not be used for anything else.
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
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float pt) {
        if (visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);

            GuiHelper.drawColouredRect(x + 1, y + 1, width - 2, height - 2, backColour);
            int border = hovered ? borderColourActive : borderColourInactive;
            GuiHelper.drawColouredRect(x, y, width, 1, border);
            GuiHelper.drawColouredRect(x, y + height - 1, width, 1, border);
            GuiHelper.drawColouredRect(x, y, 1, height, border);
            GuiHelper.drawColouredRect(x + width - 1, y, 1, height, border);

            GuiHelper.drawCenteredString(mc.fontRenderer, displayString, x + width / 2, y + (height / 2) - (mc.fontRenderer.FONT_HEIGHT / 2), 0xFFFFFF, false);

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
