package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 10/09/2016.
 * See the Draconic Reactor gui for an example of this in use.
 * Simply allows you to define a GuiElement as a "pointer" that moves between min pos and max pos.
 */
public class GuiSlideIndicator extends GuiElement<GuiSlideIndicator> {

    private GuiElement<?> slideElement;
    private Supplier<Double> position = () -> 0D;
    private boolean horizontal = false;
    private int minOffset = 0;
    private int maxOffset = 0;

    public GuiSlideIndicator() {}

    public GuiSlideIndicator(Supplier<Double> position) {
        this.position = position;
    }

    public void setPosition(Supplier<Double> position) {
        this.position = position;
    }

    public GuiSlideIndicator setSlideElement(GuiElement<?> slideElement) {
        if (this.slideElement != null) {
            removeChild(this.slideElement);
        }
        this.slideElement = slideElement;
        addChild(this.slideElement);

        slideElement.setXPosMod(() -> {
            if (!horizontal) {
                return xPos();
            }
            int travel = xSize() - slideElement.xSize() - minOffset - maxOffset;
            return xPos() + minOffset + (int) (travel * getPos());
        });

        slideElement.setYPosMod(() -> {
            if (horizontal) {
                return yPos();
            }
            int travel = ySize() - slideElement.ySize() - minOffset - maxOffset;
            return maxYPos() - slideElement.ySize() - minOffset - (int) (travel * getPos());
        });

        return this;
    }

    /**
     * @param minOffset offset in pixels applied to the minimum slide position
     * @param maxOffset offset in pixels applied to the maximim slide position
     * @return this
     */
    public GuiSlideIndicator setOffsets(int minOffset, int maxOffset) {
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        return this;
    }

    public GuiSlideIndicator setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public double getPos() {
        return MathHelper.clip(position.get(), 0, 1);
    }
}
