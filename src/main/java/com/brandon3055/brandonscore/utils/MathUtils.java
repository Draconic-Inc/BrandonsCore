package com.brandon3055.brandonscore.utils;

/**
 * Created by brandon3055 on 18/9/19.
 */
public class MathUtils {
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

    public static int getNearestMultiple(int number, int multiple) {
        int result = number;

        if (number < 0) result *= -1;

        if (result % multiple == 0) return number;
        else if (result % multiple < multiple / 2) result = result - result % multiple;
        else result = result + (multiple - result % multiple);

        if (number < 0) result *= -1;

        return result;
    }

    public static int clamp(int num, int min, int max) {
        return Math.max(min, Math.min(num, max));
    }

    public static float clamp(float num, float min, float max) {
        return Math.max(min, Math.min(num, max));
    }

    public static double clamp(double num, double min, double max) {
        return Math.max(min, Math.min(num, max));
    }

    public static long clamp(long num, long min, long max) {
        return Math.max(min, Math.min(num, max));
    }
}
