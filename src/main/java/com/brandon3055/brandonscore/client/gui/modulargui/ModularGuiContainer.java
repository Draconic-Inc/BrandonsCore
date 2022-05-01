package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.matrix.MatrixStack;
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
    public ModularGuiContainer getScreen() {
        return this;
    }

    @Override
    public int xSize() {
        return imageWidth;
    }

    @Override
    public int ySize() {
        return imageHeight;
    }

    @Override
    public void setUISize(int xSize, int ySize) {
        this.imageWidth = xSize;
        this.imageHeight = ySize;
    }

    @Override
    public int guiLeft() {
        return leftPos;
    }

    @Override
    public int guiTop() {
        return topPos;
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
            InputMappings.Input mouseKey = InputMappings.getKey(keyCode, scanCode);
            if (super.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
                this.onClose();
                return true;
            } else {
                boolean handled = this.checkHotbarKeyPressed(keyCode, scanCode);// Forge MC-146650: Needs to return true when the key is handled
                if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                    if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                        this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
                        handled = true;
                    } else if (this.minecraft.options.keyDrop.isActiveAndMatches(mouseKey)) {
                        this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
                        handled = true;
                    }
                } else if (this.minecraft.options.keyDrop.isActiveAndMatches(mouseKey)) {
                    handled = true; // Forge MC-146650: Emulate MC bug, so we don't drop from hotbar when pressing drop without hovering over a item.
                }

                return handled;
            }
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
        return minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth();
    }

    public double getMouseY() {
        return minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight();
    }

    //endregion

    //region Render

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        renderBackgroundLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {}

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (dumbGui) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            return;
        }

        if (enableDefaultBackground) {
            renderBackground(matrixStack);
        }

        renderSuperScreen(matrixStack, mouseX, mouseY, partialTicks);
        renderOverlayLayer(mouseX, mouseY, partialTicks);

        if (itemTooltipsEnabled) {
            RenderSystem.translated(0, 0, 400);
            renderTooltip(matrixStack, mouseX, mouseY);
            RenderSystem.translated(0, 0, -400);
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

    public void renderSuperScreen(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int left = this.leftPos;
        int top = this.topPos;
        this.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawBackground(this, matrixStack, mouseX, mouseY));
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();

        for (int i = 0; i < this.buttons.size(); ++i) {
            this.buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
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

        for (int i1 = 0; i1 < this.container.slots.size(); ++i1) {
            Slot slot = this.container.slots.get(i1);
            if (slot.isActive()) {
                this.drawSlot(matrixStack, slot);
            }

            boolean occluded = manager.isAreaUnderElement(slot.x + guiLeft(), slot.y + guiTop(), 16, 16, 100);
            if (!occluded || experimentalSlotOcclusion) {
                if (!occluded && this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                    this.hoveredSlot = slot;
                    RenderSystem.disableDepthTest();
                    int j1 = slot.x;
                    int k1 = slot.y;
                    RenderSystem.colorMask(true, true, true, false);
                    int slotColor = this.getSlotColor(i1);
                    this.fillGradient(matrixStack, j1, k1, j1 + 16, k1 + 16, slotColor, slotColor);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.enableDepthTest();
                }
                drawSlotOverlay(slot, occluded);
            }
        }

        this.renderLabels(matrixStack, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, matrixStack, mouseX, mouseY));
        PlayerInventory playerinventory = this.minecraft.player.inventory;
        ItemStack itemstack = this.draggingItem.isEmpty() ? playerinventory.getCarried() : this.draggingItem;
        if (!itemstack.isEmpty()) {
            int j2 = 8;
            int k2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathHelper.ceil((float) itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(this.quickCraftingRemainder);
                if (itemstack.isEmpty()) {
                    s = "" + TextFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(itemstack, mouseX - left - 8, mouseY - top - k2, s);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float) (Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int l2 = this.snapbackEnd.x - this.snapbackStartX;
            int i3 = this.snapbackEnd.y - this.snapbackStartY;
            int l1 = this.snapbackStartX + (int) ((float) l2 * f);
            int i2 = this.snapbackStartY + (int) ((float) i3 * f);
            this.renderFloatingItem(this.snapbackItem, l1, i2, null);
        }

        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
    }

    private void drawSlot(MatrixStack matrixStack, Slot slotIn) {
        int yPos = slotIn.y;
        int xPos = slotIn.x;

        boolean occluded = manager.isAreaUnderElement(xPos + guiLeft(), yPos + guiTop(), 16, 16, 100);
        if (occluded && !experimentalSlotOcclusion) {
            return;
        }

        ItemStack itemstack = slotIn.getItem();
        boolean flag = false;
        boolean flag1 = slotIn == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemstack1 = this.minecraft.player.inventory.getCarried();
        String s = null;
        if (slotIn == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slotIn) && !itemstack1.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (Container.canItemQuickReplace(slotIn, itemstack1, true) && this.container.canDragTo(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                Container.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemstack, slotIn.getItem().isEmpty() ? 0 : slotIn.getItem().getCount());
                int k = Math.min(itemstack.getMaxStackSize(), slotIn.getMaxStackSize(itemstack));
                if (itemstack.getCount() > k) {
                    s = TextFormatting.YELLOW.toString() + k;
                    itemstack.setCount(k);
                }
            } else {
                this.quickCraftSlots.remove(slotIn);
                this.recalculateQuickCraftRemaining();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;
        if (itemstack.isEmpty() && slotIn.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slotIn.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                this.minecraft.getTextureManager().bind(textureatlassprite.atlas().location());
                blit(matrixStack, xPos, yPos, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                fill(matrixStack, xPos, yPos, xPos + 16, yPos + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, xPos, yPos);
            if (!occluded) {
                this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, xPos, yPos, s);
            }
        }

        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    protected void drawSlotOverlay(Slot slot, boolean occluded) {}

    public void setExperimentalSlotOcclusion(boolean experimentalSlotOcclusion) {
        this.experimentalSlotOcclusion = experimentalSlotOcclusion;
    }

    @Override
    public int getGuiLeft() { return guiLeft(); }

    @Override
    public int getGuiTop() { return guiTop(); }

    @Override
    public int getXSize() { return xSize(); }

    @Override
    public int getYSize() { return ySize(); }
}
