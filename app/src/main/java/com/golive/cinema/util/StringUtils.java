package com.golive.cinema.util;

import com.golive.network.helper.Md5Helper;
import com.initialjie.log.Logger;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Wangzj on 2016/8/30.
 */

public class StringUtils {
    private static final String DEFAULT_STR = "";

    public static boolean isNullOrEmpty(String str) {
        return null == str || 0 == str.length();
    }

    public static String getDefaultStringIfEmpty(String str) {
        return getDefaultStringIfEmpty(str, DEFAULT_STR);
    }

    public static String getDefaultStringIfEmpty(String str, String defaultStr) {
        if (isNullOrEmpty(str)) {
            str = defaultStr;
        }
        return str;
    }


    public static boolean isValidMac(String mac) {
        if (StringUtils.isNullOrEmpty(mac)) {
            return false;
        }
//        String macAddressRule = "([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}";
        String macAddressRule = "([A-Fa-f0-9]{2}){5}[A-Fa-f0-9]{2}";
//        String macAddressRule = "[0-9A-F]{2}[0-9A-F]{2}[0-9A-F]{2}[0-9A-F]{2}[0-9A-F]{2}[0-9A-F
// ]{2}";
        if (mac.matches(macAddressRule)) {
            Logger.i("StringUtils", "it is a valid MAC address");
            return true;
        } else {
            Logger.e("StringUtils", "it is not a valid MAC address!!!");
            return false;
        }
    }

    public static String parse(String text) {
        if (text == null) return text;

        Pattern p = Pattern.compile("<e\\s*?code=[\"'](.*?)[\"']\\s*?(?:/>|>\\s*?</e>)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        while (m.find()) {
            text = text.replace(m.group(0), convertUnicode("[" + m.group(1) + "]"));
        }

        return text;
    }

    public static String convertUnicode(String emo) {
        emo = emo.substring(1, emo.length() - 1);
        if (emo.length() < 6) {
            return new String(Character.toChars(Integer.parseInt(emo, 16)));
        }
        String[] emos = emo.split("_");
        char[] char0 = Character.toChars(Integer.parseInt(emos[0], 16));
        char[] char1 = Character.toChars(Integer.parseInt(emos[1], 16));
        char[] emoji = new char[char0.length + char1.length];
        System.arraycopy(char0, 0, emoji, 0, char0.length);
        for (int i = char0.length; i < emoji.length; i++) {
            emoji[i] = char1[i - char0.length];
        }
        return new String(emoji);
    }

    public static HashMap getReportParams(String... values) {
        HashMap<String, String> map = new HashMap<>();
        if (null == values || values.length < 3) {
            return map;
        }

        String value = values[0];
        if (!StringUtils.isNullOrEmpty(value)) {
            String[] stringArray = value.split("&");
            for (String str : stringArray) {
                if (StringUtils.isNullOrEmpty(str)) {
                    continue;
                }
                String[] params = str.split("=");
                if (params.length > 1) {
                    String key = params[0];
                    String value1 = values[1];
                    String value2 = values[2];
                    if (!StringUtils.isNullOrEmpty(key) && !StringUtils.isNullOrEmpty(value1)
                            && !StringUtils.isNullOrEmpty(value2) && (key.equals(value1)
                            || key.equals(value2))) {
                        map.put(params[0], params[1]);
                    }
                }
            }
        }

        return map;
    }

    public static String getReportUrl(String url, String mac) {
        if (url == null) {
            return null;
        }
        return url.replace("__MAC__", "_MAC_").replace("_MAC_", Md5Helper.calculateMd5(mac));
    }

}
