package com.golive.cinema.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 时间帮助类
 *
 * @author wangzijie
 */
public class DateHelper {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT2 = "yyyyMMddHHmmss";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String GMT_TIME_FORMAT = "GMT+00:00";

    public static int getYear(String str) {
        Date date = getDate(str);
        if (date != null) {
            String formated = new SimpleDateFormat("yyyy").format(date);
            return Integer.parseInt(formated);
        }
        return 0;
    }

    public static int getMonth(String str) {
        Date date = getDate(str);
        if (date != null) {
            String formated = new SimpleDateFormat("MM").format(date);
            return Integer.parseInt(formated);
        }
        return 0;
    }

    public static int getDay(String str) {
        Date date = getDate(str);
        if (date != null) {
            String formated = new SimpleDateFormat("dd").format(date);
            return Integer.parseInt(formated);
        }
        return 0;
    }

    private static Date getDate(String date) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {
            return df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 时间比较，date-当前时间
     * <P>
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 返回天数差
     */
    public static long compareWithNow(String date) throws ParseException {
        SimpleDateFormat dfs = new SimpleDateFormat(DATE_FORMAT);
        Date begin = new Date();

        return diffDays(date, dfs.format(begin));
    }

    /**
     * 时间比较，date1-date2
     * <P>
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 返回天数差
     */
    public static long diffDays(String date1, String date2)
            throws ParseException {
        SimpleDateFormat dfs = new SimpleDateFormat(DATE_FORMAT);
        Date begin = dfs.parse(date2);
        Date end = dfs.parse(date1);
        long between = (end.getTime() - begin.getTime()) / 1000;// 除以1000是为了转换成秒
        long day = between / (24 * 3600);
        long hour = between % (24 * 3600) / 3600;
        long minute = between % 3600 / 60;
        long second = between % 60 / 60;
        System.out.println(day + "天" + hour + "小时" + minute + "分" + second
                + "秒");
        return day;
    }

    /**
     * 时间比较，date1-date2
     * <P>
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 0相等; >0 date1前于date2; <0 date1后于date2
     */
    public static long compareTo(String date1, String date2)
            throws ParseException {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date dt1 = df.parse(date1);
        Date dt2 = df.parse(date2);
        return dt1.getTime() - dt2.getTime();
    }

    /**
     * 毫秒转时分秒
     */
    public static String toHMSTime(long millionSeconds) {
        millionSeconds /= 1000;
        long h = millionSeconds / 3600;
        long m = millionSeconds / 60 % 60;
        long s = millionSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);

        // SimpleDateFormat formatter = new
        // SimpleDateFormat("HH:mm:ss");//初始化Formatter的转换格式。
        // String hms = formatter.format(millionSeconds);
        // return hms;
    }

    public static long toMsTime(String timeStr) {

        long time = 0;

        if (StringUtils.isNullOrEmpty(timeStr)) {
            return 0;
        }

        try {
            String[] times = timeStr.split(":");
            if (3 > times.length) {
                return 0;
            }
            time = Long.parseLong(times[0]) * 3600 + Long.parseLong(times[1])
                    * 60 + Long.parseLong(times[2]);

            time *= 1000;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    /**
     * 字符串转换成日期
     *
     * @return date
     */
    public static Date stringFormatToDate(String str, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String stringFormatDateGMT(String str, String format) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy",
                Locale.ENGLISH);

        Date date = null;
        try {
            date = dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat2 = new SimpleDateFormat(format);
        return dateFormat2.format(date);
    }

    /**
     * 日期转换成字符串
     *
     * @return str
     */
    public static String dateFormatToString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String str = dateFormat.format(date);
        return str;
    }

    /**
     * convert date to seconds
     *
     * @param dateStr date string
     * @return seconds
     */
    public static long convertDateToSecond(String dateStr) throws ParseException {
        if (StringUtils.isNullOrEmpty(dateStr)) {
            return 0;
        }

        // trim
        dateStr = dateStr.trim();
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        Date date = formatter.parse(dateStr);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.SECOND) +
                c.get(Calendar.MINUTE) * 60 +
                c.get(Calendar.HOUR_OF_DAY) * 3600;

//        String time_str = dateStr.trim();
//        String regex = ":";
//        String[] strs = time_str.split(regex);
//        return Integer.parseInt(strs[0].trim()) * 60 * 60
//                + Integer.parseInt(strs[1].trim()) * 60
//                + Integer.parseInt(strs[2].trim());
    }

    public static String formatGMTTime(int second) {
        long time = second * 1000;
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(GMT_TIME_FORMAT));
        return formatter.format(time);
    }
}
