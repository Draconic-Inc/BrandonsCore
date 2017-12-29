package com.brandon3055.brandonscore.utils;

import java.util.Calendar;

/**
 * Created by brandon3055 on 21/12/2017.
 */
public class HolidayHelper {

    private static long lastCheck = -1;
    private static boolean isAprilFools = false;

    static Calendar curTime = Calendar.getInstance();
    static Calendar holidayStart = Calendar.getInstance();
    static Calendar holidayEnd = Calendar.getInstance();

    public static boolean isAprilFools() {
        if (Math.abs(System.currentTimeMillis() - lastCheck) > 100000) {
            setDate(holidayStart, Calendar.MARCH, 31, false);
            setDate(holidayEnd, Calendar.APRIL, 2, true);
            isAprilFools = dateCheck();
            lastCheck = System.currentTimeMillis();
        }

        return isAprilFools;
    }

    static void setDate(Calendar cal, int month, int date, boolean endOfDay) {
        cal.clear();
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, date);

        if (endOfDay) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
    }

    static boolean dateCheck() {
        curTime = Calendar.getInstance();
        return curTime.after(holidayStart) && curTime.before(holidayEnd);
    }
}
