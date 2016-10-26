package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.EnumAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;

import java.io.IOException;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiButtonToggle extends MGuiButton {

    public boolean isPressed;

    public MGuiButtonToggle(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiButtonToggle(IModularGui gui, int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, buttonId, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiButtonToggle(IModularGui gui, String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, buttonName, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiButtonToggle(IModularGui gui, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize, buttonText);
    }


    @Override
    public void renderBackgroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        String displayString = getDisplayString();
        FontRenderer fontrenderer = mc.fontRendererObj;
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        boolean hovered = isMouseOver(mouseX, mouseY);
        int k = getRenderState(isPressed());
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawTexturedModalRect(xPos, yPos, 0, 46 + k * 20, xSize % 2 + xSize / 2, ySize);
        drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos, 200 - xSize / 2, 46 + k * 20, xSize / 2, ySize);

        if (ySize < 20) {
            drawTexturedModalRect(xPos, yPos + 3, 0, (46 + k * 20) + 20 - ySize + 3, xSize % 2 + xSize / 2, ySize - 3);
            drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos + 3, 200 - xSize / 2, (46 + k * 20) + 20 - ySize + 3, xSize / 2, ySize - 3);
        }

        int l = getTextColour(hovered, disabled);

        if (alignment == EnumAlignment.CENTER) {
            drawCenteredString(fontrenderer, displayString, xPos + xSize / 2, yPos + (ySize - 8) / 2, l, dropShadow);
        }
        else {
            int buffer = 1 + ((ySize - fontrenderer.FONT_HEIGHT) / 2);
            if (alignment == EnumAlignment.LEFT) {
                drawString(fontrenderer, displayString, xPos + buffer, yPos + (ySize - 8) / 2, l, dropShadow);
            }
            else {
                drawString(fontrenderer, displayString, ((xPos + xSize) - buffer) - fontrenderer.getStringWidth(displayString), yPos + (ySize - 8) / 2, l, dropShadow);
            }
        }
    }

    public MGuiButtonToggle setPressed(boolean pressed) {
        isPressed = pressed;
        return this;
    }

    public boolean isPressed() {
        return isPressed;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY) && !disabled) {
            isPressed = !isPressed();
            modularGui.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, isPressed ? 1.0F : 0.9F));
            if (listener != null) {
                listener.onMGuiEvent("BUTTON_PRESS", this);
            }
            return true;
        }
        return false;
    }
}
