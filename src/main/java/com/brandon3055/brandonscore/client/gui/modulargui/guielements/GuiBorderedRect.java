package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider.HoverColour;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Created by brandon3055 on 3/09/2016.
 * Renders a simple but surprisingly useful bordered rectangle.
 * The colour options are extremely flexible with the ability to add colour callbacks for the fill and boarder colours
 * and the ability to specify different colours depending on whether or not the cursor is over the element.
 */
public class GuiBorderedRect extends GuiElement<GuiBorderedRect> {

    private HoverColour<Integer> fillColour;
    private HoverColour<Integer> borderColour;
    private HoverColour<Integer> borderTopLeft;
    private HoverColour<Integer> borderBottomRight;
    private boolean is3dEffect = false;
    public double borderWidth = 1;
    public double doubleBorder = 0;

    public GuiBorderedRect() {}

    public GuiBorderedRect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiBorderedRect(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        boolean hovering = isMouseOver(mouseX, mouseY);
        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        if (is3dEffect) {
            int fill = getFillColour(hovering);
            int topLeft = getBorderTopLeft(hovering);
            int bottomRight = getBorderBottomRight(hovering);
            if (doubleBorder > 0) {
                int border = getBorderColour(hovering);
                drawBorderedRect(getter, xPos(), yPos(), xSize(), ySize(), doubleBorder, fill, border);
            }
            drawShadedRect(getter, xPos() + doubleBorder, yPos() + doubleBorder, xSize() - (2 * doubleBorder), ySize() - (2 * doubleBorder), borderWidth, fill, topLeft, bottomRight, midColour(topLeft, bottomRight));
        } else {
            drawBorderedRect(getter, xPos(), yPos(), xSize(), ySize(), borderWidth, getFillColour(hovering), getBorderColour(hovering));
        }
        getter.endBatch();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    //region Colour setters and getters

    public GuiBorderedRect setColours(int fill, int fillHover, int border, int borderHover) {
        setFillColours(fill, fillHover);
        setBorderColours(border, borderHover);
        return this;
    }

    public GuiBorderedRect setColours(int fill, int border) {
        setFillColour(fill);
        setBorderColour(border);
        return this;
    }

    public GuiBorderedRect setColours(int fill, int topLeft, int bottomRight) {
        setFillColour(fill);
        set3dTopLeftColour(topLeft);
        set3dBottomRightColour(bottomRight);
        return this;
    }

    //Border Colour

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public GuiBorderedRect setBorderColourL(HoverColour<Integer> rectBorderColour) {
        this.borderColour = rectBorderColour;
        return this;
    }

    public GuiBorderedRect setBorderColours(int border, int borderHover) {
        setBorderColourL(hovering -> hovering ? borderHover : border);
        return this;
    }

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public GuiBorderedRect setBorderColour(int border) {
        if (borderColour != null) {
            int hover = borderColour.getColour(true);
            setBorderColourL(hovering -> hovering ? hover : border);
        }

        setBorderColourL(hovering -> border);
        return this;
    }

    public GuiBorderedRect set3dBottomRightColourL(HoverColour<Integer> borderBottomRight) {
        this.borderBottomRight = borderBottomRight;
        is3dEffect = borderBottomRight != null;
        return this;
    }

    public GuiBorderedRect set3dBottomRightColour(int border, int borderHover) {
        set3dBottomRightColourL(hovering -> hovering ? borderHover : border);
        return this;
    }

    public GuiBorderedRect set3dBottomRightColour(int border) {
        set3dBottomRightColourL(hovering -> border);
        return this;
    }

    public GuiBorderedRect set3dTopLeftColourL(HoverColour<Integer> borderTopLeft) {
        this.borderTopLeft = borderTopLeft;
        is3dEffect = borderTopLeft != null;
        return this;
    }

    public GuiBorderedRect set3dTopLeftColour(int border, int borderHover) {
        set3dTopLeftColourL(hovering -> hovering ? borderHover : border);
        return this;
    }

    public GuiBorderedRect set3dTopLeftColour(int border) {
        set3dTopLeftColourL(hovering -> border);
        return this;
    }

    //Fill Colour

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public GuiBorderedRect setFillColourL(HoverColour<Integer> rectBackColour) {
        this.fillColour = rectBackColour;
        return this;
    }

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public GuiBorderedRect setFillColours(int fill, int fillHover) {
        setFillColourL(hovering -> hovering ? fillHover : fill);
        return this;
    }

    /**
     * Note: if you only set the border colour OR the fill colour that colour will be used for both the border AND the fill.
     */
    public GuiBorderedRect setFillColour(int fill) {
        if (fillColour != null) {
            int hover = fillColour.getColour(true);
            setFillColourL(hovering -> hovering ? hover : fill);
        }

        setFillColourL(hovering -> fill);
        return this;
    }

    public GuiBorderedRect setGetters(GuiColourProvider<Integer> fillGetter, GuiColourProvider<Integer> borderGetter) {
        setFillColourL((e) -> fillGetter.getColour());
        setBorderColourL((e) -> borderGetter.getColour());
        return this;
    }

    public GuiBorderedRect set3DGetters(GuiColourProvider<Integer> fillGetter, GuiColourProvider<Integer> topLeft, GuiColourProvider<Integer> bottomRight) {
        setFillColourL((e) -> fillGetter.getColour());
        set3dTopLeftColourL((e) -> topLeft.getColour());
        set3dBottomRightColourL((e) -> bottomRight.getColour());
        return this;
    }

    public GuiBorderedRect setHoverGetters(GuiColourProvider.HoverColour<Integer> fillGetter, GuiColourProvider.HoverColour<Integer> borderGetter) {
        setFillColourL(fillGetter);
        setBorderColourL(borderGetter);
        return this;
    }

    public GuiBorderedRect setHoverGetters(GuiColourProvider.HoverColour<Integer> fillGetter, GuiColourProvider.HoverColour<Integer> borderGetter, GuiColourProvider.HoverColour<Integer> shadeGetter) {
        setFillColourL(fillGetter);
        setBorderColourL(borderGetter);
        set3dBottomRightColourL(shadeGetter);
        return this;
    }

    //endregion


    /**
     * Allows you to adjust the width of the border around the rectangle.
     * Values less then 1 are permitted but may not render correctly at certain GUI scales.
     * Default: 1
     */
    public GuiBorderedRect setBorderWidth(double borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    /**
     * When using 3D boarders this will apply a regular boarder of this width using the regular boarder colour.
     * The 3D boarder will then be drawn inside this regular boarder.
     */
    public GuiBorderedRect setDoubleBorder(double outerWidth) {
        this.doubleBorder = outerWidth;
        return this;
    }

    protected int getFillColour(boolean hover) {
        if (fillColour != null) {
            return fillColour.getColour(hover);
        } else if (borderColour != null) {
            return borderColour.getColour(hover);
        }
        return 0xFFFFFFFF;
    }

    protected int getBorderColour(boolean hover) {
        if (borderColour != null) {
            return borderColour.getColour(hover);
        } else if (fillColour != null) {
            return fillColour.getColour(hover);
        }
        return 0xFF000000;
    }

    protected int getBorderBottomRight(boolean hover) {
        if (borderBottomRight != null) {
            return borderBottomRight.getColour(hover);
        }
        return 0xFF000000;
    }

    protected int getBorderTopLeft(boolean hover) {
        if (borderTopLeft != null) {
            return borderTopLeft.getColour(hover);
        }
        return 0xFF000000;
    }

//    public void drawShadedRect(GuiElement element, int x, int y, int width, int height, int bw, int fill, int topLeftColour, int bottomRightColour, int cornerMixColour) {
//        element.drawColouredRect(x + bw, y + bw, width - bw - bw, height - bw - bw, fill);
//        element.drawColouredRect(x, y, width - bw, bw, topLeftColour);
//        element.drawColouredRect(x, y + bw, bw, height - bw - bw, topLeftColour);
//        element.drawColouredRect(x + bw, y + height - bw, width - bw, bw, bottomRightColour);
//        element.drawColouredRect(x + width - bw, y + bw, bw, height - bw - bw, bottomRightColour);
//        element.drawColouredRect(x + width - bw, y, bw, bw, cornerMixColour);
//        element.drawColouredRect(x, y + height - bw, bw, bw, cornerMixColour);
//    }
}
