package com.brandon3055.brandonscore.utils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

/**
 * Created by Brandon on 1/07/2014.
 */
@Deprecated
public class InfoHelper {

    public static final int GUI_TITLE = 0x00FFFF;

    @SuppressWarnings("unchecked")
    public static void addEnergyInfo(ItemStack stack, List<ITextComponent> list) {
		String energy = Utils.formatNumber(EnergyUtils.getEnergyStored(stack));
        String maxEnergy = Utils.formatNumber(EnergyUtils.getMaxEnergyStored(stack));
		list.add(new StringTextComponent(I18n.format("info.bc.charge.txt") + ": " + energy + " / " + maxEnergy + " RF"));
    }

//    @SuppressWarnings("unchecked")
//    public static void addLore(ItemStack stack, List list, boolean addLeadingLine) {
//        String[] lore = getLore(stack);
//        if (addLeadingLine) list.add("");
//        if (lore == null) {
//            list.add("" + TextFormatting.ITALIC + "" + TextFormatting.DARK_PURPLE + "Invalid lore localization! (something is broken)");
//            return;
//        }
//        for (String s : lore) list.add("" + TextFormatting.ITALIC + "" + TextFormatting.DARK_PURPLE + s);
//    }

//    /**
//     * Add lore with a blank line above it
//     */
//    public static void addLore(ItemStack stack, List list) {
//        addLore(stack, list, true);
//    }

//    /**
//     * Add the standard energy and lore information
//     */
//    @SuppressWarnings("unchecked")
//    public static void addEnergyAndLore(ItemStack stack, List list) {
//        if (!isShiftKeyDown())
//            list.add(I18n.format("info.de.hold.txt") + " " + TextFormatting.AQUA + "" + TextFormatting.ITALIC + I18n.format("info.de.shift.txt") + TextFormatting.RESET + " " + TextFormatting.GRAY + I18n.format("info.de.forDetails.txt"));
//        else {
//            addEnergyInfo(stack, list);
//            addLore(stack, list);
//        }
//    }

//    /**
//     * returns lore text or an empty string if the lore is not set
//     */
//    public static String[] getLore(ItemStack stack) {
//        String unlocalizeLore = stack.getItem().getTranslationKey() + ".lore";
//        String rawLore = I18n.format(unlocalizeLore);
//
//        if (rawLore.contains(unlocalizeLore)) {
//            //LogHelper.error("Invalid or missing Lore localization \""+unlocalizeLore+"\"");
//            return null;
//        }
//
//        String lineCountS = rawLore.substring(0, 1);
//        rawLore = rawLore.substring(1);
//        int lineCount = 0;
//
//        try {
//            lineCount = Integer.parseInt(lineCountS);
//        }
//        catch (NumberFormatException e) {
//            LogHelperBC.error("Invalid Lore Format! Lore myst start with the number of lines \"3Line 1\\nLine 2\\nLine 3\"");
//        }
//
//        String[] loreLines = new String[lineCount];
//
//        for (int i = 0; i < lineCount; i++) {
//            if (rawLore.contains("\\n")) loreLines[i] = rawLore.substring(0, rawLore.indexOf("\\n"));
//            else loreLines[i] = rawLore;
//            if (rawLore.contains("\\n")) rawLore = rawLore.substring(rawLore.indexOf("\\n") + 2);
//        }
//
//        return loreLines;
//    }

    private static boolean isShiftKeyDown() {
        return Screen.hasShiftDown();
    }

    private static boolean isCtrlKeyDown() {
        return Screen.hasControlDown();
    }

    @SuppressWarnings("unchecked")
    public static boolean holdShiftForDetails(List<ITextComponent> list, boolean inverted) {
        if (isShiftKeyDown() == inverted)
            list.add(new TranslationTextComponent("info.bc.holdShiftForDetails.txt", TextFormatting.AQUA + "" + TextFormatting.ITALIC, TextFormatting.RESET + "" + TextFormatting.GRAY));
        return isShiftKeyDown();
    }

    public static boolean holdShiftForDetails(List<ITextComponent> list) {
        return holdShiftForDetails(list, false);
    }

    /**
     * "Information Text Colour" The colour used for custom tool tip info
     */
    @Deprecated //I would rather have colour built into modular GUI elements in most cases.
    public static String ITC() {
        return "" + TextFormatting.RESET + "" + TextFormatting.DARK_AQUA;
    }

    /**
     * "Highlighted Information Text Colour" The colour used for parts that need to stand out
     */
    @Deprecated //I would rather have colour built into modular GUI elements in most cases.
    public static String HITC() {
        return "" + TextFormatting.RESET + "" + TextFormatting.ITALIC + "" + TextFormatting.GOLD;
    }


}
