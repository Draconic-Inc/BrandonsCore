package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.integration.IRecipeRenderer;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;

/**
 * Each line of markdown is given to an item container. This container creates and
 * manages the individual part represented by the line of markdown.
 * An "part" refers to a line of text, a link, an image, an entity renderer or a stack renderer.
 */
public class PartContainer extends MGuiElementBase<PartContainer> {
    private static Pattern leftPat = Pattern.compile("(?<=[^\\\\]|^)(§align:left)");
    private static Pattern centrePat = Pattern.compile("(?<=[^\\\\]|^)(§align:center)");
    private static Pattern rightPat = Pattern.compile("(?<=[^\\\\]|^)(§align:right)");
    private static Pattern shadowPat = Pattern.compile("(?<=[^\\\\]|^)(§shadow)");

    public int colour = 0xFF3c3f41;
    public boolean shadow = false;
    public List<MouseIntractable> mouseIntractables = new ArrayList<>();
    public GuiAlign align;

    private LinkedList<Part> containerParts = new LinkedList<>();
    public IRecipeRenderer hoveredRecipe = null;
    public GuiMarkdownElement element;

    public PartContainer(GuiMarkdownElement element) {
        this.element = element;
        this.align = element.currentAlign;
    }

    /**
     * Takes the entire list of markdown lines, Reads 1 or more liens then returns the remaining lines.
     * This will usually only take the first line except in cases like tables which are made of more than
     * 1 line.
     */
    public void parseMarkdown(LinkedList<String> markdownLines) {
        readParagraphFormatting(markdownLines);

        if (markdownLines.isEmpty()) return;

        if (checkHeading(markdownLines) || checkRule(markdownLines)) {
            return;
        }

        applyBuilders(markdownLines);
    }

    //region Pre Builder Part Checks

    /**
     * Checks if the next line is a heading and if so builds the heading part.
     *
     * @return true if a heading was parsed.
     */
    private boolean checkHeading(LinkedList<String> markdownLines) {
        String line = markdownLines.getFirst();
        String line2 = markdownLines.size() > 1 ? markdownLines.get(1) : "";
        int headingSize = 0;

        if (line.startsWith("#") || Part.isAllChar(line2, '=') || Part.isAllChar(line2, '-')) {
            while (headingSize < line.length() && line.charAt(headingSize) == '#') headingSize++;

            boolean underlineHeadding = false;
            if (headingSize == 0 && Part.isAllChar(line2, '=')) {
                headingSize = 1;
                underlineHeadding = true;
            }
            else if (headingSize == 0 && Part.isAllChar(line2, '-')) {
                headingSize = 2;
                underlineHeadding = true;
            }
            else if (headingSize <= 0 || headingSize > 6) {
                return false;
            }
            markdownLines.removeFirst();

            if (headingSize > 0 && headingSize <= 6) {

                headingSize = 7 - headingSize;
                final String headingText;
                if (!underlineHeadding) {
                    headingText = Part.trim(Part.applyTextFormatting(line), '#');
                }
                else {
                    markdownLines.removeFirst();
                    headingText = Part.applyTextFormatting(line);
                }

                float scaleFactor = 1F + (headingSize / 3F);
                List<String> list = fontRenderer.listFormattedStringToWidth(headingText, (int) (xSize() / scaleFactor));

                Part part = new Part(this) {
                    @Override
                    public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                        for (String string : list) {

                            float x = xPos;
                            float y = yPos + (font.FONT_HEIGHT * scaleFactor * list.indexOf(string));
                            float scaledWidth = font.getStringWidth(string) * scaleFactor;

                            switch (align) {
                                case CENTER:
                                    x = xPos + (xSize() / 2F) - (scaledWidth / 2F);
                                    break;
                                case RIGHT:
                                    x = xPos + (xSize() - scaledWidth);
                                    break;
                            }

                            GlStateManager.pushMatrix();
                            GlStateManager.translate(x, y, 0);
                            GlStateManager.scale(scaleFactor, scaleFactor, 1);
                            GlStateManager.translate(-x, -y, 0);

                            font.drawString(string, x, y, colour, shadow);

                            GlStateManager.popMatrix();
                        }

                    }
                };

                part.width = xSize();
                part.height = MathHelper.ceil(fontRenderer.FONT_HEIGHT * scaleFactor * list.size() * 1.1);
                setYSize(part.height);

                containerParts.add(part);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the next line is a horizontal rule and if so builds the rule part.
     *
     * @return true if a rule was parsed.
     */
    private boolean checkRule(LinkedList<String> markdownLines) {
        String line = markdownLines.getFirst();

        Pattern rulePat = Pattern.compile("(?<=[^\\\\]|^)(§rule\\[[^]]*])");
        Pattern ruleOptionsPat = Pattern.compile("(?<=§rule\\[)([^]]*)(?=])");

        Matcher ruleMatch = rulePat.matcher(line);
        Matcher ruleOptMatch = ruleOptionsPat.matcher(line);

        if (ruleMatch.find()) {
            String options = ruleOptMatch.find() ? ruleOptMatch.group() : "";
            int ruleHeight;
            int topPadding;
            int bottomPadding;
            int ruleWidth;

            if (options.contains("colour:")) {
                try {
                    colour = Part.readColour(Part.readOption(options, "colour", "#606060"));
                }
                catch (NumberFormatException e) {
                    Part.addError(markdownLines, "Invalid Colour Value! Valid formats are 0xRRGGBB or #RRGGBB (hex) or Red,Green,Blue (RGB)", line);
                    return false;
                }
            }

            if (options.contains("align:")) {
                try {
                    align = GuiAlign.valueOf(Part.readOption(options, "align", "left").toUpperCase());
                }
                catch (Throwable e) {
                    Part.addError(markdownLines, "Invalid Align Value! Valid values are left, center and right", line);
                    return false;
                }
            }

            try {
                String value = Part.readOption(options, "width", "100%");
                int v;
                if (value.endsWith("%")) {
                    v = (int) ((Double.parseDouble(value.replace("%", "")) / 100D) * xSize());
                }
                else if (value.endsWith("px")) {
                    v = Integer.parseInt(value.replace("px", ""));
                }
                else { throw new NumberFormatException(); }
                ruleWidth = MathHelper.clip(v, 0, xSize());
            }
            catch (NumberFormatException e) {
                Part.addError(markdownLines, "Height value must be a number!", line);
                return false;
            }

            try {
                ruleHeight = Integer.parseInt(Part.readOption(options, "height", "5"));
            }
            catch (NumberFormatException e) {
                Part.addError(markdownLines, "Height value must be a number!", line);
                return false;
            }

            try {
                topPadding = Integer.parseInt(Part.readOption(options, "top_padding", "5"));
                bottomPadding = Integer.parseInt(Part.readOption(options, "bottom_padding", "5"));
            }
            catch (NumberFormatException e) {
                Part.addError(markdownLines, "Padding value(s) must be a number!", line);
                return false;
            }

            Part part = new Part(this) {
                @Override
                public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                    if (ruleHeight > 0 && ruleWidth > 0) {
                        float x = xPos;
                        switch (align) {
                            case CENTER:
                                x = xPos + (xSize() / 2F) - (ruleWidth / 2F);
                                break;
                            case RIGHT:
                                x = xPos + (xSize() - ruleWidth);
                                break;
                        }
                        drawColouredRect(x, yPos + topPadding, ruleWidth, ruleHeight, 0xFF000000 | colour);
                    }
                }
            };

            part.width = xSize();
            part.height = MathHelper.ceil(topPadding + ruleHeight + bottomPadding);
            setYSize(part.height);
            containerParts.add(part);

            markdownLines.removeFirst();
            return true;
        }

        return false;
    }

    //endregion

    /**
     * Runs the next markdown line through the part builders to build the associated parts.
     * This method should be called last after all special case parsing such as headings, tables and rule's
     */
    private void applyBuilders(LinkedList<String> markdownLines) {
        String next = markdownLines.removeFirst();
        boolean empty = next.isEmpty();

        fontRenderer.resetStyles();
        BCFontRenderer.setStileToggleMode(true);

        int nextPart = next.length();
        int xPos = getInsetRect().x;
        int yPos = getInsetRect().y;
        int maxYPos = yPos;
        while (next.length() > 0) {
            for (IPartBuilder builder : GuiMarkdownElement.partBuilders) {
                int i = builder.matches(next);
                if (i == 0) {
                    builder.finalXPos = xPos;
                    builder.finalYPos = yPos;
                    int builderStartY = yPos;
                    next = builder.build(fontRenderer, next, nextPart, fontRenderer, this, containerParts, getInsetRect().x, getInsetRect().x + getInsetRect().width, xPos, yPos, maxYPos);
                    nextPart = next.length();
                    xPos = builder.finalXPos;
                    yPos = builder.finalYPos;
                    if (builderStartY + builder.builtHeight > maxYPos) {
                        maxYPos = builderStartY + builder.builtHeight;
                    }
                    break;
                }
                else if (i > 0 && i < nextPart) {
                    nextPart = i;
                }
            }
        }
        if (empty) {
            setYSize(fontRenderer.FONT_HEIGHT);
        }
        else {
            setYSize(maxYPos - getInsetRect().y);
        }

        BCFontRenderer.setStileToggleMode(false);
    }

    /**
     * Reads paragraph formatting tags such as §colour, §align and §shadow
     */
    private void readParagraphFormatting(LinkedList<String> markdownLines) {
        String line = markdownLines.getFirst();

        //region Read Paragraph formatting flags
        boolean formatLine = false;
        if (leftPat.matcher(line).find()) {
            element.currentAlign = LEFT;
            line = line.replace("§align:left", "");
            formatLine = true;
        }
        else if (centrePat.matcher(line).find()) {
            element.currentAlign = CENTER;
            line = line.replace("§align:center", "");
            formatLine = true;
        }
        else if (rightPat.matcher(line).find()) {
            element.currentAlign = GuiAlign.RIGHT;
            line = line.replace("§align:right", "");
            formatLine = true;
        }
        if (shadowPat.matcher(line).find()) {
            shadow = true;
            line = line.replace("§shadow", "");
            formatLine = true;
        }

        align = element.currentAlign;

        Matcher c = GuiMarkdownElement.colourPat.matcher(line);
        Matcher cx = GuiMarkdownElement.colourExtractPat.matcher(line);

        try {
            if (c.find() && cx.find()) {
                formatLine = true;
                String raw = cx.group();
                colour = Part.readColour(raw);
                line = c.replaceAll("");
            }
        }
        catch (NumberFormatException e) {
            markdownLines.removeFirst();
            markdownLines.addFirst("§4Invalid colour value! Must be a hex starting with 0x or # or it can be separate integer or float R G B values separated by comma's. Float values must contain a decimal point.§4 " + line);
            return;
        }


        if (formatLine) {
            markdownLines.removeFirst();
            if (!line.isEmpty()) {
                while (line.startsWith(" ") && line.length() > 1) line = line.substring(1);
                markdownLines.addFirst(line);
            }
            readParagraphFormatting(markdownLines);
        }

        //endregion
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (MouseIntractable mi : mouseIntractables) {
            if (mi.isMouseOver && mi.onClick(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void linkClick(String linkTarget, int button) {
        LogHelperBC.dev("Link Clicked! " + linkTarget);
        if (element.linkListener != null) {
            element.linkListener.accept(linkTarget, button);
        }
    }

    public void imageClick(String linkTarget, int button) {
        LogHelperBC.dev("Image Clicked! " + linkTarget);
        if (element.imageListener != null) {
            element.imageListener.accept(linkTarget, button);
        }
    }

    public void recipeClick(ItemStack stackClicked, int button) {
        LogHelperBC.dev("Recipe Clicked! " + stackClicked);
        if (element.recipeListener != null) {
            element.recipeListener.accept(stackClicked, button);
        }
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        fontRenderer.resetStyles();
        BCFontRenderer.setStileToggleMode(true);

        int xPos = getInsetRect().x;
        int yPos = getInsetRect().y;
        int maxWidth = getInsetRect().width;
        int prevHeight = 0;

        int alignOffset = 0;

        for (int i = 0; i < containerParts.size(); i++) {
            Part part = containerParts.get(i);
            if (xPos + part.width > getInsetRect().x + maxWidth) {
                xPos = getInsetRect().x;
                yPos += prevHeight;
                prevHeight = 0;
            }

            //Calculate offset for alignment
            if (xPos == getInsetRect().x && align != GuiAlign.LEFT) {
                int width = 0;
                for (int j = i; j < containerParts.size(); j++) {
                    Part p = containerParts.get(j);
                    if (width + p.width > maxWidth) {
                        break;
                    }
                    width += p.width;
                }

                alignOffset = align == CENTER ? (maxWidth - width) / 2 : (maxWidth - width);
            }

            part.lastXPos = xPos + alignOffset;
            part.lastYPos = yPos;
            part.render(fontRenderer, xPos + alignOffset, yPos, mouseX, mouseY, colour, shadow, partialTicks);
            prevHeight = Math.max(part.height, prevHeight);
            xPos += part.width;
        }

        GlStateManager.color(1, 1, 1, 1);
        BCFontRenderer.setStileToggleMode(false);
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        BCFontRenderer.setStileToggleMode(false);
        fontRenderer.resetStyles();
        for (MouseIntractable mi : mouseIntractables) {
            if (mi.isMouseOver && (!mi.hoverText.isEmpty() || mi.errorText != null)) {
                if (mi.errorText != null) {
                    drawHoveringText(Collections.singletonList(mi.errorText), mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                }
                else {
                    drawHoveringText(mi.hoverText, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                }
                return true;
            }
            else if (mi.isMouseOver && mi.hoverStack != null) {
                List<String> list = mi.hoverStack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips);

                for (int i = 0; i < list.size(); ++i) {
                    if (i == 0) {
                        list.set(i, mi.hoverStack.getRarity().rarityColor + list.get(i));
                    }
                    else {
                        list.set(i, TextFormatting.GRAY + list.get(i));
                    }
                }

                GuiUtils.preItemToolTip(mi.hoverStack);
                this.drawHoveringText(mi.hoverStack, list, mouseX, mouseY, screenWidth, screenHeight, -1, fontRenderer);
                GuiUtils.postItemToolTip();
                return true;
            }
        }

        if (hoveredRecipe != null && isMouseOver(mouseX, mouseY)) {
            hoveredRecipe.renderOverlay(minecraft, mouseX, mouseY);
            hoveredRecipe = null;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        int mouseX = Mouse.getX() * screenWidth / this.mc.displayWidth;
        int mouseY = screenHeight - Mouse.getY() * screenHeight / this.mc.displayHeight - 1;
        mouseIntractables.forEach(mouseInteract -> mouseInteract.updateMouseOver(mouseX, mouseY));
        return super.onUpdate();
    }
}
