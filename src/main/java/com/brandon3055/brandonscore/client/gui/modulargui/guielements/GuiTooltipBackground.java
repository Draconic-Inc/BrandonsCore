package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.gui.GuiUtils;

/**
 * Created by brandon3055 on 29/04/2022
 */
public class GuiTooltipBackground extends GuiElement<GuiTooltipBackground> {

    private int backgroundColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
    private int borderColor = GuiUtils.DEFAULT_BORDER_COLOR_START;

    public GuiTooltipBackground() {
    }

    public GuiTooltipBackground(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTooltipBackground(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        MultiBufferSource.BufferSource getter = RenderUtils.getTypeBuffer();
        GuiHelper.drawHoverRect(getter, new PoseStack(), xPos(), yPos(), xSize(), ySize(), backgroundColor, borderColor);
        getter.endBatch();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    public GuiTooltipBackground setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public GuiTooltipBackground setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }
}
