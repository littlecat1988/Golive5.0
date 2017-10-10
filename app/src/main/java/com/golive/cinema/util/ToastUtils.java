package com.golive.cinema.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/2/21.
 */

public class ToastUtils {

    /** 之前显示的内容 */
    private static String oldMsg;
    /** Toast对象 */
    private static Toast toast = null;
    /** 第一次时间 */
    private static long oneTime = 0;
    /** 第二次时间 */
    private static long twoTime = 0;

    /**
     * 显示Toast
     */
    public static void showToast(Context context, String message) {
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (!StringUtils.isNullOrEmpty(message) && !StringUtils.isNullOrEmpty(oldMsg)
                    && message.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message;
                toast.setText(message);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

}
