package com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.DLResourceLocation;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.IPartBuilder;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MouseIntractable;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.Part;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.PartContainer;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class PartBuilderImage extends IPartBuilder {
    private static Pattern imgPat = Pattern.compile("(?<=[^\\\\]|^)(§img\\[[^]]*]\\{[^§]*})|(?<=[^\\\\]|^)(§img\\[[^]]*])");
    private static Pattern imgURL = Pattern.compile("(?<=§img\\[)([^]]*)(?=])");
    private static Pattern imgOPS = Pattern.compile("(?<=]\\{)(.*)(?=})");

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    @Override
    public int matches(String test) {
        Matcher matcher = imgPat.matcher(test);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    /**
     * §img[http://url.png]
     * §img[http://url.png]{hover:"Hover text for the image"}
     * <p>
     * //width and height parameters accepts both a % value and a px value where percent is the percent of the page width and px is a precise
     * //Note you only need to set width or height. Setting both to values that do not match the image aspect ratio will distort the image
     * //number of pixels. Defaults to 100%
     */
    @Override
    public String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel) {
        builtHeight = font.FONT_HEIGHT;
        //region Extract Image Data

        Matcher linkMatch = imgPat.matcher(markdown);
        String match;

        if (linkMatch.find(0)) {
            match = linkMatch.group();
        }
        else {
            LogHelperBC.error("Failed to build " + getClass().getSimpleName() + " This suggests a false match occurred which should not be possible!");
            return "";
        }

        Matcher urlMatch = imgURL.matcher(match);
        String url;
        if (!urlMatch.find() || (url = urlMatch.group()).isEmpty()) {
            return linkMatch.replaceFirst("[Broken Image. No image URL Found]");
        }

        Matcher opsMatch = imgOPS.matcher(match);
        String ops = opsMatch.find() ? opsMatch.group() : "";

        final int borderColour;
        final int borderColourHover;

        if (ops.toLowerCase().contains("border_colour") || ops.toLowerCase().contains("border_colour_hover")) {
            try {
                borderColour = Part.readColour(Part.readOption(ops, "border_colour", "#FFFFFF"));
                borderColourHover = Part.readColour(Part.readOption(ops, "border_colour_hover", "#" + Integer.toHexString(borderColour)));
            }
            catch (NumberFormatException e) {
                return linkMatch.replaceFirst("[Broken Image. Invalid Colour Value! Must be hex starting with 0x or # or a red,green,blue value]");
            }
        }
        else {
            borderColour = -1;
            borderColourHover = -1;
        }

        final int leftPad;
        final int rightPad;
        final int topPad;
        final int bottomPad;
        int padding;

        try {
            padding = Integer.parseInt(Part.readOption(ops, "padding", "1"));
            leftPad = Integer.parseInt(Part.readOption(ops, "left_pad", String.valueOf(padding)));
            rightPad = Integer.parseInt(Part.readOption(ops, "right_pad", String.valueOf(padding)));
            topPad = Integer.parseInt(Part.readOption(ops, "top_pad", String.valueOf(padding)));
            bottomPad = Integer.parseInt(Part.readOption(ops, "bottom_pad", String.valueOf(padding)));
        }
        catch (NumberFormatException e) {
            return linkMatch.replaceFirst("[Broken Image. Invalid padding value! Must be an integer number]");
        }

        int width;
        int height;
        try {
            String widthString = Part.readOption(ops, "width", "-1px");
            String heightString = Part.readOption(ops, "height", "-1px");
            int w;
            if (widthString.endsWith("%")) {
                w = (int) ((Double.parseDouble(widthString.replace("%", "")) / 100D) * container.xSize());
            }
            else if (widthString.endsWith("px")) {
                w = Integer.parseInt(widthString.replace("px", ""));
            }
            else { throw new NumberFormatException(); }
            int h;
            int maxY = container.getParent() != null ? container.getParent().ySize() : 255;
            if (heightString.endsWith("%")) {
                h = (int) ((Double.parseDouble(heightString.replace("%", "")) / 100D) * maxY);
            }
            else if (heightString.endsWith("px")) {
                h = Integer.parseInt(heightString.replace("px", ""));
            }
            else { throw new NumberFormatException(); }
            width = MathHelper.clip(w, -1, container.xSize());
            height = MathHelper.clip(h, -1, maxY);
        }
        catch (NumberFormatException e) {
            return linkMatch.replaceFirst("[Broken Image. Invalid width or height value! Must be an integer number]");
        }

        String hover = Part.readOption(ops, "hover", "");

        //endregion

        DLResourceLocation resourceLocation = DLRSCache.getResource(url);

        MouseIntractable mi = new MouseIntractable() {
            @Override
            public boolean onClick(int mouseX, int mouseY, int button) {
                container.imageClick(url, button);
                return true;
            }
        };

        if (!hover.isEmpty()) {
            if (hover.contains("\\n")) {
                mi.hoverText.addAll(Arrays.asList(hover.split("(\\\\n)")));
            }
            else {
                mi.hoverText.add(hover);
            }
        }
        container.mouseIntractables.add(mi);

        Part part = new Part(container) {
            @Override
            public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                DLResourceLocation resourceLocation = DLRSCache.getResource(url);
                ResourceHelperBC.bindTexture(resourceLocation);

                if (resourceLocation.dlStateChanged()) {
                    container.element.reload();
                    return;
                }

                if (resourceLocation.dlFailed) {
                    mi.errorText = container.element.imageDLFailedMessage;
                }
                else if (!resourceLocation.dlFinished) {
                    mi.errorText = "Downloading...";
                }
                else if (mi.errorText != null) {
                    mi.errorText = null;
                }


                if (borderColour != -1) {
                    int border = mi.isMouseOver ? borderColourHover : borderColour;
                    container.drawColouredRect(xPos, yPos, width, height, 0xFF000000 | border);
                }

                int w = width - rightPad - leftPad;
                int h = height - bottomPad - topPad;
                GlStateManager.color(1, 1, 1, 1);
                container.drawModalRectWithCustomSizedTexture(xPos + leftPad, yPos + topPad, 0, 0, w, h, w, h);
                GlStateManager.color(font.red, font.blue, font.green, 1);
            }
        };

        mi.parts.add(part);

        //region Calculate width and height
        if (width == -1 && height == -1) width = 28;
        if (width != -1) {
            part.width = width;
            if (height == -1) {
                if (resourceLocation.sizeSet) {
                    part.height = (int) (((double) resourceLocation.height / (double) resourceLocation.width) * width);
                }
                else {
                    part.height = width;
                }
            }
        }
        if (height != -1) {
            part.height = height;
            if (width == -1) {
                if (resourceLocation.sizeSet) {
                    part.width = (int) (((double) resourceLocation.width / (double) resourceLocation.height) * height);
                }
                else {
                    part.width = height;
                }
            }
        }
        //endregion

        if (finalXPos + width > elementRight) {
            finalXPos = elementLeft;
            finalYPos = nextYLevel;
        }

        parts.add(part);
        finalXPos += width;
        builtHeight = (finalYPos - yPos) + part.height + 1;


        return linkMatch.replaceFirst("");
    }
}
