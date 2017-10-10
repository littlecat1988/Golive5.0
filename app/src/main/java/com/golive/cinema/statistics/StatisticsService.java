package com.golive.cinema.statistics;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_AD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_FILM_DETAIL;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_ENTER_USER_CENTER;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .REPORT_EXIT_AD;
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
        .REPORT_PARAM_ID;
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
        .REPORT_TYPE_NOT_VALID;
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

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.MvpService;
import com.initialjie.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Wangzj on 2016/12/24.
 */

public class StatisticsService extends MvpService<StatisticsContract.Presenter> implements
        StatisticsContract.View {

    private boolean mIsActived;
    private ExecutorService mThreadExecutor;
    private StatisticsContract.Presenter mPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("onCreate");
        init();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
//        Logger.d("onStartCommand");
        if (mThreadExecutor != null && !mThreadExecutor.isShutdown()) {
            mThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    handleCommand(intent);
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        setActived(false);
        if (mThreadExecutor != null) {
            mThreadExecutor.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected StatisticsContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(StatisticsContract.Presenter presenter) {
        Logger.d("setPresenter, presenter : " + presenter);
        mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isActived();
    }

    private void handleCommand(Intent intent) {
        boolean refresh = intent.getBooleanExtra(REPORT_USER_BEHAVIOR_INTENT_REFRESH, false);
//        Logger.d("handleCommand, refresh : " + refresh);
        StatisticsContract.Presenter presenter = getPresenter();
        if (refresh) {
            String serverUrl = intent.getStringExtra(REPORT_PARAM_URL);
            Logger.d("handleCommand, refresh, serverUrl : " + serverUrl);
            // change server url
            Constants.APP_MAIN_CONFIG_URL = serverUrl;
            presenter.refreshReport();
        }

        int reportType = intent.getIntExtra(REPORT_USER_BEHAVIOR_INTENT_TYPE,
                REPORT_TYPE_NOT_VALID);
//        Logger.d("handleCommand, report type : " + reportType);
        if (REPORT_TYPE_NOT_VALID == reportType) {
            return;
        }

        switch (reportType) {
            case REPORT_TYPE_APP_EXCEPTION: {
                String exceptionType = intent.getStringExtra(REPORT_PARAM_EXCEPTION_TYPE);
                String exceptionCode = intent.getStringExtra(REPORT_PARAM_EXCEPTION_CODE);
                String exceptionMsg = intent.getStringExtra(REPORT_PARAM_EXCEPTION_MSG);
                String exceptionLevel = intent.getStringExtra(REPORT_PARAM_EXCEPTION_LEVEL);
                String partnerId = intent.getStringExtra(REPORT_PARAM_PARTNER_ID);
                presenter.reportAppException(exceptionType, exceptionCode, exceptionMsg,
                        exceptionLevel, partnerId);
                break;
            }
            case REPORT_TYPE_APP_START: {
                String caller = intent.getStringExtra(REPORT_PARAM_CALLER);
                String destination = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String filmId = intent.getStringExtra(REPORT_PARAM_ID);
                String userStatus = intent.getStringExtra(REPORT_PARAM_USER_STATUS);
                String networkType = intent.getStringExtra(REPORT_PARAM_NETWORK_TYPE);
                String osVersion = intent.getStringExtra(REPORT_PARAM_OS_VERSION);
                String versionCode = intent.getStringExtra(REPORT_PARAM_VERSION_CODE);
                String kdmVersion = intent.getStringExtra(REPORT_PARAM_KDM_VERSION);
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                presenter.reportAppStart(caller, destination, networkType, osVersion, versionCode,
                        filmId, userStatus, kdmVersion, duration);
                break;
            }
            case REPORT_TYPE_APP_EXIT: {
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                presenter.reportAppExit(duration);
                break;
            }
            case REPORT_TYPE_HARDWARE_INFO: {
                String wireMac = intent.getStringExtra(REPORT_PARAM_MAC);
                String wirelessMac = intent.getStringExtra(REPORT_PARAM_WIRELESS_MAC);
                String bluetoothMac = intent.getStringExtra(REPORT_PARAM_BLUETOOTH_MAC);
                String sn = intent.getStringExtra(REPORT_PARAM_SN);
                String cpuId = intent.getStringExtra(REPORT_PARAM_CPU_ID);
                String deviceId = intent.getStringExtra(REPORT_PARAM_DEVICE_ID);
                String deviceName = intent.getStringExtra(REPORT_PARAM_DEVICE_NAME);
                String deviceType = intent.getStringExtra(REPORT_PARAM_DEVICE_TYPE);
                String memory = intent.getStringExtra(REPORT_PARAM_DEVICE_MEMORY);
                String storage = intent.getStringExtra(REPORT_PARAM_DEVICE_STORAGE);
                String density = intent.getStringExtra(REPORT_PARAM_DEVICE_SCREEN_DENSITY);
                String resolution = intent.getStringExtra(REPORT_PARAM_DEVICE_SCREEN_RESOLUTION);
                String screenSize = intent.getStringExtra(REPORT_PARAM_DEVICE_SCREEN_SCREEN_SIZE);
                presenter.reportHardwareInfo(wirelessMac, wireMac, bluetoothMac, sn, cpuId,
                        deviceId, deviceName, deviceType, memory, storage, density, resolution,
                        screenSize);
                break;
            }
            case REPORT_TYPE_ENTER_ACTIVITY: {
                String code = intent.getStringExtra(REPORT_PARAM_CODE);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String caller = intent.getStringExtra(REPORT_PARAM_CALLER);
                presenter.reportEnterActivity(code, filmName, caller);
                break;
            }
            case REPORT_TYPE_EXIT_ACTIVITY: {
                String code = intent.getStringExtra(REPORT_PARAM_CODE);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String destination = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                presenter.reportExitActivity(code, filmName, destination, duration);
                break;
            }
            case REPORT_ENTER_FILM_DETAIL: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmStatus = intent.getStringExtra(REPORT_PARAM_FILM_STATUS);
                String caller = intent.getStringExtra(REPORT_PARAM_CALLER);
                presenter.reportEnterFilmDetail(filmId, filmName, filmStatus, caller);
                break;
            }
            case REPORT_EXIT_FILM_DETAIL: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmStatus = intent.getStringExtra(REPORT_PARAM_FILM_STATUS);
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String destination = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                presenter.reportExitFilmDetail(filmId, filmName, filmStatus, duration,
                        destination);
                break;
            }
            case REPORT_EXIT_PROMPT:
            case REPORT_EXIT_WATCH_NOTICE: {
                String button = intent.getStringExtra(REPORT_PARAM_BUTTON);
                String type = intent.getStringExtra(REPORT_PARAM_TYPE);
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                if (REPORT_EXIT_PROMPT == reportType) {
                    presenter.reportExitPrompt(button, type, duration);
                }
                if (REPORT_EXIT_WATCH_NOTICE == reportType) {
                    presenter.reportExitWatchNotice(button, type, duration);
                }
                break;
            }

            case REPORT_VIDEO_START: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                String caller = intent.getStringExtra(REPORT_PARAM_CALLER);
                mPresenter.reportVideoStart(filmId, filmName, filmType, watchType,
                        definition, orderSerial, valueType, caller);
                break;
            }

            case REPORT_VIDEO_LOAD: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String videoUrl = intent.getStringExtra(REPORT_PARAM_URL);
                String bufferDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String mediaIp = intent.getStringExtra(REPORT_PARAM_VIDEO_IP);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                String speed = intent.getStringExtra(REPORT_PARAM_SPEED);
                mPresenter.reportVideoLoad(filmId, filmName, filmType, watchType,
                        videoUrl, bufferDuration, definition, mediaIp, orderSerial, valueType,
                        speed);
                break;
            }

            case REPORT_VIDEO_BLOCKED: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String mediaIp = intent.getStringExtra(REPORT_PARAM_VIDEO_IP);
                String bufferDuration = intent.getStringExtra(REPORT_PARAM_BUFFER_DURATION);
                String watchDuration = intent.getStringExtra(REPORT_PARAM_WATCH_DURATION);
                String playDuration = intent.getStringExtra(REPORT_PARAM_PLAY_DURATION);
                String totalDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String playProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                mPresenter.reportVideoBlock(filmId, filmName, filmType, watchType,
                        definition, mediaIp, bufferDuration, watchDuration,
                        playDuration, totalDuration, playProgress, orderSerial, valueType);
                break;
            }
            case REPORT_VIDEO_EXIT: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String watchDuration = intent.getStringExtra(REPORT_PARAM_WATCH_DURATION);
                String playDuration = intent.getStringExtra(REPORT_PARAM_PLAY_DURATION);
                String totalDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String playProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                mPresenter.reportVideoExit(filmId, filmName, filmType, watchType,
                        definition, watchDuration, playDuration, totalDuration, playProgress,
                        orderSerial, valueType);
                break;
            }

            case REPORT_VIDEO_PLAY_PAUSE: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String pauseDuration = intent.getStringExtra(REPORT_PARAM_PAUSE_DURATION);
                String watchDuration = intent.getStringExtra(REPORT_PARAM_WATCH_DURATION);
                String playDuration = intent.getStringExtra(REPORT_PARAM_PLAY_DURATION);
                String totalDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String playProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                mPresenter.reportVideoPlayPause(filmId, filmName, filmType, watchType,
                        definition, pauseDuration, watchDuration, playDuration, totalDuration,
                        playProgress, orderSerial, valueType);
                break;
            }

            case REPORT_VIDEO_SEEK: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String seekToPosition = intent.getStringExtra(REPORT_PARAM_PLAY_POSITION);
                String playProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                String toType = intent.getStringExtra(REPORT_PARAM_VIDEO_SEEK_TYPE);
                mPresenter.reportVideoSeek(filmId, filmName, filmType, watchType,
                        definition, playProgress, seekToPosition, toType, orderSerial, valueType);
                break;
            }

            case REPORT_VIDEO_STREAM_SWITCH: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String toDefinition = intent.getStringExtra(REPORT_PARAM_VIDEO_TO_DEFINITION);
                String watchDuration = intent.getStringExtra(REPORT_PARAM_WATCH_DURATION);
                String playDuration = intent.getStringExtra(REPORT_PARAM_PLAY_DURATION);
                String totalDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String playProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                mPresenter.reportVideoStreamSwitch(filmId, filmName, filmType, watchType,
                        definition, toDefinition, watchDuration, playDuration, totalDuration,
                        playProgress, orderSerial, valueType);
                break;
            }

            case REPORT_VIDEO_EXCEPTION: {
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmType = intent.getStringExtra(REPORT_PARAM_FILM_TYPE);
                String watchType = intent.getStringExtra(REPORT_PARAM_VIDEO_WATCH_TYPE);
                String definition = intent.getStringExtra(REPORT_PARAM_VIDEO_DEFINITION);
                String errCode = intent.getStringExtra(REPORT_PARAM_PLAY_ERR_CODE);
                String errMsg = intent.getStringExtra(REPORT_PARAM_PLAY_ERR_MSG);
                String pauseDuration = intent.getStringExtra(REPORT_PARAM_PAUSE_DURATION);
                String watchDuration = intent.getStringExtra(REPORT_PARAM_WATCH_DURATION);
                String playDuration = intent.getStringExtra(REPORT_PARAM_PLAY_DURATION);
                String playProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                String orderSerial = intent.getStringExtra(REPORT_PARAM_ORDER_SERIAL);
                String valueType = intent.getStringExtra(REPORT_PARAM_VALUE_TYPE);
                mPresenter.reportVideoException(filmId, filmName, filmType, watchType,
                        definition, errCode, errMsg, pauseDuration, watchDuration, playDuration,
                        playProgress, orderSerial, valueType);
                break;
            }

            case REPORT_PLAY_AD_START: {
                String adId = intent.getStringExtra(REPORT_PARAM_AD_ID);
                String adName = intent.getStringExtra(REPORT_PARAM_AD_NAME);
                String adType = intent.getStringExtra(REPORT_PARAM_AD_TYPE);
                String adUrl = intent.getStringExtra(REPORT_PARAM_URL);
                String adLocation = intent.getStringExtra(REPORT_PARAM_AD_LOCATION);
                String adDefinition = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String adOwnerCode = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_CODE);
                String adOwnerName = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_NAME);
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmPlayProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                mPresenter.reportPlayAdStart(adId, adName, adType, adUrl,
                        adDefinition, adLocation, adOwnerCode, adOwnerName,
                        filmId, filmName, filmPlayProgress);
                break;
            }

            case REPORT_PLAY_AD_LOAD: {
                String adId = intent.getStringExtra(REPORT_PARAM_AD_ID);
                String adName = intent.getStringExtra(REPORT_PARAM_AD_NAME);
                String adType = intent.getStringExtra(REPORT_PARAM_AD_TYPE);
                String adUrl = intent.getStringExtra(REPORT_PARAM_URL);
                String adMediaIp = intent.getStringExtra(REPORT_PARAM_VIDEO_IP);
                String adLocation = intent.getStringExtra(REPORT_PARAM_AD_LOCATION);
//                String adDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String adProgress = intent.getStringExtra(REPORT_PARAM_AD_PROGRESS);
                String adDefinition = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String bufferDuration = intent.getStringExtra(REPORT_PARAM_BUFFER_DURATION);
                String adOwnerCode = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_CODE);
                String adOwnerName = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_NAME);
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmPlayProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                mPresenter.reportPlayAdLoad(adId, adName, adType, adUrl, adMediaIp,
                        adDefinition, adLocation, adProgress, bufferDuration,
                        adOwnerCode, adOwnerName,
                        filmId, filmName, filmPlayProgress);
                break;
            }

            case REPORT_PLAY_AD_BLOCKED: {
                String adId = intent.getStringExtra(REPORT_PARAM_AD_ID);
                String adName = intent.getStringExtra(REPORT_PARAM_AD_NAME);
                String adType = intent.getStringExtra(REPORT_PARAM_AD_TYPE);
                String adUrl = intent.getStringExtra(REPORT_PARAM_URL);
                String adMediaIp = intent.getStringExtra(REPORT_PARAM_VIDEO_IP);
                String adLocation = intent.getStringExtra(REPORT_PARAM_AD_LOCATION);
                String adDefinition = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String adDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String adProgress = intent.getStringExtra(REPORT_PARAM_AD_PROGRESS);
                String bufferDuration = intent.getStringExtra(REPORT_PARAM_BUFFER_DURATION);
                String adOwnerCode = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_CODE);
                String adOwnerName = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_NAME);
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmPlayProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                mPresenter.reportPlayAdBlocked(adId, adName, adType, adUrl, adMediaIp,
                        adDefinition, adLocation, adDuration, adProgress, bufferDuration,
                        adOwnerCode, adOwnerName,
                        filmId, filmName, filmPlayProgress);
                break;
            }

            case REPORT_PLAY_AD_EXCEPTION: {
                String adId = intent.getStringExtra(REPORT_PARAM_AD_ID);
                String adName = intent.getStringExtra(REPORT_PARAM_AD_NAME);
                String adType = intent.getStringExtra(REPORT_PARAM_AD_TYPE);
                String adUrl = intent.getStringExtra(REPORT_PARAM_URL);
                String adLocation = intent.getStringExtra(REPORT_PARAM_AD_LOCATION);
                String adDefinition = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String errCode = intent.getStringExtra(REPORT_PARAM_PLAY_ERR_CODE);
                String errMsg = intent.getStringExtra(REPORT_PARAM_PLAY_ERR_MSG);
                String adOwnerCode = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_CODE);
                String adOwnerName = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_NAME);
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmPlayProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                mPresenter.reportPlayAdException(adId, adName, adType, adUrl,
                        adDefinition, adLocation, errCode, errMsg,
                        adOwnerCode, adOwnerName, filmId, filmName, filmPlayProgress);
                break;
            }

            case REPORT_PLAY_AD_EXIT: {
                String adId = intent.getStringExtra(REPORT_PARAM_AD_ID);
                String adName = intent.getStringExtra(REPORT_PARAM_AD_NAME);
                String adType = intent.getStringExtra(REPORT_PARAM_AD_TYPE);
                String adUrl = intent.getStringExtra(REPORT_PARAM_URL);
                String adLocation = intent.getStringExtra(REPORT_PARAM_AD_LOCATION);
                String adDefinition = intent.getStringExtra(REPORT_PARAM_DESTINATION);
                String adDuration = intent.getStringExtra(REPORT_PARAM_DURATION);
                String adProgress = intent.getStringExtra(REPORT_PARAM_AD_PROGRESS);
                String bufferDuration = intent.getStringExtra(REPORT_PARAM_BUFFER_DURATION);
                String adOwnerCode = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_CODE);
                String adOwnerName = intent.getStringExtra(REPORT_PARAM_AD_PROVIDER_NAME);
                String filmId = intent.getStringExtra(REPORT_PARAM_FILM_ID);
                String filmName = intent.getStringExtra(REPORT_PARAM_FILM_NAME);
                String filmPlayProgress = intent.getStringExtra(REPORT_PARAM_PLAY_PROGRESS);
                mPresenter.reportPlayAdExit(adId, adName, adType, adUrl,
                        adDefinition, adLocation, adDuration, adProgress,
                        bufferDuration, adOwnerCode, adOwnerName, filmId,
                        filmName, filmPlayProgress);
                break;
            }

            case REPORT_ENTER_AD: {
                String caller = intent.getStringExtra(REPORT_PARAM_CALLER);
                mPresenter.reportEnterAd(caller);
                break;
            }

            case REPORT_EXIT_AD: {
                String caller = intent.getStringExtra(REPORT_PARAM_CALLER);
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                mPresenter.reportExitAd(caller, duration);
                break;
            }

            case REPORT_THIRD_PARTY_AD: {
                String url = intent.getStringExtra(REPORT_PARAM_URL);
                mPresenter.reportThirdPartyAd(url);
                break;
            }

            case REPORT_THIRD_EXPOSURE_PARTY_AD: {
                int adType = intent.getIntExtra(REPORT_PARAM_ADVERT_EXPOSURE_AD_ZONE_TYPE,
                        Constants.AD_REQUEST_TYPE_BOOT);
                String adCode = intent.getStringExtra(REPORT_PARAM_ADVERT_EXPOSURE_AD_CODE);
                String materialCode = intent.getStringExtra(
                        REPORT_PARAM_ADVERT_EXPOSURE_AD_MATERIAL_CODE);
                String showTime = intent.getStringExtra(REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TIME);
                String showType = intent.getStringExtra(REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TYPE);
                String pkgName = intent.getStringExtra(REPORT_PARAM_ADVERT_EXPOSURE_AD_PKG_NAME);
                String activityName = intent.getStringExtra(
                        REPORT_PARAM_ADVERT_EXPOSURE_AD_ACTIVITY_NAME);
                mPresenter.reportAdThirdExposure(adType, adCode, materialCode, showTime, showType,
                        pkgName, activityName);
            }

            case REPORT_ENTER_USER_CENTER:
                mPresenter.reportEnterUserCenter();
                break;

            case REPORT_EXIT_USER_CENTER:
                String duration = intent.getStringExtra(REPORT_PARAM_DURATION);
                mPresenter.reportExitUserCenter(duration);
                break;

            case REPORT_TYPE_CLICK_USER_CENTER_TOP_UP:
                String price = intent.getStringExtra(REPORT_PARAM_ORDER_PRICE);
                String accountBalance = intent.getStringExtra(REPORT_PARAM_ACCOUNT_BALANCE);
                String button = intent.getStringExtra(REPORT_PARAM_BUTTON);
                mPresenter.reportClickUserCenterTopUp(price, accountBalance, button);
                break;

            case REPORT_TYPE_CLICK_USER_CENTER_VIP:
                String vPrice = intent.getStringExtra(REPORT_PARAM_ORDER_PRICE);
                String vBalance = intent.getStringExtra(REPORT_PARAM_ACCOUNT_BALANCE);
                String vButton = intent.getStringExtra(REPORT_PARAM_BUTTON);
                mPresenter.reportClickUserCenterVip(vPrice, vBalance, vButton);
                break;
            default:
                break;
        }
    }

    private void init() {
        setActived(true);

//        // get server url
//        String serverUrl = SharedPreferencesHelper.getString(this, Constants.PREF_FILE_NAME,
//                Constants.PREF_SERVER_URL_REPORT, null);
//        // has server url
//        if (!StringUtils.isNullOrEmpty(serverUrl)) {
//            // change the server url
//            Constants.APP_MAIN_CONFIG_URL = serverUrl;
//            // remove it
//            SharedPreferencesHelper.remove(this, Constants.PREF_FILE_NAME,
//                    Constants.PREF_SERVER_URL_REPORT);
//        }

        mThreadExecutor = Executors.newSingleThreadExecutor();
        Context context = getApplicationContext();
        StatisticsPresenter statisticsPresenter = new StatisticsPresenter(this,
                Injection.provideGetMainConfigUseCase(context),
                Injection.provideReportAppExceptionUseCase(context),
                Injection.provideReportAppStartBehaviorUseCase(context),
                Injection.provideReportAppExitBehaviorUseCase(context),
                Injection.provideReportEnterActivityUseCase(context),
                Injection.provideReportExitActivityUseCase(context),
                Injection.provideReportHardwareInfoUseCase(context),
                Injection.provideReportEnterFilmDetailUseCase(context),
                Injection.provideReportExitFilmDetailUseCase(context),
                Injection.provideReportExitPromptUseCase(context),
                Injection.provideReportExitWatchNoticeUseCase(context),
                Injection.provideReportClickUserCenterTopUpUseCase(context),
                Injection.provideReportClickUserCenterVipUseCase(context),
                Injection.provideReportEnterUserCenterUseCase(context),
                Injection.provideReportExitUserCenterUseCase(context),
                Injection.provideReportEnterEventUseCase(context),
                Injection.provideReportExitEventUseCase(context),
                Injection.provideReportEnterAdUseCase(context),
                Injection.provideReportExitAdUseCase(context),
                Injection.provideReportThirdPartyAdUseCase(context),
                Injection.provideReportPlayAdStartUseCase(context),
                Injection.provideReportPlayAdLoadUseCase(context),
                Injection.provideReportPlayAdBlockedUseCase(context),
                Injection.provideReportPlayAdExceptionUseCase(context),
                Injection.provideReportPlayAdExitUseCase(context),
                Injection.provideReportVideoStartUseCase(context),
                Injection.provideReportVideoLoadUseCase(context),
                Injection.provideReportVideoPlayPauseUseCase(context),
                Injection.provideReportVideoSeekUseCase(context),
                Injection.provideReportVideoStreamSwitchUseCase(context),
                Injection.provideReportVideoBlockUseCase(context),
                Injection.provideReportVideoExceptionUseCase(context),
                Injection.provideReportVideoExitUseCase(context),
                Injection.provideReportThirdAdvertExposureUseCase(context)
        );
    }

    private synchronized boolean isActived() {
        return mIsActived;
    }

    private synchronized void setActived(boolean actived) {
        mIsActived = actived;
    }
}
