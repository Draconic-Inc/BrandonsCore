package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 3/09/2016.
 * Renders a simple inventory slot background
 */
public class GuiSlotRender extends MGuiElementBase<GuiSlotRender> {

    public GuiSlotRender() {
        setSize(18, 18);
    }

    public GuiSlotRender(int xPos, int yPos) {
        super(xPos, yPos);
        setSize(18, 18);
    }

    public GuiSlotRender(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        bindTexture(BCTextures.widgets());
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawScaledCustomSizeModalRect(getInsetRect().x, getInsetRect().y, 0, 0, 18, 18, getInsetRect().width, getInsetRect().height, 255, 255);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }
}
