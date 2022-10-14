package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 26/05/2022
 */
public class GuiTextField extends GuiElement<GuiTextField> {
    private int cursorPos;
    private int maxLength = 32;
    private int displayPos;
    private int highlightPos;
    private boolean focused;
    private boolean isEditable = true;
    private boolean isFocusable = true;
    private boolean shiftPressed;
    private boolean canLoseFocus = true;


    private Consumer<String> changeListener;
    private Consumer<String> returnListener;
    private Consumer<Boolean> focusListener;
    private Runnable onFinishEdit;

    private String _value_ = ""; //Never edit or read this directly
    private Consumer<String> setValue = s -> _value_ = s;
    private Supplier<String> getValue = () -> _value_;
    private Supplier<String> suggestion = null;

    private Supplier<Integer> textColor = () -> 0xe0e0e0;
    private Supplier<Integer> textColorUneditable = () -> 0x707070;
    private Supplier<Boolean> shadow = () -> true;

    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (string, pos) -> {
        return FormattedCharSequence.forward(string, Style.EMPTY);
    };

    public GuiTextField() {
        getInsets().left = 4;
    }

    public GuiTextField(String defaultText) {
        this();
        _setValue(defaultText);
    }

    //## Setup ##

    @Deprecated //This is a bit crashy when combined with client controlled managed values. I need to find a fix.
    public GuiTextField linkExternalValue(Supplier<String> getValue, Consumer<String> setValue) {
        this.setValue = setValue;
        this.getValue = getValue;
        return this;
    }

    public GuiTextField onValueChanged(Consumer<String> changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public GuiTextField onValueChanged(Runnable changeListener) {
        return onValueChanged(e -> changeListener.run());
    }

    public GuiTextField onReturnPressed(Consumer<String> returnListener) {
        this.returnListener = returnListener;
        return this;
    }

    public GuiTextField onReturnPressed(Runnable returnListener) {
        return onReturnPressed(e -> returnListener.run());
    }

    public GuiTextField onFocusChanged(Consumer<Boolean> focusListener) {
        this.focusListener = focusListener;
        return this;
    }

    public GuiTextField setSuggestion(String suggestion) {
        return setSuggestion(suggestion == null || suggestion.isEmpty() ? null : () -> suggestion);
    }

    public GuiTextField setSuggestion(@Nullable Supplier<String> suggestion) {
        this.suggestion = suggestion;
        return this;
    }

    public GuiTextField setFilter(Predicate<String> filter) {
        this.filter = filter;
        return this;
    }

    public GuiTextField setFormatter(BiFunction<String, Integer, FormattedCharSequence> formatter) {
        this.formatter = formatter;
        return this;
    }

    public GuiTextField setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
        return this;
    }

    public GuiTextField setTextColor(Supplier<Integer> textColor) {
        this.textColor = textColor;
        return this;
    }

    public GuiTextField setTextColor(int textColor) {
        return setTextColor(() -> textColor);
    }

    public GuiTextField setTextColorUneditable(Supplier<Integer> textColorUneditable) {
        this.textColorUneditable = textColorUneditable;
        return this;
    }

    public GuiTextField setTextColorUneditable(int textColorUneditable) {
        return setTextColorUneditable(() -> textColorUneditable);
    }

    public GuiTextField setShadow(Supplier<Boolean> shadow) {
        this.shadow = shadow;
        return this;
    }

    public GuiTextField setShadow(boolean shadow) {
        return setShadow(() -> shadow);
    }

    public GuiTextField addBackground(GuiColourProvider.HoverColour<Integer> fillColour, GuiColourProvider.HoverColour<Integer> borderColour) {
        GuiBorderedRect bg = new GuiBorderedRect()
                .setFillColourL(fillColour)
                .setBorderColourL(borderColour);
        bg.bindPosition(this);
        bg.bindSize(this, false);
        addChild(bg);
        return this;
    }

    public GuiTextField addBackground(int fillColour, int borderColour) {
        return addBackground(e -> fillColour, e -> borderColour);
    }

    public GuiTextField addBackground(Supplier<Integer> fillColour, Supplier<Integer> borderColour) {
        return addBackground(e -> fillColour.get(), e -> borderColour.get());
    }

    public GuiTextField onFinishEdit(Runnable onFinishEdit) {
        this.onFinishEdit = onFinishEdit;
        return this;
    }

    public GuiTextField setFocusable(boolean focusable) {
        this.isFocusable = focusable;
        return this;
    }

    //## Text field logic ##

    public GuiTextField setValue(String newValue) {
        if (this.filter.test(newValue)) {
            if (newValue.length() > maxLength) {
                _setValue(newValue.substring(0, maxLength));
            } else {
                _setValue(newValue);
            }

            moveCursorToEnd();
            setHighlightPos(cursorPos);
            onValueChange(newValue);
        }
        return this;
    }

    public GuiTextField setValueQuietly(String newValue) {
        if (this.filter.test(newValue)) {
            if (newValue.length() > maxLength) {
                _setValue(newValue.substring(0, maxLength));
            } else {
                _setValue(newValue);
            }

            moveCursorToEnd(false);
            setHighlightPos(cursorPos);
        }
        return this;
    }

    public String getValue() {
        return getValue.get();
    }

    //Internal set value
    private void _setValue(String newValue) {
        setValue.accept(newValue);
    }

    public String getHighlighted() {
        int i = Math.min(cursorPos, highlightPos);
        int j = Math.max(cursorPos, highlightPos);
        return getValue().substring(i, j);
    }

    public void insertText(String text) {
        String value = getValue();
        int selectStart = Math.min(cursorPos, highlightPos);
        int selectEnd = Math.max(cursorPos, highlightPos);
        int freeSpace = maxLength - value.length() - (selectStart - selectEnd);
        String toInsert = SharedConstants.filterText(text);
        int insertLen = toInsert.length();
        if (freeSpace < insertLen) {
            toInsert = toInsert.substring(0, freeSpace);
            insertLen = freeSpace;
        }

        String newValue = (new StringBuilder(value)).replace(selectStart, selectEnd, toInsert).toString();
        if (filter.test(newValue)) {
            _setValue(newValue);
            setCursorPosition(selectStart + insertLen);
            setHighlightPos(cursorPos);
            onValueChange(newValue);
        }
    }

    private void onValueChange(String newValue) {
        if (changeListener != null) {
            changeListener.accept(newValue);
        }
    }

    private void deleteText(int i) {
        if (Screen.hasControlDown()) {
            deleteWords(i);
        } else {
            deleteChars(i);
        }
    }

    public void deleteWords(int i) {
        if (!getValue().isEmpty()) {
            if (highlightPos != cursorPos) {
                insertText("");
            } else {
                deleteChars(getWordPosition(i) - cursorPos);
            }
        }
    }

    public void deleteChars(int i1) {
        String value = getValue();
        if (!value.isEmpty()) {
            if (highlightPos != cursorPos) {
                insertText("");
            } else {
                int i = getCursorPos(i1);
                int j = Math.min(i, cursorPos);
                int k = Math.max(i, cursorPos);
                if (j != k) {
                    String s = (new StringBuilder(value)).delete(j, k).toString();
                    if (filter.test(s)) {
                        _setValue(s);
                        moveCursorTo(j);
                    }
                }
            }
        }
    }

    public int getWordPosition(int i) {
        return getWordPosition(i, getCursorPosition());
    }

    private int getWordPosition(int i, int i1) {
        return getWordPosition(i, i1, true);
    }

    private int getWordPosition(int i1, int i2, boolean b) {
        String value = getValue();
        int i = i2;
        boolean flag = i1 < 0;
        int j = Math.abs(i1);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = value.length();
                i = value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (b && i < l && value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (b && i > 0 && value.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int pos) {
        moveCursorTo(getCursorPos(pos));
    }

    private int getCursorPos(int i) {
        return Util.offsetByCodepoints(getValue(), cursorPos, i);
    }

    public void moveCursorTo(int pos, boolean notify) {
        setCursorPosition(pos);
        if (!shiftPressed) {
            setHighlightPos(cursorPos);
        }

        if (notify) {
            onValueChange(getValue());
        }
    }

    public void moveCursorTo(int pos) {
        moveCursorTo(pos, true);
    }

    public void setCursorPosition(int pos) {
        cursorPos = Mth.clamp(pos, 0, getValue().length());
    }

    public void moveCursorToStart() {
        moveCursorTo(0);
    }

    public void moveCursorToEnd(boolean notify) {
        moveCursorTo(getValue().length(), notify);
    }

    public void moveCursorToEnd() {
        moveCursorToEnd(true);
    }

    private boolean isEditable() {
        return isEditable;
    }

    public GuiTextField setEditable(boolean editable) {
        this.isEditable = editable;
        return this;
    }

    public void setFocus(boolean focused) {
        if (this.focused && !focused && onFinishEdit != null) {
            onFinishEdit.run();
        }
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }

    public GuiTextField setMaxLength(int newWidth) {
        String value = getValue();
        maxLength = newWidth;
        if (value.length() > newWidth) {
            _setValue(value.substring(0, newWidth));
            onValueChange(value);
        }
        return this;
    }

    private int getMaxLength() {
        return maxLength;
    }

    public int getCursorPosition() {
        return cursorPos;
    }

    public int getInnerWidth() {
        return xSize() - getInsets().left - getInsets().right;
    }

    public void setHighlightPos(int p_94209_) {
        String value = getValue();
        int i = value.length();
        highlightPos = Mth.clamp(p_94209_, 0, i);
        if (fontRenderer != null) {
            if (displayPos > i) {
                displayPos = i;
            }

            int j = getInnerWidth();
            String s = fontRenderer.plainSubstrByWidth(value.substring(displayPos), j);
            int k = s.length() + displayPos;
            if (highlightPos == displayPos) {
                displayPos -= fontRenderer.plainSubstrByWidth(value, j, true).length();
            }

            if (highlightPos > k) {
                displayPos += highlightPos - k;
            } else if (highlightPos <= displayPos) {
                displayPos -= displayPos - highlightPos;
            }

            displayPos = Mth.clamp(displayPos, 0, i);
        }
    }


    //## Input Handling ##

    @Override
    protected boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!canConsumeInput()) {
            return false;
        } else {
            shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                moveCursorToEnd();
                setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getHighlighted());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (isEditable) {
                    insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getHighlighted());
                if (isEditable) {
                    insertText("");
                }

                return true;
            } else {
                switch (keyCode) {
                    case 259:
                        if (isEditable) {
                            shiftPressed = false;
                            deleteText(-1);
                            shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 257: {
                        if (onFinishEdit != null) {
                            onFinishEdit.run();
                        }
                        if (returnListener != null) {
                            returnListener.accept(getValue());
                            return true;
                        }
                    }
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        //Consume key presses when we are typing so we dont do something dumb like close the screen when you type e
                        return keyCode != GLFW.GLFW_KEY_ESCAPE;
                    case 261:
                        if (isEditable) {
                            shiftPressed = false;
                            deleteText(1);
                            shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            moveCursorTo(getWordPosition(1));
                        } else {
                            moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            moveCursorTo(getWordPosition(-1));
                        } else {
                            moveCursor(-1);
                        }

                        return true;
                    case 268:
                        moveCursorToStart();
                        return true;
                    case 269:
                        moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean canConsumeInput() {
        return isFocused() && isEditable() && isEnabled();
    }

    @Override
    public boolean charTyped(char charTyped, int charCode) {
        if (!canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(charTyped)) {
            if (isEditable) {
                insertText(Character.toString(charTyped));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = isMouseOver(mouseX, mouseY);
        if (canLoseFocus) {
            setFocus(clicked && isFocusable);
        } else {
            setFocus(isFocusable);
        }

        if (isFocused() && clicked && button == 0) {
            int i = Mth.floor(mouseX) - xPos();
            i -= getInsets().left;

            String s = fontRenderer.plainSubstrByWidth(getValue().substring(displayPos), getInnerWidth());
            moveCursorTo(fontRenderer.plainSubstrByWidth(s, i).length() + displayPos);
            return true;
        } else {
            return false;
        }
    }

    //Ensure that focus is removed even if the normal click event is consumed by another element.
    @Override
    public void globalClick(double mouseX, double mouseY, int button) {
        super.globalClick(mouseX, mouseY, button);
        if (isFocused() && !isMouseOver(mouseX, mouseY)) {
            setFocus(isFocusable && !canLoseFocus);
        }
    }

    //## Rendering ##

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0, 0, getRenderZLevel());

        //Draw Border
//        if (isBordered()) {
//            int i = isFocused() ? BORDER_COLOR_FOCUSED : BORDER_COLOR;
//            fill(poseStack, x - 1, y - 1, x + width + 1, y + height + 1, i);
//            fill(poseStack, x, y, x + width, y + height, BACKGROUND_COLOR);
//        }

        String value = getValue();

        int colour = isEditable ? textColor.get() : textColorUneditable.get();
        int textStart = cursorPos - displayPos;
        int highlightStart = highlightPos - displayPos;
        String displayText = fontRenderer.plainSubstrByWidth(value.substring(displayPos), getInnerWidth());
        boolean flag = textStart >= 0 && textStart <= displayText.length();
        boolean flag1 = isFocused() && TimeKeeper.getClientTick() / 6 % 2 == 0 && flag;
        int l = xPos() + getInsets().left;
//        int i1 = bordered ? yPos() + ((ySize() - 8) / 2) : yPos();
        int i1 = yPos() + ((ySize() - 8) / 2); //<- Render centered on Y??? why wouldn't I want this?
        int j1 = l;
        if (highlightStart > displayText.length()) {
            highlightStart = displayText.length();
        }

        if (!displayText.isEmpty()) {
            String s1 = flag ? displayText.substring(0, textStart) : displayText;
            if (shadow.get()) {
                j1 = fontRenderer.drawShadow(poseStack, formatter.apply(s1, displayPos), (float) l, (float) i1, colour);
            } else {
                j1 = fontRenderer.draw(poseStack, formatter.apply(s1, displayPos), (float) l, (float) i1, colour);
            }
        }

        boolean flag2 = cursorPos < value.length() || value.length() >= getMaxLength();
        int k1 = j1;
        if (!flag) {
            k1 = textStart > 0 ? l + xSize() : l;
        } else if (flag2) {
            k1 = j1 - 1;
            --j1;
        }

        if (!displayText.isEmpty() && flag && textStart < displayText.length()) {
            if (shadow.get()) {
                fontRenderer.drawShadow(poseStack, formatter.apply(displayText.substring(textStart), cursorPos), (float) j1, (float) i1, colour);
            } else {
                fontRenderer.draw(poseStack, formatter.apply(displayText.substring(textStart), cursorPos), (float) j1, (float) i1, colour);
            }
        }

        if (suggestion != null && value.isEmpty()) {
            if (shadow.get()) {
                fontRenderer.drawShadow(poseStack, suggestion.get(), (float) (k1 - 1), (float) i1, -8355712);
            } else {
                fontRenderer.draw(poseStack, suggestion.get(), (float) (k1 - 1), (float) i1, -8355712);
            }
        }

        if (flag1) {
            if (flag2) {
                GuiComponent.fill(poseStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
            } else {
                if (shadow.get()) {
                    fontRenderer.drawShadow(poseStack, "_", (float) k1, (float) i1, colour);
                } else {
                    fontRenderer.draw(poseStack, "_", (float) k1, (float) i1, colour);
                }
            }
        }

        if (highlightStart != textStart) {
            int l1 = l + fontRenderer.width(displayText.substring(0, highlightStart));
            renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
        }
    }

    private void renderHighlight(int left, int top, int right, int bottom) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        if (right > xPos() + xSize()) {
            right = xPos() + xSize();
        }

        if (left > xPos() + xSize()) {
            left = xPos() + xSize();
        }

        double renderZ = getRenderZLevel();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(left, bottom, renderZ).endVertex();
        bufferbuilder.vertex(right, bottom, renderZ).endVertex();
        bufferbuilder.vertex(right, top, renderZ).endVertex();
        bufferbuilder.vertex(left, top, renderZ).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }
}
