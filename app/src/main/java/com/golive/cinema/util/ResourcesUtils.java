package com.golive.cinema.util;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Wangzj on 2017/1/16.
 */

public class ResourcesUtils {

    /**
     * Get error description according to error code.
     *
     * @param context   context
     * @param errorCode error code
     * @return error description
     */
    public static String getErrorDescription(Context context, String errorCode) {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier("e_" + errorCode, "string", context.getPackageName());
        if (0 == resId) {
            return null;
        }
        return resources.getString(resId);
    }
}
