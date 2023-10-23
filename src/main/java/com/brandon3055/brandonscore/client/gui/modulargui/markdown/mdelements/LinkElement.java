package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class LinkElement extends MDElementBase<LinkElement> {

    private MDElementContainer container;
    private String linkTarget;
    public String altText = "";
    public Style linkStyle = Style.TEXT;
    public boolean shadow = false;
    private String displayText = "";
    public Supplier<Integer> defaultColour = null;

    public LinkElement(MDElementContainer container, String linkTarget) {
        this.container = container;
        this.linkTarget = linkTarget;
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        displayText = altText.isEmpty() ? linkTarget : altText;
        if (layout.getWidth() < 10) return;

        int avalibleTextWidth = layout.getWidth() - leftPad - rightPad;
        List<String> wrappedText = fontRenderer.listFormattedStringToWidth(displayText, avalibleTextWidth);
        int textWidth = 0;
        for (String text : wrappedText) {
            textWidth = Math.max(textWidth, fontRenderer.width(text));
        }
        int textHeight = fontRenderer.wordWrapHeight(displayText, avalibleTextWidth);

        setSize(textWidth + leftPad + rightPad, textHeight + topPad + bottomPad);

        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        boolean isMouseOver = isMouseOver(mouseX, mouseY);
        int colour = hasColour || (hasColourHover && isMouseOver) ? getColour(isMouseOver) : defaultColour.get();
        int colourBorder = hasColourBorder || (hasColourBorderHover && isMouseOver) ? getColourBorder(isMouseOver) : colour;
        int drawX = xPos() + leftPad;
        int drawWidth = xSize() - leftPad - rightPad;

//        if (linkStyle == Style.TEXT) {
//            String text = ChatFormatting.UNDERLINE + displayText;
//            if (isMouseOver) {
//                text = ChatFormatting.ITALIC + text;
//            }
//
//            drawCenteredSplitString(fontRenderer, text, drawX + drawWidth / 2, yPos() + topPad, xSize() - leftPad - rightPad, colour, shadow);
//        } else {
//            int textColour;
//
//            if (linkStyle == Style.VANILLA) {
//                renderVanillaButtonTexture(xPos(), yPos(), xSize(), ySize(), isMouseOver, false);
//                textColour = colour;
//            } else {
//                drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0xFF000000 | colour, 0xFF000000 | colourBorder);
//                textColour = defaultColour.get();
//            }
//
//            drawCenteredSplitString(fontRenderer, displayText, drawX + drawWidth / 2, yPos() + topPad, xSize() - leftPad - rightPad, textColour, shadow);
//        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOver(mouseX, mouseY)) {
            if (container.linkDisplayTarget != null) {
                GuiElement e = container.linkDisplayTarget;
                int width = fontRenderer.width(linkTarget);
                int height = fontRenderer.wordWrapHeight(linkTarget, e.xSize()) + 4;
                zOffset += container.linkDisplayZOffset;
                drawColouredRect(e.xPos(), e.maxYPos() - height, Math.min(Math.max(width + 4, e.xSize() / 2), e.xSize()), height, 0x90000000);
                drawSplitString(fontRenderer, linkTarget, e.xPos() + 2, e.maxYPos() - height + 2, e.xSize(), 0xc0c0c0, false);
                zOffset -= container.linkDisplayZOffset;
            }

            if (enableTooltip && !tooltip.isEmpty()) {
                PoseStack poseStack = new PoseStack();
                poseStack.translate(0, 0, getRenderZLevel());
                renderTooltip(poseStack, tooltip.stream().map(TextComponent::new).collect(Collectors.toList()), mouseX, mouseY);
            }
            return true;
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            container.handleLinkClick(linkTarget, mouseButton);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public enum Style {
        TEXT,
        SOLID,
        VANILLA
    }
}
