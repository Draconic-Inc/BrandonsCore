package com.brandon3055.brandonscore.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;


public final class ItemNBTHelper {


    // SETTERS ///////////////////////////////////////////////////////////////////
    public static CompoundTag getCompound(ItemStack stack) {
        if (stack.getTag() == null) stack.setTag(new CompoundTag());
        return stack.getTag();
    }

    public static ItemStack setByte(ItemStack stack, String tag, byte b) {
        getCompound(stack).putByte(tag, b);
        return stack;
    }

    public static ItemStack setBoolean(ItemStack stack, String tag, boolean b) {
        getCompound(stack).putBoolean(tag, b);
        return stack;
    }

    public static ItemStack setShort(ItemStack stack, String tag, short s) {
        getCompound(stack).putShort(tag, s);
        return stack;
    }

    public static ItemStack setInteger(ItemStack stack, String tag, int i) {
        getCompound(stack).putInt(tag, i);
        return stack;
    }

    public static ItemStack setLong(ItemStack stack, String tag, long i) {
        getCompound(stack).putLong(tag, i);
        return stack;
    }

    public static ItemStack setFloat(ItemStack stack, String tag, float f) {
        getCompound(stack).putFloat(tag, f);
        return stack;
    }

    public static ItemStack setDouble(ItemStack stack, String tag, double d) {
        getCompound(stack).putDouble(tag, d);
        return stack;
    }

    public static ItemStack setString(ItemStack stack, String tag, String s) {
        getCompound(stack).putString(tag, s);
        return stack;
    }

    public static ItemStack setUUID(ItemStack stack, String tag, UUID uuid) {
        getCompound(stack).putUUID(tag, uuid);
        return stack;
    }

    // GETTERS ///////////////////////////////////////////////////////////////////

    public static boolean verifyExistance(ItemStack stack, String tag) {
        CompoundTag compound = stack.getTag();
        if (compound == null) return false;
        else return stack.getTag().contains(tag);
    }

    public static byte getByte(ItemStack stack, String tag, int defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getByte(tag) : (byte) defaultExpected;
    }

    public static boolean getBoolean(ItemStack stack, String tag, boolean defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getBoolean(tag) : defaultExpected;
    }

    public static short getShort(ItemStack stack, String tag, short defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getShort(tag) : defaultExpected;
    }

    public static int getInteger(ItemStack stack, String tag, int defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getInt(tag) : defaultExpected;
    }

    public static long getLong(ItemStack stack, String tag, long defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getLong(tag) : defaultExpected;
    }

    public static float getFloat(ItemStack stack, String tag, float defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getFloat(tag) : defaultExpected;
    }

    public static double getDouble(ItemStack stack, String tag, double defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getDouble(tag) : defaultExpected;
    }

    public static String getString(ItemStack stack, String tag, String defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getString(tag) : defaultExpected;
    }

    public static UUID getUUID(ItemStack stack, String tag, UUID defaultExpected) {
        return verifyExistance(stack, tag) ? stack.getTag().getUUID(tag) : defaultExpected;
    }
}