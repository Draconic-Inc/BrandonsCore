package com.brandon3055.brandonscore.client.gui.modulargui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiContainer<T extends Container> extends ContainerScreen implements IModularGui<ModularGuiContainer> {

    protected GuiElementManager manager = new GuiElementManager(this);
    protected int zLevel = 0;
    protected T container;
    protected boolean itemTooltipsEnabled = true;
    public boolean enableDefaultBackground = true;


    public ModularGuiContainer(T container, PlayerInventory inv, ITextComponent titleIn) {
        super(container, inv, titleIn);
        this.container = container;
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.fontRenderer;
        this.width = minecraft.mainWindow.getScaledWidth();
        this.height = minecraft.mainWindow.getScaledHeight();
        manager.setWorldAndResolution(minecraft, width, height);
    }

    public T getContainer() {
        return container;
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
    public void setUISize(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
    }

    @Override
    public int guiLeft() {
        return guiLeft;
    }

    @Override
    public int guiTop() {
        return guiTop;
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

    //    @Override
//    public void handleMouseInput() throws IOException {
//        if (manager.handleMouseInput()) {
//            return true;
//        }
//
//        super.handleMouseInput();
//    }

    public double getMouseX() {
        return minecraft.mouseHelper.getMouseX() * this.width / this.minecraft.mainWindow.getWidth();
    }

    public double getMouseY() {
        return this.height - minecraft.mouseHelper.getMouseY() * this.height / this.minecraft.mainWindow.getHeight() - 1;
    }

    //endregion

    //region Render

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackgroundLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(-guiLeft(), -guiTop(), 0.0F);
//
//        renderForegroundLayer(mouseX, mouseY, mc.getRenderPartialTicks());
//
//        GlStateManager.popMatrix();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
//        drawSuperScreen(mouseX, mouseY, partialTicks);
        renderOverlayLayer(mouseX, mouseY, partialTicks);
        if (itemTooltipsEnabled) {
            renderHoveredToolTip(mouseX, mouseY);
        }
    }


    public void renderBackgroundLayer(int mouseX, int mouseY, float partialTicks) {
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

    //region Overriding vanilla stuff and things

//TODO Re implement these overrides
//    //This override is needed because vanilla draws item highlights with Depth disabled meaning they render on top of everything which just will not do!
//    public void drawSuperScreen(int mouseX, int mouseY, float partialTicks) {
//        if (enableDefaultBackground) {
//            this.drawDefaultBackground();
//        }
//        int i = this.guiLeft;
//        int j = this.guiTop;
//        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
//        GlStateManager.disableRescaleNormal();
//        RenderHelper.disableStandardItemLighting();
//        GlStateManager.disableLighting();
//        GlStateManager.disableDepth();
//
//        //Just in case someone for some crasy reason actually uses these in a modular gui... But no one ever should!
//        for (int bi = 0; bi < this.buttonList.size(); ++bi) {
//            ((GuiButton) this.buttonList.get(bi)).drawButton(this.mc, mouseX, mouseY, partialTicks);
//        }
//
//        for (int lj = 0; lj < this.labelList.size(); ++lj) {
//            ((GuiLabel) this.List.get(lj)).drawLabel(this.mc, mouseX, mouseY);
//        }
//
//        RenderHelper.enableGUIStandardItemLighting();
//        GlStateManager.pushMatrix();
//        GlStateManager.translate((float) i, (float) j, 0.0F);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        GlStateManager.enableRescaleNormal();
//        this.hoveredSlot = null;
//        int k = 240;
//        int l = 240;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//
//        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
//            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i1);
//
//            if (slot.isEnabled()) {
//                this.drawSlot(slot);
//            }
//
//            int slotXPos = slot.xPos;
//            int slotYPos = slot.yPos;
//            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.isEnabled() && !manager.isAreaUnderElement(slotXPos + guiLeft(), slotYPos + guiTop(), 16, 16, 100)) {
//                this.hoveredSlot = slot;
//                GlStateManager.disableLighting();
//                GlStateManager.disableDepth();
//                int j1 = slot.xPos;
//                int k1 = slot.yPos;
//                GlStateManager.colorMask(true, true, true, false);
//                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
//                GlStateManager.colorMask(true, true, true, true);
//                GlStateManager.enableLighting();
//                GlStateManager.enableDepth();
//            }
//        }
//        GlStateManager.enableDepth();
//
//        RenderHelper.disableStandardItemLighting();
//        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
//        RenderHelper.enableGUIStandardItemLighting();
//        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, mouseX, mouseY));
//        InventoryPlayer inventoryplayer = this.mc.player.inventory;
//        ItemStack itemstack = this.draggedStack.isEmpty() ? inventoryplayer.getItemStack() : this.draggedStack;
//
//        if (!itemstack.isEmpty()) {
//            int j2 = 8;
//            int k2 = this.draggedStack.isEmpty() ? 8 : 16;
//            String s = null;
//
//            if (!this.draggedStack.isEmpty() && this.isRightMouseClick) {
//                itemstack = itemstack.copy();
//                itemstack.setCount(MathHelper.ceil((float) itemstack.getCount() / 2.0F));
//            }
//            else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
//                itemstack = itemstack.copy();
//                itemstack.setCount(this.dragSplittingRemnant);
//
//                if (itemstack.isEmpty()) {
//                    s = "" + TextFormatting.YELLOW + "0";
//                }
//            }
//
//            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
//        }
//
//        if (!this.returningStack.isEmpty()) {
//            float f = (float) (Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
//
//            if (f >= 1.0F) {
//                f = 1.0F;
//                this.returningStack = ItemStack.EMPTY;
//            }
//
//            int l2 = this.returningStackDestSlot.xPos - this.touchUpX;
//            int i3 = this.returningStackDestSlot.yPos - this.touchUpY;
//            int l1 = this.touchUpX + (int) ((float) l2 * f);
//            int i2 = this.touchUpY + (int) ((float) i3 * f);
//            this.drawItemStack(this.returningStack, l1, i2, (String) null);
//        }
//
//        GlStateManager.popMatrix();
//        GlStateManager.enableLighting();
//        GlStateManager.enableDepth();
//        RenderHelper.enableStandardItemLighting();
//    }
//
//    @Override
//    public void drawSlot(Slot slotIn) {
//        int xPos = slotIn.xPos;
//        int yPos = slotIn.yPos;
//        ItemStack itemstack = slotIn.getStack();
//        boolean flag = false;
//        boolean flag1 = slotIn == this.clickedSlot && !this.draggedStack.isEmpty() && !this.isRightMouseClick;
//        ItemStack itemstack1 = this.mc.player.inventory.getItemStack();
//        String s = null;
//
//        if (manager.isAreaUnderElement(xPos + guiLeft(), yPos + guiTop(), 16, 16, 100)) {
//            return;
//        }
//
//        if (slotIn == this.clickedSlot && !this.draggedStack.isEmpty() && this.isRightMouseClick && !itemstack.isEmpty()) {
//            itemstack = itemstack.copy();
//            itemstack.setCount(itemstack.getCount() / 2);
//        }
//        else if (this.dragSplitting && this.dragSplittingSlots.contains(slotIn) && !itemstack1.isEmpty()) {
//            if (this.dragSplittingSlots.size() == 1) {
//                return;
//            }
//
//            if (Container.canAddItemToSlot(slotIn, itemstack1, true) && this.inventorySlots.canDragIntoSlot(slotIn)) {
//                itemstack = itemstack1.copy();
//                flag = true;
//                Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack, slotIn.getStack().isEmpty() ? 0 : slotIn.getStack().getCount());
//                int k = Math.min(itemstack.getMaxStackSize(), slotIn.getItemStackLimit(itemstack));
//
//                if (itemstack.getCount() > k) {
//                    s = TextFormatting.YELLOW.toString() + k;
//                    itemstack.setCount(k);
//                }
//            }
//            else {
//                this.dragSplittingSlots.remove(slotIn);
//                this.updateDragSplitting();
//            }
//        }
//
//        super.zLevel = 100.0F;
//        this.itemRender.zLevel = 100.0F;
//
//        if (itemstack.isEmpty() && slotIn.isEnabled()) {
//            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();
//
//            if (textureatlassprite != null) {
//                GlStateManager.disableLighting();
//                this.mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
//                this.drawTexturedModalRect(xPos, yPos, textureatlassprite, 16, 16);
//                GlStateManager.enableLighting();
//                flag1 = true;
//            }
//        }
//
//        if (!flag1) {
//            if (flag) {
//                drawRect(xPos, yPos, xPos + 16, yPos + 16, -2130706433);
//            }
//
//            GlStateManager.enableDepth();
//            this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, itemstack, xPos, yPos);
//            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, itemstack, xPos, yPos, s);
//            if (!manager.isAreaUnderElement(xPos + guiLeft, yPos + guiTop, 18, 18, 100)) {
//                this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, itemstack, xPos, yPos);
//                this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, itemstack, xPos, yPos, s);
//            }
//        }
//
//        this.itemRender.zLevel = 0.0F;
//        super.zLevel = 0.0F;
//    }
//
//    //endregion
}
