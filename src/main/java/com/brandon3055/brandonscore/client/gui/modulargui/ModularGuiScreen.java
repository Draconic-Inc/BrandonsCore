package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.gui.modulargui.modularelements.ElementButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiScreen extends GuiScreen implements IModularGui<ModularGuiScreen> {

    protected int xSize;
    protected int ySize;
    protected List<GuiElementBase> elements = new ArrayList<GuiElementBase>();

    public ModularGuiScreen() {
        this(0, 0);
    }

    public ModularGuiScreen(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void initElements() {
        for (GuiElementBase element : elements) {
            element.initElement();
        }
    }

    //region IModularGui

    @Override
    public ModularGuiScreen getScreen() {
        return this;
    }

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public int guiLeft() {
        return (this.width - this.xSize) / 2;
    }

    @Override
    public int guiTop() {
        return (this.height - this.ySize) / 2;
    }

    @Override
    public int screenWidth() {
        return width;
    }

    @Override
    public int screenHeight() {
        return height;
    }

    @Override
    public Minecraft getMinecraft() {
        return mc;
    }

    @Override
    public List<GuiElementBase> getElements() {
        return elements;
    }

    @Override
    public void elementButtonAction(ElementButton button) {
    }

    //endregion

    //region Mouse & Key

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (GuiElementBase element : elements) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (GuiElementBase element : elements) {
            if (element.isEnabled()) {
                element.mouseReleased(mouseX, mouseY, state);
            }
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (GuiElementBase element : elements) {
            if (element.isEnabled()) {
                element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            }
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (GuiElementBase element : elements) {
            if (element.isEnabled() && element.keyTyped(typedChar, keyCode)) {
                return;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        for (GuiElementBase element : elements) {
            if (element.isEnabled()) {
                element.handleMouseInput();
            }
        }

        super.handleMouseInput();
    }

    //endregion

    //region Render

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderBackgroundLayer(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderForegroundLayer(mouseX, mouseY, partialTicks);
        renderOverlayLayer(mouseX, mouseY, partialTicks);
    }

    public void renderBackgroundLayer(int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : elements) {
            if (element.isEnabled()) {
                element.renderBackgroundLayer(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderForegroundLayer(int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : elements) {
            if (element.isEnabled()) {
                element.renderForegroundLayer(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderOverlayLayer(int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : elements) {
            if (element.isEnabled()) {
                element.renderOverlayLayer(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    //endregion
}
