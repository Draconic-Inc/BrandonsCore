package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider.HoverColour;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is a simple label element that allows you to render a label (aka a string) in your GUI.
 */
public class GuiLabel extends GuiElement<GuiLabel> {

    protected boolean trim = true;
    protected boolean midTrim = false;
    protected boolean wrap = false;
    protected boolean dropShadow = true;
    protected GuiAlign alignment = GuiAlign.CENTER;
    protected TextRotation rotation = TextRotation.NORMAL;

    private int textColour = 0xFFFFFFFF;
    private String labelText = "";
    private Supplier<String> displayStringSupplier;
    private Supplier<Component> displaySupplier;
    private Supplier<Boolean> shadowStateSupplier;
    private HoverColour<Integer> texColGetter;

    public GuiLabel() {}

    public GuiLabel(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiLabel(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    public GuiLabel(String labelText) {
        this.labelText = labelText;
    }

    public GuiLabel(Component label) {
        displaySupplier = () -> label;
    }

    public GuiLabel(Supplier<String> displayStringSupplier) {
        this.setDisplaySupplier(displayStringSupplier);
    }

    public GuiLabel(int xPos, int yPos, String labelText) {
        this(xPos, yPos);
        this.labelText = labelText;
    }

    public GuiLabel(int xPos, int yPos, int xSize, int ySize, String labelText) {
        this(xPos, yPos, xSize, ySize);
        this.labelText = labelText;
    }

    //region Display String

    public GuiLabel setLabelText(String displayString) {
        this.labelText = displayString;
        return this;
    }

    /**
     * Allows you to add a string supplier that will override the default display string.
     */
    public GuiLabel setDisplaySupplier(Supplier<String> displayStringSupplier) {
        this.displayStringSupplier = displayStringSupplier;
        return this;
    }

    public GuiLabel setComponentSupplier(Supplier<Component> displaySupplier) {
        this.displaySupplier = displaySupplier;
        return this;
    }

    public String getLabelText() {
        return displayStringSupplier != null ? displayStringSupplier.get() : labelText;
    }

    public GuiLabel setAlignment(GuiAlign alignment) {
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
    public GuiLabel setTrim(boolean trim) {
        this.trim = trim;
        if (trim) wrap = false;
        return this;
    }

    public GuiLabel setMidTrim(boolean midTrim) {
        this.midTrim = midTrim;
        return this;
    }

    /**
     * Set to true the label text will be wrapped (rendered as multiple lines of text) if it is too long to fit within
     * the size of the element.
     */
    public GuiLabel setWrap(boolean wrap) {
        this.wrap = wrap;
        if (wrap) trim = false;
        return this;
    }

    /**
     * Allows you to rotate this label. Note this only rotates the text so if for example your rotating this label
     * 90 degrees then you will need to set its x and y size accordingly.
     */
    public GuiLabel setRotation(TextRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public TextRotation getRotation() {
        return rotation;
    }

    public GuiLabel setShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        shadowStateSupplier = null;
        return this;
    }

    public GuiLabel setShadowStateSupplier(Supplier<Boolean> shadowStateSupplier) {
        this.shadowStateSupplier = shadowStateSupplier;
        return this;
    }

    public boolean hasShadow() {
        return shadowStateSupplier != null ? shadowStateSupplier.get() : dropShadow;
    }

    public int getTextColour(boolean hovered) {
        if (texColGetter != null) {
            return texColGetter.getColour(hovered);
        }

        return textColour;
    }

    /**
     * Allows you to add a color getter callback that will override the default text colour for this label.
     * This also allows you to return different colour values based on whether or not the cursor is over the element.
     */
    public GuiLabel setHoverableTextCol(HoverColour<Integer> texColGetter) {
        this.texColGetter = texColGetter;
        return this;
    }

    public GuiLabel setTextColGetter(GuiColourProvider<Integer> texColGetter) {
        this.texColGetter = hovering -> texColGetter.getColour();
        return this;
    }

    public GuiLabel setTextColour(ChatFormatting colour, ChatFormatting colourHover) {
        return setTextColour(colour.getColor(), colourHover.getColor());
    }

    public GuiLabel setTextColour(int colour, int colourHover) {
        setHoverableTextCol(hovering -> hovering ? colourHover : colour);
        return this;
    }

    public GuiLabel setTextColour(int colour) {
        if (texColGetter != null) {
            int hover = texColGetter.getColour(true);
            setHoverableTextCol(hovering -> hovering ? hover : colour);
        }

        setHoverableTextCol(hovering -> colour);
        return this;
    }

    public GuiLabel setTextColour(ChatFormatting colour) {
        setTextColour(colour.getColor());
        return this;
    }

    //endregion

    //region Render

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        boolean mouseOver = isMouseOver(mouseX, mouseY);

        super.renderElement(mc, mouseX, mouseY, partialTicks);

        Component component = displaySupplier == null ? new TextComponent(getLabelText()) : displaySupplier.get();
        if (!component.getString().isEmpty()) {

            int colour = getTextColour(mouseOver);
            int widthLimit = rotation == TextRotation.NORMAL || rotation == TextRotation.ROT_180 ? getInsetRect().width : getInsetRect().height;

            int ySize = fontRenderer.lineHeight;
            if (wrap && !trim) {
                ySize = fontRenderer.getSplitter().splitLines(component, widthLimit, component.getStyle()).size() * fontRenderer.lineHeight;
            }

//            boolean wrap = this.wrap && fontRenderer.width(displayString) > widthLimit;

            int yPos = (getInsetRect().y + (getInsetRect().height / 2)) - (ySize / 2);
            int xPos = getInsetRect().x;

            switch (rotation) {
                case NORMAL:
                    drawCustomString(fontRenderer, component, xPos, yPos, widthLimit, colour, getAlignment(), wrap, trim, midTrim, hasShadow());
                    break;
                case ROT_CC:
                    xPos = (getInsetRect().x + (getInsetRect().width / 2)) - (ySize / 2);
                    yPos = getInsetRect().y;
                    drawCustomString(fontRenderer, component, xPos, yPos, widthLimit, colour, getAlignment(), wrap, trim, midTrim, hasShadow());
                    break;
                case ROT_C:
                    xPos = (getInsetRect().x + (getInsetRect().width / 2)) - (ySize / 2);
                    yPos = getInsetRect().y;
                    drawCustomString(fontRenderer, component, xPos + ySize, yPos, widthLimit, colour, getAlignment(), wrap, trim, midTrim, hasShadow());
                    break;
                case ROT_180:
                    drawCustomString(fontRenderer, component, xPos, yPos, widthLimit, colour, getAlignment(), wrap, trim, midTrim, hasShadow());
                    break;
            }
        }
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0x8000FFFF);
    }

    //endregion

    /**
     * Automatically adjusts the width of this element to fit the text it is displaying.
     */
    public GuiLabel setWidthFromText() {
        int textWidth = fontRenderer.width(getLabelText());
        setXSize(textWidth + getInsets().left + getInsets().right);
        return this;
    }

    /**
     * Automatically adjusts the width of this element to fit the text it is displaying.
     * and applies the specified ySize
     */
    public GuiLabel setWidthFromText(int ySize) {
        setYSize(ySize);
        return setWidthFromText();
    }

    /**
     * Automatically adjusts the height of this element to match the height of the text taking into consideration
     * the added height from wrapping if the text is too long to fit within the current xSize.
     */
    public GuiLabel setHeightForText() {
        int textHeight = fontRenderer.wordWrapHeight(getLabelText(), getInsetRect().width);
        setYSize(textHeight + getInsets().top + getInsets().bottom);
        return this;
    }

    /**
     * Applies the given xSize then automatically adjusts the height of this element to match the height of the text taking into consideration
     * the added height from wrapping if the text is too long to fit within the given xSize.
     */
    public GuiLabel setHeightForText(int xSize) {
        setXSize(xSize);
        return setHeightForText();
    }
}
