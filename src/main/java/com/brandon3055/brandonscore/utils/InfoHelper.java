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
public class InfoHelper {

    @Deprecated
    public static final int GUI_TITLE = 0x00FFFF;

    public static void addEnergyInfo(ItemStack stack, List<ITextComponent> list) {
//		String energy = Utils.formatNumber(EnergyUtils.getEnergyStored(stack));
//        String maxEnergy = Utils.formatNumber(EnergyUtils.getMaxEnergyStored(stack));
//		list.add(new StringTextComponent(I18n.format("op.brandonscore.charge") + ": " + energy + " / " + maxEnergy + " " + I18n.format("op.brandonscore." + (isShiftKeyDown() ? "operational_potential" : "op"))));
    }


    private static boolean isShiftKeyDown() {
        return Screen.hasShiftDown();
    }

    private static boolean isCtrlKeyDown() {
        return Screen.hasControlDown();
    }

    public static boolean holdShiftForDetails(List<ITextComponent> list, boolean inverted) {
        if (isShiftKeyDown() == inverted){
            list.add(new StringTextComponent(I18n.format("item_info.brandonscore.shift_for_details", TextFormatting.AQUA + "" + TextFormatting.ITALIC, TextFormatting.RESET + "" + TextFormatting.GRAY)).mergeStyle(TextFormatting.GRAY));
        }
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
