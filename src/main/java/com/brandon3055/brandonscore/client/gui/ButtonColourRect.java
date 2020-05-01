package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;

/**
 * Created by brandon3055 on 4/07/2016.
 */
@Deprecated //May keep these for when i need to inject a button onto a vanilla gui. But should not be used for anything else.
public class ButtonColourRect extends Button {
    private final int backColour;
    private final int borderColourInactive;
    private final int borderColourActive;

    public ButtonColourRect(int xPos, int yPos, int width, int hight, String displayString, Button.IPressable onPress, int backColour, int borderColourInactive, int borderColourActive) {
        super(xPos, yPos, width, hight, displayString, onPress);
        this.backColour = backColour;
        this.borderColourInactive = borderColourInactive;
        this.borderColourActive = borderColourActive;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float pt) {
        if (visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);

            GuiHelper.drawColouredRect(x + 1, y + 1, width - 2, height - 2, backColour);
            int border = isHovered ? borderColourActive : borderColourInactive;
            GuiHelper.drawColouredRect(x, y, width, 1, border);
            GuiHelper.drawColouredRect(x, y + height - 1, width, 1, border);
            GuiHelper.drawColouredRect(x, y, 1, height, border);
            GuiHelper.drawColouredRect(x + width - 1, y, 1, height, border);

            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            GuiHelper.drawCenteredString(fontRenderer, getMessage(), x + width / 2, y + (height / 2) - (fontRenderer.FONT_HEIGHT / 2), 0xFFFFFF, false);

            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();
        }
    }


}
