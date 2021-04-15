package com.brandon3055.brandonscore.client.gui.modulargui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiScreen extends Screen implements IModularGui<ModularGuiScreen> {

    protected int xSize;
    protected int ySize;
    protected GuiElementManager manager = new GuiElementManager(this);
    protected int zLevel = 0;

    public ModularGuiScreen(ITextComponent titleIn) {
        this(titleIn, 0, 0);
    }

    public ModularGuiScreen(ITextComponent titleIn, int xSize, int ySize) {
        super(titleIn);
        this.xSize = xSize;
        this.ySize = ySize;
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.width = minecraft.getWindow().getGuiScaledWidth();
        this.height = minecraft.getWindow().getGuiScaledHeight();
        manager.setWorldAndResolution(minecraft, width, height);
    }

    /**
     * If you need to do anything in init use the reloadGui method, Remember you should no longer be adding elements during init as it may be called more than once.
     */
    @Override
    public final void init() {
        super.init();
        manager.onGuiInit(minecraft, width, height);
        reloadGui();
    }

    public void reloadGui() {
        manager.reloadElements();
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
    public void setUISize(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
    }

    @Override
    public int guiLeft() {
        return (this.width - this.xSize) / 2;
    }

    @Override
    public int guiTop() {
        return (this.height - this.ySize) / 2;
    }

    public GuiElementManager getManager() {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (manager.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (manager.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        manager.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        if (manager.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (manager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (manager.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int charCode) {
        if (manager.charTyped(charTyped, charCode)) {
            return true;
        }
        return super.charTyped(charTyped, charCode);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (manager.mouseScrolled(mouseX, mouseY, scrollAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    //endregion

    //region Render

    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        renderElements(mouseX, mouseY, partialTicks);
        super.render(mStack, mouseX, mouseY, partialTicks);
        renderOverlayLayer(mouseX, mouseY, partialTicks);
    }

    public void renderElements(int mouseX, int mouseY, float partialTicks) {
        manager.renderElements(minecraft, mouseX, mouseY, partialTicks);
    }

    public void renderOverlayLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    //endregion

    //region Update

    @Override
    public void tick() {
        super.tick();
        manager.onUpdate();
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);
        manager.setWorldAndResolution(mc, width, height);
    }

    //endregion


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
