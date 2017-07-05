package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider.HoverColour;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 3/09/2016.
 * Renders a simple but surprisingly useful bordered rectangle.
 * The colour options are extremely flexible with the ability to add colour callbacks for the fill and boarder colours
 * and the ability to specify different colours depending on whether or not the cursor is over the element.
 */
public class MGuiBorderedRect extends MGuiElementBase<MGuiBorderedRect> {

    private HoverColour<Integer> fillColour;
    private HoverColour<Integer> borderColour;

    public double borderWidth = 1;

    public MGuiBorderedRect() {}

    public MGuiBorderedRect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public MGuiBorderedRect(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        boolean hovering = isMouseOver(mouseX, mouseY);
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), borderWidth, getFillColour(hovering), getBorderColour(hovering));
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }


    public MGuiBorderedRect setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    //region Colour setters and getters

    public MGuiBorderedRect setColours(int fill, int fillHover, int border, int borderHover) {
        setFillColours(fill, fillHover);
        setBorderColours(border, borderHover);
        return this;
    }

    public MGuiBorderedRect setColours(int fill, int border) {
        setFillColour(fill);
        setBorderColour(border);
        return this;
    }

    //Border Colour

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public MGuiBorderedRect setBorderColourGetter(HoverColour<Integer> rectBorderColour) {
        this.borderColour = rectBorderColour;
        return this;
    }

    public MGuiBorderedRect setBorderColours(int border, int borderHover) {
        setBorderColourGetter(hovering -> hovering ? borderHover : border);
        return this;
    }

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public MGuiBorderedRect setBorderColour(int border) {
        if (borderColour != null) {
            int hover = borderColour.getColour(true);
            setBorderColourGetter(hovering -> hovering ? hover : border);
        }

        setBorderColourGetter(hovering -> border);
        return this;
    }

    //Fill Colour

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public MGuiBorderedRect setFillColourGetter(HoverColour<Integer> rectBackColour) {
        this.fillColour = rectBackColour;
        return this;
    }

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public MGuiBorderedRect setFillColours(int fill, int fillHover) {
        setFillColourGetter(hovering -> hovering ? fillHover : fill);
        return this;
    }

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public MGuiBorderedRect setFillColour(int fill) {
        if (fillColour != null) {
            int hover = fillColour.getColour(true);
            setFillColourGetter(hovering -> hovering ? hover : fill);
        }

        setFillColourGetter(hovering -> fill);
        return this;
    }

    //endregion


    /**
     * Allows you to adjust the width of the border around the rectangle.
     * Values less then 1 are permitted but may not render correctly at certain GUI scales.
     * Default: 1
     */
    public MGuiBorderedRect setBorderWidth(double borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    protected int getFillColour(boolean hover) {
        if (fillColour != null) {
            return fillColour.getColour(hover);
        }
        else if (borderColour != null) {
            return borderColour.getColour(hover);
        }
        return 0xFFFFFFFF;
    }

    protected int getBorderColour(boolean hover) {
        if (borderColour != null) {
            return borderColour.getColour(hover);
        }
        else if (fillColour != null) {
            return fillColour.getColour(hover);
        }
        return 0xFF000000;
    }
}
