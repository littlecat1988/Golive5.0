package com.golive.cinema.util;

/**
 * Created by Wangzj on 2016/12/1.
 */

public class DoubleFormatUtil {
    /**
     * Round the double.
     *
     * @param d     double
     * @param scale scale
     */
    public static String round(double d, int scale) {
        return String.format("%." + scale + "f", d);
    }

    /**
     * Round the double.
     *
     * @param d     double
     * @param scale scale
     */
    public static String round(String d, int scale) {
        return round(Double.parseDouble(d), scale);
    }
}
