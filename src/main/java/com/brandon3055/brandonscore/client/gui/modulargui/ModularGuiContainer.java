package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.platform.GLX;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiContainer<T extends Container> extends ContainerScreen<T> implements IModularGui<ModularGuiContainer> {

    protected GuiElementManager manager = new GuiElementManager(this);
    protected int zLevel = 0;
    protected T container;
    protected boolean itemTooltipsEnabled = true;
    public boolean enableDefaultBackground = true;
    private boolean experimentalSlotOcclusion = false;
    @Deprecated
    protected boolean dumbGui = false;

    public ModularGuiContainer(T container, PlayerInventory inv, ITextComponent titleIn) {
        super(container, inv, titleIn);
        this.container = container;
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.fontRenderer;
        this.width = minecraft.getMainWindow().getScaledWidth();
        this.height = minecraft.getMainWindow().getScaledHeight();
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
        if (!dumbGui && manager.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dumbGui && manager.mouseReleased(mouseX, mouseY, button)) {
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
        if (!dumbGui && manager.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!dumbGui && manager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
            if (keyCode == 256 || this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
                this.minecraft.player.closeScreen();
                onClose();
                return true; // Forge MC-146650: Needs to return true when the key is handled.
            }

            if (this.func_195363_d(keyCode, scanCode))
                return true; // Forge MC-146650: Needs to return true when the key is handled.
            if (this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {
                if (this.minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseKey)) {
                    this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber, 0, ClickType.CLONE);
                    return true; // Forge MC-146650: Needs to return true when the key is handled.
                } else if (this.minecraft.gameSettings.keyBindDrop.isActiveAndMatches(mouseKey)) {
                    this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber, hasControlDown() ? 1 : 0, ClickType.THROW);
                    return true; // Forge MC-146650: Needs to return true when the key is handled.
                }
            } else if (this.minecraft.gameSettings.keyBindDrop.isActiveAndMatches(mouseKey)) {
                return true; // Forge MC-146650: Emulate MC bug, so we don't drop from hotbar when pressing drop without hovering over a item.
            }

            return false; // Forge MC-146650: Needs to return false when the key is not handled.
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!dumbGui && manager.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int charCode) {
        if (!dumbGui && manager.charTyped(charTyped, charCode)) {
            return true;
        }
        return super.charTyped(charTyped, charCode);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (!dumbGui && manager.mouseScrolled(mouseX, mouseY, scrollAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    public double getMouseX() {
        return minecraft.mouseHelper.getMouseX() * this.width / this.minecraft.getMainWindow().getWidth();
    }

    public double getMouseY() {
        return this.height - minecraft.mouseHelper.getMouseY() * this.height / this.minecraft.getMainWindow().getHeight() - 1;
    }

    //endregion

    //region Render

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackgroundLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (dumbGui) {
            super.render(mouseX, mouseY, partialTicks);
            return;
        }

        if (enableDefaultBackground) {
            renderBackground();
        }

        renderSuperScreen(mouseX, mouseY, partialTicks);
        renderOverlayLayer(mouseX, mouseY, partialTicks);

        if (itemTooltipsEnabled) {
//            RenderSystem.translated(0, 0, 300);
            renderHoveredToolTip(mouseX, mouseY);
//            RenderSystem.translated(0, 0, -300);
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

    public void renderSuperScreen(int mouseX, int mouseY, float partialTicks) {
        int left = this.guiLeft;
        int top = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawBackground(this, mouseX, mouseY));
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();

        for (int i = 0; i < this.buttons.size(); ++i) {
            this.buttons.get(i).render(mouseX, mouseY, partialTicks);
        }

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) left, (float) top, 0.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableRescaleNormal();
        this.hoveredSlot = null;
        int k = 240;
        int l = 240;
        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < this.container.inventorySlots.size(); ++i1) {
            Slot slot = this.container.inventorySlots.get(i1);
            if (slot.isEnabled()) {
                this.drawSlot(slot);
            }

            boolean occluded = manager.isAreaUnderElement(slot.xPos + guiLeft(), slot.yPos + guiTop(), 16, 16, 100);
            if (!occluded || experimentalSlotOcclusion) {
                if (!occluded && this.isSlotSelected(slot, mouseX, mouseY) && slot.isEnabled()) {
                    this.hoveredSlot = slot;
                    RenderSystem.disableDepthTest();
                    int j1 = slot.xPos;
                    int k1 = slot.yPos;
                    RenderSystem.colorMask(true, true, true, false);
                    int slotColor = this.getSlotColor(i1);
                    this.fillGradient(j1, k1, j1 + 16, k1 + 16, slotColor, slotColor);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.enableDepthTest();
                }
                drawSlotOverlay(slot, occluded);
            }
        }

        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, mouseX, mouseY));
        PlayerInventory playerinventory = this.minecraft.player.inventory;
        ItemStack itemstack = this.draggedStack.isEmpty() ? playerinventory.getItemStack() : this.draggedStack;
        if (!itemstack.isEmpty()) {
            int j2 = 8;
            int k2 = this.draggedStack.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggedStack.isEmpty() && this.isRightMouseClick) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathHelper.ceil((float) itemstack.getCount() / 2.0F));
            } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(this.dragSplittingRemnant);
                if (itemstack.isEmpty()) {
                    s = "" + TextFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - left - 8, mouseY - top - k2, s);
        }

        if (!this.returningStack.isEmpty()) {
            float f = (float) (Util.milliTime() - this.returningStackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.returningStack = ItemStack.EMPTY;
            }

            int l2 = this.returningStackDestSlot.xPos - this.touchUpX;
            int i3 = this.returningStackDestSlot.yPos - this.touchUpY;
            int l1 = this.touchUpX + (int) ((float) l2 * f);
            int i2 = this.touchUpY + (int) ((float) i3 * f);
            this.drawItemStack(this.returningStack, l1, i2, null);
        }

        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
    }

    private void drawSlot(Slot slotIn) {
        int yPos = slotIn.yPos;
        int xPos = slotIn.xPos;

        boolean occluded = manager.isAreaUnderElement(xPos + guiLeft(), yPos + guiTop(), 16, 16, 100);
        if (occluded && !experimentalSlotOcclusion) {
            return;
        }

        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == this.clickedSlot && !this.draggedStack.isEmpty() && !this.isRightMouseClick;
        ItemStack itemstack1 = this.minecraft.player.inventory.getItemStack();
        String s = null;
        if (slotIn == this.clickedSlot && !this.draggedStack.isEmpty() && this.isRightMouseClick && !itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        } else if (this.dragSplitting && this.dragSplittingSlots.contains(slotIn) && !itemstack1.isEmpty()) {
            if (this.dragSplittingSlots.size() == 1) {
                return;
            }

            if (Container.canAddItemToSlot(slotIn, itemstack1, true) && this.container.canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack, slotIn.getStack().isEmpty() ? 0 : slotIn.getStack().getCount());
                int k = Math.min(itemstack.getMaxStackSize(), slotIn.getItemStackLimit(itemstack));
                if (itemstack.getCount() > k) {
                    s = TextFormatting.YELLOW.toString() + k;
                    itemstack.setCount(k);
                }
            } else {
                this.dragSplittingSlots.remove(slotIn);
                this.updateDragSplitting();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.zLevel = 100.0F;
        if (itemstack.isEmpty() && slotIn.isEnabled()) {
            Pair<ResourceLocation, ResourceLocation> pair = slotIn.func_225517_c_();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getAtlasSpriteGetter(pair.getFirst()).apply(pair.getSecond());
                this.minecraft.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
                blit(xPos, yPos, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                fill(xPos, yPos, xPos + 16, yPos + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderItemAndEffectIntoGUI(this.minecraft.player, itemstack, xPos, yPos);
            if (!occluded) {
                this.itemRenderer.renderItemOverlayIntoGUI(this.font, itemstack, xPos, yPos, s);
            }
        }

        this.itemRenderer.zLevel = 0.0F;
        this.setBlitOffset(0);
    }

    protected void drawSlotOverlay(Slot slot, boolean occluded) {}

    public void setExperimentalSlotOcclusion(boolean experimentalSlotOcclusion) {
        this.experimentalSlotOcclusion = experimentalSlotOcclusion;
    }

    public int getGuiLeft() { return guiLeft(); }

    public int getGuiTop() { return guiTop(); }

    public int getXSize() { return xSize(); }

    public int getYSize() { return ySize(); }
}
