package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.gui.ScreenUtils;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 29/04/2022
 */
public class GuiTooltipBackground extends GuiElement<GuiTooltipBackground> {

    private Supplier<Integer> backgroundColor = () -> ScreenUtils.DEFAULT_BACKGROUND_COLOR;
    private Supplier<Integer> borderColor = () -> ScreenUtils.DEFAULT_BORDER_COLOR_START;
    private Supplier<Integer> borderEndColor = () -> (borderColor.get() & 0xFEFEFE) >> 1 | borderColor.get() & 0xFF000000;
    private boolean empty = false;

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
        MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0, 0, getRenderZLevel());
        GuiHelper.drawHoverRect(getter, poseStack, xPos(), yPos(), xSize(), ySize(), backgroundColor.get(), borderColor.get(), borderEndColor.get(), empty);
        getter.endBatch();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    public GuiTooltipBackground setBackgroundColor(int backgroundColor) {
        this.backgroundColor = () -> backgroundColor;
        return this;
    }

    public GuiTooltipBackground setBorderColor(int borderColor) {
        this.borderColor = () -> borderColor;
        return this;
    }

    public GuiTooltipBackground setBorderEndColor(int borderEndColor) {
        this.borderEndColor = () -> borderEndColor;
        return this;
    }

    public GuiTooltipBackground setBackgroundColor(Supplier<Integer> backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public GuiTooltipBackground setBorderColor(Supplier<Integer> borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public GuiTooltipBackground setBorderEndColor(Supplier<Integer> borderEndColor) {
        this.borderEndColor = borderEndColor;
        return this;
    }

    public GuiTooltipBackground setEmpty(boolean empty) {
        this.empty = empty;
        return this;
    }
}
