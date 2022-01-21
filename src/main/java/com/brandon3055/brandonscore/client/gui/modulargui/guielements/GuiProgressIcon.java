package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.RenderMaterial;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GuiProgressIcon extends GuiElement<GuiProgressIcon> {
    private RenderMaterial baseTexture;
    private RenderMaterial overlayTexture;
    private Direction direction;
    private Supplier<Double> progressSupplier = () -> 0D;
    private int upperMargin = 0;
    private int lowerMargin = 0;

    public GuiProgressIcon(RenderMaterial baseTexture, RenderMaterial overlayTexture, Direction animDirection) {
        super();
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
        this.direction = animDirection;
    }

    public GuiProgressIcon setProgressSupplier(Supplier<Double> progressSupplier) {
        this.progressSupplier = progressSupplier;
        return this;
    }

    public GuiProgressIcon setProgress(double progress) {
        this.progressSupplier = () -> progress;
        return this;
    }

    /**
     * Use this to account for any empty (transparent) pixels at the lower or upper end of the icon sprite
     *
     * @param lowerMargin The number of empty pixels on the lower side of the sprite
     * @param upperMargin The number of empty pixels on the upper side of the sprite
     */
    public GuiProgressIcon setMargins(int lowerMargin, int upperMargin) {
        this.upperMargin = upperMargin;
        this.lowerMargin = lowerMargin;
        return this;
    }

    public double getProgress() {
        return progressSupplier.get();
    }

    private double getRenderState() {
        double progress = getProgress();
        double axis = direction == direction.LEFT || direction == direction.RIGHT ? xSize() : ySize();
        double size = axis - upperMargin - lowerMargin;
        return MathHelper.clip((lowerMargin + Math.ceil((size * progress))) / axis, 0, 1);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        drawSprite(baseTexture.buffer(getter, BCSprites::makeType), xPos(), yPos(), xSize(), ySize(), baseTexture.sprite());
        direction.draw(this, getter);
        getter.endBatch();
    }

    enum Direction {
        /**
         * Progress bar animated from bottom to top
         */
        UP((icon, getter) -> GuiHelper.drawPartialSprite(icon.overlayTexture.buffer(getter, BCSprites::makeType), icon.xPos(), icon.yPos() + (icon.ySize() * (1D - icon.getRenderState())), icon.xSize(), icon.ySize() * icon.getRenderState(), icon.overlayTexture.sprite(), 0, 1D - icon.getRenderState(), 1, 1)),
        /**
         * Progress bar animated from top to bottom
         */
        DOWN((icon, getter) -> GuiHelper.drawPartialSprite(icon.overlayTexture.buffer(getter, BCSprites::makeType), icon.xPos(), icon.yPos(), icon.xSize(), icon.ySize() * icon.getRenderState(), icon.overlayTexture.sprite(), 0, 0, 1, icon.getRenderState())),
        /**
         * Progress bar animated from right to left
         */
        LEFT((icon, getter) -> GuiHelper.drawPartialSprite(icon.overlayTexture.buffer(getter, BCSprites::makeType), icon.xPos() + (icon.xSize() * (1D - icon.getRenderState())), icon.yPos(), icon.xSize() * icon.getRenderState(), icon.ySize(), icon.overlayTexture.sprite(), 1D - icon.getRenderState(), 0, 1, 1)),
        /**
         * Progress bar animated from left to right
         */
        RIGHT((icon, getter) -> GuiHelper.drawPartialSprite(icon.overlayTexture.buffer(getter, BCSprites::makeType), icon.xPos(), icon.yPos(), icon.xSize() * icon.getRenderState(), icon.ySize(), icon.overlayTexture.sprite(), 0, 0, icon.getRenderState(), 1));

        private BiConsumer<GuiProgressIcon, IRenderTypeBuffer.Impl> drawFunc;

        Direction(BiConsumer<GuiProgressIcon, IRenderTypeBuffer.Impl> drawFunc) {
            this.drawFunc = drawFunc;
        }

        private void draw(GuiProgressIcon icon, IRenderTypeBuffer.Impl getter) {
            drawFunc.accept(icon, getter);
        }
    }
}