package com.golive.cinema.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2016/9/13.
 */

public class DeviceTypeRuntimeCheck {

    public static boolean isTV(Context context) {
        UiModeManager uiModeManager =
                (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        boolean isTV = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            isTV = Configuration.UI_MODE_TYPE_TELEVISION == uiModeManager.getCurrentModeType();
        }
        if (isTV) {
            Logger.d("Running on a TV Device");
        } else {
            Logger.d("Running on a non-TV Device");
        }
        return isTV;
    }
}
