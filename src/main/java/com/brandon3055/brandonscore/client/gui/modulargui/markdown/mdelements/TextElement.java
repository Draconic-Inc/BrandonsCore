package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MarkdownTextRenderer;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.MarkerElement.Type.NEW_LINE;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class TextElement extends MDElementBase<TextElement> {

    private String text;
    private MarkdownTextRenderer textRenderer;
    public Supplier<Integer> colour = null;
    public boolean shadow = false;
    private double textScale;

    //Heading 0 = plain text element
    public TextElement(String text, int heading) {
        this.boundless = true;
        this.text = text;
        this.hasSubParts = true;

        if (heading == 0) {
            this.textScale = 1;
        }
        else {
            heading = 7 - heading;
            textScale = 1D + (heading / 3D);
        }
        textRenderer = new MarkdownTextRenderer(this, fontRenderer);
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {

        if (layout.getWidth() < 5) return;
        toRemove.addAll(subParts);
        subParts.clear();

        double fHeight = (fontRenderer.lineHeight + 1) * textScale;
        String text = this.text;

        boolean newln = false;
        while (text.length() > 0) {
            int avalibleWidth = layout.getWidth() - layout.getCaretXOffset();
            int nextSplit = fontRenderer.sizeStringToWidth(text, (int) Math.ceil(avalibleWidth / textScale));
            if (nextSplit == 0 && avalibleWidth == layout.getWidth()) {
                nextSplit = 1;
            }
            if (nextSplit == 0) {
                layout.newLine(0);
                MarkerElement marker = new MarkerElement(NEW_LINE);
                subParts.add(marker);
                newln = true;
                continue;
            }

            String nextPart = text.substring(0, nextSplit);
            nextPart = nextPart.replace("\n", "");

            //Remove leading spaces when wrapping.
            if (newln && nextPart.length() > 1 && nextPart.startsWith(" ")) {
                nextPart = nextPart.substring(1);
            }

            text = text.substring(nextSplit);
            double nextWidth = fontRenderer.width(nextPart) * textScale;

            TextElementPart part = new TextElementPart(this, nextPart, textScale, shadow, colour);
            part.setSize((int) Math.ceil(nextWidth), (int) Math.ceil(fHeight));
            addChild(part);
            subParts.add(part);
            part.layoutElement(layout, lineElement);

            newln = false;
        }
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        textRenderer.reset();
//        fontRenderer.resetStyles(); //TODO font renderer changes
//        BCFontRenderer.setStileToggleMode(true);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        BCFontRenderer.setStileToggleMode(false);
//        fontRenderer.resetStyles();
    }



    private static class TextElementPart extends MDElementBase<TextElementPart> {
        private TextElement element;
        private final String text;
        private final double scale;
        private boolean shadow;
        private Supplier<Integer> colour;

        public TextElementPart(TextElement element, String text, double scale, boolean shadow, Supplier<Integer> colour) {
            this.element = element;
            this.text = text;
            this.scale = scale;
            this.shadow = shadow;
            this.colour = colour;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 0.2, 0, 0xFF00FF00);
            if (scale > 1) {
//                RenderSystem.pushMatrix();
//                RenderSystem.translated(xPos(), yPos(), 0);
//                RenderSystem.scaled(scale, scale, 1);
////                fontRenderer.drawString(text, 0, 0, colour.get(), shadow);
//                drawString(fontRenderer, text, 0, 0, colour.get(), shadow);
//                RenderSystem.popMatrix();
            }
            else {
//                fontRenderer.drawString(text, xPos(), yPos(), colour.get(), shadow);
                element.textRenderer.drawFormattedString(text, xPos(), yPos(), colour.get(), shadow);
            }
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }
}
