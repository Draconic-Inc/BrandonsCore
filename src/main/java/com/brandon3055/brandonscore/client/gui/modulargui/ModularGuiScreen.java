package com.brandon3055.brandonscore.client.gui.modulargui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiScreen extends GuiScreen implements IModularGui<ModularGuiScreen> {

    protected int xSize;
    protected int ySize;
    protected ModuleManager manager = new ModuleManager(this);
    protected int zLevel = 0;

    public ModularGuiScreen() {
        this(0, 0);
    }

    public ModularGuiScreen(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
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

    public ModuleManager getManager() {
        return manager;
    }

    @Override
    public void setZLevel(int zLevel) {
        this.zLevel = zLevel;
    }

    @Override
    public int getZLevel() {
        return zLevel;
    }

    //endregion

    //region Mouse & Key

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (manager.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (manager.mouseReleased(mouseX, mouseY, state)) {
            return;
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (manager.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
            return;
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (manager.keyTyped(typedChar, keyCode)) {
            return;
        }

        if (keyCode == 1)
        {
            this.mc.thePlayer.closeScreen();
            this.mc.displayGuiScreen((GuiScreen)null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (manager.handleMouseInput()) {
            return;
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
        manager.renderBackgroundLayer(mc, mouseX, mouseY, partialTicks);
    }

    public void renderForegroundLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderForegroundLayer(mc, mouseX, mouseY, partialTicks);
    }

    public void renderOverlayLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderOverlayLayer(mc, mouseX, mouseY, partialTicks);
    }

    //endregion

    //region Update

    @Override
    public void updateScreen() {
        super.updateScreen();
        manager.onUpdate();
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        manager.setWorldAndResolution(mc, width, height);
    }

    //endregion

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
