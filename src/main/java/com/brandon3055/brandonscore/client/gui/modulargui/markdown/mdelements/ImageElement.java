package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Cuboid6;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.DLResourceLocation;
import com.brandon3055.brandonscore.lib.ScissorHelper;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class ImageElement extends MDElementBase<ImageElement> {

    private static final Random rand = new Random();
    private static final List<Block> LOADING_BLOCKS = Lists.newArrayList(Blocks.STONE, Blocks.SAND, Blocks.GRASS, Blocks.COBBLESTONE, Blocks.LOG, Blocks.GLASS, Blocks.MYCELIUM, Blocks.CHEST, Blocks.ENCHANTING_TABLE, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BEACON, Blocks.BOOKSHELF, Blocks.DIAMOND_ORE, Blocks.OBSIDIAN, Blocks.DIRT, Blocks.DISPENSER, Blocks.FURNACE, Blocks.HAY_BLOCK);
    private ItemStack renderLoadingStack = ItemStack.EMPTY;
    private int loadingTime = 0;
    private boolean downloading = false;

    private MDElementContainer container;
    private String imageURL;
    private DLResourceLocation resourceLocation;
    public String linkTo = "";

    public ImageElement(MDElementContainer container, String imageURL) {
        this.container = container;
        this.imageURL = imageURL;
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        resourceLocation = DLRSCache.getResource(imageURL);
        int w = 0;
        int h = 0;

        if (!resourceLocation.dlFinished || resourceLocation.dlFailed) {
            w = 76;
            h = 76;
            downloading = true;
        }
        else {
            if (width == -1 && height == -1) {
                width = 32;
            }
            if (width != -1) {
                w = screenRelativeSize ? (int) (MathHelper.clip(width / 100D, 0, 1) * layout.getWidth()) : MathHelper.clip(width, 8, layout.getWidth());
                if (height == -1) {
                    if (resourceLocation.sizeSet) {
                        h = (int) (((double) resourceLocation.height / (double) resourceLocation.width) * w);
                    }
                    else {
                        h = w;
                    }
                }
            }
            if (height != -1) {
                h = height;
                if (width == -1) {
                    if (resourceLocation.sizeSet) {
                        w = (int) (((double) resourceLocation.width / (double) resourceLocation.height) * height);
                    }
                    else {
                        w = height;
                    }
                }
            }
        }

        setSize(w, h);
        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (downloading) {
            renderDownloading(partialTicks);
        }
        else {
            ResourceHelperBC.bindTexture(resourceLocation);
            GlStateManager.color(1, 1, 1, 1);
            boolean mouseOver = isMouseOver(mouseX, mouseY);

            if (hasColourBorder) {
                drawColouredRect(xPos(), yPos(), xSize(), ySize(), 0xFF000000 | getColourBorder(mouseOver));
            }
            else if (hasColourBorderHover && mouseOver) {
                drawColouredRect(xPos(), yPos(), xSize(), ySize(), 0xFF000000 | colourBorderHover);
            }

            int w = xSize() - rightPad - leftPad;
            int h = ySize() - bottomPad - topPad;
            container.drawModalRectWithCustomSizedTexture(xPos() + leftPad, yPos() + topPad, 0, 0, w, h, w, h);
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    private void renderDownloading(float partialTicks) {
        boolean failed = resourceLocation.dlFailed;
        float failTicks = failed ? 0 : partialTicks;
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, failed ? 0xFFFF0000 : 0xFF00FF00);
        double anim = (64 + 10) * ((loadingTime + failTicks) / 100D);

        bindTexture(BCTextures.MODULAR_GUI);
        double texAnim = Math.max(0, (1 - (anim / 64)) * 48);
        double texX = xPos() + (xSize() / 2D) - 20;
        double texY = Math.max(yPos() - 48 + ((48 - texAnim) * 2), yPos());
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, getRenderZLevel() + 200);
        drawScaledCustomSizeModalRect(texX, texY + 1, failed ? 20 : 0, 18 + (24 - Math.min(24, 48 - texAnim)), 20, Math.min(Math.min(24, 48 - texAnim), texAnim), 40, Math.min(Math.min(48, (48 - texAnim) * 2), texAnim * 2), 256, 256);

        GlStateManager.translate(xPos() + xSize() / 2D, yPos() + ySize() - 32, 0);
        GlStateManager.rotate((BCClientEventHandler.elapsedTicks + partialTicks) * 3F, 0, 1, 0);
        GlStateManager.scale(-64, -64, -64);
        GlStateManager.rotate(-30, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        ScissorHelper.pushGuiScissor(mc, xPos(), maxYPos() - anim, xSize(), anim, screenWidth, screenHeight);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.pushMatrix();
        double shrink = 1 - MathHelper.clip((anim - 64) / 10D, 0, 1);
        GlStateManager.scale(shrink, shrink, shrink);
        mc.getRenderItem().renderItem(renderLoadingStack, FIXED);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        ScissorHelper.popScissor();

        Cuboid6 cuboid6 = new Cuboid6(-0.251, -0.251, -0.251, 0.251, 0.251, 0.251);
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        if (failed) {
            GlStateManager.color(0, 0, 0, 1);
        }
        else {
            GlStateManager.color(1, 1, 1, 1);
        }
        RenderUtils.drawCuboidOutline(cuboid6);
        GlStateManager.enableTexture2D();

        GlStateManager.color(fontRenderer.red, fontRenderer.blue, fontRenderer.green, 1);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOver(mouseX, mouseY)) {
            if (!linkTo.isEmpty() && container.linkDisplayTarget != null) {
                MGuiElementBase e = container.linkDisplayTarget;
                int width = fontRenderer.getStringWidth(linkTo);
                int height = fontRenderer.getWordWrappedHeight(linkTo, e.xSize()) + 4;
                zOffset += container.linkDisplayZOffset;
                drawColouredRect(e.xPos(), e.maxYPos() - height, Math.max(width + 4, e.xSize() / 2), height, 0x90000000);
                drawSplitString(fontRenderer, linkTo, e.xPos() + 2, e.maxYPos() - height + 2, e.xSize(), 0xc0c0c0, false);
                zOffset -= container.linkDisplayZOffset;
            }

            List<String> tooltip = new ArrayList<>();
            if (resourceLocation.dlFailed) {
                tooltip.add(TextFormatting.RED + I18n.format("gui.bc.downloading_image_failed.info"));
            }
            else if (!resourceLocation.dlFinished) {
                tooltip.add(TextFormatting.GREEN + I18n.format("gui.bc.downloading_image.info"));
            }

            if (enableTooltip && !this.tooltip.isEmpty()) {
                tooltip.addAll(this.tooltip);
            }

            if (!tooltip.isEmpty()) {
                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0 && GuiScreen.isShiftKeyDown()) {
                DLRSCache.clearResourceCache(imageURL);
                DLRSCache.clearFileCache(imageURL);
                container.layoutMarkdownElements();
                loadingTime = 0;
                return true;
            }
            else if (mouseButton != 1 && !linkTo.isEmpty()) {
                container.handleLinkClick(linkTo, mouseButton);
                return true;
            }
            else if (mouseButton == 1) {
                container.handleLinkClick(imageURL, mouseButton);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onUpdate() {
        if (resourceLocation != null) {
            if (!resourceLocation.dlFinished || resourceLocation.dlFailed || loadingTime > 0) {
                if (loadingTime == 0) {
                    renderLoadingStack = new ItemStack(LOADING_BLOCKS.get(rand.nextInt(LOADING_BLOCKS.size())));
                }
                loadingTime++;

                if (resourceLocation.dlFailed && loadingTime == 41) {
                    loadingTime = 40;
                }

                if (loadingTime >= 100) {
                    loadingTime = 0;
                    if (resourceLocation.dlStateChanged()) {
                        container.layoutMarkdownElements();
                        downloading = false;
                        return true;
                    }
                }
            }
        }

        return super.onUpdate();
    }
}
