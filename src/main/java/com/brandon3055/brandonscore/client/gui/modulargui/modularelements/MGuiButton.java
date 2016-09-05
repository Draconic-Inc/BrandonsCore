package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is button based on GuiButton with similar appearance and functionality.
 * Extend this if you want to create a custom button element that calls elementButtonAction in your IModularGui
 */
public class MGuiButton extends MGuiElementBase {

    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    public String displayString = "";
    public int buttonId = -1;
    public String buttonName = "";
    public boolean disabled = false;
    protected IButtonListener listener = null;

    public MGuiButton(IModularGui modularGui) {
        super(modularGui);
        if (modularGui instanceof IButtonListener) {
            listener = (IButtonListener) modularGui;
        }
    }

    public MGuiButton(IModularGui gui, int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
        this.buttonId = buttonId;
        if (modularGui instanceof IButtonListener) {
            listener = (IButtonListener) modularGui;
        }
    }

    public MGuiButton(IModularGui gui, String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
        this.buttonName = buttonName;
        if (modularGui instanceof IButtonListener) {
            listener = (IButtonListener) modularGui;
        }
    }

    public MGuiButton(IModularGui gui, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
        if (modularGui instanceof IButtonListener) {
            listener = (IButtonListener) modularGui;
        }
    }

    public MGuiButton setListener(IButtonListener listener) {
        this.listener = listener;
        return this;
    }

    protected int getRenderState(boolean hovered) {
        int i = 1;

        if (disabled) {
            i = 0;
        } else if (hovered) {
            i = 2;
        }

        return i;
    }

    @Override
    public void renderBackgroundLayer(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        FontRenderer fontrenderer = mc.fontRendererObj;
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        boolean hovered = isMouseOver(mouseX, mouseY);
        int k = getRenderState(hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawTexturedModalRect(xPos, yPos, 0, 46 + k * 20, xSize % 2 + xSize / 2, ySize);
        drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos, 200 - xSize / 2, 46 + k * 20, xSize / 2, ySize);

        if (ySize < 20){
            drawTexturedModalRect(xPos, yPos+3, 0, (46 + k * 20)+20-ySize+3, xSize % 2 + xSize / 2, ySize-3);
            drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos+3, 200 - xSize / 2, (46 + k * 20)+20-ySize+3, xSize / 2, ySize-3);
        }

        int l = 14737632;

        if (disabled)
        {
            l = 10526880;
        }
        else if (hovered)
        {
            l = 16777120;
        }
        drawCenteredString(fontrenderer, displayString, xPos + xSize / 2, yPos + (ySize - 8) / 2, l, true);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            modularGui.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (listener != null) {
                listener.buttonClick(this);
            }
            return true;
        }
        return false;
    }

    public MGuiButton setButtonId(int buttonId) {
        this.buttonId = buttonId;
        return this;
    }

    public MGuiButton setButtonName(String buttonName) {
        this.buttonName = buttonName;
        return this;
    }

    public MGuiButton setDisplayString(String displayString) {
        this.displayString = displayString;
        return this;
    }
}
