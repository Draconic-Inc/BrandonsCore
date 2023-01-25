package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.inventory.ContainerBCore;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public abstract class ModularGuiContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IModularGui<ModularGuiContainer> {

    protected GuiElementManager manager = new GuiElementManager(this);
    public final Inventory playerInv;
    protected int zLevel = 0;
    protected T container;
    protected boolean itemTooltipsEnabled = true;
    public boolean enableDefaultBackground = true;
    private boolean experimentalSlotOcclusion = false;
    @Deprecated
    protected boolean dumbGui = false;
    private FloatingItemRenderOverride floatingItemOverride = null;

    public ModularGuiContainer(T container, Inventory playerInv, Component titleIn) {
        super(container, playerInv, titleIn);
        this.container = container;
        this.playerInv = playerInv;
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        manager.onGuiInit(minecraft, width, height);
        reloadGui();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
            InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
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

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        renderBackgroundLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {}

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
            matrixStack.translate(0, 0, 510); //10 above element hover text.
            renderTooltip(matrixStack, mouseX, mouseY);
            matrixStack.translate(0, 0, -510);
        }
    }

    public void renderBackgroundLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderElements(minecraft, mouseX, mouseY, partialTicks);
    }

    public void renderOverlayLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        manager.onUpdate();
        if (container instanceof ContainerBCore c) {
            c.clientTick();
        }
    }

    private void renderSuperScreen(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        int left = this.leftPos;
        int top = this.topPos;
        this.renderBg(poseStack, partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.DrawBackground(this, poseStack, mouseX, mouseY));
        RenderSystem.disableDepthTest();

        for (Widget widget : this.renderables) {
            widget.render(poseStack, mouseX, mouseY, partialTicks);
        }

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(left, top, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int k = 0; k < this.menu.slots.size(); ++k) {
            Slot slot = this.menu.slots.get(k);
            if (slot.isActive()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderSlot(poseStack, slot);
            }

            boolean occluded = manager.isAreaUnderElement(slot.x + guiLeft(), slot.y + guiTop(), 16, 16, 100);
            if (!occluded || experimentalSlotOcclusion) {
                if (!occluded && this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                    this.hoveredSlot = slot;
                    int l = slot.x;
                    int i1 = slot.y;
                    renderSlotHighlight(poseStack, l, i1, this.getBlitOffset(), this.getSlotColor(k));
                }
                drawSlotOverlay(slot, occluded);
            }
        }

        this.renderLabels(poseStack, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.DrawForeground(this, poseStack, mouseX, mouseY));
        ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (!itemstack.isEmpty()) {
            int l1 = 8;
            int i2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemstack = itemstack.copy();
                itemstack.setCount(Mth.ceil((float) itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(this.quickCraftingRemainder);
                if (itemstack.isEmpty()) {
                    s = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(itemstack, mouseX - left - 8, mouseY - top - i2, s);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float) (Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int j2 = this.snapbackEnd.x - this.snapbackStartX;
            int k2 = this.snapbackEnd.y - this.snapbackStartY;
            int j1 = this.snapbackStartX + (int) ((float) j2 * f);
            int k1 = this.snapbackStartY + (int) ((float) k2 * f);
            this.renderFloatingItem(this.snapbackItem, j1, k1, null);
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    @Override
    public void renderSlot(PoseStack poseStack, Slot slot) {
        int xPos = slot.x;
        int yPos = slot.y;
        boolean occluded = manager.isAreaUnderElement(xPos + guiLeft(), yPos + guiTop(), 16, 16, 100);
        if (occluded && !experimentalSlotOcclusion) {
            return;
        }

        ItemStack itemstack = slot.getItem();
        boolean flag = false;
        boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack carried = this.menu.getCarried();
        String countText = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)) {
                itemstack = carried.copy();
                flag = true;
                AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemstack, slot.getItem().isEmpty() ? 0 : slot.getItem().getCount());
                int k = Math.min(itemstack.getMaxStackSize(), slot.getMaxStackSize(itemstack));
                if (itemstack.getCount() > k) {
                    countText = ChatFormatting.YELLOW.toString() + k;
                    itemstack.setCount(k);
                }
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;
        if (itemstack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                blit(poseStack, xPos, yPos, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                fill(poseStack, xPos, yPos, xPos + 16, yPos + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, xPos, yPos, slot.x + slot.y * this.imageWidth);
            if (!occluded){
                this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, xPos, yPos, countText);
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
    public int getGuiLeft() {return guiLeft();}

    @Override
    public int getGuiTop() {return guiTop();}

    @Override
    public int getXSize() {return xSize();}

    @Override
    public int getYSize() {return ySize();}

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, button) && manager.getElementsAtPosition(mouseX, mouseY).stream().noneMatch(GuiElement::isEnabled);
    }

    public T getContainer() {
        return container;
    }

    @Override
    public void renderFloatingItem(ItemStack stack, int x, int y, String altText) {
        if (floatingItemOverride != null && floatingItemOverride.renderFloatingItem(stack, x, y, altText)) {
            return;
        }
        super.renderFloatingItem(stack, x, y, altText);
    }

    public void setFloatingItemOverride(FloatingItemRenderOverride floatingItemOverride) {
        this.floatingItemOverride = floatingItemOverride;
    }

    public interface FloatingItemRenderOverride {
        boolean renderFloatingItem(ItemStack stack, int x, int y, String altText);
    }
}
