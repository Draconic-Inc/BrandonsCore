package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.DLResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class ImageElement extends MDElementBase<ImageElement> {

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

        if (width == -1 && height == -1) {
            width = 32;
        }
        if (width != -1) {
            w = screenRelativeSize ? (int)(MathHelper.clip(width / 100D, 0, 1) * layout.getWidth()) : MathHelper.clip(width, 8, layout.getWidth());
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

        setSize(w, h);
        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
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
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
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

            if (enableTooltip && !tooltip.isEmpty()) {
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
        if (resourceLocation.dlStateChanged()) {
            container.layoutMarkdownElements();
            return true;
        }
        return super.onUpdate();
    }
}
