package com.golive.cinema.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/5/23.
 */

public class DensityUtil {
    private static int screenWidth;
    private static int screenHeight;
    private static WindowManager mWindowManager;
    private static Display display;
    private static DisplayMetrics displayMetrics;

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale =getDensity(context);
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = getDensity(context);
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕的宽度
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        if(context==null)return 0;
        if (screenWidth == 0) {
            initManagerAndDisplay(context);
            screenWidth=displayMetrics.widthPixels;
            screenHeight=displayMetrics.heightPixels;
        }
        return screenWidth;
    }
    /**
     * 获取屏幕的高度
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if(context==null)return 0;
        if (screenHeight == 0) {
            initManagerAndDisplay(context);
            screenWidth=displayMetrics.widthPixels;
            screenHeight=displayMetrics.heightPixels;
        }
        return screenHeight;
    }

    private static void initManagerAndDisplay(Context context){
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }
        if(display==null){
            display=mWindowManager.getDefaultDisplay();
        }
        if(displayMetrics==null){
            displayMetrics=context.getResources().getDisplayMetrics();
        }
        display.getMetrics(displayMetrics);
    }
    public static float getDensity(Context context){
        if(displayMetrics==null){
            displayMetrics=context.getResources().getDisplayMetrics();
        }
        return displayMetrics.density;
    }
}
