package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.lib.ScissorHelper;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GuiTextArea extends MGuiElementBase<GuiTextArea> {

    private int placeHolderColor = Color.GRAY.getRGB();
    private String placeHolder = "";
    private List<String> lines = new ArrayList<>();
    private int currentLine;
    private int maxLineStringLength = 64;
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
    private Consumer<String> changeListener;
    private Consumer<Boolean> focusListener;
    private Predicate<String> validator = s -> true;
    public int fillColour = 0xFF5f5f60;
    public int borderColour = 0xFF000000;

    private GuiSlideControl scrollBar = null;
    private boolean scrollEnable;
    private int editAreaWidth;
    private double contentHeight;

    public GuiTextArea() {
    }

    public GuiTextArea(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTextArea(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    public GuiTextArea setPlaceHolderColor(int placeHolderColor) {
        this.placeHolderColor = placeHolderColor;
        return this;
    }

    public GuiTextArea setPlaceHolder(String placeHolder) {
        this.placeHolder = Strings.nullToEmpty(placeHolder);
        return this;
    }

    public GuiTextArea setChangeListener(Consumer<String> changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public GuiTextArea setChangeListener(Runnable changeListener) {
        return setChangeListener(s -> changeListener.run());
    }

    public GuiTextArea setFocusListener(Consumer<Boolean> focusListener) {
        this.focusListener = focusListener;
        return this;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public GuiTextArea setLineText(String textIn, int line) {
        if (line >= 0 && line < this.lines.size() && this.validator.test(textIn)) {
            this.lines.set(line, textIn.length() > this.maxLineStringLength ? textIn.substring(0, this.maxLineStringLength) : textIn);
            this.setCursorPositionEnd();
        }
        return this;
    }

    public GuiTextArea forceSetText(String textIn) {
        String[] split = textIn.split("\n");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.length() > this.maxLineStringLength) {
                split[i] = s.substring(0, this.maxLineStringLength);
            }
        }
        this.lines = new ArrayList<>(Arrays.asList(split));

        this.setCursorPositionEnd();

        return this;
    }

    public int getLines() {
        return this.lines.size();
    }

    public String getCurrentLineText() {
        boolean change = false;
        while (this.lines.size() < this.currentLine + 1) {
            this.lines.add("");
            change = true;
        }
        if (change) {
            this.updateScrollBar();
        }
        return this.lines.get(this.currentLine);
    }

    public String getText(int line) {
        return this.lines.size() > line ? this.lines.get(line) : null;
    }

    public List<String> getAllText() {
        return Collections.unmodifiableList(new ArrayList<>(this.lines));
    }

    private void trimTailEmptyLines() {
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (i == this.currentLine) {
                break;
            }
            String line = lines.get(i);
            if (Strings.isNullOrEmpty(line)) {
                lines.remove(i);
            } else {
                break;
            }
        }
        if (this.currentLine >= this.lines.size()) {
            this.moveCurrentLineTo(this.lines.size() - 1);
        }
        this.updateScrollBar();
    }

    public String getSelectedText() {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return getCurrentLineText().substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator) {
        this.validator = theValidator;
    }

    public void writeText(String textToWrite) {
        String newStr = "";
        String line = getCurrentLineText();
        String[] split = textToWrite.split("\n");
        textToWrite = split[0];
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxLineStringLength - line.length() - (i - j);

        if (!line.isEmpty()) {
            newStr = newStr + line.substring(0, i);
        }

        int l;

        if (k < textToWrite.length()) {
            newStr = newStr + textToWrite.substring(0, k);
            l = k;
        } else {
            newStr = newStr + textToWrite;
            l = textToWrite.length();
        }

        if (!line.isEmpty() && j < line.length()) {
            newStr = newStr + line.substring(j);
        }

        if (split.length > 1) {
            for (int m = split.length - 1; m > 0; m--) {
                String s = ChatAllowedCharacters.filterAllowedCharacters(split[m]);
                if (this.validator.test(s)) {
                    this.lines.add(this.currentLine + 1, s);
                }
            }
        }

        newStr = ChatAllowedCharacters.filterAllowedCharacters(newStr);
        if (this.validator.test(newStr)) {
            this.lines.set(this.currentLine, newStr);
            this.moveCursorBy(i - this.selectionEnd + l);

            if (changeListener != null) {
                changeListener.accept(Joiner.on('\n').join(this.lines));
            }
        }
    }

    public void deleteWords(int num) {
        if (!getCurrentLineText().isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int num) {
        String rawline = getCurrentLineText();
        String line = rawline;
        if (!line.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";

                if (i >= 0) {
                    s = line.substring(0, i);
                }

                if (j < line.length()) {
                    s = s + line.substring(j);
                }

                if (this.validator.test(s)) {
                    line = s;

                    if (flag) {
                        this.moveCursorBy(num);
                    }

                    if (changeListener != null) {
                        changeListener.accept(Joiner.on('\n').join(this.lines));
                    }
                }
            }
        }
        if (line != rawline) {
            this.lines.set(this.currentLine, line);
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
        String line = getCurrentLineText();
        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = line.length();
                i = line.indexOf(' ', i);

                if (i == -1) {
                    i = l;
                } else {
                    while (skipWs && i < l && line.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipWs && i > 0 && line.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && line.charAt(i - 1) != ' ') {
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
        int i = getCurrentLineText().length();
        this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(getCurrentLineText().length());
    }

    private void moveCurrentLineBy(int num) {
        this.moveCurrentLineTo(this.currentLine + num);
    }

    private void moveCurrentLineTo(int newLine) {
        this.currentLine = MathHelper.clamp(newLine, 0, this.lines.size() - 1);
        if (this.scrollEnable) {
            int ySize = this.enableBackgroundDrawing ? getInsetRect().height - 8 : getInsetRect().height;
            int currentY = this.currentLine * (4 + this.fontRenderer.FONT_HEIGHT);
            if (currentY < this.getScrollPos()) {
                this.scrollBar.updateRawPos(currentY / (this.contentHeight - ySize));
            } else {
                double diff = (currentY + this.fontRenderer.FONT_HEIGHT + 1) - (this.getScrollPos() + ySize);
                if (diff > 0) {
                    this.scrollBar.updateRawPos(this.scrollBar.getRawPos() + diff / (this.contentHeight - ySize));
                }
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case Keyboard.KEY_RETURN:
                    this.lines.add(this.currentLine + 1, "");
                    this.moveCurrentLineBy(1);
                    this.lineScrollOffset = 0;
                    this.setCursorPositionZero();
                    this.updateScrollBar();
                    return true;
                case Keyboard.KEY_BACK:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (this.isEnabled) {
                            this.lines.remove(this.currentLine);
                            if (this.currentLine > 0) {
                                this.moveCurrentLineBy(-1);
                            }
                            this.lineScrollOffset = 0;
                            this.setCursorPositionEnd();
                            this.updateScrollBar();
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;
                case Keyboard.KEY_HOME:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }
                    return true;
                case Keyboard.KEY_UP:
                    if (this.currentLine > 0) {
                        this.moveCurrentLineBy(-1);
                        this.lineScrollOffset = 0;
                        int maxLength = getCurrentLineText().length();
                        if (this.cursorPosition > maxLength) {
                            this.cursorPosition = maxLength;
                        }
                        this.selectionEnd = this.cursorPosition;
                    }
                    return true;
                case Keyboard.KEY_DOWN:
                    if (this.currentLine < this.lines.size() - 1) {
                        this.moveCurrentLineBy(1);
                        this.lineScrollOffset = 0;
                        int maxLength = getCurrentLineText().length();
                        if (this.cursorPosition > maxLength) {
                            this.cursorPosition = maxLength;
                        }
                        this.selectionEnd = this.cursorPosition;
                    }
                    return true;
                case Keyboard.KEY_LEFT:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                case Keyboard.KEY_RIGHT:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;
                case Keyboard.KEY_END:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(getCurrentLineText().length());
                    } else {
                        this.setCursorPositionEnd();
                    }
                    return true;
                case Keyboard.KEY_DELETE:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }
                    return true;
                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }
                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        if (this.scrollEnable) {
            return this.scrollBar.handleMouseScroll(mouseX, mouseY, scrollDirection);
        }
        return false;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.scrollEnable) {
            this.scrollBar.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.scrollEnable) {
            if (this.scrollBar.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        boolean mouseOver = isMouseOver(mouseX, mouseY);

        if (this.canLoseFocus) {
            this.setFocused(mouseOver);
        }

        if (this.isFocused && mouseOver && mouseButton == 0) {
            int i = mouseX - xPos();
            int y = mouseY - yPos();

            if (this.enableBackgroundDrawing) {
                i -= 4;
                y -= 4;
            }
            if (this.scrollEnable) {
                y += this.getScrollPos() + 4;
            }

            int line = y / (4 + this.fontRenderer.FONT_HEIGHT);
            if (line >= this.lines.size()) {
                line = this.lines.size() - 1;
            }
            if (line < 0) {
                line = 0;
            }
            if (this.currentLine != line) {
                this.moveCurrentLineTo(line);
                this.lineScrollOffset = 0;
                this.cursorCounter = 0;
            }

            String s = fontRenderer.trimStringToWidth(getCurrentLineText().substring(this.lineScrollOffset), this.editAreaWidth);
            this.setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + this.lineScrollOffset);
        }

        return mouseOver || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int startX = this.enableBackgroundDrawing ? getInsetRect().x + 4 : getInsetRect().x;
        int startY = this.enableBackgroundDrawing ? getInsetRect().y + 4 : getInsetRect().y;
        int xSize = getInsetRect().width;
        int ySize = getInsetRect().height;

        double yResScale = minecraft.displayHeight / (double) screenHeight;
        double xResScale = minecraft.displayWidth / (double) screenWidth;
        double scaledWidth = xSize * xResScale;
        double scaledHeight = this.enableBackgroundDrawing ? (ySize - 8) * yResScale : ySize * yResScale;
        int x = (int) ((startX - 1) * xResScale);
        int y = (int) (minecraft.displayHeight - (startY * yResScale) - scaledHeight);

        if (this.getEnableBackgroundDrawing()) {
            drawBorderedRect(xPos(), yPos(), this.scrollEnable ? xSize - 10 : xSize, ySize, 1, fillColour, borderColour);
        }
        ScissorHelper.pushScissor(x, y, (int) scaledWidth, (int) scaledHeight);
        drawTextBox(startX, startY);
        ScissorHelper.popScissor();
        if (this.scrollBar != null) {
            this.scrollBar.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    private void drawTextBox(int startX, int startY) {
        if (this.lines.isEmpty() && !Strings.isNullOrEmpty(this.placeHolder) && !this.isFocused) {
            drawString(fontRenderer, this.placeHolder, startX, startY, this.placeHolderColor, false);
            return;
        }

        int color = this.isEnabled ? this.enabledColor : this.disabledColor;
        int currentY = startY;
        if (this.scrollEnable) {
            currentY -= this.getScrollPos();
        }
        if (this.isFocused) {
            for (int i = 0; i < this.lines.size(); i++, currentY += 4 + this.fontRenderer.FONT_HEIGHT) {
                if (currentY + this.fontRenderer.FONT_HEIGHT < startY) {
                    continue;
                }
                if (i == this.currentLine) {
                    String rawLine = this.lines.get(i);
                    String line = this.fontRenderer.trimStringToWidth(rawLine.substring(this.lineScrollOffset), this.editAreaWidth);

                    int physicsCursorPosition = this.cursorPosition - this.lineScrollOffset;
                    int physicsSelectionEnd = this.selectionEnd - this.lineScrollOffset;

                    boolean cursorValid = physicsCursorPosition >= 0 && physicsCursorPosition <= line.length();
                    boolean shouldDrawCursor = this.isFocused && this.cursorCounter / 6 % 2 == 0 && cursorValid;

                    int xAfterCursor = startX;
                    if (physicsSelectionEnd > line.length()) {
                        physicsSelectionEnd = line.length();
                    }

                    if (!line.isEmpty()) {
                        String strBeforeCursor = this.isFocused && cursorValid ? line.substring(0, physicsCursorPosition) : line;
                        xAfterCursor = drawString(fontRenderer, strBeforeCursor, startX, currentY, color, false);
                    }

                    boolean cursorInWords = this.cursorPosition < rawLine.length() || rawLine.length() >= this.getMaxLineStringLength();
                    int xCursor = xAfterCursor;

                    if (!cursorValid) {
                        xCursor = physicsCursorPosition > 0 ? startX + this.fontRenderer.getStringWidth(line) : startX;
                    } else if (cursorInWords) {
                        xCursor = xAfterCursor - 1;
                    }

                    if (this.isFocused && !line.isEmpty() && cursorValid && physicsCursorPosition < line.length()) {
                        drawString(fontRenderer, line.substring(physicsCursorPosition), xAfterCursor, currentY, color, false);
                    }

                    if (shouldDrawCursor) {
                        if (cursorInWords) {
                            drawRect(xCursor, currentY - 1, xCursor + 1, currentY + 1 + fontRenderer.FONT_HEIGHT, -3092272);
                        } else {
                            drawString(fontRenderer, "_", xCursor, currentY, color, true);
                        }
                    }

                    if (physicsSelectionEnd != physicsCursorPosition) {
                        int l1 = startX + fontRenderer.getStringWidth(line.substring(0, physicsSelectionEnd));
                        this.drawCursorVertical(xCursor, currentY - 1, l1 - 1, currentY + 1 + fontRenderer.FONT_HEIGHT);
                    }
                } else {
                    String line = this.fontRenderer.trimStringToWidth(this.lines.get(i), this.editAreaWidth);
                    drawString(this.fontRenderer, line, startX, currentY, color, false);
                }
            }
        } else {
            for (int i = 0; i < this.lines.size(); i++, currentY += 4 + this.fontRenderer.FONT_HEIGHT) {
                if (currentY + this.fontRenderer.FONT_HEIGHT < startY) {
                    continue;
                }
                String line = this.lines.get(i);
                line = this.fontRenderer.trimStringToWidth(line, this.editAreaWidth);
                drawString(this.fontRenderer, line, startX, currentY, color, false);
            }
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

    public GuiTextArea setMaxLineStringLength(int length) {
        this.maxLineStringLength = length;

        for (int i = 0; i < this.lines.size(); i++) {
            String s = this.lines.get(i);

            if (s.length() > length) {
                this.lines.set(i, s.substring(0, length));
            }
        }
        return this;
    }

    public int getMaxLineStringLength() {
        return this.maxLineStringLength;
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
        } else if (this.isFocused && !isFocusedIn) {
            this.lineScrollOffset = 0;
            this.trimTailEmptyLines();
        }

        this.isFocused = isFocusedIn;
        if (focusListener != null) {
            focusListener.accept(isFocused);
        }
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    @Override
    public GuiTextArea setEnabled(boolean enabled) {
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
        String line = getCurrentLineText();
        int i = line.length();

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

            int j = this.editAreaWidth;
            String s = fontRenderer.trimStringToWidth(line.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;

            if (position == this.lineScrollOffset) {
                this.lineScrollOffset -= fontRenderer.trimStringToWidth(line, j, true).length();
            }

            if (position > k) {
                this.lineScrollOffset += position - k;
            } else if (position <= this.lineScrollOffset) {
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

        if (this.isFocused) {
            this.updateCursorCounter();
        }

        return false;
    }

    private double getScrollPos() {
        int height = this.enableBackgroundDrawing ? ySize() - 4 : ySize();
        return (this.contentHeight - height) * this.scrollBar.getRawPos();
    }

    @Override
    public GuiTextArea translate(int xAmount, int yAmount) {
        GuiTextArea thiz = super.translate(xAmount, yAmount);
        if (this.scrollBar != null) {
            this.scrollBar.setPos(this.maxXPos() - 10, this.yPos());
        }
        return thiz;
    }

    @Override
    public GuiTextArea setXSize(int xSize) {
        GuiTextArea thiz = super.setXSize(xSize);
        if (this.scrollBar != null) {
            this.scrollBar.setXPos(this.maxXPos() - 10);
        }
        this.editAreaWidth = getWidth();
        return thiz;
    }

    @Override
    public GuiTextArea setYSize(int ySize) {
        GuiTextArea thiz = super.setYSize(ySize);
        if (this.scrollBar != null) {
            this.scrollBar.setYSize(this.ySize());
        }
        return thiz;
    }

    private void updateScrollBar() {
        this.contentHeight = Math.max(1, this.lines.size() * (4 + this.fontRenderer.FONT_HEIGHT));
        if (this.scrollBar == null) {
            this.scrollBar = new GuiSlideControl(GuiSlideControl.SliderRotation.VERTICAL)
                .setPos(maxXPos() - 10, yPos())
                .setSize(10, ySize())
                .setDefaultBackground(0xFF000000, 0xFFFFFFFF)
                .setDefaultSlider(0xFFA0A0A0, 0xFF707070);
            this.addChild(this.scrollBar);
            this.scrollBar.setParentScroll(true);
            this.scrollBar.allowMiddleClickDrag(true);
            this.scrollBar.clearScrollChecks();
            this.scrollBar.addScrollCheck((slider, mouseX, mouseY) -> slider.isMouseOver(mouseX, mouseY) || !GuiScreen.isShiftKeyDown());

            this.scrollBar.setRange(0, this.contentHeight);
        }
        if (this.contentHeight < getInsetRect().height) {
            this.scrollBar.setEnabled(false);
            this.scrollBar.setHidden(true);
            this.scrollBar.updateRawPos(0, false);
            this.editAreaWidth = this.getWidth();
            this.setCursorPosition(this.getCursorPosition());
            this.scrollEnable = false;
        } else {
            if (!this.scrollBar.isEnabled()) {
                this.scrollBar.setEnabled(true);
                this.scrollBar.setHidden(false);
                this.editAreaWidth = this.getWidth() - 10;
                this.setCursorPosition(this.getCursorPosition());
                this.scrollEnable = true;
            }
            this.scrollBar.setScaledSliderSize(getInsetRect().height / this.contentHeight);
        }
    }
}
