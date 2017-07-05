package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class MGuiColourPicker extends MGuiPopUpDialog implements IGuiEventListener {

    public GuiButton cancelButton;
    public GuiButton selectButton;
    public MGuiTextField hexField;
    public GuiSlideControl redSlider;
    public GuiSlideControl greenSlider;
    public GuiSlideControl blueSlider;
    public GuiSlideControl alphaSlider;
    private boolean includeAlpha = true;
    public IGuiEventListener listener;
    public Colour colour = new ColourARGB(0xFFFFFFFF);

    public MGuiColourPicker(int xPos, int yPos, MGuiElementBase parent) {
        super(xPos, yPos, parent);
        setSize(80, 80);
        dragZoneSize = 3;
        if (parent instanceof IGuiEventListener) {
            listener = (IGuiEventListener) parent;
        }
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

        addChild(hexField = new MGuiTextField(xPos + 4, yPos + 4, xSize - 8, 12).setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb())).setListener(this));
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

//        addChild(redSlider = new GuiSlideControl(xPos + 4, hexField.yPos() + 14, xSize - 8, 8).setListener(this).setBarSize(3));
//        redSlider.horizontal = true;
//        redSlider.setPos((colour.r & 0xFF) / 255D);
//        redSlider.sliderColour = 0xFFFF0000;
//        addChild(greenSlider = new GuiSlideControl(xPos + 4, redSlider.yPos() + 10, xSize - 8, 8).setListener(this).setBarSize(3));
//        greenSlider.horizontal = true;
//        greenSlider.setPos((colour.g & 0xFF) / 255D);
//        greenSlider.sliderColour = 0xFF00FF00;
//        addChild(blueSlider = new GuiSlideControl(xPos + 4, greenSlider.yPos() + 10, xSize - 8, 8).setListener(this).setBarSize(3));
//        blueSlider.horizontal = true;
//        blueSlider.setPos((colour.b & 0xFF) / 255D);
//        blueSlider.sliderColour = 0xFF0000FF;
//        if (includeAlpha) {
//            addChild(alphaSlider = new GuiSlideControl(xPos + 4,blueSlider.yPos() + 10, xSize - 8, 8).setListener(this).setBarSize(3));
//            alphaSlider.horizontal = true;
//            alphaSlider.setPos((colour.a & 0xFF) / 255D);
//            alphaSlider.sliderColour = 0xFFFFFFFF;
//        }

        addChild(selectButton = new MGuiButtonSolid(xPos + 4, yPos + ySize - 14, (xSize / 2) - 5, 10, I18n.format("generic.ok.txt")).setListener(this));
        addChild(cancelButton = new MGuiButtonSolid(selectButton.xPos() + selectButton.xSize() + 2, yPos + ySize - 14, (xSize / 2) - 5, 10, I18n.format("gui.back")).setListener(this));
        super.addChildElements();
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//        zOffset = 50;
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0xFFFFFFFF, 0xFF000000);
        drawBorderedRect(xPos() + 4, yPos() + ySize() - 22, xSize() - 8, 6, 0.5, includeAlpha ? colour.argb() : mixColours(0xFF000000, colour.argb()), 0xFF000000);

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        if (eventElement == redSlider) {
            colour.r = (byte) (redSlider.getPosition() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == greenSlider) {
            colour.g = (byte) (greenSlider.getPosition() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == blueSlider) {
            colour.b = (byte) (blueSlider.getPosition() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == alphaSlider) {
            colour.a = (byte) (alphaSlider.getPosition() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == hexField) {
            try {
                int pos = hexField.getCursorPosition();
                colour.set(Utils.parseHex(hexField.getText()));
                redSlider.updateRawPos((colour.r & 0xFF) / 255D);
                greenSlider.updateRawPos((colour.g & 0xFF) / 255D);
                blueSlider.updateRawPos((colour.b & 0xFF) / 255D);
                alphaSlider.updateRawPos((colour.a & 0xFF) / 255D);
                if (!includeAlpha) {
                    colour.a = (byte) 0xFF;
                }
                hexField.setCursorPosition(pos);
            }
            catch (Exception e) {}
        }
        else if (eventElement == cancelButton) {
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.ColourEvent(this, getColourARGB(), true, false), this);
            }
            close();
        }
        else if (eventElement == selectButton) {
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.ColourEvent(this, getColourARGB(), false, false), this);
            }
            close();
        }
    }

    public MGuiColourPicker setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    public void setColour(int colour) {
        this.colour = new ColourARGB(colour);
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public Colour getColour() {
        return colour;
    }

    public int getColourARGB() {
        return colour.argb();
    }

    /**
     * Sets weather or not this picker should allow you to adjust alpha. This should be set before initialization
     * @param includeAlpha
     * @return
     */
    public MGuiColourPicker setIncludeAlpha(boolean includeAlpha) {
        this.includeAlpha = includeAlpha;
        setYSize(70);
        return this;
    }
}

