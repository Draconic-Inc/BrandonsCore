package com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.IPartBuilder;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.Part;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.PartContainer;

import java.util.LinkedList;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.GuiMarkdownElement.profiler;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class PartBuilderText extends IPartBuilder {

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    @Override
    public int matches(String test) {
        return 0;
    }

    @Override
    public String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel) {
        profiler.startSection("Build Text");
        String text = Part.applyTextFormatting(markdown.substring(0, nextPart));

        font.resetStyles();
        BCFontRenderer.setStileToggleMode(true);

        if (text.length() > 0) {
            LinkedList<String> rawWords = Part.splitOnSpace(text);
            LinkedList<String> words = new LinkedList<>();

            //This splits up any massive single words that are too long to fit on the page
            for (String word : rawWords) {
                if (word.length() < 32) {
                    words.add(word);
                }
                else if (font.getStringWidth(word) < elementRight - elementLeft) {
                    words.add(word);
                }
                else {
                    int startI = 0;
                    int endI = 31;
                    while (endI <= word.length()) {
                        String part = word.substring(startI, endI);
                        if (endI == word.length() - 1) {
                            words.add(part);
                            break;
                        }
                        else if (font.getStringWidth(part) > (elementRight - elementLeft) - 10){
                            words.add(part);
                            startI = endI;
                        }
                        endI++;
                    }
                }
            }

            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                if (word.length() == 0) continue;

                int maxWordWidth = font.getStringWidth(word);

                boolean wrapped = false;
                if (finalXPos + maxWordWidth > elementRight) {
                    finalXPos = elementLeft;
                    finalYPos = Math.max(finalYPos + font.FONT_HEIGHT, nextYLevel);
                    wrapped = true;
                }

                boolean bold = font.boldStyle;

                //Some funky shit requires to fix some edge case wrapping issues
                if (i + 1 < words.size()) {
                    String nextWord = words.get(i + 1);
                    if (finalXPos + maxWordWidth + font.getStringWidth(nextWord) > elementRight && finalXPos + maxWordWidth + font.getStringWidth(nextWord) - 4 <= elementRight) {
                        maxWordWidth += 4;
                    }
                }
                font.boldStyle = bold;

                char fc = word.charAt(0);
                if ((fc == 32 || fc == 10) && wrapped && word.length() > 0) {
                    word = word.substring(1);
                    bold = font.boldStyle;
                    maxWordWidth = font.getStringWidth(word);
                    font.boldStyle = bold;
                }

                final String line = word;
                Part part = new Part(container) {
                    @Override
                    public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                        font.drawString(line, xPos, yPos, colour, shadow);
                    }
                };

                part.width = maxWordWidth;
                part.height = font.FONT_HEIGHT;
                parts.add(part);
                finalXPos += maxWordWidth;
            }
        }
        builtHeight = (finalYPos - yPos) + font.FONT_HEIGHT + 1;

        BCFontRenderer.setStileToggleMode(false);

        return markdown.substring(nextPart);
    }
}
