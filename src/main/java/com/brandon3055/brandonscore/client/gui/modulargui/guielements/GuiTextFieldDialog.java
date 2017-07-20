package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 23/10/2016.
 * This is a simple popup text field with an ok button that allows the user to specify a string
 */
public class GuiTextFieldDialog extends GuiPopUpDialogBase<GuiTextFieldDialog> implements IGuiEventListener {

    public GuiTextField textField;
    public GuiButton okButton;
    protected int maxLength = 64;
    protected String defaultText = "";
    protected Predicate<String> validator = null;
    protected Consumer<String> changeCallBack;
    protected Consumer<String> confirmCallBack;
    protected IGuiEventListener listener;

    public GuiTextFieldDialog(MGuiElementBase parent) {
        super(parent);
        setSize(200, 20);
    }

    public GuiTextFieldDialog(int xPos, int yPos, MGuiElementBase parent) {
        super(xPos, yPos, parent);
        setSize(200, 20);
    }

    public GuiTextFieldDialog(int xPos, int yPos, int xSize, int ySize, MGuiElementBase parent) {
        super(xPos, yPos, xSize, ySize, parent);
        setSize(200, 20);
    }

    @Override
    public void addChildElements() {
        addChild(textField = new GuiTextField(xPos(), yPos(), xSize() - 20, ySize()).setListener(this));
        textField.setMaxStringLength(maxLength);
        textField.setText(defaultText);

        if (validator != null) {
            textField.setValidator(validator);
        }

        addChild(okButton = new GuiButton(xPos() + textField.xSize(), yPos(), 20, ySize(), I18n.format("generic.ok.txt")).setTrim(false).setFillColour(0xFF000000).setBorderColours(0xFF555555, 0xFF777777).setListener(this));
        super.addChildElements();
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        if (event.isTextFiled()) {
            if (listener != null) {
                listener.onMGuiEvent(event, eventElement);
            }
            if (event.asTextField().textChanged() && changeCallBack != null) {
                changeCallBack.accept(textField.getText());
            }
            else if (event.asTextField().textEnterPressed() && confirmCallBack != null) {
                confirmCallBack.accept(textField.getText());
            }
            if (event.asTextField().textEnterPressed()) {
                close();
            }
        }

        if (eventElement == okButton) {
            if (confirmCallBack != null) {
                confirmCallBack.accept(textField.getText());
            }
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.TextFieldEvent(textField, textField.getText(), false, true), textField);
            }
            close();
        }
    }

    /**
     * Allows you to add a validator that restricts what the user is able to enter into this field.
     */
    public GuiTextFieldDialog setValidator(Predicate<String> validator) {
        this.validator = validator;
        if (textField != null) {
            textField.setValidator(validator);
        }
        return this;
    }

    public GuiTextFieldDialog setText(String text) {
        this.defaultText = text;
        if (textField != null) {
            textField.setText(text);
        }
        return this;
    }

    /**
     * Add a callback that will be called whenever the text in the text field changes.
     */
    public GuiTextFieldDialog addTextChangeCallback(Consumer<String> changeCallBack) {
        this.changeCallBack = changeCallBack;
        return this;
    }

    /**
     * Add a callback that will be called when the user presses the ok button or the enter key.
     */
    public GuiTextFieldDialog addTextConfirmCallback(Consumer<String> confirmCallBack) {
        this.confirmCallBack = confirmCallBack;
        return this;
    }

    /**
     * Apply a listener to which all text field events will be forwarded. The OK button will be forwarded as
     * a text field "Enter Pressed" event.
     * Unlike other event dispatcher elements this element will not have its listener automatically assigned to its parent.
     */
    public GuiTextFieldDialog setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    public GuiTextFieldDialog setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (textField != null) {
            textField.setMaxStringLength(maxLength);
        }
        return this;
    }
}
