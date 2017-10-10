package com.golive.cinema.statistics;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_AD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_EVENT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_FILM_DETAIL;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_USER_CENTER;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_AD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_EVENT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_FILM_DETAIL;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_PROMPT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_USER_CENTER;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_WATCH_NOTICE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ACCOUNT_BALANCE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_ACTIVITY_NAME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_MATERIAL_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_PKG_NAME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TIME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ADVERT_EXPOSURE_AD_ZONE_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_ID;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_LOCATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_NAME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_PROGRESS;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_PROVIDER_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_PROVIDER_NAME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_AD_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_BLUETOOTH_MAC;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_BUFFER_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_BUTTON;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_CALLER;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_CPU_ID;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DESTINATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_ID;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_MEMORY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_NAME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_SCREEN_DENSITY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_SCREEN_RESOLUTION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_SCREEN_SCREEN_SIZE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_STORAGE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DEVICE_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_EXCEPTION_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_EXCEPTION_LEVEL;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_EXCEPTION_MSG;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_EXCEPTION_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_FILM_ID;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_FILM_NAME;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_FILM_STATUS;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_FILM_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_KDM_VERSION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_MAC;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_NETWORK_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ORDER_PRICE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_ORDER_SERIAL;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_OS_VERSION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PARTNER_ID;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PAUSE_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PLAY_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PLAY_ERR_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PLAY_ERR_MSG;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PLAY_POSITION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_PLAY_PROGRESS;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_SN;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_SPEED;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_URL;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_USER_STATUS;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VALUE_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VERSION_CODE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VIDEO_DEFINITION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VIDEO_IP;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VIDEO_SEEK_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VIDEO_TO_DEFINITION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_VIDEO_WATCH_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_WATCH_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PARAM_WIRELESS_MAC;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PLAY_AD_BLOCKED;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PLAY_AD_EXCEPTION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PLAY_AD_EXIT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PLAY_AD_LOAD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_PLAY_AD_START;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_THIRD_EXPOSURE_PARTY_AD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_THIRD_PARTY_AD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_APP_EXCEPTION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_APP_EXIT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_APP_START;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_CLICK_USER_CENTER_TOP_UP;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_CLICK_USER_CENTER_VIP;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_ENTER_ACTIVITY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_EXIT_ACTIVITY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_TYPE_HARDWARE_INFO;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_USER_BEHAVIOR_INTENT_ACTION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_USER_BEHAVIOR_INTENT_REFRESH;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_USER_BEHAVIOR_INTENT_TYPE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_BLOCKED;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_EXCEPTION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_EXIT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_LOAD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_PLAY_PAUSE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_SEEK;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_START;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_VIDEO_STREAM_SWITCH;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.util.StringUtils;
import com.initialjie.hw.entity.DeviceConfig;
import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2016/12/29.
 */

public class StatisticsHelper {
    private static StatisticsHelper INSTANCE;
    private final Context mContext;
    private DeviceConfig mDeviceConfig;

    private StatisticsHelper(@NonNull Context context) {
        checkNotNull(context);
        // in case of memory leak.
        mContext = context.getApplicationContext();
    }

    public static StatisticsHelper getInstance(@NonNull Context context) {
        if (null == INSTANCE) {
            synchronized (StatisticsHelper.class) {
                if (null == INSTANCE) {
                    INSTANCE = new StatisticsHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        Logger.d("destroyInstance");
        if (INSTANCE != null) {
            Context context = INSTANCE.mContext;
            Intent intent = new Intent(REPORT_USER_BEHAVIOR_INTENT_ACTION);
            // set package name
            intent.setPackage(context.getPackageName());
            // stop service
            context.stopService(intent);
            INSTANCE = null;
        }
    }

    /**
     * refresh
     */
    public void refresh(String serverUrl) {
        Bundle extras = new Bundle();
        extras.putBoolean(REPORT_USER_BEHAVIOR_INTENT_REFRESH, true);
        extras.putString(REPORT_PARAM_URL, serverUrl);
        report(extras);
    }

    /**
     * Report
     */
    private void report(@Nullable Bundle extras) {
        Intent intent = new Intent(REPORT_USER_BEHAVIOR_INTENT_ACTION);
        if (extras != null) {
            intent.putExtras(extras);
        }
        // set package name
        intent.setPackage(mContext.getPackageName());
        // start service
        mContext.startService(intent);
    }

    /**
     * 上报程序异常
     *
     * @param exceptionType 异常类型 0-系统自定义异常；1-用户自定义异常
     * @param exceptionCode 异常编码
     * @param exceptionMsg  异常信息
     */
    public void reportAppException(String exceptionType, String exceptionCode,
            String exceptionMsg, String exceptionLevel) {
        final DeviceConfig config = getDeviceConfig();
        final String partner = config != null ? config.getPartner() : null;
        exceptionType = StringUtils.getDefaultStringIfEmpty(exceptionType);
        exceptionCode = StringUtils.getDefaultStringIfEmpty(exceptionCode);
        exceptionMsg = StringUtils.getDefaultStringIfEmpty(exceptionMsg);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_APP_EXCEPTION);
        extras.putString(REPORT_PARAM_EXCEPTION_TYPE, exceptionType);
        extras.putString(REPORT_PARAM_EXCEPTION_CODE, exceptionCode);
        extras.putString(REPORT_PARAM_EXCEPTION_MSG, exceptionMsg);
        extras.putString(REPORT_PARAM_EXCEPTION_LEVEL, exceptionLevel);
        extras.putString(REPORT_PARAM_PARTNER_ID, partner);
        report(extras);
    }

    /**
     * 上报应用启动
     *
     * @param caller      调起者（0--桌面点击、1--应用点击、2--乐视TV版）
     * @param destination 目标位置（0--首页、1--详情页、2--活动页、3-预告片）
     * @param netType     网络状态（0--无线、1--有线）
     * @param osVersion   OS软件版本号
     * @param versionCode APK软件版本号
     * @param filmId      商品ID
     * @param userStatus  用户状态（0--会员、1--非会员、2--未登录）
     * @param kdmVersion  Kdm version
     * @param duration    启动耗时
     */
    public void reportAppStart(String caller, String destination, String netType, String osVersion,
            String versionCode, String filmId, String userStatus, String kdmVersion,
            String duration) {
        caller = StringUtils.getDefaultStringIfEmpty(caller);
        destination = StringUtils.getDefaultStringIfEmpty(destination);
        netType = StringUtils.getDefaultStringIfEmpty(netType);
        osVersion = StringUtils.getDefaultStringIfEmpty(osVersion);
        versionCode = StringUtils.getDefaultStringIfEmpty(versionCode);
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        userStatus = StringUtils.getDefaultStringIfEmpty(userStatus);
        kdmVersion = StringUtils.getDefaultStringIfEmpty(kdmVersion);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_APP_START);
        extras.putString(REPORT_PARAM_CALLER, caller);
        extras.putString(REPORT_PARAM_DESTINATION, destination);
        extras.putString(REPORT_PARAM_NETWORK_TYPE, netType);
        extras.putString(REPORT_PARAM_OS_VERSION, osVersion);
        extras.putString(REPORT_PARAM_VERSION_CODE, versionCode);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_USER_STATUS, userStatus);
        extras.putString(REPORT_PARAM_KDM_VERSION, kdmVersion);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 上报应用退出
     *
     * @param duration 使用时长
     */
    public void reportAppExit(String duration) {
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_APP_EXIT);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 上报硬件信息
     *
     * @param wirelessMac      无线MAC地址
     * @param wireMac          有线MAC地址
     * @param bluetoothMac     蓝牙的MAC地址
     * @param sn               厂商提供的终端SN
     * @param cpuId            CPU的ID
     * @param deviceId         厂商提供的设备ID
     * @param deviceName       设备名称
     * @param deviceType       设备型号
     * @param memory           设备的内存大小
     * @param storage          设备的存储大小
     * @param screenDensity    屏幕密度
     * @param screenResolution 屏幕分辨率
     * @param screenSize       屏幕尺寸
     */
    public void reportHardwareInfo(String wirelessMac, String wireMac, String bluetoothMac,
            String sn, String cpuId, String deviceId, String deviceName, String deviceType,
            String memory, String storage, String screenDensity, String screenResolution,
            String screenSize) {
        wirelessMac = StringUtils.getDefaultStringIfEmpty(wirelessMac);
        wireMac = StringUtils.getDefaultStringIfEmpty(wireMac);
        bluetoothMac = StringUtils.getDefaultStringIfEmpty(bluetoothMac);
        sn = StringUtils.getDefaultStringIfEmpty(sn);
        cpuId = StringUtils.getDefaultStringIfEmpty(cpuId);
        deviceId = StringUtils.getDefaultStringIfEmpty(deviceId);
        deviceName = StringUtils.getDefaultStringIfEmpty(deviceName);
        deviceType = StringUtils.getDefaultStringIfEmpty(deviceType);
        memory = StringUtils.getDefaultStringIfEmpty(memory);
        storage = StringUtils.getDefaultStringIfEmpty(storage);
        screenDensity = StringUtils.getDefaultStringIfEmpty(screenDensity);
        screenResolution = StringUtils.getDefaultStringIfEmpty(screenResolution);
        screenSize = StringUtils.getDefaultStringIfEmpty(screenSize);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_HARDWARE_INFO);
        extras.putString(REPORT_PARAM_WIRELESS_MAC, wirelessMac);
        extras.putString(REPORT_PARAM_MAC, wireMac);
        extras.putString(REPORT_PARAM_BLUETOOTH_MAC, bluetoothMac);
        extras.putString(REPORT_PARAM_SN, sn);
        extras.putString(REPORT_PARAM_CPU_ID, cpuId);
        extras.putString(REPORT_PARAM_DEVICE_ID, deviceId);
        extras.putString(REPORT_PARAM_DEVICE_NAME, deviceName);
        extras.putString(REPORT_PARAM_DEVICE_TYPE, deviceType);
        extras.putString(REPORT_PARAM_DEVICE_MEMORY, memory);
        extras.putString(REPORT_PARAM_DEVICE_STORAGE, storage);
        extras.putString(REPORT_PARAM_DEVICE_SCREEN_DENSITY, screenDensity);
        extras.putString(REPORT_PARAM_DEVICE_SCREEN_RESOLUTION, screenResolution);
        extras.putString(REPORT_PARAM_DEVICE_SCREEN_SCREEN_SIZE, screenSize);
        report(extras);
    }

    /**
     * Report enter activity.
     *
     * @param code     activity code
     * @param filmName film name if enter film detail
     * @param caller   caller
     */
    public void reportEnterActivity(int code, @Nullable String filmName, int caller) {
//        code = StringUtils.getDefaultStringIfEmpty(code);
//        caller = StringUtils.getDefaultStringIfEmpty(caller);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        Logger.d("report Enter Activity, code:" + code + ", filmName:" + filmName);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_ENTER_ACTIVITY);
        extras.putString(REPORT_PARAM_CODE, String.valueOf(code));
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_CALLER, String.valueOf(caller));
        report(extras);
    }

    /**
     * Report exit activity
     *
     * @param code     activity code
     * @param filmName film name if enter film detail
     * @param to       去向，下一个Activity的编号
     * @param duration 停留时长
     */
    public void reportExitActivity(int code, @Nullable String filmName, @Nullable String to,
            String duration) {
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        to = StringUtils.getDefaultStringIfEmpty(to);
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Logger.d("report Exit Activity, code:" + code + ", filmName:" + filmName + ", duration:"
                + duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_EXIT_ACTIVITY);
        extras.putString(REPORT_PARAM_CODE, String.valueOf(code));
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_DESTINATION, to);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 进入影片详情页
     *
     * @param filmId     影片ID
     * @param filmName   影片名称
     * @param filmStatus 页面状态（1--普通、2--预售未购买、3--预售已购买、4--场次中未购买、
     *                   5--场次中已购买、6--订单过期、7--上映期）
     * @param caller     调起者
     */
    public void reportEnterFilmDetail(String filmId, String filmName, String filmStatus,
            String caller) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmStatus = StringUtils.getDefaultStringIfEmpty(filmStatus);
        caller = StringUtils.getDefaultStringIfEmpty(caller);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_ENTER_FILM_DETAIL);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_STATUS, filmStatus);
        extras.putString(REPORT_PARAM_CALLER, caller);
        report(extras);
    }

    /**
     * 退出影片详情页
     *
     * @param filmId      影片ID
     * @param filmName    影片名称
     * @param filmStatus  页面状态（1--普通、2--预售未购买、3--预售已购买、4--场次中未购买、
     *                    5--场次中已购买、6--订单过期、7--上映期）
     * @param duration    停留时长
     * @param destination 去向
     */
    public void reportExitFilmDetail(String filmId, String filmName, String filmStatus,
            String duration, String destination) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmStatus = StringUtils.getDefaultStringIfEmpty(filmStatus);
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        destination = StringUtils.getDefaultStringIfEmpty(destination);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_EXIT_FILM_DETAIL);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_STATUS, filmStatus);
        extras.putString(REPORT_PARAM_DURATION, duration);
        extras.putString(REPORT_PARAM_DESTINATION, destination);
        report(extras);
    }

    /**
     * 退出提示页
     *
     * @param button   按钮（1--确定、2--继续观看、3--坚持退出）
     * @param type     停留时长
     * @param duration 类型（1--购买成功(不带宣传图片)、2--购买成功(带宣传图片)、3--影片还未
     *                 上映(不带宣传图片)、4--影片还未上映(带宣传图片)、5--场次中途退出、6--电影票
     *                 据过期）
     */
    public void reportExitPrompt(String button, String type, String duration) {
        button = StringUtils.getDefaultStringIfEmpty(button);
        type = StringUtils.getDefaultStringIfEmpty(type);
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_EXIT_PROMPT);
        extras.putString(REPORT_PARAM_BUTTON, button);
        extras.putString(REPORT_PARAM_TYPE, type);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 退出观影须知页
     *
     * @param button   按钮（1--我知道了、2--返回（遥控器）
     * @param type     类型（1--普通、2--预购）
     * @param duration 停留时长
     */
    public void reportExitWatchNotice(String button, String type, String duration) {
        button = StringUtils.getDefaultStringIfEmpty(button);
        type = StringUtils.getDefaultStringIfEmpty(type);
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_EXIT_WATCH_NOTICE);
        extras.putString(REPORT_PARAM_BUTTON, button);
        extras.putString(REPORT_PARAM_TYPE, type);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 播放器打开
     *
     * @param filmId      影片ID
     * @param filmName    影片名称
     * @param filmType    影片类型（1--正片、2--预告片）
     * @param watchType   观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition  清晰度（1--标清、2--高清、3--超清、其它）
     * @param orderSerial 订单号
     * @param valueType   价格类型（0--免费、1--付费）
     * @param caller      来源（0--详情页、1--精彩片花）
     */
    public void reportVideoStart(String filmId, String filmName, String filmType, String watchType,
            String definition, String orderSerial, String valueType, String caller) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        caller = StringUtils.getDefaultStringIfEmpty(caller);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_START);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        extras.putString(REPORT_PARAM_CALLER, caller);
        report(extras);
    }

    /**
     * 开始播放缓冲结束
     *
     * @param filmId         影片ID
     * @param filmName       影片名称
     * @param filmType       影片类型（1--正片、2--预告片
     * @param watchType      观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param videoUrl       播放源的地址
     * @param bufferDuration 缓冲时长
     * @param definition     清晰度
     * @param mediaIp        内容服务器的IP
     * @param orderSerial    订单号
     * @param valueType      价格类型（0--免费、1--付费）
     * @param speed          下载速率（Kbits/s）
     */
    public void reportVideoLoad(String filmId, String filmName, String filmType, String watchType,
            String videoUrl, String bufferDuration, String definition, String mediaIp,
            String orderSerial, String valueType, String speed) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        videoUrl = StringUtils.getDefaultStringIfEmpty(videoUrl);
        bufferDuration = StringUtils.getDefaultStringIfEmpty(bufferDuration);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        mediaIp = StringUtils.getDefaultStringIfEmpty(mediaIp);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        speed = StringUtils.getDefaultStringIfEmpty(speed);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_LOAD);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_URL, videoUrl);
        extras.putString(REPORT_PARAM_DURATION, bufferDuration);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_VIDEO_IP, mediaIp);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        extras.putString(REPORT_PARAM_SPEED, speed);
        report(extras);
    }

    /**
     * 播放器退出
     *
     * @param filmId        影片ID
     * @param filmName      影片名称
     * @param filmType      影片类型（1--正片、2--预告片）
     * @param watchType     观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition    清晰度（1--标清、2--高清、3--超清、其它）
     * @param watchDuration 用户观看时长
     * @param playDuration  电影播放时长
     * @param totalDuration 电影总时长
     * @param playProgress  电影播放进度
     * @param orderSerial   订单号
     * @param valueType     价格类型（0--免费、1--付费）
     */
    public void reportVideoExit(String filmId, String filmName, String filmType, String watchType,
            String definition, String watchDuration, String playDuration, String totalDuration,
            String playProgress, String orderSerial, String valueType) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        watchDuration = StringUtils.getDefaultStringIfEmpty(watchDuration);
        playDuration = StringUtils.getDefaultStringIfEmpty(playDuration);
        totalDuration = StringUtils.getDefaultStringIfEmpty(totalDuration);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_EXIT);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_WATCH_DURATION, watchDuration);
        extras.putString(REPORT_PARAM_PLAY_DURATION, playDuration);
        extras.putString(REPORT_PARAM_DURATION, totalDuration);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, playProgress);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        report(extras);
    }

    /**
     * 播放暂停
     *
     * @param filmId        影片ID
     * @param filmName      影片名称
     * @param filmType      影片类型（1--正片、2--预告片）
     * @param watchType     观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition    清晰度（1--标清、2--高清、3--超清、其它）
     * @param pauseDuration 暂停时长
     * @param watchDuration 用户观看时长
     * @param playDuration  电影播放时长
     * @param totalDuration 电影总时长
     * @param playProgress  电影播放进度
     * @param orderSerial   订单号
     * @param valueType     价格类型（0--免费、1--付费）
     */
    public void reportVideoPlayPause(String filmId, String filmName, String filmType,
            String watchType, String definition, String pauseDuration, String watchDuration,
            String playDuration, String totalDuration, String playProgress, String orderSerial,
            String valueType) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        pauseDuration = StringUtils.getDefaultStringIfEmpty(pauseDuration);
        watchDuration = StringUtils.getDefaultStringIfEmpty(watchDuration);
        playDuration = StringUtils.getDefaultStringIfEmpty(playDuration);
        totalDuration = StringUtils.getDefaultStringIfEmpty(totalDuration);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_PLAY_PAUSE);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_PAUSE_DURATION, pauseDuration);
        extras.putString(REPORT_PARAM_WATCH_DURATION, watchDuration);
        extras.putString(REPORT_PARAM_PLAY_DURATION, playDuration);
        extras.putString(REPORT_PARAM_DURATION, totalDuration);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, playProgress);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        report(extras);
    }

    /**
     * 播放快进/快退
     *
     * @param filmId         影片ID
     * @param filmName       影片名称
     * @param filmType       影片类型（1--正片、2--预告片）
     * @param watchType      观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition     清晰度（1--标清、2--高清、3--超清、其它）
     * @param playProgress   电影播放进度
     * @param seekToPosition 目标位置
     * @param orderSerial    订单号
     * @param valueType      价格类型（0--免费、1--付费）
     */
    public void reportVideoSeek(String filmId, String filmName, String filmType, String watchType,
            String definition, String playProgress, String seekToPosition, String toType,
            String orderSerial,
            String valueType) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        seekToPosition = StringUtils.getDefaultStringIfEmpty(seekToPosition);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_SEEK);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, playProgress);
        extras.putString(REPORT_PARAM_PLAY_POSITION, seekToPosition);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        extras.putString(REPORT_PARAM_VIDEO_SEEK_TYPE, toType);
        report(extras);
    }

    /**
     * 播放缓冲结束
     *
     * @param filmId         影片ID
     * @param filmName       影片名称
     * @param filmType       影片类型（1--正片、2--预告片）
     * @param watchType      观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition     清晰度（1--标清、2--高清、3--超清、其它）
     * @param mediaIp        内容服务器的IP
     * @param bufferDuration 缓冲时长
     * @param watchDuration  用户观看时长
     * @param playDuration   电影播放时长
     * @param totalDuration  电影总时长
     * @param playProgress   电影播放进度
     * @param orderSerial    订单号
     * @param valueType      价格类型（0--免费、1--付费）
     */
    public void reportVideoBlock(String filmId, String filmName, String filmType,
            String watchType, String definition, String mediaIp, String bufferDuration,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        mediaIp = StringUtils.getDefaultStringIfEmpty(mediaIp);
        bufferDuration = StringUtils.getDefaultStringIfEmpty(bufferDuration);
        watchDuration = StringUtils.getDefaultStringIfEmpty(watchDuration);
        playDuration = StringUtils.getDefaultStringIfEmpty(playDuration);
        totalDuration = StringUtils.getDefaultStringIfEmpty(totalDuration);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_BLOCKED);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_VIDEO_IP, mediaIp);
        extras.putString(REPORT_PARAM_BUFFER_DURATION, bufferDuration);
        extras.putString(REPORT_PARAM_WATCH_DURATION, watchDuration);
        extras.putString(REPORT_PARAM_PLAY_DURATION, playDuration);
        extras.putString(REPORT_PARAM_DURATION, totalDuration);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, playProgress);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        report(extras);
    }

    /**
     * 切换码流
     *
     * @param filmId        影片ID
     * @param filmName      影片名称
     * @param filmType      影片类型（1--正片、2--预告片）
     * @param watchType     观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition    清晰度（1--标清、2--高清、3--超清、其它）
     * @param toDefinition  切换成清晰度（1--标清、2--高清、3--超清、其它）
     * @param watchDuration 用户观看时长
     * @param playDuration  电影播放时长
     * @param totalDuration 电影总时长
     * @param playProgress  电影播放进度
     * @param orderSerial   订单号
     * @param valueType     价格类型（0--免费、1--付费）
     */
    public void reportVideoStreamSwitch(String filmId, String filmName, String filmType,
            String watchType, String definition, String toDefinition, String watchDuration,
            String playDuration, String totalDuration, String playProgress, String orderSerial,
            String valueType) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        toDefinition = StringUtils.getDefaultStringIfEmpty(toDefinition);
        watchDuration = StringUtils.getDefaultStringIfEmpty(watchDuration);
        playDuration = StringUtils.getDefaultStringIfEmpty(playDuration);
        totalDuration = StringUtils.getDefaultStringIfEmpty(totalDuration);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_STREAM_SWITCH);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_VIDEO_TO_DEFINITION, toDefinition);
        extras.putString(REPORT_PARAM_WATCH_DURATION, watchDuration);
        extras.putString(REPORT_PARAM_PLAY_DURATION, playDuration);
        extras.putString(REPORT_PARAM_DURATION, totalDuration);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, playProgress);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        report(extras);
    }

    /**
     * 播放异常
     *
     * @param filmId        影片ID
     * @param filmName      影片名称
     * @param filmType      影片类型（1--正片、2--预告片）
     * @param watchType     观看类型（1--首发在线、2--同步下载、3--同步在线）
     * @param definition    清晰度（1--标清、2--高清、3--超清、其它）
     * @param errCode       异常编码
     * @param errMsg        异常内容
     * @param watchDuration 用户观看时长
     * @param playDuration  电影播放时长
     * @param totalDuration 电影总时长
     * @param playProgress  电影播放进度
     * @param orderSerial   订单号
     * @param valueType     价格类型（0--免费、1--付费）
     */
    public void reportVideoException(String filmId, String filmName, String filmType,
            String watchType, String definition, String errCode, String errMsg,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        filmType = StringUtils.getDefaultStringIfEmpty(filmType);
        watchType = StringUtils.getDefaultStringIfEmpty(watchType);
        definition = StringUtils.getDefaultStringIfEmpty(definition);
        errCode = StringUtils.getDefaultStringIfEmpty(errCode);
        errMsg = StringUtils.getDefaultStringIfEmpty(errMsg);
        watchDuration = StringUtils.getDefaultStringIfEmpty(watchDuration);
        playDuration = StringUtils.getDefaultStringIfEmpty(playDuration);
        totalDuration = StringUtils.getDefaultStringIfEmpty(totalDuration);
        playProgress = StringUtils.getDefaultStringIfEmpty(playProgress);
        orderSerial = StringUtils.getDefaultStringIfEmpty(orderSerial);
        valueType = StringUtils.getDefaultStringIfEmpty(valueType);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_VIDEO_EXCEPTION);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_FILM_TYPE, filmType);
        extras.putString(REPORT_PARAM_VIDEO_WATCH_TYPE, watchType);
        extras.putString(REPORT_PARAM_VIDEO_DEFINITION, definition);
        extras.putString(REPORT_PARAM_PLAY_ERR_CODE, errCode);
        extras.putString(REPORT_PARAM_PLAY_ERR_MSG, errMsg);
        extras.putString(REPORT_PARAM_WATCH_DURATION, watchDuration);
        extras.putString(REPORT_PARAM_PLAY_DURATION, playDuration);
        extras.putString(REPORT_PARAM_DURATION, totalDuration);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, playProgress);
        extras.putString(REPORT_PARAM_ORDER_SERIAL, orderSerial);
        extras.putString(REPORT_PARAM_VALUE_TYPE, valueType);
        report(extras);
    }

    /**
     * 跳转到零花钱
     *
     * @param caller 所在位置（1--天天赚钱、2--我的钱包、3--详情页）
     */
    public void reportEnterAd(String caller) {
        caller = StringUtils.getDefaultStringIfEmpty(caller);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_ENTER_AD);
        extras.putString(REPORT_PARAM_CALLER, caller);
        report(extras);
    }

    /**
     * 从零花钱返回
     *
     * @param caller   所在位置（1--天天赚钱、2--我的钱包、3--详情页）
     * @param duration 在零花钱停留时长
     */
    public void reportExitAd(String caller, String duration) {
        caller = StringUtils.getDefaultStringIfEmpty(caller);
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_EXIT_AD);
        extras.putString(REPORT_PARAM_CALLER, caller);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 广告播放开始
     *
     * @param adId             广告ID
     * @param adName           广告名称
     * @param adType           广告类型(1-广告，2-广告组)
     * @param adUrl            广告视频的URL
     * @param adDefinition     广告清晰度
     * @param adLocation       广告位置(1-正片前；2-正片中；3-正片后)
     * @param adOwnerCode      广告商编码
     * @param adOwnerName      广告商名称
     * @param filmId           电影商品ID
     * @param filmName         影片名
     * @param filmPlayProgress 电影播放进度(单位：秒)
     */
    public void reportPlayAdStart(String adId, String adName, String adType, String adUrl,
            String adDefinition, String adLocation, String adOwnerCode, String adOwnerName,
            String filmId, String filmName, long filmPlayProgress) {
        adId = StringUtils.getDefaultStringIfEmpty(adId);
        adName = StringUtils.getDefaultStringIfEmpty(adName);
        adType = StringUtils.getDefaultStringIfEmpty(adType);
        adUrl = StringUtils.getDefaultStringIfEmpty(adUrl);
        adDefinition = StringUtils.getDefaultStringIfEmpty(adDefinition);
        adLocation = StringUtils.getDefaultStringIfEmpty(adLocation);
        adOwnerCode = StringUtils.getDefaultStringIfEmpty(adOwnerCode);
        adOwnerName = StringUtils.getDefaultStringIfEmpty(adOwnerName);
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
//        filmPlayProgress = StringUtils.getDefaultStringIfEmpty(filmPlayProgress);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_PLAY_AD_START);
        extras.putString(REPORT_PARAM_AD_ID, adId);
        extras.putString(REPORT_PARAM_AD_NAME, adName);
        extras.putString(REPORT_PARAM_AD_TYPE, adType);
        extras.putString(REPORT_PARAM_URL, adUrl);
        extras.putString(REPORT_PARAM_AD_LOCATION, adLocation);
        extras.putString(REPORT_PARAM_DESTINATION, adDefinition);
        extras.putString(REPORT_PARAM_AD_PROVIDER_CODE, adOwnerCode);
        extras.putString(REPORT_PARAM_AD_PROVIDER_NAME, adOwnerName);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, String.valueOf(filmPlayProgress));
        report(extras);
    }

    /**
     * 广告开始播放缓冲结束
     *
     * @param adId             广告ID
     * @param adName           广告名称
     * @param adType           广告类型(1-广告，2-广告组)
     * @param adUrl            广告视频的URL
     * @param adMediaIp        内容服务器的IP
     * @param adDefinition     广告清晰度
     * @param adLocation       广告位置(1-正片前；2-正片中；3-正片后)
     * @param bufferDuration   缓冲时长
     * @param adOwnerCode      广告商编码
     * @param adOwnerName      广告商名称
     * @param filmId           电影商品ID
     * @param filmName         影片名
     * @param filmPlayProgress 电影播放进度(单位：秒)
     */
    public void reportPlayAdLoad(String adId, String adName, String adType, String adUrl,
            String adMediaIp, String adDefinition, String adLocation, String bufferDuration,
            String adOwnerCode, String adOwnerName, String filmId, String filmName,
            long filmPlayProgress) {
        adId = StringUtils.getDefaultStringIfEmpty(adId);
        adName = StringUtils.getDefaultStringIfEmpty(adName);
        adType = StringUtils.getDefaultStringIfEmpty(adType);
        adUrl = StringUtils.getDefaultStringIfEmpty(adUrl);
        adMediaIp = StringUtils.getDefaultStringIfEmpty(adMediaIp);
        adDefinition = StringUtils.getDefaultStringIfEmpty(adDefinition);
        adLocation = StringUtils.getDefaultStringIfEmpty(adLocation);
        bufferDuration = StringUtils.getDefaultStringIfEmpty(bufferDuration);
        adOwnerCode = StringUtils.getDefaultStringIfEmpty(adOwnerCode);
        adOwnerName = StringUtils.getDefaultStringIfEmpty(adOwnerName);
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
//        filmPlayProgress = StringUtils.getDefaultStringIfEmpty(filmPlayProgress);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_PLAY_AD_LOAD);
        extras.putString(REPORT_PARAM_AD_ID, adId);
        extras.putString(REPORT_PARAM_AD_NAME, adName);
        extras.putString(REPORT_PARAM_AD_TYPE, adType);
        extras.putString(REPORT_PARAM_URL, adUrl);
        extras.putString(REPORT_PARAM_VIDEO_IP, adMediaIp);
        extras.putString(REPORT_PARAM_AD_LOCATION, adLocation);
        extras.putString(REPORT_PARAM_DESTINATION, adDefinition);
        extras.putString(REPORT_PARAM_BUFFER_DURATION, bufferDuration);
        extras.putString(REPORT_PARAM_AD_PROVIDER_CODE, adOwnerCode);
        extras.putString(REPORT_PARAM_AD_PROVIDER_NAME, adOwnerName);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, String.valueOf(filmPlayProgress));
        report(extras);
    }

    /**
     * 广告播放缓冲结束
     *
     * @param adId             广告ID
     * @param adName           广告名称
     * @param adType           广告类型(1-广告，2-广告组)
     * @param adUrl            广告视频的URL
     * @param adMediaIp        内容服务器的IP
     * @param adDefinition     广告清晰度
     * @param adLocation       广告位置(1-正片前；2-正片中；3-正片后)
     * @param adDuration       广告总时长(单位：秒)
     * @param adProgress       广告播放进度(单位：秒)
     * @param bufferDuration   缓冲时长
     * @param adOwnerCode      广告商编码
     * @param adOwnerName      广告商名称
     * @param filmId           电影商品ID
     * @param filmName         影片名
     * @param filmPlayProgress 电影播放进度(单位：秒)
     */
    public void reportPlayAdBlocked(String adId, String adName, String adType,
            String adUrl, String adMediaIp, String adDefinition, String adLocation,
            String adDuration, String adProgress, String bufferDuration, String adOwnerCode,
            String adOwnerName, String filmId, String filmName, long filmPlayProgress) {
        adId = StringUtils.getDefaultStringIfEmpty(adId);
        adName = StringUtils.getDefaultStringIfEmpty(adName);
        adType = StringUtils.getDefaultStringIfEmpty(adType);
        adUrl = StringUtils.getDefaultStringIfEmpty(adUrl);
        adMediaIp = StringUtils.getDefaultStringIfEmpty(adMediaIp);
        adDefinition = StringUtils.getDefaultStringIfEmpty(adDefinition);
        adLocation = StringUtils.getDefaultStringIfEmpty(adLocation);
        adDuration = StringUtils.getDefaultStringIfEmpty(adDuration);
        adProgress = StringUtils.getDefaultStringIfEmpty(adProgress);
        bufferDuration = StringUtils.getDefaultStringIfEmpty(bufferDuration);
        adOwnerCode = StringUtils.getDefaultStringIfEmpty(adOwnerCode);
        adOwnerName = StringUtils.getDefaultStringIfEmpty(adOwnerName);
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
//        filmPlayProgress = StringUtils.getDefaultStringIfEmpty(filmPlayProgress);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_PLAY_AD_BLOCKED);
        extras.putString(REPORT_PARAM_AD_ID, adId);
        extras.putString(REPORT_PARAM_AD_NAME, adName);
        extras.putString(REPORT_PARAM_AD_TYPE, adType);
        extras.putString(REPORT_PARAM_URL, adUrl);
        extras.putString(REPORT_PARAM_VIDEO_IP, adMediaIp);
        extras.putString(REPORT_PARAM_AD_LOCATION, adLocation);
        extras.putString(REPORT_PARAM_DESTINATION, adDefinition);
        extras.putString(REPORT_PARAM_DURATION, adDuration);
        extras.putString(REPORT_PARAM_AD_PROGRESS, adProgress);
        extras.putString(REPORT_PARAM_BUFFER_DURATION, bufferDuration);
        extras.putString(REPORT_PARAM_AD_PROVIDER_CODE, adOwnerCode);
        extras.putString(REPORT_PARAM_AD_PROVIDER_NAME, adOwnerName);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, String.valueOf(filmPlayProgress));
        report(extras);
    }

    /**
     * 广告播放异常
     *
     * @param adId             广告ID
     * @param adName           广告名称
     * @param adType           广告类型(1-广告，2-广告组)
     * @param adUrl            广告视频的URL
     * @param adDefinition     广告清晰度
     * @param adLocation       广告位置(1-正片前；2-正片中；3-正片后)
     * @param errCode          异常编码
     * @param errMsg           异常内容
     * @param adOwnerCode      广告商编码
     * @param adOwnerName      广告商名称
     * @param filmId           电影商品ID
     * @param filmName         影片名
     * @param filmPlayProgress 电影播放进度(单位：秒)
     */
    public void reportPlayAdException(String adId, String adName, String adType, String adUrl,
            String adDefinition, String adLocation, String errCode, String errMsg,
            String adOwnerCode, String adOwnerName, String filmId, String filmName,
            long filmPlayProgress) {
        adId = StringUtils.getDefaultStringIfEmpty(adId);
        adName = StringUtils.getDefaultStringIfEmpty(adName);
        adType = StringUtils.getDefaultStringIfEmpty(adType);
        adUrl = StringUtils.getDefaultStringIfEmpty(adUrl);
        adDefinition = StringUtils.getDefaultStringIfEmpty(adDefinition);
        adLocation = StringUtils.getDefaultStringIfEmpty(adLocation);
        errCode = StringUtils.getDefaultStringIfEmpty(errCode);
        errMsg = StringUtils.getDefaultStringIfEmpty(errMsg);
        adOwnerCode = StringUtils.getDefaultStringIfEmpty(adOwnerCode);
        adOwnerName = StringUtils.getDefaultStringIfEmpty(adOwnerName);
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
//        filmPlayProgress = StringUtils.getDefaultStringIfEmpty(filmPlayProgress);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_PLAY_AD_EXCEPTION);
        extras.putString(REPORT_PARAM_AD_ID, adId);
        extras.putString(REPORT_PARAM_AD_NAME, adName);
        extras.putString(REPORT_PARAM_AD_TYPE, adType);
        extras.putString(REPORT_PARAM_URL, adUrl);
        extras.putString(REPORT_PARAM_AD_LOCATION, adLocation);
        extras.putString(REPORT_PARAM_DESTINATION, adDefinition);
        extras.putString(REPORT_PARAM_PLAY_ERR_CODE, errCode);
        extras.putString(REPORT_PARAM_PLAY_ERR_MSG, errMsg);
        extras.putString(REPORT_PARAM_AD_PROVIDER_CODE, adOwnerCode);
        extras.putString(REPORT_PARAM_AD_PROVIDER_NAME, adOwnerName);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, String.valueOf(filmPlayProgress));
        report(extras);
    }

    /**
     * 广告播放结束
     *
     * @param adId             广告ID
     * @param adName           广告名称
     * @param adType           广告类型(1-广告，2-广告组)
     * @param adUrl            广告视频的URL
     * @param adDefinition     广告清晰度
     * @param adLocation       广告位置(1-正片前；2-正片中；3-正片后)
     * @param adDuration       广告总时长(单位：秒)
     * @param adProgress       广告播放进度(单位：秒)
     * @param bufferDuration   缓冲时长
     * @param adOwnerCode      广告商编码
     * @param adOwnerName      广告商名称
     * @param filmId           电影商品ID
     * @param filmName         影片名
     * @param filmPlayProgress 电影播放进度(单位：秒)
     */
    public void reportPlayAdExit(String adId, String adName, String adType, String adUrl,
            String adDefinition, String adLocation, String adDuration, String adProgress,
            String bufferDuration, String adOwnerCode, String adOwnerName, String filmId,
            String filmName, long filmPlayProgress) {
        adId = StringUtils.getDefaultStringIfEmpty(adId);
        adName = StringUtils.getDefaultStringIfEmpty(adName);
        adType = StringUtils.getDefaultStringIfEmpty(adType);
        adUrl = StringUtils.getDefaultStringIfEmpty(adUrl);
        adDefinition = StringUtils.getDefaultStringIfEmpty(adDefinition);
        adLocation = StringUtils.getDefaultStringIfEmpty(adLocation);
        adDuration = StringUtils.getDefaultStringIfEmpty(adDuration);
        adProgress = StringUtils.getDefaultStringIfEmpty(adProgress);
        bufferDuration = StringUtils.getDefaultStringIfEmpty(bufferDuration);
        adOwnerCode = StringUtils.getDefaultStringIfEmpty(adOwnerCode);
        adOwnerName = StringUtils.getDefaultStringIfEmpty(adOwnerName);
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
//        filmPlayProgress = StringUtils.getDefaultStringIfEmpty(filmPlayProgress);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_PLAY_AD_EXIT);
        extras.putString(REPORT_PARAM_AD_ID, adId);
        extras.putString(REPORT_PARAM_AD_NAME, adName);
        extras.putString(REPORT_PARAM_AD_TYPE, adType);
        extras.putString(REPORT_PARAM_URL, adUrl);
        extras.putString(REPORT_PARAM_AD_LOCATION, adLocation);
        extras.putString(REPORT_PARAM_DESTINATION, adDefinition);
        extras.putString(REPORT_PARAM_DURATION, adDuration);
        extras.putString(REPORT_PARAM_AD_PROGRESS, adProgress);
        extras.putString(REPORT_PARAM_BUFFER_DURATION, bufferDuration);
        extras.putString(REPORT_PARAM_AD_PROVIDER_CODE, adOwnerCode);
        extras.putString(REPORT_PARAM_AD_PROVIDER_NAME, adOwnerName);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_PLAY_PROGRESS, String.valueOf(filmPlayProgress));
        report(extras);
    }

    /**
     * 进入用户中心
     */
    public void reportEnterUserCenter() {
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_ENTER_USER_CENTER);
        report(extras);
    }

    /**
     * 退出用户中心
     *
     * @param duration 停留时长
     */
    public void reportExitUserCenter(String duration) {
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_EXIT_USER_CENTER);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * 用户中心-充值页点击
     *
     * @param price          价格
     * @param accountBalance 余额
     * @param button         按钮（1--切换支付宝、2--切换微信、3--一键支付）
     */
    public void reportClickUserCenterTopUp(String price, String accountBalance, String button) {
        price = StringUtils.getDefaultStringIfEmpty(price);
        accountBalance = StringUtils.getDefaultStringIfEmpty(accountBalance);
        button = StringUtils.getDefaultStringIfEmpty(button);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_CLICK_USER_CENTER_TOP_UP);
        extras.putString(REPORT_PARAM_ORDER_PRICE, price);
        extras.putString(REPORT_PARAM_ACCOUNT_BALANCE, accountBalance);
        extras.putString(REPORT_PARAM_BUTTON, button);
        report(extras);
    }

    /**
     * 用户中心-全球播会员页点击
     *
     * @param price          价格
     * @param accountBalance 余额
     * @param button         按钮（1--切换支付宝、2--切换微信、3--一键支付）
     */
    public void reportClickUserCenterVip(String price, String accountBalance, String button) {
        price = StringUtils.getDefaultStringIfEmpty(price);
        accountBalance = StringUtils.getDefaultStringIfEmpty(accountBalance);
        button = StringUtils.getDefaultStringIfEmpty(button);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_TYPE_CLICK_USER_CENTER_VIP);
        extras.putString(REPORT_PARAM_ORDER_PRICE, price);
        extras.putString(REPORT_PARAM_ACCOUNT_BALANCE, accountBalance);
        extras.putString(REPORT_PARAM_BUTTON, button);
        report(extras);
    }

    /**
     * 进入活动页
     *
     * @param filmId   影片ID
     * @param filmName 影片名称
     * @param caller   调起者
     */
    public void reportEnterEvent(String filmId, String filmName, String caller) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        caller = StringUtils.getDefaultStringIfEmpty(caller);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_ENTER_EVENT);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_CALLER, caller);
        report(extras);
    }

    /**
     * 退出活动页
     *
     * @param filmId      影片ID
     * @param filmName    影片名称
     * @param destination 去向
     * @param duration    停留时长
     */
    public void reportExitEvent(String filmId, String filmName, String destination,
            String duration) {
        filmId = StringUtils.getDefaultStringIfEmpty(filmId);
        filmName = StringUtils.getDefaultStringIfEmpty(filmName);
        destination = StringUtils.getDefaultStringIfEmpty(destination);
        duration = StringUtils.getDefaultStringIfEmpty(duration);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_EXIT_EVENT);
        extras.putString(REPORT_PARAM_FILM_ID, filmId);
        extras.putString(REPORT_PARAM_FILM_NAME, filmName);
        extras.putString(REPORT_PARAM_DESTINATION, destination);
        extras.putString(REPORT_PARAM_DURATION, duration);
        report(extras);
    }

    /**
     * Report play third-party ad
     */
    public void reportThirdPartyAd(String reportUrl) {
        reportUrl = StringUtils.getDefaultStringIfEmpty(reportUrl);
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_THIRD_PARTY_AD);
        extras.putString(REPORT_PARAM_URL, reportUrl);
        report(extras);
    }

    /**
     * report play ad exposure
     */
    public void reportAdExposure(int adType, String adCode, String materialCode, String showTime,
            String pkgName, String activityName) {
        adCode = StringUtils.getDefaultStringIfEmpty(adCode);
        materialCode = StringUtils.getDefaultStringIfEmpty(materialCode);
        showTime = StringUtils.getDefaultStringIfEmpty(showTime);
        String showType = "1";
        Bundle extras = new Bundle();
        extras.putInt(REPORT_USER_BEHAVIOR_INTENT_TYPE, REPORT_THIRD_EXPOSURE_PARTY_AD);
        extras.putInt(REPORT_PARAM_ADVERT_EXPOSURE_AD_ZONE_TYPE, adType);
        extras.putString(REPORT_PARAM_ADVERT_EXPOSURE_AD_CODE, adCode);
        extras.putString(REPORT_PARAM_ADVERT_EXPOSURE_AD_MATERIAL_CODE, materialCode);
        extras.putString(REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TIME, showTime);
        extras.putString(REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TYPE, showType);
        extras.putString(REPORT_PARAM_ADVERT_EXPOSURE_AD_PKG_NAME, pkgName);
        extras.putString(REPORT_PARAM_ADVERT_EXPOSURE_AD_ACTIVITY_NAME, activityName);
        report(extras);
    }

    private DeviceConfig getDeviceConfig() {
        if (null == mDeviceConfig) {
            synchronized (this) {
                if (null == mDeviceConfig) {
                    mDeviceConfig = DeviceConfig.getDefaultDeviceConfig(mContext);
                }
            }
        }
        return mDeviceConfig;
    }
}
