package com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.IPartBuilder;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MouseIntractable;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.Part;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.PartContainer;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.integration.IRecipeRenderer;
import com.brandon3055.brandonscore.integration.JeiHelper;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.RIGHT;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.GuiMarkdownElement.profiler;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class PartBuilderRecipe extends IPartBuilder {
    private static Pattern recipePat = Pattern.compile("(?<=[^\\\\]|^)(§recipe\\[[^§]*]\\{[^§]*})|(?<=[^\\\\]|^)(§recipe\\[[^§ ]*])");
    private static Pattern recipeStack = Pattern.compile("(?<=§recipe\\[)(.*)(?=][{])|(?<=§recipe\\[)(.*)(?=])");
    private static Pattern recipeOPS = Pattern.compile("(?<=]\\{)(.*)(?=})");

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    @Override
    public int matches(String test) {
        Matcher matcher = recipePat.matcher(test);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    @Override
    public String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel) {
        profiler.startSection("Build Recipe");
        builtHeight = font.FONT_HEIGHT;
        //region Extract Recipe Data

        Matcher recipeMatch = recipePat.matcher(markdown);
        String match;

        if (recipeMatch.find(0)) {
            match = recipeMatch.group();
        }
        else {
            LogHelperBC.error("Failed to build " + getClass().getSimpleName() + " This suggests a false match occurred which should not be possible!");
            return "";
        }

        Matcher stackMatch = recipeStack.matcher(match);
        String stackString;
        if (!stackMatch.find() || (stackString = stackMatch.group()).isEmpty()) {
            return recipeMatch.replaceFirst("[Broken recipe. No stack string Found]");
        }

        Matcher opsMatch = recipeOPS.matcher(match);
        String ops = opsMatch.find() ? opsMatch.group() : "";

        final int borderColour;
        final int borderColourHover;

        if (ops.toLowerCase().contains("border_colour") || ops.toLowerCase().contains("border_colour_hover")) {
            try {
                borderColour = Part.readColour(Part.readOption(ops, "border_colour", "#FFFFFF"));
                borderColourHover = Part.readColour(Part.readOption(ops, "border_colour_hover", "#" + Integer.toHexString(borderColour)));
            }
            catch (NumberFormatException e) {
                return recipeMatch.replaceFirst("[Broken Recipe. Invalid Colour Value! Must be hex starting with 0x or # or a red,green,blue value]");
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
        final int spacing;
        int padding;

        try {
            padding = Integer.parseInt(Part.readOption(ops, "padding", "1"));
            leftPad = Integer.parseInt(Part.readOption(ops, "left_pad", String.valueOf(padding)));
            rightPad = Integer.parseInt(Part.readOption(ops, "right_pad", String.valueOf(padding)));
            topPad = Integer.parseInt(Part.readOption(ops, "top_pad", String.valueOf(padding)));
            bottomPad = Integer.parseInt(Part.readOption(ops, "bottom_pad", String.valueOf(padding)));
            spacing = Integer.parseInt(Part.readOption(ops, "spacing", "0"));
        }
        catch (NumberFormatException e) {
            return recipeMatch.replaceFirst("[Broken Image. Invalid padding value! Must be an integer number]");
        }

        StackReference stackRef = StackReference.fromString(stackString);
        ItemStack stack;
        if (stackRef == null || (stack = stackRef.createStack()).isEmpty()) {
            return recipeMatch.replaceFirst("[Broken recipe. Specified Item or Block could not be bound!]");
        }

        if (!JeiHelper.jeiAvailable()) {
            return recipeMatch.replaceFirst("[Broken recipe. The mod JEI (Just Enough Items) is required to display recipes!]");
        }

        List<IRecipeRenderer> renderers = JeiHelper.getRecipeRenderers(stack);
        if (renderers == null) {
            return recipeMatch.replaceFirst("[Broken recipe. No recipe's were found for " + stackString + "]");
        }

        //endregion

        MouseIntractable mi = new MouseIntractable() {
            @Override
            public boolean onClick(int mouseX, int mouseY, int button) {
                for (IRecipeRenderer renderer : renderers) {
                    Object ingredient;
                    if ((ingredient = renderer.getIngredientUnderMouse(mouseX, mouseY)) instanceof ItemStack) {
                        container.recipeClick((ItemStack) ingredient, button);
                        return true;
                    }
                }
                return false;
            }
        };
        container.mouseIntractables.add(mi);

        int lastHeight = 0;
        for (IRecipeRenderer renderer : renderers) {
            if (finalXPos + renderer.getWidth() + spacing > elementRight) {
                finalXPos = elementLeft;
                finalYPos = Math.max(nextYLevel, lastHeight);
                lastHeight = 0;
            }

            if (finalYPos + renderer.getHeight() > lastHeight) {
                lastHeight = finalYPos + renderer.getHeight();
            }

            Part part = new Part(container) {
                @Override
                public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                    int offset = container.align == CENTER ? (spacing / 2) : container.align == RIGHT ? spacing : 0;
                    if (borderColour != -1) {
                        int border = mi.isMouseOver ? borderColourHover : borderColour;
                        container.drawColouredRect(xPos + offset, yPos, width - spacing, height, 0xFF000000 | border);
                    }

                    renderer.render(container.mc, xPos + leftPad + offset, yPos + topPad, mouseX, mouseY);
                    if (GuiHelper.isInRect(xPos, yPos, renderer.getWidth(), renderer.getHeight(), mouseX, mouseY)) {
                        container.hoveredRecipe = renderer;
                    }
                    GlStateManager.color(font.red, font.blue, font.green, 1);
                }
            };
            part.width = renderer.getWidth() + leftPad + rightPad + spacing;
            part.height = renderer.getHeight() + topPad + bottomPad;
            finalXPos += part.width;

            parts.add(part);
            mi.parts.add(part);
        }

        finalYPos = Math.max(nextYLevel, lastHeight);
        builtHeight = (finalYPos - yPos);


        return recipeMatch.replaceFirst("");
    }
}
