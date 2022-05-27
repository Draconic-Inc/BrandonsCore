package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 23/10/2016.
 * This is a simple popup text field with an ok button that allows the user to specify a string
 */
public class GuiTextFieldDialog extends GuiPopUpDialogBase<GuiTextFieldDialog> {

    private String title = "";
    public int titleColour = 0xFFFFFF;
    public GuiTextField textField;
    public GuiButton okButton;
    protected int maxLength = 64;
    protected String defaultText = "";
    protected Predicate<String> validator = null;
    protected Consumer<String> changeCallBack;
    protected Consumer<String> confirmCallBack;

    public GuiTextFieldDialog(GuiElement parent) {
        super(parent);
        setSize(200, 20);
    }

    public GuiTextFieldDialog(GuiElement parent, String title) {
        super(parent);
        this.title = title;
        setSize(200, 40);
        setDragBar(15);
        setInsets(18, 3, 3, 3);
    }

    public GuiTextFieldDialog(int xPos, int yPos, GuiElement parent) {
        super(xPos, yPos, parent);
        setSize(200, 20);
    }

    public GuiTextFieldDialog(int xPos, int yPos, int xSize, int ySize, GuiElement parent) {
        super(xPos, yPos, xSize, ySize, parent);
        setSize(200, 20);
    }

    @Override
    public void addChildElements() {
        addChild(textField = new GuiTextField().setPosAndSize(getInsetRect()).setXSize(getInsetRect().width - 20));
        textField.onValueChanged(text -> {if (changeCallBack != null) changeCallBack.accept(text);});
        textField.onReturnPressed(text -> {
            if (confirmCallBack != null) confirmCallBack.accept(text);
            close();
        });
        textField.setMaxLength(maxLength);
        textField.setValue(defaultText);

        if (validator != null) {
            textField.setFilter(validator);
        }

        addChild(okButton = new GuiButton(textField.maxXPos(), textField.yPos(), 20, textField.ySize(), I18n.get("mod_gui.brandonscore.button.ok")).setTrim(false).setFillColour(0xFF000000).setBorderColours(0xFF555555, 0xFF777777));
        okButton.onPressed(() -> {
            if (confirmCallBack != null) {
                confirmCallBack.accept(textField.getValue());
            }
            close();
        });
        super.addChildElements();
    }

    /**
     * Allows you to add a positionRestraint that restricts what the user is able to enter into this field.
     */
    public GuiTextFieldDialog setValidator(Predicate<String> validator) {
        this.validator = validator;
        if (textField != null) {
            textField.setFilter(validator);
        }
        return this;
    }

    public GuiTextFieldDialog setText(String text) {
        this.defaultText = text;
        if (textField != null) {
            textField.setValue(text);
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

    public GuiTextFieldDialog setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (textField != null) {
            textField.setMaxLength(maxLength);
        }
        return this;
    }

    public GuiTextFieldDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public GuiTextFieldDialog setTitleColour(int titleColour) {
        this.titleColour = titleColour;
        return this;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        if (!title.isEmpty()) {
            drawString(fontRenderer, title, xPos() + 4, yPos() + 6, titleColour);
        }
    }
}
