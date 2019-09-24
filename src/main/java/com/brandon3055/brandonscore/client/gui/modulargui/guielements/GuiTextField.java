package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 10/09/2016.
 * A strait port of the vanilla text field
 */
public class GuiTextField extends MGuiElementBase<GuiTextField> {

    private int placeHolderColor = Color.GRAY.getRGB();
    private String placeHolder = "";
    private String text = "";
    private int maxStringLength = 64;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private IGuiEventListener listener;
    private Consumer<String> changeListener;
    private Consumer<String> returnListener;
    private Consumer<Boolean> focusListener;
    private Predicate<String> validator = s -> true;
    public int fillColour = 0xFF5f5f60;
    public int borderColour = 0xFF000000;
    private Supplier<String> linkedValue;

    public GuiTextField() {}

    public GuiTextField(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTextField(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    public GuiTextField setPlaceHolderColor(int placeHolderColor) {
        this.placeHolderColor = placeHolderColor;
        return this;
    }

    public GuiTextField setPlaceHolder(String placeHolder) {
        this.placeHolder = Strings.nullToEmpty(placeHolder);
        return this;
    }

    public GuiTextField setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    public GuiTextField setChangeListener(Consumer<String> changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public GuiTextField setReturnListener(Consumer<String> returnListener) {
        this.returnListener = returnListener;
        return this;
    }

    public GuiTextField setChangeListener(Runnable changeListener) {
        return setChangeListener((s) -> changeListener.run());
    }

    public GuiTextField setReturnListener(Runnable returnListener) {
        return setReturnListener((s) -> returnListener.run());
    }

    public GuiTextField setFocusListener(Consumer<Boolean> focusListener) {
        this.focusListener = focusListener;
        return this;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public GuiTextField setText(String textIn) {
        if (this.validator.test(textIn)) {
            if (textIn.length() > this.maxStringLength) {
                this.text = textIn.substring(0, this.maxStringLength);
            }
            else {
                this.text = textIn;
            }

            this.setCursorPositionEnd();
        }
        return this;
    }

    /**
     * The value to which this field is linked. If the values returned by this supplier is not the same as the text field the text field will automatically get updated.
     */
    public GuiTextField setLinkedValue(Supplier<String> linkedValue) {
        this.linkedValue = linkedValue;
        return this;
    }

    public GuiTextField forceSetText(String textIn) {
        if (textIn.length() > this.maxStringLength) {
            this.text = textIn.substring(0, this.maxStringLength);
        }
        else {
            this.text = textIn;
        }

        this.setCursorPositionEnd();

        return this;
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator) {
        this.validator = theValidator;
    }

    public void writeText(String textToWrite) {
        String s = "";
        String s1 = textToWrite;//ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - j);

        if (!this.text.isEmpty()) {
            s = s + this.text.substring(0, i);
        }

        int l;

        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        }
        else {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text.isEmpty() && j < this.text.length()) {
            s = s + this.text.substring(j);
        }

        if (this.validator.test(s)) {
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l);

            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.TextFieldEvent(this, text, true, false), this);
            }
            if (changeListener != null) {
                changeListener.accept(text);
            }
        }
    }

    public void deleteWords(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            }
            else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            }
            else {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";

                if (i >= 0) {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length()) {
                    s = s + this.text.substring(j);
                }

                if (this.validator.test(s)) {
                    this.text = s;

                    if (flag) {
                        this.moveCursorBy(num);
                    }

                    if (listener != null) {
                        listener.onMGuiEvent(new GuiEvent.TextFieldEvent(this, text, true, false), this);
                    }

                    if (changeListener != null) {
                        changeListener.accept(text);
                    }
                }
            }
        }
    }

    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    public int getNthWordFromPos(int n, int pos) {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1) {
                    i = l;
                }
                else {
                    while (skipWs && i < l && this.text.charAt(i) == 32) {
                        ++i;
                    }
                }
            }
            else {
                while (skipWs && i > 0 && this.text.charAt(i - 1) == 32) {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != 32) {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int num) {
        this.setCursorPosition(this.selectionEnd + num);
    }

    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        }
        else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        }
        else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        }
        else {
            switch (keyCode) {
                case 28:
                    if (listener != null) {
                        listener.onMGuiEvent(new GuiEvent.TextFieldEvent(this, text, false, true), this);
                    }
                    if (returnListener != null) {
                        returnListener.accept(text);
                    }
                    return true;
                case 14:

                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    }
                    else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;
                case 199:

                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    }
                    else {
                        this.setCursorPositionZero();
                    }

                    return true;
                case 203:

                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        }
                        else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    }
                    else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    }
                    else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                case 205:

                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        }
                        else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    }
                    else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    }
                    else {
                        this.moveCursorBy(1);
                    }

                    return true;
                case 207:

                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    }
                    else {
                        this.setCursorPositionEnd();
                    }

                    return true;
                case 211:

                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    }
                    else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;
                default:

                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    }
                    else {
                        return false;
                    }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean mouseOver = isMouseOver(mouseX, mouseY);

        if (this.canLoseFocus) {
            this.setFocused(mouseOver);
        }

        if (this.isFocused && mouseOver && mouseButton == 0) {
            int i = mouseX - xPos();

            if (this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + this.lineScrollOffset);
        }
        else if (this.isFocused && mouseOver && mouseButton == 1) {
            setText("");
            if (listener != null) {
                listener.onMGuiEvent(new GuiEvent.TextFieldEvent(this, text, true, false), this);
            }
            if (changeListener != null) {
                changeListener.accept(text);
            }
        }

        return mouseOver || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawTextBox();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    public void drawTextBox() {
        if (this.getEnableBackgroundDrawing()) {
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, fillColour, borderColour);
        }
        int startX = this.enableBackgroundDrawing ? xPos() + 4 : xPos();
        int startY = this.enableBackgroundDrawing ? yPos() + (ySize() - 8) / 2 : yPos();

        if (Strings.isNullOrEmpty(this.text) && !Strings.isNullOrEmpty(this.placeHolder) && !this.isFocused) {
            drawString(fontRenderer, this.placeHolder, startX, startY, this.placeHolderColor, false);
            return;
        }

        int color = this.isEnabled ? this.enabledColor : this.disabledColor;
        int physicsCursorPosition = this.cursorPosition - this.lineScrollOffset;
        int physicsSelectionEnd = this.selectionEnd - this.lineScrollOffset;
        if (this.isFocused) {
            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean cursorValid = physicsCursorPosition >= 0 && physicsCursorPosition <= s.length();
            boolean shouldDrawCursor = this.isFocused && this.cursorCounter / 6 % 2 == 0 && cursorValid;
            int xAfterCursor = startX;

            if (physicsSelectionEnd > s.length()) {
                physicsSelectionEnd = s.length();
            }

            if (!s.isEmpty()) {
                String strBeforeCursor = this.isFocused && cursorValid ? s.substring(0, physicsCursorPosition) : s;
                xAfterCursor = drawString(fontRenderer, strBeforeCursor, startX, startY, color, false);
            }

            boolean cursorInWords = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int xCursor = xAfterCursor;

            if (!cursorValid) {
                xCursor = physicsCursorPosition > 0 ? startX + fontRenderer.getStringWidth(s) : startX;
            } else if (cursorInWords) {
                xCursor = xAfterCursor - 1;
            }

            if (this.isFocused && !s.isEmpty() && cursorValid && physicsCursorPosition < s.length()) {
                drawString(fontRenderer, s.substring(physicsCursorPosition), xAfterCursor, startY, color, false);
            }

            if (shouldDrawCursor) {
                if (cursorInWords) {
                    drawRect(xCursor, startY - 1, xCursor + 1, startY + 1 + fontRenderer.FONT_HEIGHT, -3092272);
                } else {
                    drawString(fontRenderer, "_", xCursor, startY, color, true);
                }
            }

            if (physicsSelectionEnd != physicsCursorPosition) {
                int l1 = startX + fontRenderer.getStringWidth(s.substring(0, physicsSelectionEnd));
                this.drawCursorVertical(xCursor, startY - 1, l1 - 1, startY + 1 + fontRenderer.FONT_HEIGHT);
            }
        } else {
            String str = this.text;
            str = this.fontRenderer.trimStringToWidth(str, this.getWidth());
            drawString(this.fontRenderer, str, startX, startY, color, false);
        }
    }

    private void drawCursorVertical(int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > xPos() + xSize()) {
            endX = xPos() + xSize();
        }

        if (startX > xPos() + xSize()) {
            startX = xPos() + xSize();
        }

        double zLevel = getRenderZLevel();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        buffer.begin(7, DefaultVertexFormats.POSITION);
        buffer.pos((double) startX, (double) endY, zLevel).endVertex();
        buffer.pos((double) endX, (double) endY, zLevel).endVertex();
        buffer.pos((double) endX, (double) startY, zLevel).endVertex();
        buffer.pos((double) startX, (double) startY, zLevel).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public GuiTextField setMaxStringLength(int length) {
        this.maxStringLength = length;

        if (this.text.length() > length) {
            this.text = this.text.substring(0, length);
        }
        return this;
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) {
        this.enableBackgroundDrawing = enableBackgroundDrawingIn;
    }

    public void setTextColor(int color) {
        this.enabledColor = color;
    }

    public void setDisabledTextColour(int color) {
        this.disabledColor = color;
    }

    public void setFocused(boolean isFocusedIn) {
        if (isFocusedIn && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = isFocusedIn;
        if (focusListener != null) {
            focusListener.accept(isFocused);
        }
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public GuiTextField setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        super.setEnabled(enabled);
        return this;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public int getWidth() {
        return this.getEnableBackgroundDrawing() ? xSize() - 8 : xSize();
    }

    public void setSelectionPos(int position) {
        int i = this.text.length();

        if (position > i) {
            position = i;
        }

        if (position < 0) {
            position = 0;
        }

        this.selectionEnd = position;

        if (fontRenderer != null) {
            if (this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;

            if (position == this.lineScrollOffset) {
                this.lineScrollOffset -= fontRenderer.trimStringToWidth(this.text, j, true).length();
            }

            if (position > k) {
                this.lineScrollOffset += position - k;
            }
            else if (position <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - position;
            }

            this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
        }
    }

    public void setCanLoseFocus(boolean canLoseFocusIn) {
        this.canLoseFocus = canLoseFocusIn;
    }

    public void setColours(int fillColour, int borderColour) {
        this.fillColour = fillColour;
        this.borderColour = borderColour;
    }

    @Override
    public boolean onUpdate() {
        if (super.onUpdate()) {
            return true;
        }

        if (linkedValue != null && !linkedValue.get().equals(getText())) {
            setText(linkedValue.get());
            return true;
        }

        if (this.isFocused) {
            this.updateCursorCounter();
        }

        return false;
    }
}
