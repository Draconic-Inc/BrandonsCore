package com.brandon3055.brandonscore.client.gui.modulargui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
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
//        super.drawScreen(mouseX, mouseY, partialTicks);
        drawSuperScreen(mouseX, mouseY, partialTicks);
        renderOverlayLayer(mouseX, mouseY, partialTicks);
    }


    public void renderBackgroundLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderBackgroundLayer(mc, mouseX, mouseY, partialTicks);

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i1);


        }
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

    //region Overriding vanilla stuff and things

    //This override is needed because vanilla draws item highlights with Depth disabled meaning they render on top of everything which just will not do!
    public void drawSuperScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
//        super.drawScreen(mouseX, mouseY, partialTicks);

        //Just in case someone for some crasy reason actually uses these in a modular gui... But no one ever should!
        for (int bi = 0; bi < this.buttonList.size(); ++bi) {
            ((GuiButton) this.buttonList.get(bi)).drawButton(this.mc, mouseX, mouseY);
        }

        for (int lj = 0; lj < this.labelList.size(); ++lj) {
            ((GuiLabel) this.labelList.get(lj)).drawLabel(this.mc, mouseX, mouseY);
        }

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) i, (float) j, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        this.theSlot = null;
        int k = 240;
        int l = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i1);
            this.drawSlot(slot);

            int slotXPos = slot.xDisplayPosition;
            int slotYPos = slot.yDisplayPosition;
            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered() && !manager.isAreaUnderElement(slotXPos + guiLeft(), slotYPos + guiTop(), 16, 16, 100)) {
                this.theSlot = slot;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.colorMask(true, true, true, false);
                this.drawGradientRect(slotXPos, slotYPos, slotXPos + 16, slotYPos + 16, -2130706433, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = this.draggedStack == null ? inventoryplayer.getItemStack() : this.draggedStack;

        if (itemstack != null) {
            int j2 = 8;
            int k2 = this.draggedStack == null ? 8 : 16;
            String s = null;

            if (this.draggedStack != null && this.isRightMouseClick) {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_float_int((float) itemstack.stackSize / 2.0F);
            }
            else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.stackSize = this.dragSplittingRemnant;

                if (itemstack.stackSize == 0) {
                    s = "" + TextFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }

        if (this.returningStack != null) {
            float f = (float) (Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;

            if (f >= 1.0F) {
                f = 1.0F;
                this.returningStack = null;
            }

            int l2 = this.returningStackDestSlot.xDisplayPosition - this.touchUpX;
            int i3 = this.returningStackDestSlot.yDisplayPosition - this.touchUpY;
            int l1 = this.touchUpX + (int) ((float) l2 * f);
            int i2 = this.touchUpY + (int) ((float) i3 * f);
            this.drawItemStack(this.returningStack, l1, i2, (String) null);
        }

        GlStateManager.popMatrix();

        if (inventoryplayer.getItemStack() == null && this.theSlot != null && this.theSlot.getHasStack()) {
            ItemStack itemstack1 = this.theSlot.getStack();
            this.renderToolTip(itemstack1, mouseX, mouseY);
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    //Turns out i didnt actually need to override anything in this but who knows maby i will in the future.
    @Override
    public void drawSlot(Slot slotIn) {
        int xDisplayPosition = slotIn.xDisplayPosition;
        int yDisplayPosition = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == this.clickedSlot && this.draggedStack != null && !this.isRightMouseClick;
        ItemStack itemstack1 = this.mc.thePlayer.inventory.getItemStack();
        String s = null;

        if (slotIn == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.stackSize /= 2;
        }
        else if (this.dragSplitting && this.dragSplittingSlots.contains(slotIn) && itemstack1 != null) {
            if (this.dragSplittingSlots.size() == 1) {
                return;
            }

            if (Container.canAddItemToSlot(slotIn, itemstack1, true) && this.inventorySlots.canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);

                if (itemstack.stackSize > itemstack.getMaxStackSize()) {
                    s = TextFormatting.YELLOW + "" + itemstack.getMaxStackSize();
                    itemstack.stackSize = itemstack.getMaxStackSize();
                }

                if (itemstack.stackSize > slotIn.getItemStackLimit(itemstack)) {
                    s = TextFormatting.YELLOW + "" + slotIn.getItemStackLimit(itemstack);
                    itemstack.stackSize = slotIn.getItemStackLimit(itemstack);
                }
            }
            else {
                this.dragSplittingSlots.remove(slotIn);
                this.updateDragSplitting();
            }
        }

        super.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;


        if (itemstack == null && slotIn.canBeHovered()) {
            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();

            if (textureatlassprite != null) {
                GlStateManager.disableLighting();
                this.mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
                this.drawTexturedModalRect(xDisplayPosition, yDisplayPosition, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                drawRect(xDisplayPosition, yDisplayPosition, xDisplayPosition + 16, yDisplayPosition + 16, -2130706433);
            }

            GlStateManager.enableDepth();
            if (!manager.isAreaUnderElement(xDisplayPosition + guiLeft, yDisplayPosition + guiTop, 18, 18, 100)) {
                this.itemRender.renderItemAndEffectIntoGUI(this.mc.thePlayer, itemstack, xDisplayPosition, yDisplayPosition);
                this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemstack, xDisplayPosition, yDisplayPosition, s);
            }
        }

        this.itemRender.zLevel = 0.0F;
        super.zLevel = 0.0F;
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
