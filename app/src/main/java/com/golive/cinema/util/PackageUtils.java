package com.golive.cinema.util;

import static com.golive.cinema.Constants.TCL_APP_MARKET2_PACKAGE;
import static com.golive.cinema.Constants.TCL_APP_MARKET_PACKAGE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.golive.cinema.Constants;
import com.golive.cinema.MyApplication;
import com.initialjie.log.Logger;

import java.io.File;

/**
 * Created by Wangzj on 2017/2/20.
 */

public class PackageUtils {

    /**
     * Get version code of package.
     *
     * @param context     context
     * @param packageName package name
     */
    public static int getVersionCode(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get version name of package.
     *
     * @param context     context
     * @param packageName package name
     */
    public static String getVersionName(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo != null ? packInfo.versionName : null;
    }

    /**
     * Restart apk in delay time
     *
     * @param context     context
     * @param packageName package name
     * @param delayMillis delay time
     */
    public static void startApkDelay(@NonNull Context context, String packageName,
            int delayMillis) {
        PackageManager packageManager = context.getPackageManager();
        Intent startIntent = packageManager.getLaunchIntentForPackage(packageName);
        PendingIntent pi = PendingIntent.getActivity(context, 0, startIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMillis, pi);
    }

    /**
     * 应用是否已安装
     *
     * @return true：应用存在;false：应用不存在
     */
    public static boolean isAppExists(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }


    /*=============================================================================
      Not common!
    * =============================================================================*/

    /**
     * 安装
     */
    public static boolean installApp(Context context, File file) {
        if (context == null || null == file) {
            return false;
        }

        String saveFilePath = file.getAbsolutePath();
        if (StringUtils.isNullOrEmpty(saveFilePath)) {
            return false;
        }

//        /**
//         *TCL应用商店并且不是欢视商店，走TCL服务升级
//         * TCL商店版本是2013年之前的都不提供服务升级
//         * 欢视商店和非TCL渠道暂时默认使用系统安装器
//         **/
//
//        if ((isAppExists(context, Constants.TCL_APP_MARKET_PACKAGE)
//                || isAppExists(context, Constants.TCL_APP_MARKET2_PACKAGE))
//                && !isAppExists(context, Constants.TCL_HUAN_APP_STORE_PACKAGE)) {
//            final int minVersionCode = 301000;
//            if (getVersionCode(context, Constants.TCL_APP_MARKET_PACKAGE) >= minVersionCode
//                    || getVersionCode(context, Constants.TCL_APP_MARKET2_PACKAGE)
//                    >= minVersionCode) {

        // TCL自升级最大支持版本
        final int maxTclStoreVersion = 340000;
        boolean isTclStoreExist = isAppExists(context, TCL_APP_MARKET_PACKAGE);
        boolean isTclStore2Exist = isAppExists(context, TCL_APP_MARKET2_PACKAGE);
        int tclStoreVersion = getVersionCode(context, TCL_APP_MARKET_PACKAGE);
        int tclStore2Version = getVersionCode(context, TCL_APP_MARKET2_PACKAGE);

        // 包含TCL商店 && 商店版本 < 最大支持版本
        if (isTclStore2Exist && tclStore2Version < maxTclStoreVersion
                || isTclStoreExist && tclStoreVersion < maxTclStoreVersion) {
            // TCL自升级最小支持版本
            final int minTclStoreVersion = 301000;
            // 商店版本 >= 最小支持版本
            if (tclStore2Version >= minTclStoreVersion || tclStoreVersion >= minTclStoreVersion) {
                Intent intent = new Intent();
                intent.setAction("com.tcl.packageinstaller.service.renew.PackageInstallerService");
                intent.setPackage("com.tcl.packageinstaller.service.renew");
                intent.putExtra("uri", Uri.fromFile(file).toString());
                intent.putExtra("currentPackageName", context.getPackageName()); // packName
                context.startService(intent);
            } else {
                ToastUtils.showToast(context, "系统不支持自升级!");
            }
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(saveFilePath)),
                        "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Logger.e(e, "installApp : ");
            }
        }

        return false;
    }

    public static int checkLocalUpgrade(int remoteUpgradeType, int remoteVersion) {
        Logger.d("checkLocalUpgrade ,remoteUpgradeType=" + remoteUpgradeType + ",remoteVersion="
                + remoteVersion);
        int checkedUpgradeType = Constants.UPGRADE_TYPE_NO_UPGRADE;
        int localVersion = PackageUtils.getVersionCode(MyApplication.getContext(),
                MyApplication.getContext().getPackageName());

//        PackageManager pm = MyApplication.getContext().getPackageManager();
        // File upgradeFile = new File(GoliveApp.getAppContext()
        // .getExternalFilesDir(null), Constant.UPGRADE_FILE_NAME);
//        File upgradeFile = new File(MyApplication.getContext().getFilesDir(),
//                Constants.UPGRADE_FILE_NAME);
//        PackageInfo info = pm.getPackageArchiveInfo(
//                upgradeFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
//        if (info != null) {
//            Log.e("Utils", "info" + info.versionCode);
//            localVersion = info.versionCode;
//        }

        switch (remoteUpgradeType) {
            case 0:
                if (remoteVersion > localVersion) {
                    checkedUpgradeType = Constants.UPGRADE_TYPE_OPTIONAL_REMOTE;
                }
                break;
            case 1:
                if (remoteVersion > localVersion) {
                    checkedUpgradeType = Constants.UPGRADE_TYPE_OPTIONAL_FORCE;
                }
                break;
            case 3://自升级正常升级
                if (remoteVersion > localVersion) {
                    checkedUpgradeType = Constants.UPGRADE_TYPE_AUTO_OPTIONAL_REMOTE;
                }
                break;
            case 4: //自升级强制升级
                if (remoteVersion > localVersion) {
                    checkedUpgradeType = Constants.UPGRADE_TYPE_AUTO_OPTIONAL_FORCE;
                }
                break;
            case 2: // 不升级
            default:
                checkedUpgradeType = Constants.UPGRADE_TYPE_NO_UPGRADE;
        }
        Logger.d("checkLocalUpgrade ,remoteUpgradeType=" + remoteUpgradeType + ",remoteVersion="
                + remoteVersion);
        return checkedUpgradeType;
    }
}
