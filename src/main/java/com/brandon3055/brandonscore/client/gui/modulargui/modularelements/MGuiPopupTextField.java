package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public class MGuiPopupTextField extends MGuiPopUpDialog implements IMGuiListener {

    public MGuiTextField textField;
    public MGuiButton okButton;

    public MGuiPopupTextField(IModularGui modularGui, MGuiElementBase parent) {
        super(modularGui, parent);
        initElement();
    }

    public MGuiPopupTextField(IModularGui modularGui, int xPos, int yPos, MGuiElementBase parent) {
        super(modularGui, xPos, yPos, parent);
        this.xSize = 100;
        this.ySize = 20;
        initElement();
    }

    public MGuiPopupTextField(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(modularGui, xPos, yPos, xSize, ySize, parent);
        initElement();
    }

    @Override
    public void initElement() {
        addChild(textField = new MGuiTextField(modularGui, xPos, yPos, xSize - 20, ySize, modularGui.getMinecraft().fontRendererObj).setListener(this));
        addChild(okButton = new MGuiButton(modularGui, xPos + textField.xSize, yPos, 20, ySize, "OK").setListener(this));
        super.initElement();
    }

    @Override
    public void onMGuiEvent(String eventString, MGuiElementBase eventElement) {
        if (eventElement == okButton && parent instanceof IMGuiListener) {
            ((IMGuiListener) parent).onMGuiEvent(textField.getText(), this);
            close();
        }
    }
}
