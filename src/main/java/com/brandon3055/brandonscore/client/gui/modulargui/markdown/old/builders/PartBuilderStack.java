package com.brandon3055.brandonscore.client.gui.modulargui.markdown.old.builders;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.old.IPartBuilder;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.old.MouseIntractable;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.old.Part;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.old.PartContainer;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.old.GuiMarkdownElement.profiler;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class PartBuilderStack extends IPartBuilder {
    private static Pattern stackPat = Pattern.compile("(?<=[^\\\\]|^)(" + Utils.SELECT + "stack\\[[^" + Utils.SELECT + "]*]\\{[^" + Utils.SELECT + "]*})|(?<=[^\\\\]|^)(" + Utils.SELECT + "stack\\[[^" + Utils.SELECT + " ]*])");      //"(?<=[^\\\\]|^)(" + Utils.SELECT + "stack\\[[^]]*]\\{[^" + Utils.SELECT + "]*})|(?<=[^\\\\]|^)(" + Utils.SELECT + "stack\\[[^]]*])");
    private static Pattern stackString = Pattern.compile("(?<=" + Utils.SELECT + "stack\\[)(.*)(?=][{])|(?<=" + Utils.SELECT + "stack\\[)(.*)(?=])");                          //"(?<=" + Utils.SELECT + "stack\\[)([^]]*)(?=])");
    private static Pattern stackOPS = Pattern.compile("(?<=]\\{)(.*)(?=})");

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    @Override
    public int matches(String test) {
        Matcher matcher = stackPat.matcher(test);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    @Override
    public String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel) {
        profiler.startSection("Build Stack");
        builtHeight = font.FONT_HEIGHT;
        //region Extract Recipe Data

        Matcher stackPatMatch = stackPat.matcher(markdown);
        String match;

        if (stackPatMatch.find(0)) {
            match = stackPatMatch.group();
        }
        else {
            LogHelperBC.error("Failed to build " + getClass().getSimpleName() + " This suggests a false match occurred which should not be possible!");
            return "";
        }

        Matcher stackStringMatch = stackString.matcher(match);
        String stackString = "";
        boolean isOre;

        if (!stackStringMatch.find() || (stackString = stackStringMatch.group()).isEmpty()) {
            return stackPatMatch.replaceFirst("[Broken Stack. No stack string Found]");
        }
        isOre = OreDictionary.doesOreNameExist(stackString);

        Matcher opsMatch = stackOPS.matcher(match);
        String ops = opsMatch.find() ? opsMatch.group() : "";

        int size;
        try {
            size = Part.parseSize(elementRight - elementLeft, Part.readOption(ops, "size", "18"));
        }
        catch (NumberFormatException e) {
            return stackPatMatch.replaceFirst("[Broken Stack. Invalid size value! Must be an integer number]");
        }

        boolean drawSlot = Part.readOption(ops, "draw_slot", "false").equals("true");
        boolean drawHover = Part.readOption(ops, "draw_hover", "true").equals("true");
        String altHover = Part.readOption(ops, "alt_hover", "");

        List<ItemStack> baseStacks = new ArrayList<>();

        if (isOre) {
            baseStacks.addAll(OreDictionary.getOres(stackString));
        }
        else {
            StackReference stackRef = StackReference.fromString(stackString);
            ItemStack stack;
            if (stackRef == null || (stack = stackRef.createStack()).isEmpty()) {
                return stackPatMatch.replaceFirst("[Broken Stack. Specified Item or Block could not be found!]");
            }
            baseStacks.add(stack);
        }

        NonNullList<ItemStack> finalStacks = NonNullList.create();
        for (ItemStack stack : baseStacks) {
            if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE && stack.getHasSubtypes()) {
                stack.getItem().getSubItems(CreativeTabs.SEARCH, finalStacks);
            }
            else {
                finalStacks.add(stack);
            }
        }

        ItemStack[] stacks = finalStacks.toArray(new ItemStack[finalStacks.size()]);

        //32767

        //endregion

        //endregion

        MouseIntractable mi = new MouseIntractable() {
            @Override
            public ItemStack getHoverStack() {
                if (altHover.isEmpty()) {
                    return stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
                }
                return super.getHoverStack();
            }
        };
        if (drawHover) {
            if (!altHover.isEmpty()) {
                if (altHover.contains("\\n")) {
                    mi.hoverText.addAll(Arrays.asList(altHover.split("(\\\\n)")));
                }
                else {
                    mi.hoverText.add(altHover);
                }
            }
        }

        container.mouseIntractables.add(mi);

        if (finalXPos + size > elementRight) {
            finalXPos = elementLeft;
            finalYPos = nextYLevel;
        }

        Part part = new Part(container) {
            @Override
            public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                GlStateManager.pushMatrix();

                if (drawSlot) {
                    container.bindTexture(BCTextures.MODULAR_GUI);
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    container.drawScaledCustomSizeModalRect(xPos, yPos, 0, 0, 18, 18, width, height, 255, 255);
                }

                double scaledWidth = size / 18D;
                double scaledHeight = size / 18D;

                ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.translate(xPos + scaledWidth, yPos + scaledHeight, container.getRenderZLevel() - 80);
                GlStateManager.scale(scaledWidth, scaledHeight, 1);
                container.mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);

                if (stack.getCount() > 1) {
                    String s = "" + Utils.SELECT + "f" + stack.getCount() + "" + Utils.SELECT + "f";
                    GlStateManager.translate(0, 0, -(container.getRenderZLevel() - 80));
                    container.zOffset += 45;
                    container.drawString(font, s, 18 - (font.getStringWidth(s)) - 1, font.FONT_HEIGHT, 0xFFFFFF, true);
                    container.zOffset -= 45;
                }

                GlStateManager.color(font.red, font.blue, font.green, 1);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            }
        };
        part.width = size;
        part.height = size;
        finalXPos += part.width;

        parts.add(part);
        mi.parts.add(part);

        builtHeight = (finalYPos - yPos) + part.height + 1;

        return stackPatMatch.replaceFirst("");
    }
}
