package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IButtonListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by brandon3055 on 15/08/2017.
 */
public class GuiPopupDialogs extends GuiPopUpDialogBase<GuiPopupDialogs> {

    public GuiButton yesButton;
    public GuiButton noButton;
    public GuiButton okButton;
    public GuiButton cancelButton;

    public GuiPopupDialogs(MGuiElementBase parent) {
        super(parent);
    }

    public GuiPopupDialogs setYesListener(IButtonListener yesListener) {
        if (yesButton != null) yesButton.setListener(yesListener);
        return this;
    }

    public GuiPopupDialogs setNoListener(IButtonListener noListener) {
        if (noButton != null) noButton.setListener(noListener);
        return this;
    }

    public GuiPopupDialogs setOkListener(IButtonListener okListener) {
        if (okButton != null) okButton.setListener(okListener);
        return this;
    }

    public GuiPopupDialogs setCancelListener(IButtonListener cancelListener) {
        if (cancelButton != null) cancelButton.setListener(cancelListener);
        return this;
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message, String title, int xSize, MGuiElementBase background, boolean vanillaButtons, int buttonFill, int buttonFillHover, int buttonBorder, int buttonBorderHover) {
        GuiPopupDialogs dialog = new GuiPopupDialogs(parent);
        dialog.setXSize(xSize);
        dialog.setDragBar(12);
        dialog.addChild(background);

        boolean hasTitle = title != null && !title.isEmpty();
        if (hasTitle) {
            dialog.addChild(new GuiLabel(TextFormatting.UNDERLINE + title).setSize(dialog.xSize() - 10, 10).setRelPos(5, 4).setTextColour(0).setShadow(false).setAlignment(GuiAlign.LEFT));
        }

        GuiLabel messageLabel = new GuiLabel(message).setWrap(true).setHeightForText(dialog.xSize() - 10).setPos(5, hasTitle ? 20 : 4);
        messageLabel.setTextColour(0).setShadow(false);
        dialog.addChild(messageLabel);

        dialog.setYSize(messageLabel.ySize() + (hasTitle ? 20 : 4) + 25);
        background.setSize(dialog);

        int buttonWidth = (dialog.xSize() - 10 - type.buttons) / (type.buttons + 1);
        int buttonX = (dialog.xSize() / 2) - ((((type.buttons + 1) * buttonWidth) + type.buttons) / 2);

        if (type.yes) {
            GuiButton button = new GuiButton(I18n.format("generic.yes.txt")).setSize(buttonWidth, 14).setPos(buttonX, messageLabel.maxYPos() + 5);
            if (vanillaButtons) button.setVanillaButtonRender(true);
            else button.setRectColours(buttonFill, buttonFillHover, buttonBorder, buttonBorderHover);
            buttonX += buttonWidth + 1;
            dialog.addChild(dialog.yesButton = button);
        }
        if (type.no) {
            GuiButton button = new GuiButton(I18n.format("generic.no.txt")).setSize(buttonWidth, 14).setPos(buttonX, messageLabel.maxYPos() + 5);
            if (vanillaButtons) button.setVanillaButtonRender(true);
            else button.setRectColours(buttonFill, buttonFillHover, buttonBorder, buttonBorderHover);
            buttonX += buttonWidth + 1;
            dialog.addChild(dialog.noButton = button);
        }
        if (type.ok) {
            GuiButton button = new GuiButton(I18n.format("generic.ok.txt")).setSize(buttonWidth, 14).setPos(buttonX, messageLabel.maxYPos() + 5);
            if (vanillaButtons) button.setVanillaButtonRender(true);
            else button.setRectColours(buttonFill, buttonFillHover, buttonBorder, buttonBorderHover);
            buttonX += buttonWidth + 1;
            dialog.addChild(dialog.okButton = button);
        }
        if (type.cancel) {
            GuiButton button = new GuiButton(I18n.format("generic.cancel.txt")).setSize(buttonWidth, 14).setPos(buttonX, messageLabel.maxYPos() + 5);
            if (vanillaButtons) button.setVanillaButtonRender(true);
            else button.setRectColours(buttonFill, buttonFillHover, buttonBorder, buttonBorderHover);
            buttonX += buttonWidth + 1;
            dialog.addChild(dialog.cancelButton = button);
        }

        dialog.setCloseOnCapturedClick(true);

        return dialog;
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message, String title, int xSize, int buttonFill, int buttonFillHover, int buttonBorder, int buttonBorderHover) {
        return createDialog(parent, type, message, title, xSize, GuiTexture.newVanillaGuiTexture(0, 0), false, buttonFill, buttonFillHover, buttonBorder, buttonBorderHover);
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message, String title, int xSize) {
        return createDialog(parent, type, message, title, xSize, GuiTexture.newVanillaGuiTexture(0, 0), true, 0, 0, 0, 0);
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message, int xSize) {
        return createDialog(parent, type, message, "", xSize, GuiTexture.newVanillaGuiTexture(0, 0), true, 0, 0, 0, 0);
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message, String title, int buttonFill, int buttonFillHover, int buttonBorder, int buttonBorderHover) {
        return createDialog(parent, type, message, title, 200, GuiTexture.newVanillaGuiTexture(0, 0), false, buttonFill, buttonFillHover, buttonBorder, buttonBorderHover);
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message, String title) {
        return createDialog(parent, type, message, title, 200, GuiTexture.newVanillaGuiTexture(0, 0), true, 0, 0, 0, 0);
    }

    public static GuiPopupDialogs createDialog(MGuiElementBase parent, DialogType type, String message) {
        return createDialog(parent, type, message, "", 200, GuiTexture.newVanillaGuiTexture(0, 0), true, 0, 0, 0, 0);
    }

    public enum DialogType {
        YES_NO_OPTION(true, true, false, false, 1),
        YES_NO_CANCEL_OPTION(true, true, false, true, 2),
        OK_CANCEL_OPTION(false, false, true, true, 1),
        YES_OPTION(true, false, false, false, 0),
        NO_OPTION(false, true, false, false, 0),
        CANCEL_OPTION(false, false, false, true, 0),
        OK_OPTION(false, false, true, false, 0);

        public final boolean yes;
        public final boolean no;
        public final boolean ok;
        public final boolean cancel;
        private int buttons;

        private DialogType(boolean yes, boolean no, boolean ok, boolean cancel, int buttons) {
            this.yes = yes;
            this.no = no;
            this.ok = ok;
            this.cancel = cancel;
            this.buttons = buttons;
        }

    }
}
