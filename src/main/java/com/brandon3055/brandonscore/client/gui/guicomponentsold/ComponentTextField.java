package com.brandon3055.brandonscore.client.gui.guicomponentsold;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

/**
 * Created by brandon3055 on 16/2/2016.
 */
public class ComponentTextField extends ComponentBase {

    public GuiTextField textField;
    private int xSize;
    private int ySize;
    private GUIBase parent;
    private String label = "";
    private int labelColour = 0;

    public ComponentTextField(GUIBase parent, int x, int y, int xSize, int ySize) {
        super(x, y);
        this.xSize = xSize;
        this.ySize = ySize;
        this.parent = parent;
        textField = new GuiTextField(0, fontRendererObj, x, y, xSize, ySize);
    }

    @Override
    public int getWidth() {
        return xSize;
    }

    @Override
    public int getHeight() {
        return ySize;
    }

    @Override
    public void renderBackground(Minecraft minecraft, int offsetX, int offsetY, int mouseX, int mouseY) {

    }

    @Override
    public void renderForground(Minecraft minecraft, int offsetX, int offsetY, int mouseX, int mouseY) {
        drawRect(x - 1, y - 1, x + xSize - textField.width + 1, y + ySize + 1, -6250336);
        textField.drawTextBox();
        drawRect(x, y, x + xSize - textField.width, y + ySize, -16777216);
        fontRendererObj.drawString(label, x + 1, y + 2, labelColour);
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
        textField.mouseClicked(x, y, button);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        textField.textboxKeyTyped(par1, par2);
        if (textField.isFocused()) parent.componentCallBack(this);
    }

    public boolean isFocused() {
        return textField.isFocused();
    }

    public ComponentTextField setLabel(String label, int labelColour) {
        this.label = label;
        this.labelColour = labelColour;
        int labelLength = fontRendererObj.getStringWidth(label);
        textField.xPosition = x + labelLength;
        textField.width = xSize - labelLength;
        textField.setTextColor(labelColour);
        return this;
    }
}
