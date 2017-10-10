package com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.IPartBuilder;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MouseIntractable;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.Part;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.PartContainer;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class PartBuilderLink extends IPartBuilder {
    private static Pattern linkPat = Pattern.compile("(?<=[^\\\\]|^)(§link\\[[^]]*]\\{[^§]*})|(?<=[^\\\\]|^)(§link\\[[^]]*])");
    private static Pattern linkURL = Pattern.compile("(?<=§link\\[)([^]]*)(?=])");
    private static Pattern linkOPS = Pattern.compile("(?<=]\\{)(.*)(?=})");

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    @Override
    public int matches(String test) {
        Matcher matcher = linkPat.matcher(test);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    /**
     * //render options: text, vanilla, solid
     * //fillColour:        //For solid Render Mode
     * //borderColour:      //For solid Render Mode
     * //fillColourHover:        //For solid Render Mode
     * //borderColourHover:      //For solid Render Mode
     * //padding: Sets default padding for all sides
     * //leftPad:
     * //rightPad:
     * //topPad:
     * //bottomPad:
     * §link[http://www.google.com]{hover:"Link Hover Text",altText:"Alternate Link Text",render:}
     */
    @Override
    public String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel) {
        builtHeight = font.FONT_HEIGHT;
        //region Extract Link Data

        Matcher linkMatch = linkPat.matcher(markdown);
        String match;

        if (linkMatch.find(0)) {
            match = linkMatch.group();
        }
        else {
            LogHelperBC.error("Failed to build " + getClass().getSimpleName() + " This suggests a false match occurred which should not be possible!");
            return "";
        }

        Matcher urlMatch = linkURL.matcher(match);
        String url;
        if (!urlMatch.find() || (url = urlMatch.group()).isEmpty()) {
            return linkMatch.replaceFirst("[Broken Link. No Link Target Found]");
        }

        Matcher opsMatch = linkOPS.matcher(match);
        String ops = opsMatch.find() ? opsMatch.group() : "";

        final String render = Part.readOption(ops, "render", "text").toLowerCase();
        final int fillColour;
        final int borderColour;
        final int fillColourHover;
        final int borderColourHover;

        try {
            fillColour = Part.readColour(Part.readOption(ops, "fill_colour", "#000000"));
            borderColour = Part.readColour(Part.readOption(ops, "border_colour", "#FFFFFF"));
            fillColourHover = Part.readColour(Part.readOption(ops, "fill_colour_hover", "#000000"));
            borderColourHover = Part.readColour(Part.readOption(ops, "border_colour_hover", "#FFFFFF"));
        }
        catch (NumberFormatException e) {
            return linkMatch.replaceFirst("[Broken Link. Invalid Colour Value! Must be hex starting with 0x or # or a red,green,blue value]");
        }

        final int leftPad;
        final int rightPad;
        final int topPad;
        final int bottomPad;
        int padding = 2;

        try {
            padding = Integer.parseInt(Part.readOption(ops, "padding", "2"));
            leftPad = Integer.parseInt(Part.readOption(ops, "left_pad", String.valueOf(padding)));
            rightPad = Integer.parseInt(Part.readOption(ops, "right_pad", String.valueOf(padding)));
            topPad = Integer.parseInt(Part.readOption(ops, "top_pad", String.valueOf(padding)));
            bottomPad = Integer.parseInt(Part.readOption(ops, "bottom_pad", String.valueOf(padding)));
        }
        catch (NumberFormatException e) {
            return linkMatch.replaceFirst("[Broken Link. Invalid padding value! Must be an integer number]");
        }


        String text = Part.readOption(ops, "alt_text", url);
        String hover = Part.readOption(ops, "hover", "");

        //endregion

        //region Part Creation

        MouseIntractable mi = new MouseIntractable() {
            @Override
            public boolean onClick(int mouseX, int mouseY, int button) {
                container.linkClick(url, button);
                if (render.equals("vanilla") || render.equals("solid")) {
                    container.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
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


        font.resetStyles();
        BCFontRenderer.setStileToggleMode(true);

        if (render.equals("text")) {
            if (text.length() > 0) {
                List<String> words = Part.splitOnSpace(text);

                for (String word : words) {
                    if (word.length() == 0) continue;

                    int maxWordWidth = font.getStringWidth(word);

                    boolean wrapped = false;
                    if (finalXPos + maxWordWidth >= elementRight) {
                        finalXPos = elementLeft;
                        finalYPos = Math.max(finalYPos + font.FONT_HEIGHT, nextYLevel);
                        wrapped = true;
                    }

                    char fc = word.charAt(0);
                    if ((fc == 32 || fc == 10) && wrapped && word.length() > 0) {
                        word = word.substring(1);
                        boolean bold = font.boldStyle;
                        maxWordWidth = font.getStringWidth(word);
                        font.boldStyle = bold;
                    }

                    final String line = word;
                    Part part = new Part(container) {
                        @Override
                        public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                            String text = TextFormatting.DARK_BLUE + "" + TextFormatting.UNDERLINE + line + TextFormatting.DARK_BLUE + "" + TextFormatting.UNDERLINE;
                            if (mi.isMouseOver) {
                                text = TextFormatting.ITALIC + text + TextFormatting.ITALIC;
                            }

                            font.drawString(text, xPos, yPos, colour, shadow);
                        }
                    };

                    mi.parts.add(part);

                    part.width = maxWordWidth;
                    part.height = font.FONT_HEIGHT;
                    parts.add(part);
                    finalXPos += maxWordWidth;
                }
            }
            builtHeight = (finalYPos - yPos) + font.FONT_HEIGHT + 1;
        }
        else if (render.equals("vanilla") || render.equals("solid")) {

            int width = font.getStringWidth(text) + leftPad + rightPad;

            Part part = new Part(container) {
                @Override
                public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                    if (render.equals("vanilla")) {
                        container.renderVanillaButtonTexture(xPos, yPos, width, height, mi.isMouseOver, false);
                    }
                    else {
                        int fill = mi.isMouseOver ? fillColourHover : fillColour;
                        int border = mi.isMouseOver ? borderColourHover : borderColour;
                        container.drawBorderedRect(xPos, yPos, width, height, 1, 0xFF000000 | fill, 0xFF000000 | border);
                    }

                    GlStateManager.color(font.red, font.blue, font.green, 1);
                    font.drawString(text, xPos + leftPad, yPos + topPad, colour, shadow);
                }
            };

            mi.parts.add(part);
            part.width = width;
            part.height = font.FONT_HEIGHT + topPad + bottomPad;
            parts.add(part);
            finalXPos += width;
            builtHeight = (finalYPos - yPos) + part.height + 1;
        }
        else {
            BCFontRenderer.setStileToggleMode(false);
            return linkMatch.replaceFirst("[Broken Link. Invalid Render Type! Valid types are text, vanilla and solid]");
        }

        BCFontRenderer.setStileToggleMode(false);

        //endregion

        return linkMatch.replaceFirst("");
    }
}
