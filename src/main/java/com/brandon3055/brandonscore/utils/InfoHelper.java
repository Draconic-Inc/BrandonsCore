package com.brandon3055.brandonscore.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Created by Brandon on 1/07/2014.
 */
public class InfoHelper {

    @Deprecated
    public static final int GUI_TITLE = 0x00FFFF;

    public static void addEnergyInfo(ItemStack stack, List<Component> list) {
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

    public static boolean holdShiftForDetails(List<Component> list, boolean inverted) {
        if (isShiftKeyDown() == inverted){
            list.add(new TextComponent(I18n.get("item_info.brandonscore.shift_for_details", ChatFormatting.AQUA + "" + ChatFormatting.ITALIC, ChatFormatting.RESET + "" + ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));
        }
        return isShiftKeyDown();
    }

    public static boolean holdShiftForDetails(List<Component> list) {
        return holdShiftForDetails(list, false);
    }

    /**
     * "Information Text Colour" The colour used for custom tool tip info
     */
    @Deprecated //I would rather have colour built into modular GUI elements in most cases.
    public static String ITC() {
        return "" + ChatFormatting.RESET + "" + ChatFormatting.DARK_AQUA;
    }

    /**
     * "Highlighted Information Text Colour" The colour used for parts that need to stand out
     */
    @Deprecated //I would rather have colour built into modular GUI elements in most cases.
    public static String HITC() {
        return "" + ChatFormatting.RESET + "" + ChatFormatting.ITALIC + "" + ChatFormatting.GOLD;
    }


}
