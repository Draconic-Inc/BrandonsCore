package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

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
    private boolean canFocus = true;
    private boolean isFocused;
    private boolean isFieldEnabled = true; //This is similar to the the disabled field in Button.
    private boolean blinkCursor = false;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int cursorColor = 0xffd0d0d0;
    private Supplier<Integer> textColourSupplier;
    private int disabledColor = 7368816;
    private boolean shadow = true;
    private Supplier<Boolean> shadowSupplier;

    private Consumer<String> changeListener;
    private Consumer<String> returnListener;
    private Consumer<Boolean> focusListener;
    private Predicate<String> validator = s -> true;
    private Consumer<String> linkedSetter;
    private Supplier<String> linkedGetter;

    private GuiColourProvider.HoverColour<Integer> fillColour = h -> 0xFF5f5f60;
    private GuiColourProvider.HoverColour<Integer> borderColour = h -> 0xFF000000;
    private boolean shiftCache;
    private String suggestion;

    public int textZOffset = 0;

    private BiFunction<String, Integer, String> textFormatter = (p_195610_0_, p_195610_1_) -> {
        return p_195610_0_;
    };
    private Runnable onFinishEdit;

    public GuiTextField() {}

    public GuiTextField(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTextField(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        lineScrollOffset = 0;
        this.updateCursor(0);
        if (!this.shiftCache) {
            this.setSelectionPos(this.cursorPosition);
        }
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


    public GuiTextField setTextAndNotify(String textIn) {
        boolean changed = !textIn.equals(getText());
        setText(textIn);
        if (changed) {
            this.notifyListeners(textIn);
        }
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
        }
        return this;
    }

    //Sets the text value without validation and without notifying listeners.
    public GuiTextField setTextQuietly(String newText) {
        if (newText.length() > this.maxStringLength) {
            this.text(newText.substring(0, this.maxStringLength));
        } else {
            this.text(newText);
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

    public GuiTextField setValidator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    public void writeText(String textToWrite) {
        String s = "";
        String s1 = SharedConstants.filterText(textToWrite);
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

//        this.notifyListeners(this.text());//No this should not be a thing
    }

    public void updateCursor(int pos) {
        this.cursorPosition = Mth.clamp(pos, 0, this.text().length());
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text().length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isEditing()) {
            return false;
        } else {
            this.shiftCache = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.setCursorPositionEnd();
                this.setSelectionPos(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.isFieldEnabled) {
                    this.writeText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                if (this.isFieldEnabled) {
                    this.writeText("");
                }

                return true;
            } else {
                switch (keyCode) {
                    case 257:
                        if (onFinishEdit != null) {
                            onFinishEdit.run();
                        }
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
                        //Consume key presses when we are typing so we dont do something dumb like close the screen when you type e
                        return keyCode != GLFW.GLFW_KEY_ESCAPE;
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
        }
    }

    public boolean isEditing() {
        return /*this.isEnabled() && */this.isFocused() && this.isFieldEnabled;
    }

    @Override
    public boolean charTyped(char charType, int charCode) {
        if (!this.isEditing()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(charType)) {
            if (this.isFieldEnabled) {
                this.writeText(Character.toString(charType));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean mouseOver = isMouseOver(mouseX, mouseY);
        if (mouseOver && canFocus) {
            this.setFocused(true);
        } else if (!mouseOver && canLoseFocus) {
            this.setFocused(false);
        }

        if (this.isFocused() && mouseOver && button == 0) {
            int i = Mth.floor(mouseX) - this.xPos();
            if (this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = this.fontRenderer.plainSubstrByWidth(this.text().substring(this.lineScrollOffset), this.getAdjustedWidth());
            this.setCursorPosition(this.fontRenderer.plainSubstrByWidth(s, i).length() + this.lineScrollOffset);
            return true;
        } else {
            return false;
        }
    }

    //Ensure that focus is removed even if the normal click event is consumed by another element.
    @Override
    public void globalClick(double mouseX, double mouseY, int button) {
        super.globalClick(mouseX, mouseY, button);
        if (isFocused() && canLoseFocus && !isMouseOver(mouseX, mouseY)) {
            setFocused(false);
        }
    }

//    public void setFocused(boolean isFocused) {
//        if (this.isFocused && !isFocused && onFinishEdit != null) {
//            onFinishEdit.run();
//        }
//        this.isFocused = isFocused;
////        if (!isFocused) setCursorPosition(0);
//    }

    public void drawTextBox(PoseStack matrixStack, boolean mouseOver) {
        BrandonsCore.LOGGER.info("FIIIIIX MEEEEEE GuiTextField");
////        if (this.isEnabled()) {
//        if (this.getEnableBackgroundDrawing()) {
//            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, getFillColour(mouseOver), getBorderColour(mouseOver));
//        }
//
//        double zLevel = getRenderZLevel();
//
//        int i = this.isFieldEnabled ? getTextColor() : this.disabledColor;
//        int j = this.cursorPosition - this.lineScrollOffset;
//        int k = this.selectionEnd - this.lineScrollOffset;
//        String s = this.fontRenderer.plainSubstrByWidth(this.text().substring(this.lineScrollOffset), this.getAdjustedWidth());
//        boolean flag = j >= 0 && j <= s.length();
//        boolean flag1 = this.isFocused() && this.cursorCounter / 10 % 2 == 0 && flag;
//        int stringX = this.enableBackgroundDrawing ? this.xPos() + 4 : this.xPos();
//        int stringY = this.enableBackgroundDrawing ? this.yPos() + (this.ySize() - 8) / 2 : this.yPos();
//        int j1 = stringX;
//        if (k > s.length()) {
//            k = s.length();
//        }
//
//        matrixStack.translate(0, 0, textZOffset + zLevel);
//        if (!s.isEmpty()) {
//            String s1 = flag ? s.substring(0, j) : s;
//            if (getShadow()) {
//                j1 = this.fontRenderer.drawShadow(matrixStack, this.textFormatter.apply(s1, this.lineScrollOffset), (float) stringX, (float) stringY, i);
//            } else {
//                j1 = this.fontRenderer.draw(matrixStack, this.textFormatter.apply(s1, this.lineScrollOffset), (float) stringX, (float) stringY, i);
//            }
//        }
//
//        boolean flag2 = this.cursorPosition < this.text().length() || this.text().length() >= this.getMaxStringLength();
//        int k1 = j1;
//        if (!flag) {
//            k1 = j > 0 ? stringX + this.xSize() : stringX;
//        } else if (flag2) {
//            k1 = j1 - 1;
//            if (getShadow()) {
//                --j1;
//            }
//        }
//
//        if (!s.isEmpty() && flag && j < s.length()) {
//            if (getShadow()) {
//                this.fontRenderer.drawShadow(matrixStack, this.textFormatter.apply(s.substring(j), this.cursorPosition), (float) j1, (float) stringY, i);
//            } else {
//                this.fontRenderer.draw(matrixStack, this.textFormatter.apply(s.substring(j), this.cursorPosition), (float) j1, (float) stringY, i);
//            }
//        }
//
//        if (!flag2 && this.suggestion != null) {
//            if (getShadow()) {
//                this.fontRenderer.drawShadow(matrixStack, this.suggestion, (float) (k1 - 1), (float) stringY, 0xff808080);
//            } else {
//                this.fontRenderer.draw(matrixStack, this.suggestion, (float) (k1 - 1), (float) stringY, 0xff808080);
//            }
//        }
//
//        if (flag1) {
//            if (flag2) {
//                GuiComponent.fill(matrixStack, k1, stringY - 1, k1 + 1, stringY + 1 + 9, cursorColor);
//            } else {
//                if (getShadow()) {
//                    this.fontRenderer.drawShadow(matrixStack, "_", (float) k1, (float) stringY, i);
//                } else {
//                    this.fontRenderer.draw(matrixStack, "_", (float) k1, (float) stringY, i);
//                }
//            }
//        }
//
//        if (k != j) {
//            int l1 = stringX + this.fontRenderer.width(s.substring(0, k));
//            this.drawSelectionBox(k1, stringY - 1, l1 - 1, stringY + 1 + 9);
//        }
//        matrixStack.translate(0, 0, -(textZOffset + zLevel));
    }

    private void drawSelectionBox(int startX, int startY, int endX, int endY) {
//        if (startX < endX) {
//            int i = startX;
//            startX = endX;
//            endX = i;
//        }
//
//        if (startY < endY) {
//            int j = startY;
//            startY = endY;
//            endY = j;
//        }
//
//        if (endX > this.xPos() + this.xSize()) {
//            endX = this.xPos() + this.xSize();
//        }
//
//        if (startX > this.xPos() + this.xSize()) {
//            startX = this.xPos() + this.xSize();
//        }
//
//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder bufferbuilder = tessellator.getBuilder();
//        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
//        RenderSystem.disableTexture();
//        RenderSystem.enableColorLogicOp();
//        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
//        bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
//        bufferbuilder.vertex(startX, endY, 0.0D).endVertex();
//        bufferbuilder.vertex(endX, endY, 0.0D).endVertex();
//        bufferbuilder.vertex(endX, startY, 0.0D).endVertex();
//        bufferbuilder.vertex(startX, startY, 0.0D).endVertex();
//        tessellator.end();
//        RenderSystem.disableColorLogicOp();
//        RenderSystem.enableTexture();
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

    public GuiTextField setTextColor(Supplier<Integer> color) {
        this.textColourSupplier = color;
        return this;
    }

    public GuiTextField setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
        return this;
    }

    public GuiTextField setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public GuiTextField setShadowSupplier(Supplier<Boolean> shadowSupplier) {
        this.shadowSupplier = shadowSupplier;
        return this;
    }

    public boolean getShadow() {
        return shadowSupplier == null ? shadow : shadowSupplier.get();
    }

    public int getTextColor() {
        return textColourSupplier == null ? enabledColor : textColourSupplier.get();
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
        if (!newFocus && onFinishEdit != null) {
            onFinishEdit.run();
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
        this.selectionEnd = Mth.clamp(position, 0, texLen);
        if (this.fontRenderer != null) {
            if (this.lineScrollOffset > texLen) {
                this.lineScrollOffset = texLen;
            }

            int j = this.getAdjustedWidth();
            String s = this.fontRenderer.plainSubstrByWidth(this.text().substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;
            if (this.selectionEnd == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRenderer.plainSubstrByWidth(this.text(), j, true).length();
            }

            if (this.selectionEnd > k) {
                this.lineScrollOffset += this.selectionEnd - k;
            } else if (this.selectionEnd <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - this.selectionEnd;
            }

            this.lineScrollOffset = Mth.clamp(this.lineScrollOffset, 0, texLen);
        }

    }

    public GuiTextField setCanFocus(boolean canFocus) {
        this.canFocus = canFocus;
        return this;
    }

    public GuiTextField setCanLoseFocus(boolean canLoseFocusIn) {
        this.canLoseFocus = canLoseFocusIn;
        return this;
    }

    public GuiTextField setColours(int fillColour, int borderColour) {
        this.fillColour = h -> fillColour;
        this.borderColour = h -> borderColour;
        return this;
    }

    public int getFillColour(boolean hovering) {
        return fillColour.getColour(hovering);
    }

    public int getBorderColour(boolean hovering) {
        return borderColour.getColour(hovering);
    }

    public GuiTextField setFillColour(GuiColourProvider.HoverColour<Integer> fillColour) {
        this.fillColour = fillColour;
        return this;
    }

    public GuiTextField setBorderColour(GuiColourProvider.HoverColour<Integer> borderColour) {
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
        drawTextBox(new PoseStack(), isMouseOver(mouseX, mouseY));
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
        } else {
            this.defaultTextStorage = text;
        }
    }

    public void onFinishEdit(Runnable onFinishEdit) {
        this.onFinishEdit = onFinishEdit;
    }
}
