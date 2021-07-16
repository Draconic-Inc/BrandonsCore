package com.brandon3055.brandonscore.utils;

import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Vector3;

import java.util.Random;

/**
 * Created by brandon3055 on 18/9/19.
 */
public class MathUtils {

    private static Random rand = new Random();
    private static float[] randSet = new float[4096];
    private static int randPos = 0;

    static {
        rand.setSeed(123); //Just to ensure consistency
        for (int i = 0; i < randSet.length; i++) {
            randSet[i] = rand.nextFloat();
        }
    }

    /**
     * This is a 'random' float generator.
     * - It actually just loops through a set of randSet.length different random floats
     * Much faster than java random and more than sufficient for certain rendering tasks.
     */
    public static float nextFloat() {
        return randSet[randPos++ % randSet.length];
    }

    /**
     * Sets the current position for 'random' float generator.
     * Effectively the same function as {@link Random#setSeed(long)} because the floats
     * will always be supplied in the same order.
     */
    public static void setRandSeed(long i) {
        randPos = (int) i % randSet.length;
    }

    /**
     * Rounds the number of decimal places based on the given multiplier.<br>
     * e.g.<br>
     * Input: 17.5245743<br>
     * multiplier: 1000<br>
     * Output: 17.534<br>
     * multiplier: 10<br>
     * Output 17.5<br><br>
     *
     * @param number     The input value.
     * @param multiplier The multiplier.
     * @return The input rounded to a number of decimal places based on the multiplier.
     */
    public static double round(double number, double multiplier) {
        return Math.round(number * multiplier) / multiplier;
    }

    public static double map(double valueIn, double inMin, double inMax, double outMin, double outMax) {
        return (valueIn - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public static double clampMap(double valueIn, double inMin, double inMax, double outMin, double outMax) {
        return autoClamp(map(valueIn, inMin, inMax, outMin, outMax), outMin, outMax);
    }

    public static int getNearestMultiple(int number, int multiple) {
        int result = number;

        if (number < 0) result *= -1;

        if (result % multiple == 0) return number;
        else if (result % multiple < multiple / 2) result = result - result % multiple;
        else result = result + (multiple - result % multiple);

        if (number < 0) result *= -1;

        return result;
    }

    @Deprecated
    public static int clamp(int num, int min, int max) {
        return Math.max(min, Math.min(num, max));
    }

    @Deprecated
    public static float clamp(float num, float min, float max) {
        return Math.max(min, Math.min(num, max));
    }

    @Deprecated
    public static double clamp(double num, double min, double max) {
        return Math.max(min, Math.min(num, max));
    }

    @Deprecated
    public static long clamp(long num, long min, long max) {
        return Math.max(min, Math.min(num, max));
    }

    public static double autoClamp(double value, double boundA, double boundB) {
        return MathHelper.clip(value, Math.min(boundA, boundB), Math.max(boundA, boundB));
    }

    public static Vector3 interpolateVec3(Vector3 from, Vector3 to, double position) {
        return new Vector3(from.x + (to.x - from.x) * position,
                from.y + (to.y - from.y) * position,
                from.z + (to.z - from.z) * position);
    }

    public static double distanceSq(Vector3 vec1, Vector3 vec2) {
        double dx = vec1.x - vec2.x;
        double dy = vec1.y - vec2.y;
        double dz = vec1.z - vec2.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double distance(Vector3 vec1, Vector3 vec2) {
        return Math.sqrt(distanceSq(vec1, vec2));
    }
}
