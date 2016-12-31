package com.brandon3055.brandonscore.client.gui.modulargui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiContainer<T extends Container> extends GuiContainer implements IModularGui<ModularGuiContainer> {

    protected ModuleManager manager = new ModuleManager(this);
    protected int zLevel = 0;
    protected T container;
    protected boolean slotsHidden = false;

    public ModularGuiContainer(T container) {
        super(container);
        this.container = container;
    }

    //region IModularGui

    @Override
    public ModularGuiContainer getScreen() {
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
        return guiLeft;
    }

    @Override
    public int guiTop() {
        return guiTop;
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

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (manager.handleMouseInput()) {
            return;
        }

        super.handleMouseInput();
    }

    public int getMouseX() {
        return Mouse.getEventX() * this.width / this.mc.displayWidth;
    }

    public int getMouseY() {
        return this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
    }

    //endregion

    //region Render

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackgroundLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-guiLeft(), -guiTop(), 0.0F);

        renderForegroundLayer(mouseX, mouseY, mc.getRenderPartialTicks());

        GlStateManager.popMatrix();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

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

    public void hideInventorySlots(boolean hide) {
        if (hide && !slotsHidden) {
            slotsHidden = true;
            for (Slot slot : container.inventorySlots) {
                slot.xDisplayPosition += 1000;
                slot.yDisplayPosition += 1000;
            }
        }
        else if (!hide && slotsHidden) {
            slotsHidden = false;
            for (Slot slot : container.inventorySlots) {
                slot.xDisplayPosition -= 1000;
                slot.yDisplayPosition -= 1000;
            }
        }
    }
}
