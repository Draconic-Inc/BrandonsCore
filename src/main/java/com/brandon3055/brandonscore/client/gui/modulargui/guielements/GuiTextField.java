package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 10/09/2016.
 * A strait port of the vanilla text field
 */
public class GuiTextField extends GuiElement<GuiTextField> {

    private int maxStringLength = 64;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isFieldEnabled = true; //This is similar to the the disabled field in Button.
    private boolean blinkCursor = false;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;

    private Consumer<String> changeListener;
    private Consumer<String> returnListener;
    private Consumer<Boolean> focusListener;
    private Predicate<String> validator = s -> true;
    private Consumer<String> linkedSetter;
    private Supplier<String> linkedGetter;

    public int fillColour = 0xFF5f5f60;
    public int borderColour = 0xFF000000;

    private boolean shiftCache;
    private String suggestion;

    private BiFunction<String, Integer, String> textFormatter = (p_195610_0_, p_195610_1_) -> {
        return p_195610_0_;
    };

    public GuiTextField() {}

    public GuiTextField(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTextField(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    //Listeners

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

    public GuiTextField setTextFormatter(BiFunction<String, Integer, String> textFormatter) {
        this.textFormatter = textFormatter;
        return this;
    }


    public GuiTextField setText(String textIn) {
        if (this.validator.test(textIn)) {
            if (textIn.length() > this.maxStringLength) {
                this.text(textIn.substring(0, this.maxStringLength));
            } else {
                this.text(textIn);
            }

            this.setCursorPositionEnd();
            this.setSelectionPos(this.cursorPosition);
            this.notifyListeners(textIn);
        }
        return this;
    }

    //Sets the text value without validation and without notifying listeners.
    public GuiTextField setTextQuietly(String newText) {
        if (newText.length() > this.maxStringLength) {
            this.text(newText.substring(0, this.maxStringLength));
        } else {
            this.text( newText);
        }

        this.setCursorPositionEnd();
        this.setSelectionPos(this.cursorPosition);

        return this;
    }

    public String getText() {
        return this.text();
    }

    public String getSelectedText() {
        int start = Math.min(this.cursorPosition, this.selectionEnd);
        int end = Math.max(this.cursorPosition, this.selectionEnd);
        return this.text().substring(start, end);
    }

    public void setValidator(Predicate<String> validator) {
        this.validator = validator;
    }

    public void writeText(String textToWrite) {
        String s = "";
        String s1 = SharedConstants.filterAllowedCharacters(textToWrite);
        int selectStart = Math.min(this.cursorPosition, this.selectionEnd);
        int selectEnd = Math.max(this.cursorPosition, this.selectionEnd);
        int k = this.maxStringLength - this.text().length() - (selectStart - selectEnd);
        if (!this.text().isEmpty()) {
            s = s + this.text().substring(0, selectStart);
        }

        int l;
        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        } else {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text().isEmpty() && selectEnd < this.text().length()) {
            s = s + this.text().substring(selectEnd);
        }

        if (this.validator.test(s)) {
            this.text(s);
            this.updateCursor(selectStart + l);
            this.setSelectionPos(this.cursorPosition);
            this.notifyListeners(this.text());
        }
    }

    private void notifyListeners(String newText) {
        if (changeListener != null) {
            changeListener.accept(newText);
        }
    }

    private void delete(int dir) {
        if (Screen.hasControlDown()) {
            this.deleteWords(dir);
        } else {
            this.deleteFromCursor(dir);
        }

    }

    public void deleteWords(int num) {
        if (!this.text().isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int num) {
        if (!this.text().isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";
                if (i >= 0) {
                    s = this.text().substring(0, i);
                }

                if (j < this.text().length()) {
                    s = s + this.text().substring(j);
                }

                if (this.validator.test(s)) {
                    this.text(s);
                    if (flag) {
                        this.moveCursorBy(num);
                    }

                    this.notifyListeners(this.text());
                }
            }
        }
    }

    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    private int getNthWordFromPos(int n, int pos) {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    private int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text().length();
                i = this.text().indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipWs && i < l && this.text().charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipWs && i > 0 && this.text().charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text().charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int num) {
        this.setCursorPosition(this.cursorPosition + num);
    }

    public void setCursorPosition(int pos) {
        this.updateCursor(pos);
        if (!this.shiftCache) {
            this.setSelectionPos(this.cursorPosition);
        }

        this.notifyListeners(this.text());
    }

    public void updateCursor(int pos) {
        this.cursorPosition = MathHelper.clamp(pos, 0, this.text().length());
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text().length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        if (!this.isEditing()) {
//            return false;
//        } else {
        this.shiftCache = Screen.hasShiftDown();
        if (Screen.isSelectAll(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            if (this.isFieldEnabled) {
                this.writeText(Minecraft.getInstance().keyboardListener.getClipboardString());
            }

            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
            if (this.isFieldEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case 257:
                    if (returnListener != null) {
                        returnListener.accept(getText());
                        return true;
                    }
                    return false;
                case 259:
                    if (this.isFieldEnabled) {
                        this.shiftCache = false;
                        this.delete(-1);
                        this.shiftCache = Screen.hasShiftDown();
                    }

                    return true;
                case 260:
                case 264:
                case 265:
                case 266:
                case 267:
                default:
                    return false;
                case 261:
                    if (this.isFieldEnabled) {
                        this.shiftCache = false;
                        this.delete(1);
                        this.shiftCache = Screen.hasShiftDown();
                    }

                    return true;
                case 262:
                    if (Screen.hasControlDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;
                case 263:
                    if (Screen.hasControlDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                case 268:
                    this.setCursorPositionZero();
                    return true;
                case 269:
                    this.setCursorPositionEnd();
                    return true;
            }
        }
//        }
    }

    public boolean isEditing() {
        return /*this.isEnabled() && */this.isFocused() && this.isFieldEnabled;
    }

    public boolean charTyped(char charType, int charCode) {
        if (!this.isEditing()) {
            return false;
        } else if (SharedConstants.isAllowedCharacter(charType)) {
            if (this.isFieldEnabled) {
                this.writeText(Character.toString(charType));
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
//        if (!this.isEnabled()) {
//            return false;
//        } else {
        boolean flag = p_mouseClicked_1_ >= (double) this.xPos() && p_mouseClicked_1_ < (double) (this.xPos() + this.xSize()) && p_mouseClicked_3_ >= (double) this.yPos() && p_mouseClicked_3_ < (double) (this.yPos() + this.ySize());
        if (this.canLoseFocus) {
            this.setFocused2(flag);
        }

        if (this.isFocused() && flag && p_mouseClicked_5_ == 0) {
            int i = MathHelper.floor(p_mouseClicked_1_) - this.xPos();
            if (this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = this.fontRenderer.trimStringToWidth(this.text().substring(this.lineScrollOffset), this.getAdjustedWidth());
            this.setCursorPosition(this.fontRenderer.trimStringToWidth(s, i).length() + this.lineScrollOffset);
            return true;
        } else {
            return false;
        }
//        }
    }

    public void setFocused2(boolean isFocused) {
        this.isFocused = isFocused;
    }

    public void drawTextBox() {
//        if (this.isEnabled()) {
        if (this.getEnableBackgroundDrawing()) {
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, fillColour, borderColour);
        }

        int i = this.isFieldEnabled ? this.enabledColor : this.disabledColor;
        int j = this.cursorPosition - this.lineScrollOffset;
        int k = this.selectionEnd - this.lineScrollOffset;
        String s = this.fontRenderer.trimStringToWidth(this.text().substring(this.lineScrollOffset), this.getAdjustedWidth());
        boolean flag = j >= 0 && j <= s.length();
        boolean flag1 = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && flag;
        int l = this.enableBackgroundDrawing ? this.xPos() + 4 : this.xPos();
        int i1 = this.enableBackgroundDrawing ? this.yPos() + (this.ySize() - 8) / 2 : this.yPos();
        int j1 = l;
        if (k > s.length()) {
            k = s.length();
        }

        if (!s.isEmpty()) {
            String s1 = flag ? s.substring(0, j) : s;
            j1 = this.fontRenderer.drawStringWithShadow(this.textFormatter.apply(s1, this.lineScrollOffset), (float) l, (float) i1, i);
        }

        boolean flag2 = this.cursorPosition < this.text().length() || this.text().length() >= this.getMaxStringLength();
        int k1 = j1;
        if (!flag) {
            k1 = j > 0 ? l + this.xSize() : l;
        } else if (flag2) {
            k1 = j1 - 1;
            --j1;
        }

        if (!s.isEmpty() && flag && j < s.length()) {
            this.fontRenderer.drawStringWithShadow(this.textFormatter.apply(s.substring(j), this.cursorPosition), (float) j1, (float) i1, i);
        }

        if (!flag2 && this.suggestion != null) {
            this.fontRenderer.drawStringWithShadow(this.suggestion, (float) (k1 - 1), (float) i1, -8355712);
        }

        if (flag1) {
            if (flag2) {
                AbstractGui.fill(k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
            } else {
                this.fontRenderer.drawStringWithShadow("_", (float) k1, (float) i1, i);
            }
        }

        if (k != j) {
            int l1 = l + this.fontRenderer.getStringWidth(s.substring(0, k));
            this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
        }

//        }
    }

    private void drawSelectionBox(int startX, int startY, int endX, int endY) {
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

        if (endX > this.xPos() + this.xSize()) {
            endX = this.xPos() + this.xSize();
        }

        if (startX > this.xPos() + this.xSize()) {
            startX = this.xPos() + this.xSize();
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }


    public GuiTextField setMaxStringLength(int length) {
        this.maxStringLength = length;
        if (this.text().length() > length) {
            this.text(this.text().substring(0, length));
            this.notifyListeners(this.text());
        }
        return this;
    }


    private int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    private boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public GuiTextField setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) {
        this.enableBackgroundDrawing = enableBackgroundDrawingIn;
        return this;
    }

    public GuiTextField setTextColor(int color) {
        this.enabledColor = color;
        return this;
    }

    public GuiTextField setDisabledTextColour(int color) {
        this.disabledColor = color;
        return this;
    }

    //Seriously... WTF is up with this method?
    public boolean changeFocus(boolean noOp) {
        if (this.isFieldEnabled) {
            this.isFocused = !this.isFocused;
            this.onFocusedChanged(this.isFocused);
            return this.isFocused;
        }
        return false;
    }

    protected void onFocusedChanged(boolean newFocus) {
        if (newFocus) {
            this.cursorCounter = 0;
        }
        if (focusListener != null) {
            focusListener.accept(newFocus);
        }
    }


    public void setFocused(boolean isFocusedIn) {
        if (isFocusedIn != this.isFocused) {
            onFocusedChanged(isFocusedIn);
        }
        this.isFocused = isFocusedIn;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public GuiTextField setFieldEnabled(boolean enabled) {
        this.isFieldEnabled = enabled;
        return this;
    }

    public int getAdjustedWidth() {
        return this.getEnableBackgroundDrawing() ? this.xSize() - 8 : this.xSize();
    }

    public void setSelectionPos(int position) {
        int texLen = this.text().length();
        this.selectionEnd = MathHelper.clamp(position, 0, texLen);
        if (this.fontRenderer != null) {
            if (this.lineScrollOffset > texLen) {
                this.lineScrollOffset = texLen;
            }

            int j = this.getAdjustedWidth();
            String s = this.fontRenderer.trimStringToWidth(this.text().substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;
            if (this.selectionEnd == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRenderer.trimStringToWidth(this.text(), j, true).length();
            }

            if (this.selectionEnd > k) {
                this.lineScrollOffset += this.selectionEnd - k;
            } else if (this.selectionEnd <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - this.selectionEnd;
            }

            this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, texLen);
        }

    }

    public GuiTextField setCanLoseFocus(boolean canLoseFocusIn) {
        this.canLoseFocus = canLoseFocusIn;
        return this;
    }

    public GuiTextField setColours(int fillColour, int borderColour) {
        this.fillColour = fillColour;
        this.borderColour = borderColour;
        return this;
    }

    public void setSuggestion(@Nullable String p_195612_1_) {
        this.suggestion = p_195612_1_;
    }

    /**
     * This can be used to link this text field top an external value. Simply supply a supplyer that provides the external value and a consumer that sets the external value.
     */
    public GuiTextField setLinkedValue(Supplier<String> linkedGetter, Consumer<String> linkedSetter) {
        this.linkedGetter = linkedGetter;
        this.linkedSetter = linkedSetter;
        return this;
    }

    public GuiTextField setBlinkingCursor(boolean blinkCursor) {
        this.blinkCursor = blinkCursor;
        return this;
    }

    public boolean isBlinkCursor() {
        return blinkCursor;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawTextBox();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }


    @Override
    public boolean onUpdate() {
        if (super.onUpdate()) {
            return true;
        }

        if (blinkCursor) {
            ++this.cursorCounter;
        } else {
            this.cursorCounter = 0;
        }

        return false;
    }

    private String defaultTextStorage = "";
    private String text() {
        return linkedGetter == null ? defaultTextStorage : linkedGetter.get();
    }

    private void text(String text) {
        if (linkedSetter != null) {
            linkedSetter.accept(text);
        }
        else {
            this.defaultTextStorage = text;
        }
    }
}
