package com.golive.cinema.advert;

import static com.golive.cinema.Constants.ADVERT_APKTYPE;
import static com.golive.cinema.Constants.ADVERT_CLASS;
import static com.golive.cinema.Constants.ADVERT_DEPENDENCE;
import static com.golive.cinema.Constants.ADVERT_DEPENDENCE_CODE;
import static com.golive.cinema.Constants.ADVERT_ENVIRONMENT;
import static com.golive.cinema.Constants.ADVERT_PACKAGE;
import static com.golive.cinema.Constants.ADVERT_STARTMODE;
import static com.golive.cinema.Constants.PROD;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.initialjie.hw.Constants;
import com.initialjie.hw.entity.DeviceConfig;
import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2017/2/20.
 */

public class AdvertHelper {

    /**
     * Go to advert
     *
     * @param activity  activity
     * @param reqNum    startActivityForResult请求号
     * @param startMode start mode
     */
    public static boolean goAdvert(final Activity activity, final int reqNum, String startMode) {

        // advert not exist
        if (!isAdvertExist(activity, startMode)) {
            return false;
        }

        final Intent intent = getAdvertIntent(activity, startMode);
        try {
            activity.startActivityForResult(intent, reqNum);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check if advert exist
     *
     * @param context   context
     * @param startMode start mode
     */
    public static boolean isAdvertExist(Context context, String startMode) {
        boolean advertExist = false;
        Intent advertIntent = getAdvertIntent(context, startMode);
        if (advertIntent != null) {
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(advertIntent, 0);
            if (resolveInfo != null) {
                advertExist = true;
            }
        }
        Logger.d("isAdvertExist : " + advertExist);
        return advertExist;
    }

    /**
     * Get jump to advert intent
     *
     * @param context   context
     * @param startMode start mode
     */
    public static Intent getAdvertIntent(final Context context, String startMode) {
        boolean useDependence = false;
        final Intent intent = new Intent();

        // 依赖广告版
        if (ADVERT_DEPENDENCE) {

            try {
                // check class
                Class cls = Class.forName(ADVERT_CLASS);
                useDependence = true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Logger.e(e, "getAdvertIntent, check class");
            }

            // use inner class
            if (useDependence) {
                DeviceConfig config = DeviceConfig.getDefaultDeviceConfig(context);
                // TCL的机型比较独立版本和内置版本，选择版本号比较新的启动
                if (config != null && !StringUtils.isNullOrEmpty(config.getPartner())
                        && Constants.Partner.PARTNER_TCL.equalsIgnoreCase(config.getPartner())) {
                    int vercode = PackageUtils.getVersionCode(context, ADVERT_PACKAGE);
                    useDependence = ADVERT_DEPENDENCE_CODE >= vercode;
                    Logger.d("getAdvertIntent, ADVERT DEPENDENCE CODE : " + ADVERT_DEPENDENCE_CODE
                            + ", already exist advert vercode : " + vercode);
                }
            }
        }

        Logger.d("getAdvertIntent, useDependence : " + useDependence);

        // use inner class
        if (useDependence) {
//            intent.setClassName(ADVERT_PACKAGE, ADVERT_CLASS);
//            intent.setClass(context, AdvertActivity.class);
            try {
                Class cls = Class.forName(ADVERT_CLASS);
                intent.setClass(context, cls);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Logger.e(e, "getAdvertIntent, forName");
            }
        } else {
            ComponentName comp = new ComponentName(ADVERT_PACKAGE, ADVERT_CLASS);
            intent.setComponent(comp);
        }
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(0);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ADVERT_ENVIRONMENT, PROD ? 3 : 2);
        intent.putExtra(ADVERT_STARTMODE, startMode);
        try {
            int type = Integer.parseInt(com.golive.network.Constants.TYPE_CINEMA);
            intent.putExtra(ADVERT_APKTYPE, type);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return intent;
    }
}
