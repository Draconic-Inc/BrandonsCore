package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is a simple label element that allows you to render a label (aka a string) in your GUI.
 */
public class GuiTextCompLabel extends GuiElement<GuiTextCompLabel> {

    protected boolean trim = true;
//    protected boolean midTrim = false;
    protected boolean wrap = false;
    protected boolean dropShadow = true;
    protected GuiAlign alignment = GuiAlign.CENTER;
    private Component textComponent = null;
    private Supplier<Component> textSupplier;
    private Supplier<Boolean> shadowStateSupplier;

    public GuiTextCompLabel() {}

    public GuiTextCompLabel(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTextCompLabel(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    public GuiTextCompLabel(Component textComponent) {
        this.textComponent = textComponent;
    }

    public GuiTextCompLabel(Supplier<Component> displayStringSupplier) {
        this.setTextSupplier(displayStringSupplier);
    }

    public GuiTextCompLabel(int xPos, int yPos, Component textComponent) {
        this(xPos, yPos);
        this.textComponent = textComponent;
    }

    public GuiTextCompLabel(int xPos, int yPos, int xSize, int ySize, Component textComponent) {
        this(xPos, yPos, xSize, ySize);
        this.textComponent = textComponent;
    }

    //region Display String

    public GuiTextCompLabel setTextComponent(Component textComponent) {
        this.textComponent = textComponent;
        return this;
    }

    public GuiTextCompLabel setTextSupplier(Supplier<Component> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public Component getText() {
        if (textSupplier != null) {
            return textSupplier.get();
        }
        return textComponent;
    }

    public GuiTextCompLabel setAlignment(GuiAlign alignment) {
        this.alignment = alignment;
        return this;
    }

    public GuiAlign getAlignment() {
        return alignment;
    }

    /**
     * If set to true the display string will be trimmed if it is too long to fit within the bounds on the element.
     * Default enabled.
     */
    public GuiTextCompLabel setTrim(boolean trim) {
        this.trim = trim;
        if (trim) wrap = false;
        return this;
    }

//    public GuiTextCompLabel setMidTrim(boolean midTrim) {
//        this.midTrim = midTrim;
//        return this;
//    }

    /**
     * Set to true the label text will be wrapped (rendered as multiple lines of text) if it is too long to fit within
     * the size of the element.
     */
    public GuiTextCompLabel setWrap(boolean wrap) {
        this.wrap = wrap;
        if (wrap) trim = false;
        return this;
    }

    public GuiTextCompLabel setShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        shadowStateSupplier = null;
        return this;
    }

    public GuiTextCompLabel setShadowStateSupplier(Supplier<Boolean> shadowStateSupplier) {
        this.shadowStateSupplier = shadowStateSupplier;
        return this;
    }

    public boolean hasShadow() {
        return shadowStateSupplier != null ? shadowStateSupplier.get() : dropShadow;
    }

    //endregion

    //region Render

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//        boolean mouseOver = isMouseOver(mouseX, mouseY);

        super.renderElement(mc, mouseX, mouseY, partialTicks);
        Component text = getText();
        if (text != null) {
            int widthLimit = getInsetRect().width;

            int ySize = fontRenderer.lineHeight;
            if (wrap && !trim) {
                ySize = fontRenderer.wordWrapHeight(text.getString(), widthLimit);
            }

            boolean wrap = this.wrap && fontRenderer.width(text) > widthLimit;
            int yPos = (getInsetRect().y + (getInsetRect().height / 2)) - (ySize / 2);
            int xPos = getInsetRect().x;

            drawCustomString(fontRenderer, text, xPos, yPos, widthLimit, 0xFFFFFF, getAlignment(), wrap, trim, hasShadow());
        }
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0x8000FFFF);
    }

    //endregion

//    /**
//     * Automatically adjusts the width of this element to fit the text it is displaying.
//     */
//    public GuiTextCompLabel setWidthFromText() {
//        int textWidth = fontRenderer.width(getLabelText());
//        setXSize(textWidth + getInsets().left + getInsets().right);
//        return this;
//    }

//    /**
//     * Automatically adjusts the width of this element to fit the text it is displaying.
//     * and applies the specified ySize
//     */
//    public GuiTextCompLabel setWidthFromText(int ySize) {
//        setYSize(ySize);
//        return setWidthFromText();
//    }

//    /**
//     * Automatically adjusts the height of this element to match the height of the text taking into consideration
//     * the added height from wrapping if the text is too long to fit within the current xSize.
//     */
//    public GuiTextCompLabel setHeightForText() {
//        int textHeight = fontRenderer.wordWrapHeight(getLabelText(), getInsetRect().width);
//        setYSize(textHeight + getInsets().top + getInsets().bottom);
//        return this;
//    }

//    /**
//     * Applies the given xSize then automatically adjusts the height of this element to match the height of the text taking into consideration
//     * the added height from wrapping if the text is too long to fit within the given xSize.
//     */
//    public GuiTextCompLabel setHeightForText(int xSize) {
//        setXSize(xSize);
//        return setHeightForText();
//    }
}
