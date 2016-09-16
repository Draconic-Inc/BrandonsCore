package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import net.minecraft.client.Minecraft;

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
    public IMGuiListener listener;
    public int colour;

    public MGuiColourPicker(IModularGui modularGui, int xPos, int yPos, MGuiElementBase parent) {
        super(modularGui, xPos, yPos, parent);
    }

    @Override
    public void initElement() {
        super.initElement();
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawBorderedRect(xPos, yPos, xSize, ySize, 1, colour, 0xFF000000);
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onMGuiEvent(String eventString, MGuiElementBase eventElement) {

    }

    public MGuiColourPicker setListener(IMGuiListener listener) {
        this.listener = listener;
        return this;
    }
}
