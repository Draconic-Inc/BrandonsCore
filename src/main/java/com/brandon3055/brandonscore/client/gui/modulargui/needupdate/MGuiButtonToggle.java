package com.brandon3055.brandonscore.client.gui.modulargui.needupdate;

import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;

import java.io.IOException;

/**
 * Created by brandon3055 on 3/09/2016.
 */
@Deprecated //This is now built in to GuiButton
public class MGuiButtonToggle extends GuiButton {

    public boolean isPressed;

    public MGuiButtonToggle() {}

    public MGuiButtonToggle(int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(buttonId, xPos, yPos, xSize, ySize, buttonText);
    }

    public MGuiButtonToggle(String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(xPos, yPos, xSize, ySize, buttonText);
        setButtonName(buttonName);
    }

    public MGuiButtonToggle(int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(xPos, yPos, xSize, ySize, buttonText);
    }


    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        String displayString = getDisplayString();
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        boolean hovered = isMouseOver(mouseX, mouseY);
        int k = getRenderState(isPressed());
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawTexturedModalRect(xPos(), yPos(), 0, 46 + k * 20, xSize() % 2 + xSize() / 2, ySize());
        drawTexturedModalRect(xSize() % 2 + xPos() + xSize() / 2, yPos(), 200 - xSize() / 2, 46 + k * 20, xSize() / 2, ySize());

        if (ySize() < 20) {
            drawTexturedModalRect(xPos(), yPos() + 3, 0, (46 + k * 20) + 20 - ySize() + 3, xSize() % 2 + xSize() / 2, ySize() - 3);
            drawTexturedModalRect(xSize() % 2 + xPos() + xSize() / 2, yPos() + 3, 200 - xSize() / 2, (46 + k * 20) + 20 - ySize() + 3, xSize() / 2, ySize() - 3);
        }

        int l = getTextColour(hovered, disabled);

        if (alignment == GuiAlign.CENTER) {
            drawCenteredString(fontRenderer, displayString, xPos() + xSize() / 2, yPos() + (ySize() - 8) / 2, l, dropShadow);
        }
        else {
            int buffer = 1 + ((ySize() - fontRenderer.FONT_HEIGHT) / 2);
            if (alignment == GuiAlign.LEFT) {
                drawString(fontRenderer, displayString, xPos() + buffer, yPos() + (ySize() - 8) / 2, l, dropShadow);
            }
            else {
                drawString(fontRenderer, displayString, ((xPos() + xSize()) - buffer) - fontRenderer.getStringWidth(displayString), yPos() + (ySize() - 8) / 2, l, dropShadow);
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
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, isPressed ? 1.0F : 0.9F));
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.ButtonEvent(this), this);
            }
            return true;
        }
        return false;
    }
}
