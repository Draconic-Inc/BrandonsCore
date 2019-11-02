package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider.HoverDisableColour;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IButtonListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import scala.Function0;
import scala.Unit;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 30/08/2016.
 * This is the base Modular GUI button class. This should be extended when creating customized modular gui buttons.
 */
@SuppressWarnings("unchecked")
public class GuiButton extends MGuiElementBase<GuiButton>/* implements IGuiEventDispatcher*/ {
    protected static ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    protected ResourceLocation textureOverride;
    protected Supplier<ResourceLocation> textureSupplier;
    //    @Deprecated
//    protected IGuiEventListener listener = null;
    protected IButtonListener buttonListener = null;
    protected boolean trim = true;
    protected boolean wrap = false;
    protected boolean disabled = false;
    protected boolean dropShadow = true;
    protected GuiAlign alignment = GuiAlign.CENTER;
    protected TextRotation rotation = TextRotation.NORMAL;

    private double backgroundBorderWidth = 1;
    private String displayString = "";

    private boolean toggleMode = false;
    private boolean toggleActiveState = false;
    /**
     * This is not the same as disabling the element. When a button is disabled it will still render but will not be clickable.
     * May also render greyed out or give some visual indication that its disabled.
     */
    private boolean vanillaButtonRender = false;
    private boolean drawBorderedRectBackground = false;
    private Supplier<Boolean> toggleStateSupplier;
    private Supplier<String> displayStringSupplier;
    private Supplier<Boolean> disabledStateSupplier;
    private HoverDisableColour<Integer> texColGetter;
    private HoverDisableColour<Integer> rectFillColour;
    private HoverDisableColour<Integer> rectBorderColour;

    /**
     * An optional button id that can be used to identify this button.
     */
    public int buttonId = -1;
    /**
     * An optional button name that can be used to identify this button.
     */
    public String buttonName = "";
    public boolean playClick = true;
    public int textXOffset = 0;
    public int textYOffset = 0;

    //Super Constructors
    public GuiButton() {
        setSize(100, 20);
        setInsets(5, 5, 5, 5);
    }

    public GuiButton(int xPos, int yPos) {
        super(xPos, yPos);
        setSize(100, 20);
        setInsets(5, 5, 5, 5);
    }

    public GuiButton(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
        setInsets(5, 5, 5, 5);
    }

    //Button Text
    public GuiButton(String buttonText) {
        setSize(100, 20);
        setInsets(5, 5, 5, 5);
        this.displayString = buttonText;
    }

    public GuiButton(int xPos, int yPos, String buttonText) {
        this(xPos, yPos);
        this.displayString = buttonText;
    }

    public GuiButton(int xPos, int yPos, int xSize, int ySize, String buttonText) {
        this(xPos, yPos, xSize, ySize);
        this.displayString = buttonText;
    }

    //Button ID & Button Text
    public GuiButton(int buttonId, String buttonText) {
        this(buttonText);
        this.buttonId = buttonId;
    }

    public GuiButton(int buttonId, int xPos, int yPos, String buttonText) {
        this(xPos, yPos, buttonText);
        this.buttonId = buttonId;
    }

    public GuiButton(int buttonId, int xPos, int yPos, int xSize, int ySize, String buttonText) {
        this(xPos, yPos, xSize, ySize, buttonText);
        this.buttonId = buttonId;
    }

    //Button ID
    public GuiButton(int buttonId) {
        setInsets(5, 5, 5, 5);
        this.buttonId = buttonId;
    }

    public GuiButton(int buttonId, int xPos, int yPos) {
        this(xPos, yPos);
        this.buttonId = buttonId;
    }

    public GuiButton(int buttonId, int xPos, int yPos, int xSize, int ySize) {
        this(xPos, yPos, xSize, ySize);
        this.buttonId = buttonId;
    }

    @Deprecated //Fix Listeners
    public GuiButton setListener(IButtonListener buttonListener) {
        this.buttonListener = buttonListener;
        return this;
    }

    @Deprecated //Fix Listeners
    public GuiButton setListener(Runnable action) {
        return setListener((b, m) -> action.run());
    }

    @Deprecated //Fix Listeners
    public GuiButton setListener(Function0<Unit> action) {
        return setListener((b, m) -> {
            //Fixes casting issue.
            @SuppressWarnings("unused") Object unused = action.apply();
        });
    }

    public boolean isDisabled() {
        return disabledStateSupplier != null ? disabledStateSupplier.get() : disabled;
    }

    public GuiButton setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public GuiButton setDisabledStateSupplier(Supplier<Boolean> disabledStateSupplier) {
        this.disabledStateSupplier = disabledStateSupplier;
        return this;
    }

    //region Button Identification

    public GuiButton setButtonId(int buttonId) {
        this.buttonId = buttonId;
        return this;
    }

    public GuiButton setButtonName(String buttonName) {
        this.buttonName = buttonName;
        return this;
    }

    //endregion

    //region Display String

    public GuiButton setText(String displayString) {
        this.displayString = displayString;
        return this;
    }

    /**
     * Allows you to add a string supplier that will override the default display string.
     */
    public GuiButton setDisplaySupplier(Supplier<String> displayStringSupplier) {
        this.displayStringSupplier = displayStringSupplier;
        return this;
    }

    public String getDisplayString() {
        return displayStringSupplier != null ? displayStringSupplier.get() : displayString;
    }

    public GuiButton setAlignment(GuiAlign alignment) {
        this.alignment = alignment;
        return this;
    }

    public GuiAlign getAlignment() {
        return alignment;
    }

    /**
     * If set to true the display string will be trimmed if it is too long to fit in the button.
     * Default enabled.
     */
    public GuiButton setTrim(boolean trim) {
        this.trim = trim;
        if (trim) wrap = false;
        return this;
    }

    /**
     * Set to true the button display text will be wrapped (rendered as multiple lines of text) if it is too long to
     * fit within the size of the button (use setInset if you do not want the text to be able to extend all the way to the edge of the button).
     */
    public GuiButton setWrap(boolean wrap) {
        this.wrap = wrap;
        if (wrap) trim = false;
        return this;
    }

    public GuiButton setRotation(TextRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public TextRotation getRotation() {
        return rotation;
    }

    public GuiButton setShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public int getTextColour(boolean hovered, boolean disabled) {
        if (texColGetter != null) {
            return texColGetter.getColour(hovered, disabled);
        }

        if (disabled) {
            return 0xa0a0a0;
        }
        return hovered ? 0xffffa0 : 0xe0e0e0;
    }

    /**
     * Allows you to add a function that will override the default text colour for this button.
     */
    public GuiButton setTextColGetter(HoverDisableColour<Integer> texColGetter) {
        this.texColGetter = texColGetter;
        return this;
    }

    public GuiButton setTextColour(int colour, int colourHover, int colourDisabled) {
        setTextColGetter((hovering, disabled1) -> disabled1 ? colourDisabled : hovering ? colourHover : colour);
        return this;
    }

    public GuiButton setTextColour(int colour, int colourHover) {
        if (texColGetter != null) {
            int dis = texColGetter.getColour(false, true);
            setTextColGetter((hovering, disabled1) -> disabled1 ? dis : hovering ? colourHover : colour);
        }

        setTextColGetter((hovering, disabled1) -> hovering ? colourHover : colour);
        return this;
    }

    public GuiButton setTextColour(int colour) {
        if (texColGetter != null) {
            int dis = texColGetter.getColour(false, true);
            int hover = texColGetter.getColour(true, false);
            setTextColGetter((hovering, disabled1) -> disabled1 ? dis : hovering ? hover : colour);
        }

        setTextColGetter((hovering, disabled1) -> colour);
        return this;
    }

    //endregion

    //region Background

    public GuiButton setVanillaButtonRender(boolean vanillaButtonRender) {
        this.vanillaButtonRender = vanillaButtonRender;
        return this;
    }

    public GuiButton enableVanillaRender() {
        return setVanillaButtonRender(true);
    }

    public GuiButton disableVanillaRender() {
        return setVanillaButtonRender(false);
    }

    /**
     * Enable/disable the building solid bordered rectangle background.
     * Disabled by default but automatically enabled when you call any of the setRectColour methods.
     */
    public GuiButton setDrawBorderedRectBackground(boolean drawBorderedRectBackground) {
        this.drawBorderedRectBackground = drawBorderedRectBackground;
        return this;
    }

    public GuiButton setRectFillColourGetter(HoverDisableColour<Integer> rectBackColour) {
        this.rectFillColour = rectBackColour;
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setRectBorderColourGetter(HoverDisableColour<Integer> rectBorderColour) {
        this.rectBorderColour = rectBorderColour;
        setDrawBorderedRectBackground(true);
        return this;
    }

    /**
     * Set colours for the built in bordered rect.
     * When using the setFill or setBorder methods if you set one but not the other the other will default to the one you set.
     * So sat you want a single solid colour background. You would onlu ned to call one of the setFillColours or setBorderColours methods and
     * that getter would be used for both the fill and the border colours.
     */
    public GuiButton setRectColours(int fill, int fillHover, int fillDisabled, int border, int borderHover, int borderDisabled) {
        setRectFillColourGetter((hovering, disabled1) -> disabled1 ? fillDisabled : hovering ? fillHover : fill);
        setRectBorderColourGetter((hovering, disabled1) -> disabled1 ? borderDisabled : hovering ? borderHover : border);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setRectColours(int fill, int fillHover, int border, int borderHover) {
        setRectFillColourGetter((hovering, disabled1) -> hovering ? fillHover : fill);
        setRectBorderColourGetter((hovering, disabled1) -> hovering ? borderHover : border);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setFillColours(int fill, int fillHover, int fillDisabled) {
        setRectFillColourGetter((hovering, disabled1) -> disabled1 ? fillDisabled : hovering ? fillHover : fill);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setFillColours(int fill, int fillHover) {
        if (rectFillColour != null) {
            int dis = rectFillColour.getColour(false, true);
            setRectFillColourGetter((hovering, disabled1) -> disabled1 ? dis : hovering ? fillHover : fill);
        }

        setRectFillColourGetter((hovering, disabled1) -> hovering ? fillHover : fill);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setFillColour(int fill) {
        if (rectFillColour != null) {
            int dis = rectFillColour.getColour(false, true);
            int hover = rectFillColour.getColour(true, false);
            setRectFillColourGetter((hovering, disabled1) -> disabled1 ? dis : hovering ? hover : fill);
        }

        setRectFillColourGetter((hovering, disabled1) -> fill);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setBorderColours(int border, int borderHover, int borderDisabled) {
        setRectBorderColourGetter((hovering, disabled1) -> disabled1 ? borderDisabled : hovering ? borderHover : border);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setBorderColours(int border, int borderHover) {
        if (rectBorderColour != null) {
            int dis = rectBorderColour.getColour(false, true);
            setRectBorderColourGetter((hovering, disabled1) -> disabled1 ? dis : hovering ? borderHover : border);
        }

        setRectBorderColourGetter((hovering, disabled1) -> hovering ? borderHover : border);
        setDrawBorderedRectBackground(true);
        return this;
    }

    public GuiButton setBorderColour(int border) {
        if (rectBorderColour != null) {
            int dis = rectBorderColour.getColour(false, true);
            int hover = rectBorderColour.getColour(true, false);
            setRectBorderColourGetter((hovering, disabled1) -> disabled1 ? dis : hovering ? hover : border);
        }

        setRectBorderColourGetter((hovering, disabled1) -> border);
        setDrawBorderedRectBackground(true);
        return this;
    }

    /**
     * Allows you to adjust the width of the border around the background bordered rectangle.
     * Default: 1
     */
    public GuiButton setBorderWidth(double backgroundBorderWidth) {
        this.backgroundBorderWidth = backgroundBorderWidth;
        return this;
    }

    public int getFillColour(boolean hover, boolean disabled) {
        if (rectFillColour != null) {
            return rectFillColour.getColour(hover, disabled);
        }
        else if (rectBorderColour != null) {
            return rectBorderColour.getColour(hover, disabled);
        }
        return 0;
    }

    public int getBorderColour(boolean hover, boolean disabled) {
        if (rectBorderColour != null) {
            return rectBorderColour.getColour(hover, disabled);
        }
        else if (rectFillColour != null) {
            return rectFillColour.getColour(hover, disabled);
        }
        return 0;
    }

    //endregion

    //region Button Action

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (isMouseOver(mouseX, mouseY) && !isDisabled()) {
            onPressed(mouseX, mouseY, mouseButton);
            return true;
        }
        return false;
    }

    /**
     * Called when this button is pressed. Can be overriden to implement custom functionality.
     */
    public void onPressed(int mouseX, int mouseY, int mouseButton) {
        if (toggleMode) {
            toggleActiveState = !getToggleState();
        }

        if (playClick) {
            playClickSound();
        }

//        if (listener != null) {
//            listener.onMGuiEvent(new GuiEvent.ButtonEvent(this), this);
//        }

        if (buttonListener != null) {
            buttonListener.onClick(this, mouseButton);
        }
    }

    public void playClickSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, !toggleMode || toggleActiveState ? 1.0F : 0.9F));
    }

    public static void playGenericClick(Minecraft mc) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Use to enable/disable the button click sound. Enabled by default.
     */
    public GuiButton setClickEnabled(boolean playClick) {
        this.playClick = playClick;
        return this;
    }

    /**
     * When true this button will be a toggle button, Meaning click once to enable, Click again to disable.
     */
    public GuiButton setToggleMode(boolean toggleMode) {
        this.toggleMode = toggleMode;
        return this;
    }

    public GuiButton setToggleState(boolean toggleState) {
        this.toggleActiveState = toggleState;
        return this;
    }

    public boolean getToggleMode() {
        return toggleMode;
    }

    /**
     * Allows you to link the toggle state to en external source. e.g. if you wanted to link the toggle state to a field in a tile you could use this.
     */
    public GuiButton setToggleStateSupplier(Supplier<Boolean> toggleStateSupplier) {
        this.toggleStateSupplier = toggleStateSupplier;
        return this;
    }

    /**
     * @return the toggle state if this button is in Toggle mode, Will always be false if not in toggle mode.
     */
    public boolean getToggleState() {
        return toggleStateSupplier != null ? toggleStateSupplier.get() : toggleActiveState;
    }

    //endregion

    //region Render

    protected int getRenderState(boolean hovered) {
        int i = 1;

        if (isDisabled()) {
            i = 0;
        }
        else if (hovered) {
            i = 2;
        }

        return i;
    }

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        boolean mouseOver = isMouseOver(mouseX, mouseY);

        if (drawBorderedRectBackground) {
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), backgroundBorderWidth, getFillColour(mouseOver || (toggleMode && getToggleState()), isDisabled()), getBorderColour(mouseOver || (toggleMode && getToggleState()), disabled));
        }

        if (vanillaButtonRender) {
            renderVanillaButton(mc, mouseX, mouseY);
        }

        super.renderElement(mc, mouseX, mouseY, partialTicks);

        String displayString = getDisplayString();
        if (!displayString.isEmpty()) {
            int colour = getTextColour(mouseOver, isDisabled());
            int widthLimit = rotation == TextRotation.NORMAL || rotation == TextRotation.ROT_180 ? getInsetRect().width : getInsetRect().height;

            int ySize = fontRenderer.FONT_HEIGHT;
            if (wrap && !trim) {
                ySize = fontRenderer.getWordWrappedHeight(displayString, widthLimit);
            }

            boolean wrap = this.wrap && fontRenderer.getStringWidth(displayString) > widthLimit;

            int xPos = textXOffset + getInsetRect().x;
            int yPos = textYOffset + ((getInsetRect().y + (getInsetRect().height / 2)) - (ySize / 2));
            switch (rotation) {
                case NORMAL:
                    drawCustomString(fontRenderer, displayString, xPos, yPos, widthLimit, colour, getAlignment(), getRotation(), wrap, trim, dropShadow);
                    break;
                case ROT_CC:
                    xPos = textXOffset + ((getInsetRect().x + (getInsetRect().width / 2)) - (ySize / 2));
                    yPos = textYOffset + getInsetRect().y;
                    drawCustomString(fontRenderer, displayString, xPos, yPos, widthLimit, colour, getAlignment(), getRotation(), wrap, trim, dropShadow);
                    break;
                case ROT_C:
                    xPos = textXOffset + ((getInsetRect().x + (getInsetRect().width / 2)) - (ySize / 2));
                    yPos = textYOffset + getInsetRect().y;
                    drawCustomString(fontRenderer, displayString, xPos + ySize, yPos, widthLimit, colour, getAlignment(), getRotation(), wrap, trim, dropShadow);
                    break;
                case ROT_180:
                    drawCustomString(fontRenderer, displayString, xPos, yPos, widthLimit, colour, getAlignment(), getRotation(), wrap, trim, dropShadow);
                    break;
            }
            GlStateManager.color(1, 1, 1, 1);
        }
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0xFF00FF00);
    }

    protected void renderVanillaButton(Minecraft mc, int mouseX, int mouseY) {
        bindTexture(textureSupplier != null ? textureSupplier.get() : textureOverride != null ? textureOverride : BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        boolean hovered = isMouseOver(mouseX, mouseY) || (toggleMode && getToggleState());
        int texVIndex = getRenderState(hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int texHeight = Math.min(20, ySize());
        int texPos = 46 + texVIndex * 20;

        drawTexturedModalRect(xPos(), yPos(), 0, texPos, xSize() % 2 + xSize() / 2, texHeight);
        drawTexturedModalRect(xSize() % 2 + xPos() + xSize() / 2, yPos(), 200 - xSize() / 2, texPos, xSize() / 2, texHeight);

        if (ySize() < 20) {
            drawTexturedModalRect(xPos(), yPos() + 3, 0, texPos + 20 - ySize() + 3, xSize() % 2 + xSize() / 2, ySize() - 3);
            drawTexturedModalRect(xSize() % 2 + xPos() + xSize() / 2, yPos() + 3, 200 - xSize() / 2, texPos + 20 - ySize() + 3, xSize() / 2, ySize() - 3);
        }
        else if (ySize() > 20) {
            for (int y = yPos() + 17; y + 15 < yPos() + ySize(); y += 15) {
                drawTexturedModalRect(xPos(), y, 0, texPos + 2, xSize() % 2 + xSize() / 2, 15);
                drawTexturedModalRect(xSize() % 2 + xPos() + xSize() / 2, y, 200 - xSize() / 2, texPos + 2, xSize() / 2, 15);
            }

            drawTexturedModalRect(xPos(), yPos() + ySize() - 15, 0, texPos + 5, xSize() % 2 + xSize() / 2, 15);
            drawTexturedModalRect(xSize() % 2 + xPos() + xSize() / 2, yPos() + ySize() - 15, 200 - xSize() / 2, texPos + 5, xSize() / 2, 15);
        }
    }

    //Must match the vanilla button texture
    public GuiButton setTextureOverride(ResourceLocation textureOverride) {
        this.textureOverride = textureOverride;
        return this;
    }

    //Must match the vanilla button texture
    public GuiButton setTextureSupplier(Supplier<ResourceLocation> textureSupplier) {
        this.textureSupplier = textureSupplier;
        return this;
    }

    //endregion
}
