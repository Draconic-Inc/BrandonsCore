package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.EnumAlignment;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is button based on GuiButton with similar appearance and functionality.
 * Extend this if you want to create a custom button element that calls elementButtonAction in your IModularGui
 */
public class MGuiButton extends MGuiElementBase {

    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    public String displayString = "";
    public List<String> toolTip = null;
    public int buttonId = -1;
    public String buttonName = "";
    public boolean disabled = false;
    protected IMGuiListener listener = null;
    public EnumAlignment alignment = EnumAlignment.CENTER;
    public boolean dropShadow = true;
    public int toolTipDelay = 10;
    public int hoverTime;
    public boolean mouseOver = false;
    public boolean trim = false;

    public MGuiButton(IModularGui modularGui) {
        super(modularGui);
        if (modularGui instanceof IMGuiListener) {
            listener = (IMGuiListener) modularGui;
        }
    }

    public MGuiButton(IModularGui gui, int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
        this.buttonId = buttonId;
        if (modularGui instanceof IMGuiListener) {
            listener = (IMGuiListener) modularGui;
        }
    }

    public MGuiButton(IModularGui gui, String buttonName, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
        this.buttonName = buttonName;
        if (modularGui instanceof IMGuiListener) {
            listener = (IMGuiListener) modularGui;
        }
    }

    public MGuiButton(IModularGui gui, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        super(gui, xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
        if (modularGui instanceof IMGuiListener) {
            listener = (IMGuiListener) modularGui;
        }
    }

    public MGuiButton setListener(IMGuiListener listener) {
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
        String displayString = getDisplayString();
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

        if (ySize < 20) {
            drawTexturedModalRect(xPos, yPos + 3, 0, (46 + k * 20) + 20 - ySize + 3, xSize % 2 + xSize / 2, ySize - 3);
            drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos + 3, 200 - xSize / 2, (46 + k * 20) + 20 - ySize + 3, xSize / 2, ySize - 3);
        }

        if (trim && fontrenderer.getStringWidth(displayString) > xSize - 4) {
            displayString = fontrenderer.trimStringToWidth(displayString, xSize - 8) + "..";
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

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        mouseOver = isMouseOver(mouseX, mouseY);

        List<String> toolTip = getToolTip();
        if (mouseOver && hoverTime >= toolTipDelay && toolTip != null && !toolTip.isEmpty()) {
            drawHoveringText(toolTip, mouseX, mouseY, minecraft.fontRendererObj, modularGui.screenWidth(), modularGui.screenHeight());
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY) && !disabled) {
            modularGui.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
            if (listener != null) {
                listener.onMGuiEvent("BUTTON_PRESS", this);
            }
            onPressed(mouseX, mouseY, mouseButton);
            return true;
        }
        return false;
    }

    public void onPressed(int mouseX, int mouseY, int mouseButton) {}

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

    public String getDisplayString() {
        return displayString;
    }

    public MGuiButton setAlignment(EnumAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public MGuiButton setShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public MGuiButton setToolTip(String[] toolTip) {
        this.toolTip = Arrays.asList(toolTip);
        return this;
    }

    public MGuiButton setToolTip(List<String> toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    public List<String> getToolTip() {
        return toolTip;
    }

    public int getTextColour(boolean hovered, boolean disabled) {
        if (disabled) {
            return 10526880;
        }
        return hovered ? 16777120 : 14737632;
    }

    public MGuiButton setToolTipDelay(int toolTipDelay) {
        this.toolTipDelay = toolTipDelay;
        return this;
    }

    @Override
    public boolean onUpdate() {
        if (mouseOver) {
            hoverTime++;
        }
        else {
            hoverTime = 0;
        }

        return super.onUpdate();
    }

    public MGuiButton setTrim(boolean trim) {
        this.trim = trim;
        return this;
    }
}
