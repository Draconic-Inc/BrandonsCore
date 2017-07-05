package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class MGuiSlotRender extends MGuiElementBase {

    public int xOffset = 0;
    public int yOffset = 0;

    public MGuiSlotRender() {
        setSize(18, 18);
    }

    public MGuiSlotRender(int xPos, int yPos) {
        super(xPos, yPos);
        setSize(18, 18);
    }

    public MGuiSlotRender(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        bindTexture(BCTextures.MODULAR_GUI);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawScaledCustomSizeModalRect(xPos() + xOffset, yPos() + yOffset, 0, 0, 18, 18, xSize(), ySize(), 255, 255);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }
}
