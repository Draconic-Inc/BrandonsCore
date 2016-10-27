package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import com.brandon3055.brandonscore.utils.Utils;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class MGuiColourPicker extends MGuiPopUpDialog implements IMGuiListener {

    public MGuiButton cancelButton;
    public MGuiButton selectButton;
    public MGuiTextField hexField;
    public MGuiSlider redSlider;
    public MGuiSlider greenSlider;
    public MGuiSlider blueSlider;
    public MGuiSlider alphaSlider;
    private boolean includeAlpha = true;
    public IMGuiListener listener;
    public Colour colour;

    public MGuiColourPicker(IModularGui modularGui, int xPos, int yPos, MGuiElementBase parent) {
        super(modularGui, xPos, yPos, parent);
        xSize = 80;
        ySize = 80;
        dragZoneSize = 3;
        if (parent instanceof IMGuiListener) {
            listener = (IMGuiListener) parent;
        }
    }

    @Override
    public void initElement() {
        if (!includeAlpha) {
            colour.a = (byte) 255;
        }

        addChild(hexField = new MGuiTextField(modularGui, xPos + 4, yPos + 4, xSize - 8, 12, fontRenderer).setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb())).setListener(this));
        hexField.setMaxStringLength(includeAlpha ? 8 : 6);
        hexField.setValidator(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                try {
                    Utils.parseHex(input);
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            }
        });

        addChild(redSlider = new MGuiSlider(modularGui, xPos + 4, hexField.yPos + 14, xSize - 8, 8).setListener(this).setBarSize(3));
        redSlider.horizontal = true;
        redSlider.setPos((colour.r & 0xFF) / 255D);
        redSlider.sliderColour = 0xFFFF0000;
        addChild(greenSlider = new MGuiSlider(modularGui, xPos + 4, redSlider.yPos + 10, xSize - 8, 8).setListener(this).setBarSize(3));
        greenSlider.horizontal = true;
        greenSlider.setPos((colour.g & 0xFF) / 255D);
        greenSlider.sliderColour = 0xFF00FF00;
        addChild(blueSlider = new MGuiSlider(modularGui, xPos + 4, greenSlider.yPos + 10, xSize - 8, 8).setListener(this).setBarSize(3));
        blueSlider.horizontal = true;
        blueSlider.setPos((colour.b & 0xFF) / 255D);
        blueSlider.sliderColour = 0xFF0000FF;
        if (includeAlpha) {
            addChild(alphaSlider = new MGuiSlider(modularGui, xPos + 4,blueSlider.yPos + 10, xSize - 8, 8).setListener(this).setBarSize(3));
            alphaSlider.horizontal = true;
            alphaSlider.setPos((colour.a & 0xFF) / 255D);
            alphaSlider.sliderColour = 0xFFFFFFFF;
        }

        addChild(selectButton = new MGuiButtonSolid(modularGui, xPos + 4, yPos + ySize - 14, (xSize / 2) - 5, 10, I18n.format("generic.ok.txt")).setListener(this));
        addChild(cancelButton = new MGuiButtonSolid(modularGui, selectButton.xPos + selectButton.xSize + 2, yPos + ySize - 14, (xSize / 2) - 5, 10, I18n.format("gui.back")).setListener(this));
        super.initElement();
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//        zOffset = 50;
        drawBorderedRect(xPos, yPos, xSize, ySize, 1, 0xFFFFFFFF, 0xFF000000);
        drawBorderedRect(xPos + 4, yPos + ySize - 22, xSize - 8, 6, 0.5, includeAlpha ? colour.argb() : mixColours(0xFF000000, colour.argb()), 0xFF000000);

        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onMGuiEvent(String eventString, MGuiElementBase eventElement) {
        if (eventElement == redSlider) {
            colour.r = (byte) (redSlider.getPos() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == greenSlider) {
            colour.g = (byte) (greenSlider.getPos() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == blueSlider) {
            colour.b = (byte) (blueSlider.getPos() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == alphaSlider) {
            colour.a = (byte) (alphaSlider.getPos() * 255D);
            hexField.setText(Integer.toHexString(includeAlpha ? colour.argb() : colour.rgb()));
        }
        else if (eventElement == hexField) {
            try {
                int pos = hexField.getCursorPosition();
                colour.set(Utils.parseHex(hexField.getText()));
                redSlider.setPos((colour.r & 0xFF) / 255D);
                greenSlider.setPos((colour.g & 0xFF) / 255D);
                blueSlider.setPos((colour.b & 0xFF) / 255D);
                alphaSlider.setPos((colour.a & 0xFF) / 255D);
                if (!includeAlpha) {
                    colour.a = (byte) 0xFF;
                }
                hexField.setCursorPosition(pos);
            }
            catch (Exception e) {}
        }
        else if (eventElement == cancelButton) {
            if (listener != null) {
                listener.onMGuiEvent("COLOUR_PICK_CANCELED", this);
            }
            close();
        }
        else if (eventElement == selectButton) {
            if (listener != null) {
                listener.onMGuiEvent("COLOUR_PICKED", this);
            }
            close();
        }

    }

    public MGuiColourPicker setListener(IMGuiListener listener) {
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
        ySize = 70;
        return this;
    }
}

