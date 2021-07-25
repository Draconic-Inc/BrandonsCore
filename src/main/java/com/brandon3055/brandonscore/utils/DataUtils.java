package com.brandon3055.brandonscore.utils;

import codechicken.lib.util.ArrayUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 31/3/2016.
 */
public class DataUtils {

    public static String[] arrayToLowercase(String[] array) {
        String[] lowercaseArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            lowercaseArray[i] = array[i].toLowerCase(Locale.ENGLISH);
        }
        return lowercaseArray;
    }

    public static String[] arrayToString(Object[] array) {
        String[] lowercaseArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            lowercaseArray[i] = String.valueOf(array[i]);
        }
        return lowercaseArray;
    }

    public static String[] arrayToString(byte[] array) {
        String[] lowercaseArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            lowercaseArray[i] = String.valueOf(array[i]);
        }
        return lowercaseArray;
    }

    public static String[] arrayToString(int[] array) {
        String[] lowercaseArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            lowercaseArray[i] = String.valueOf(array[i]);
        }
        return lowercaseArray;
    }

    public static String stringArrayConcat(String[] array, String separator) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            s.append(array[i]);
            if (i + 1 < array.length) {
                s.append(separator);
            }
        }
        return s.toString();
    }

    /**
     * Shift all objects in an array by the number of places specified.
     * <p/>
     * Example:
     * Object[] array = {1, 2, 3, 4, 5}
     * array = arrayShift(array, 1);
     * array now equals {5, 1, 2, 3, 4}
     * Shift can be a positive or negative number
     * <p/>
     * Shift is not restricted to any max or min value so it could for example be linked to a counter that
     * continuously increments.
     */
    public static Object[] arrayShift(Object[] input, int shift) {
        Object[] newArray = new Object[input.length];

        for (int i = 0; i < input.length; i++) {
            int newPos = (i + shift) % input.length;

            if (newPos < 0) {
                newPos += input.length;
            }

            newArray[newPos] = input[i];
        }

        return newArray;
    }

    /**
     * Counts elements in the array that conform to the Function check.
     *
     * @param iterable The iterable to check.
     * @param check    The Function to apply to each element.
     * @param <T>      What we are dealing with.
     * @return The count.
     */
    public static <T> int count(Iterable<T> iterable, Function<T, Boolean> check) {
        int counter = 0;
        for (T value : iterable) {
            if (check.apply(value)) {
                counter++;
            }
        }
        return counter;
    }

    public static <T> int count(T[] iterable, Function<T, Boolean> check) {
        return ArrayUtils.count(iterable, check);
    }

    public static <E> void forEach(E[] elements, Consumer<E> consumer) {
        for (E element : elements) {
            consumer.accept(element);
        }
    }

    public static <E> void forEachMatch(E[] elements, Predicate<E> matcher, Consumer<E> consumer) {
        for (E element : elements) {
            if (matcher.test(element)) {
                consumer.accept(element);
            }
        }
    }

    public static <E> void forEachMatch(Iterable<E> elements, Predicate<E> matcher, Consumer<E> consumer) {
        for (E element : elements) {
            if (matcher.test(element)) {
                consumer.accept(element);
            }
        }
    }

    public static <E> void addIf(Iterable<E> elements, Collection<E> addTo, Predicate<E> matcher) {
        for (E element : elements) {
            if (matcher.test(element)) {
                addTo.add(element);
            }
        }
    }

    /**
     * @return Returns the first element that the matcher accepts or null if it does not accept any.
     */
    public static <E> E firstMatch(Iterable<E> elements, Predicate<E> matcher) {
        for (E element : elements) {
            if (matcher.test(element)) {
                return element;
            }
        }
        return null;
    }

    public static <E> boolean contains(E[] elements, Predicate<E> matcher) {
        for (E element : elements) {
            if (matcher.test(element)) {
                return true;
            }
        }
        return false;
    }

    //region Converters

    public static long[] toPrimitive(final Long[] array) {
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static int[] toPrimitive(final Integer[] array) {
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static short[] toPrimitive(final Short[] array) {
        final short[] result = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static byte[] toPrimitive(final Byte[] array) {
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static double[] toPrimitive(final Double[] array) {
        final double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static float[] toPrimitive(final Float[] array) {
        final float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static boolean[] toPrimitive(final Boolean[] array) {
        final boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }


    public static long[] longListToArray(final List<Long> list) {
        final long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static int[] intListToArray(final List<Integer> list) {
        final int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static short[] shortListToArray(final List<Short> list) {
        final short[] result = new short[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static byte[] byteListToArray(final List<Byte> list) {
        final byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static double[] doubleListToArray(final List<Double> list) {
        final double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static float[] floatListToArray(final List<Float> list) {
        final float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static boolean[] boolListToArray(final List<Boolean> list) {
        final boolean[] result = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static int formatColour(TextFormatting formatting) {
        switch (formatting) {
            case BLACK:
                return 0x000000;
            case DARK_BLUE:
                return 0x0000AA;
            case DARK_GREEN:
                return 0x00AA00;
            case DARK_AQUA:
                return 0x00AAAA;
            case DARK_RED:
                return 0xAA0000;
            case DARK_PURPLE:
                return 0xAA00AA;
            case GOLD:
                return 0xFFAA00;
            case GRAY:
                return 0xAAAAAA;
            case DARK_GRAY:
                return 0x555555;
            case BLUE:
                return 0x5555FF;
            case GREEN:
                return 0x55FF55;
            case AQUA:
                return 0x55FFFF;
            case RED:
                return 0xFF5555;
            case LIGHT_PURPLE:
                return 0xFF55FF;
            case YELLOW:
                return 0xFFFF55;
            case WHITE:
                return 0xFFFFFF;
            default:
                return 0xFFFFFF;
        }
    }

    //endregion

    public static long averageLongArray(long[] array) {
        long average = 0;
        double remainder = 0;
        int count = array.length;
        for (long val : array) {
            average += val / count;
            remainder += ((double) val % (double) count) / (double) count;
        }
        return average + (long) remainder;
    }

    public static <T> Set<T> concatSets(Set<T>... sets) {
        Set<T> returnSet = new HashSet<>();
        for (Set<T> set : sets) returnSet.addAll(set);
        return returnSet;
    }

    public static <T> boolean contains(Iterable<T> heldEquipment, T test) {
        for (T t : heldEquipment) {
            if (t == test) {
                return true;
            }
        }
        return false;
    }

    public static <T> T safeRemove(List<T> list, int removeIndex) {
        if (removeIndex >= 0 && removeIndex < list.size()) {
            return list.remove(removeIndex);
        }
        return null;
    }


    public static <T> T safeGet(List<T> list, int getIndex) {
        if (getIndex >= 0 && getIndex < list.size()) {
            return list.get(getIndex);
        }
        return null;
    }

    public static <T> T safeGet(List<T> list, int getIndex, Supplier<T> orDefault) {
        if (getIndex >= 0 && getIndex < list.size()) {
            return list.get(getIndex);
        }
        return orDefault.get();
    }

    public static <T> void ifPresent(List<T> list, int index, Consumer<T> callback) {
        if (index >= 0 && index < list.size()) {
            callback.accept(list.get(index));
        }
    }


}
