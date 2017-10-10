package com.golive.cinema;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.PackageUtils;
import com.initialjie.log.Logger;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Wangzj on 2016/12/16.
 */

public class CrashReportSender implements ReportSender {
    private final static String TAG = CrashReportSender.class.getSimpleName();
    private final static String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final static int MIN_CRASH_TIME_REQUIRED = 1000;
    private final static int RESTART_DELAY_MILLIS = 2000;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(CrashReportSender.class);

    private final StringBuilder mSBuilder = new StringBuilder();
    private final String mLineSeparator = Constants.LINE_SEPARATOR;

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent)
            throws ReportSenderException {

        final String crashStr = gatherInfo(errorContent);
        Log.e(TAG, crashStr);
        logger.error(crashStr);

        // app启动时间
        String appStartDateStr = errorContent.getProperty(ReportField.USER_APP_START_DATE);

        // 崩溃时间
        String crashDateStr = errorContent.getProperty(ReportField.USER_CRASH_DATE);

        // need report by default
        boolean needReport = true;
        try {
            // check whether need to report
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
            Date startDate = simpleDateFormat.parse(appStartDateStr);
            Date crashDate = simpleDateFormat.parse(crashDateStr);
            long timeDiff = (crashDate.getTime() - startDate.getTime());
            Logger.d("timeDiff : " + timeDiff + ", min time diff require : "
                    + MIN_CRASH_TIME_REQUIRED);
            // time spend less than min required time
            if (timeDiff < MIN_CRASH_TIME_REQUIRED) {
                // not need to report
                needReport = false;
                Logger.d("no need to report");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // report
        if (needReport) {
            String exceptionMsg = crashStr;
            int maxLength = Constants.REPORT_EXCEPTION_MSG_MAX_LENTH;
            if (exceptionMsg.length() >= maxLength) {
                exceptionMsg = exceptionMsg.substring(0, maxLength);
            }
            StatisticsHelper.getInstance(context.getApplicationContext())
                    .reportAppException("0", "9001", exceptionMsg, "1");
        }

        // finish all activity
        ActivityUtils.finishAllActivityExclude(context, MainActivity.class);

        // restart apk in delay
        PackageUtils.startApkDelay(context, context.getPackageName(), RESTART_DELAY_MILLIS);
    }

    /**
     * 收集信息
     */
    private String gatherInfo(CrashReportData paramCrashReportData) {

        // 清空之前的文字
        mSBuilder.setLength(0);

        // 换行
        mSBuilder.append(mLineSeparator);

        // 崩溃时间
        String crashDate = paramCrashReportData
                .getProperty(ReportField.USER_CRASH_DATE);
        appendString("crashDate", crashDate);

        // app启动时间
        String appStartDate = paramCrashReportData
                .getProperty(ReportField.USER_APP_START_DATE);
        appendString("appStartDate", appStartDate);

        // 安卓版本
        String androidVersion = paramCrashReportData
                .getProperty(ReportField.ANDROID_VERSION);
        appendString("androidVersion", androidVersion);

        // Android设备牌子
        String brand = paramCrashReportData.getProperty(ReportField.BRAND);
        appendString("brand", brand);

        // 手机类型
        String phoneModel = paramCrashReportData
                .getProperty(ReportField.PHONE_MODEL);
        appendString("phoneModel", phoneModel);

        // Android产品信息
        String product = paramCrashReportData.getProperty(ReportField.PRODUCT);
        appendString("product", product);

        // 包名
        String pkgName = paramCrashReportData
                .getProperty(ReportField.PACKAGE_NAME);
        appendString("pkgName", pkgName);

        // 版本名称
        String appVersionName = paramCrashReportData
                .getProperty(ReportField.APP_VERSION_NAME);
        appendString("appVersionName", appVersionName);

        // 版本号
        String appVersionCode = paramCrashReportData
                .getProperty(ReportField.APP_VERSION_CODE);
        appendString("appVersionCode", appVersionCode);

        // // logcat
        // String logcat = paramCrashReportData.getProperty(ReportField.LOGCAT);
        // appendString("LOGCAT", logcat);

        // stackTrace
        String stackTrace = paramCrashReportData
                .getProperty(ReportField.STACK_TRACE);
        appendString("STACK_TRACE", stackTrace);

        // 换行
        mSBuilder.append(mLineSeparator);

        return mSBuilder.toString();
    }

    private void appendString(String tag, String str) {
        // 标签
        mSBuilder.append(tag);
        // 分隔符
        mSBuilder.append(" : ");
        // 内容
        mSBuilder.append(str);
        // 换行
        mSBuilder.append(mLineSeparator);
    }
}