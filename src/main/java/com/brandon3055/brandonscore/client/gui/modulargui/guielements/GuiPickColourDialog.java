package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;

/**
 * Created by brandon3055 on 10/09/2016.
 */
@Deprecated
public class GuiPickColourDialog extends GuiPopUpDialogBase<GuiPickColourDialog> implements IGuiEventListener {

    public Colour colour = new ColourARGB(0xFFFFFFFF);
    public GuiButton cancelButton;
    public GuiButton selectButton;
    public GuiTextField hexField;
    public GuiSlideControl redSlider;
    public GuiSlideControl greenSlider;
    public GuiSlideControl blueSlider;
    public GuiSlideControl alphaSlider;
    public IGuiEventListener listener;
    private boolean cancelEnabled = false;
    private boolean includeAlpha = true;
    private Consumer<Colour> colourChangeListener = null;
    private Consumer<Colour> colourSelectListener = null;
    private GuiElement<?> backgroundElement = null;

    public GuiPickColourDialog(GuiElement parent) {
        super(parent);
        setSize(80, 80);
        setDragBar(80);
        if (parent instanceof IGuiEventListener) {
            listener = (IGuiEventListener) parent;
        }
    }

    public GuiPickColourDialog(int xPos, int yPos, GuiElement parent) {
        super(xPos, yPos, parent);
        setSize(80, 80);
        setDragBar(80);
        if (parent instanceof IGuiEventListener) {
            listener = (IGuiEventListener) parent;
        }
    }

    public GuiPickColourDialog setBackgroundElement(GuiElement<?> backgroundElement) {
        this.backgroundElement = backgroundElement;
        backgroundElement.setPos(this).bindSize(this, false);
        return this;
    }

    @Override
    public void addChildElements() {
        if (!includeAlpha) {
            colour.a = (byte) 255;
        }

        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();
        int ySize = ySize();

        addChild(hexField = new GuiTextField(xPos + 4, yPos + 4, xSize - 8, 12).setTextAndNotify(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb())));
        hexField.setChangeListener(this::onTextFieldChanged);
        hexField.setBorderColour(hovering -> 0xFF505050);
        hexField.setFillColour(hovering -> 0xFF000000);

        hexField.setMaxStringLength(includeAlpha ? 8 : 6);
        hexField.setValidator(input -> {
            try {
                Utils.parseHex(input);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        });

        addChild(redSlider = new GuiSlideControl(xPos + 4, hexField.yPos() + 14, xSize - 8, 9));
        redSlider.setReverseScrollDir(true);
        redSlider.setSliderElement(new GuiBorderedRect().setShadeColours(0xFFFF0000, 0xFF000000).setBorderWidth(0.5));
        redSlider.updatePos((colour.r & 0xFF) / 255D, false);
        redSlider.setBarStyleBackground(0xFF505050).setSliderSize(3);

        addChild(greenSlider = new GuiSlideControl(xPos + 4, redSlider.yPos() + 10, xSize - 8, 9));
        greenSlider.setReverseScrollDir(true);
        greenSlider.setSliderElement(new GuiBorderedRect().setShadeColours(0xFF00FF00, 0xFF000000).setBorderWidth(0.5));
        greenSlider.updatePos((colour.g & 0xFF) / 255D, false);
        greenSlider.setBarStyleBackground(0xFF505050).setSliderSize(3);

        addChild(blueSlider = new GuiSlideControl(xPos + 4, greenSlider.yPos() + 10, xSize - 8, 9));
        blueSlider.setReverseScrollDir(true);
        blueSlider.setSliderElement(new GuiBorderedRect().setShadeColours(0xFF0000FF, 0xFF000000).setBorderWidth(0.5));
        blueSlider.updatePos((colour.b & 0xFF) / 255D, false);
        blueSlider.setBarStyleBackground(0xFF505050).setSliderSize(3);

        if (includeAlpha) {
            addChild(alphaSlider = new GuiSlideControl(xPos + 4,blueSlider.yPos() + 10, xSize - 8, 9));
            alphaSlider.setReverseScrollDir(true);
            alphaSlider.setSliderElement(new GuiBorderedRect().setShadeColours(0xFFFFFFFF, 0xFF000000).setBorderWidth(0.5));
            alphaSlider.updatePos((colour.a & 0xFF) / 255D, false);
            alphaSlider.setBarStyleBackground(0xFF505050).setSliderSize(3);
        }

        addChild(selectButton = new GuiButton(xPos + 4, yPos + ySize - 14, cancelEnabled ? (xSize / 2) - 5 : xSize - 8, 10, I18n.get("mod_gui.brandonscore.button.ok")));
        selectButton.onPressed(() -> {
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.ColourEvent(this, getColourARGB(), false, false), this);
            }
            if (colourSelectListener != null) {
                colourSelectListener.accept(colour.copy());
            }
            close();
        });
        addChild(cancelButton = new GuiButton(selectButton.xPos() + selectButton.xSize() + 2, yPos + ySize - 14, (xSize / 2) - 5, 10, I18n.get("gui.back")));
        cancelButton.onPressed(() -> {
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.ColourEvent(this, getColourARGB(), true, false), this);
            }
            close();
        });
        selectButton.setFillColour(0xFF000000).setBorderColours(0xFF555555, 0xFF777777);
        cancelButton.setFillColour(0xFF000000).setBorderColours(0xFF555555, 0xFF777777);
        cancelButton.setEnabled(cancelEnabled);

        super.addChildElements();
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        zOffset -= 1;
        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//        drawBorderedRect(getter, xPos(), yPos(), xSize(), ySize(), 1, 0xFFFFFFFF, 0xFF000000);
        GuiHelper.drawHoverRect(getter, new PoseStack(), xPos(), yPos(), xSize(), ySize());

        if (includeAlpha) {
            int i = 0;
            for (double x = 0; x < xSize() - 8; x += 3) {
                i++;
                drawColouredRect(getter, xPos() + 4 + x, yPos() + ySize() - 22 + ((i % 2) * 3), 3, 3, 0xFF000000);
            }
        }

        drawBorderedRect(getter, xPos() + 4, yPos() + ySize() - 22, xSize() - 8, 6, 0.5, includeAlpha ? colour.argb() : mixColours(0xFF000000, colour.argb()), 0xFF000000);
        getter.endBatch();
        zOffset += 1;
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onMGuiEvent(GuiEvent event, GuiElement eventElement) {
        boolean colourChanged = false;
        if (eventElement == redSlider) {
            colour.r = (byte) (redSlider.getPosition() * 255D);
            hexField.setTextAndNotify(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
            colourChanged = true;
        }
        else if (eventElement == greenSlider) {
            colour.g = (byte) (greenSlider.getPosition() * 255D);
            hexField.setTextAndNotify(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
            colourChanged = true;
        }
        else if (eventElement == blueSlider) {
            colour.b = (byte) (blueSlider.getPosition() * 255D);
            hexField.setTextAndNotify(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
            colourChanged = true;
        }
        else if (eventElement == alphaSlider) {
            colour.a = (byte) (alphaSlider.getPosition() * 255D);
            hexField.setTextAndNotify(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
            colourChanged = true;
        }
        else if (eventElement == hexField) {

        }

        if (colourChanged) {
            onColourChangeInternal();
        }
    }

    private void onTextFieldChanged() {
        try {
            int pos = hexField.getCursorPosition();
            colour.set(Utils.parseHex(hexField.getText()));
            redSlider.updateRawPos((colour.r & 0xFF) / 255D);
            greenSlider.updateRawPos((colour.g & 0xFF) / 255D);
            blueSlider.updateRawPos((colour.b & 0xFF) / 255D);
            if (!includeAlpha) {
                colour.a = (byte) 0xFF;
            }
            else {
                alphaSlider.updateRawPos((colour.a & 0xFF) / 255D);
            }
            hexField.setCursorPosition(pos);
            onColourChangeInternal();
        }
        catch (Exception ignored) {}
    }

    private void onColourChangeInternal() {
        if (colourChangeListener != null) {
            colourChangeListener.accept(colour.copy());
        }
    }

    public GuiPickColourDialog setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * This is called whenever the colour is adjusted NOT when the ok button is pressed.
     * Adding this disabled the cancel button.
     */
    public GuiPickColourDialog setColourChangeListener(Consumer<Integer> colourChangeListener) {
        this.colourChangeListener = c -> colourChangeListener.accept(c.argb());
        setCancelEnabled(false);
        return this;
    }

    /**
     * This is called whenever the colour is adjusted NOT when the ok button is pressed.
     * Adding this disabled the cancel button.
     */
    public GuiPickColourDialog setCCColourChangeListener(Consumer<Colour> colourChangeListener) {
        this.colourChangeListener = colourChangeListener;
        setCancelEnabled(false);
        return this;
    }

    /**
     * This is called when the ok button is pressed and the picker closes.
     */
    public GuiPickColourDialog setColourSelectListener(Consumer<Integer> colourSelectListener) {
        this.colourSelectListener = c -> colourSelectListener.accept(c.argb());
        return this;
    }

    /**
     * This is called when the ok button is pressed and the picker closes.
     */
    public GuiPickColourDialog setCCColourSelectListener(Consumer<Colour> colourSelectListener) {
        this.colourSelectListener = colourSelectListener;
        return this;
    }

    public GuiPickColourDialog setCancelEnabled(boolean cancelEnabled) {
        this.cancelEnabled = cancelEnabled;
        if (cancelButton != null) {
            cancelButton.setEnabled(cancelEnabled);
        }
        return this;
    }

    public GuiPickColourDialog setColour(int colour) {
        this.colour = new ColourARGB(colour);
        return this;
    }

    public GuiPickColourDialog setColour(Colour colour) {
        this.colour = colour;
        if (colour == null) {
            this.colour = new ColourARGB(0xFFFFFFFF);
        }
        return this;
    }

    public Colour getColour() {
        return colour;
    }

    public int getColourARGB() {
        return colour.argb();
    }

    /**
     * Sets weather or not this picker should allow you to adjust alpha.
     * This must be set before calling show().
     */
    public GuiPickColourDialog setIncludeAlpha(boolean includeAlpha) {
        this.includeAlpha = includeAlpha;
        setYSize(includeAlpha ? 80 : 70);
        setDragBar(ySize());
        return this;
    }
}

