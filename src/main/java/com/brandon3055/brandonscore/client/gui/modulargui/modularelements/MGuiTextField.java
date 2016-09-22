package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;

/**
 * Created by brandon3055 on 10/09/2016.
 * A strait port of the vanilla text field
 */
public class MGuiTextField extends MGuiElementBase {

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
    private IMGuiListener listener;
    private Predicate<String> validator = Predicates.<String>alwaysTrue();
    private FontRenderer fontRendererInstance;
    public int fillColour = 0xFF5f5f60;
    public int borderColour = 0xFF000000;

    public MGuiTextField(IModularGui modularGui, FontRenderer fontRendererInstance) {
        super(modularGui);
        this.fontRendererInstance = fontRendererInstance;
    }

    public MGuiTextField(IModularGui modularGui, int xPos, int yPos, FontRenderer fontRendererInstance) {
        super(modularGui, xPos, yPos);
        this.fontRendererInstance = fontRendererInstance;
    }

    public MGuiTextField(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, FontRenderer fontRendererInstance) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.fontRendererInstance = fontRendererInstance;
    }

    public MGuiTextField setListener(IMGuiListener listener) {
        this.listener = listener;
        return this;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public MGuiTextField setText(String textIn) {
        if (this.validator.apply(textIn)) {
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

        if (this.validator.apply(s)) {
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l);

            if (listener != null) {
                listener.onMGuiEvent("TEXT_FIELD_CHANGED", this);
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

                if (this.validator.apply(s)) {
                    this.text = s;

                    if (flag) {
                        this.moveCursorBy(num);
                    }

                    if (listener != null) {
                        listener.onMGuiEvent("TEXT_FIELD_CHANGED", this);
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
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, i);
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
                        listener.onMGuiEvent("TEXT_FIELD_ENTER", this);
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
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean mouseOver = isMouseOver(mouseX, mouseY);

        if (this.canLoseFocus) {
            this.setFocused(mouseOver);
        }

        if (this.isFocused && mouseOver && mouseButton == 0) {
            int i = mouseX - xPos;

            if (this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(s, i).length() + this.lineScrollOffset);
        }
        else if (this.isFocused && mouseOver && mouseButton == 1) {
            setText("");
        }

        return mouseOver;
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawTextBox();
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    public void drawTextBox() {
        if (this.getEnableBackgroundDrawing()) {
            drawBorderedRect(xPos, yPos, xSize, ySize, 1, fillColour, borderColour);
        }

        int i = this.isEnabled ? this.enabledColor : this.disabledColor;
        int j = this.cursorPosition - this.lineScrollOffset;
        int k = this.selectionEnd - this.lineScrollOffset;
        String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
        boolean flag = j >= 0 && j <= s.length();
        boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
        int l = this.enableBackgroundDrawing ? xPos + 4 : xPos;
        int i1 = this.enableBackgroundDrawing ? yPos + (ySize - 8) / 2 : yPos;
        int j1 = l;

        if (k > s.length()) {
            k = s.length();
        }

        if (!s.isEmpty()) {
            String s1 = flag ? s.substring(0, j) : s;
//                j1 = this.fontRendererInstance.drawStringWithShadow(s1, (float)l, (float)i1, i);
            j1 = drawString(fontRendererInstance, s1, (float) l, (float) i1, i, true);
        }

        boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
        int k1 = j1;

        if (!flag) {
            k1 = j > 0 ? l + xPos : l;
        }
        else if (flag2) {
            k1 = j1 - 1;
            --j1;
        }

        if (!s.isEmpty() && flag && j < s.length()) {
            drawString(fontRendererInstance, s.substring(j), (float) j1, (float) i1, i, true);
//                j1 = this.fontRendererInstance.drawStringWithShadow(s.substring(j), (float)j1, (float)i1, i);
        }

        if (flag1) {
            if (flag2) {
                drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT, -3092272);
            }
            else {
                drawString(fontRendererInstance, "_", (float) k1, (float) i1, i, true);
//                    this.fontRendererInstance.drawStringWithShadow("_", (float)k1, (float)i1, i);
            }
        }

        if (k != j) {
            int l1 = l + this.fontRendererInstance.getStringWidth(s.substring(0, k));
            this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT);
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

        if (endX > xPos + xSize) {
            endX = xPos + xSize;
        }

        if (startX > xPos + xSize) {
            startX = xPos + xSize;
        }

        double zLevel = getRenderZLevel();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double) startX, (double) endY, zLevel).endVertex();
        vertexbuffer.pos((double) endX, (double) endY, zLevel).endVertex();
        vertexbuffer.pos((double) endX, (double) startY, zLevel).endVertex();
        vertexbuffer.pos((double) startX, (double) startY, zLevel).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public MGuiTextField setMaxStringLength(int length) {
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
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public MGuiTextField setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        super.setEnabled(enabled);
        return this;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public int getWidth() {
        return this.getEnableBackgroundDrawing() ? xSize - 8 : xSize;
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

        if (this.fontRendererInstance != null) {
            if (this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;

            if (position == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRendererInstance.trimStringToWidth(this.text, j, true).length();
            }

            if (position > k) {
                this.lineScrollOffset += position - k;
            }
            else if (position <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - position;
            }

            this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, i);
        }
    }

    public void setCanLoseFocus(boolean canLoseFocusIn) {
        this.canLoseFocus = canLoseFocusIn;
    }

    public void setColours(int fillColour, int borderColour) {
        this.fillColour = fillColour;
        this.borderColour = borderColour;
    }
}
