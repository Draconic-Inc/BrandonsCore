package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public class MGuiPopupTextField extends MGuiPopUpDialog implements IGuiEventListener {

    public MGuiTextField textField;
    public GuiButton okButton;

    public MGuiPopupTextField(MGuiElementBase parent) {
        super( parent);
        addChildElements();
    }

    public MGuiPopupTextField(int xPos, int yPos, MGuiElementBase parent) {
        super(xPos, yPos, parent);
        setSize(100, 20);
        addChildElements();
    }

    public MGuiPopupTextField(int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(xPos, yPos, xSize, ySize, parent);
        addChildElements();
    }

    @Override
    public void addChildElements() {
        childElements.clear();
        addChild(textField = new MGuiTextField(xPos(), yPos(), xSize() - 20, ySize()).setListener(this));
        addChild(okButton = new GuiButton(xPos() + textField.xSize(), yPos(), 20, ySize(), "OK").setListener(this));
        super.addChildElements();
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        if (eventElement == okButton && parent instanceof IGuiEventListener) {
            ((IGuiEventListener) parent).onMGuiEvent(new GuiEvent.TextFieldEvent(textField, textField.getText(), true, true), this);
            close();
        }
    }
}
