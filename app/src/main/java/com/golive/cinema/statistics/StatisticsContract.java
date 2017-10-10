package com.golive.cinema.statistics;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

/**
 * Created by Wangzj on 2016/12/24.
 */

public interface StatisticsContract {

    interface View extends IBaseView<Presenter> {

    }

    interface Presenter extends IBasePresenter<View> {

        void refreshReport();

        void reportAppException(String exceptionType, String exceptionCode, String exceptionMsg,
                String exceptionLevel, String partnerId);

        void reportAppStart(String caller, String destination, String netType, String osVersion,
                String versionCode, String filmId, String userStatus, String kdmVersion,
                String duration);

        void reportAppExit(String duration);

        void reportHardwareInfo(String wirelessMac, String wireMac, String bluetoothMac, String sn,
                String cpuId, String deviceId, String deviceName, String deviceType, String memory,
                String storage, String density, String resolution, String screenSize);

        void reportEnterActivity(String code, String filmName, String from);

        void reportExitActivity(String code, String filmName, String to,
                String duration);

        void reportEnterFilmDetail(String filmId, String filmName, String filmStatus,
                String caller);

        void reportExitFilmDetail(String filmId, String filmName, String filmStatus,
                String duration, String destination);

        void reportExitPrompt(String button, String type, String duration);

        void reportExitWatchNotice(String button, String type, String duration);

        void reportVideoStart(String filmId, String filmName, String filmType, String watchType,
                String definition, String orderSerial, String valueType, String caller);

        void reportVideoLoad(String filmId, String filmName, String filmType, String watchType,
                String videoUrl, String bufferDuration, String definition, String mediaIp,
                String orderSerial, String valueType, String speed);

        void reportVideoExit(String filmId, String filmName, String filmType, String watchType,
                String definition, String watchDuration, String playDuration, String totalDuration,
                String playProgress, String orderSerial, String valueType);

        void reportVideoPlayPause(String filmId, String filmName, String filmType, String watchType,
                String definition, String pauseDuration, String watchDuration, String playDuration,
                String totalDuration, String playProgress, String orderSerial, String valueType);

        void reportVideoSeek(String filmId, String filmName, String filmType, String watchType,
                String definition, String playProgress, String seekToPosition, String toType,
                String orderSerial, String valueType);

        void reportVideoBlock(String filmId, String filmName, String filmType, String watchType,
                String definition, String mediaIp, String bufferDuration, String watchDuration,
                String playDuration, String totalDuration, String playProgress, String orderSerial,
                String valueType);

        void reportVideoStreamSwitch(String filmId, String filmName, String filmType,
                String watchType, String definition, String toDefinition, String watchDuration,
                String playDuration, String totalDuration, String playProgress, String orderSerial,
                String valueType);

        void reportVideoException(String filmId, String filmName, String filmType, String watchType,
                String definition, String errCode, String errMsg, String watchDuration,
                String playDuration, String totalDuration, String playProgress, String orderSerial,
                String valueType);

        void reportEnterAd(String caller);

        void reportExitAd(String caller, String duration);

        void reportThirdPartyAd(String reportUrl);

        void reportPlayAdStart(String adId, String adName, String adType, String adUrl,
                String adDefinition, String adLocation, String adOwnerCode, String adOwnerName,
                String filmId, String filmName, String filmPlayProgress);

        void reportPlayAdLoad(String adId, String adName, String adType, String adUrl,
                String adMediaIp, String adDefinition, String adLocation,
                String adProgress, String bufferDuration, String adOwnerCode, String adOwnerName,
                String filmId, String filmName, String filmPlayProgress);

        void reportPlayAdBlocked(String adId, String adName, String adType,
                String adUrl, String adMediaIp, String adDefinition, String adLocation,
                String adDuration, String adProgress, String bufferDuration, String adOwnerCode,
                String adOwnerName, String filmId, String filmName, String filmPlayProgress);

        void reportPlayAdException(String adId, String adName, String adType, String adUrl,
                String adDefinition, String adLocation, String errCode, String errMsg,
                String adOwnerCode, String adOwnerName, String filmId, String filmName,
                String filmPlayProgress);

        void reportPlayAdExit(String adId, String adName, String adType, String adUrl,
                String adDefinition, String adLocation, String adDuration, String adProgress,
                String bufferDuration, String adOwnerCode, String adOwnerName, String filmId,
                String filmName, String filmPlayProgress);

        void reportAdThirdExposure(int adType, String adCode, String materialCode, String showTime,
                String showType, String pkgName, String activityName);

        void reportEnterUserCenter();

        void reportExitUserCenter(String duration);

        void reportClickUserCenterTopUp(String price, String accountBalance, String button);

        void reportClickUserCenterVip(String price, String accountBalance, String button);

        void reportEnterEvent(String filmId, String filmName, String caller);

        void reportExitEvent(String filmId, String filmName, String destination, String duration);
    }

    /** Report user behavior constants */
    public static interface ReportUserBehaviorConstants {

        /** Report user behavior intent action */
        String REPORT_USER_BEHAVIOR_INTENT_ACTION =
                "com.golive.cinema.statistics.StatisticsService";

        /** Report user behavior type */
        String REPORT_USER_BEHAVIOR_INTENT_REFRESH =
                "report_user_behavior_intent_refresh";

        /** Report user behavior type */
        String REPORT_USER_BEHAVIOR_INTENT_TYPE =
                "report_user_behavior_intent_type";

        /* report type */

        int REPORT_TYPE_NOT_VALID = -1;

        int REPORT_TYPE_APP_START = 0;
        int REPORT_TYPE_APP_EXIT = 1;
        int REPORT_TYPE_HARDWARE_INFO = 2;

        int REPORT_TYPE_ENTER_ACTIVITY = 3;
        int REPORT_TYPE_EXIT_ACTIVITY = 4;

        int REPORT_TYPE_APP_EXCEPTION = 5;

        int REPORT_ENTER_FILM_DETAIL = 20;
        int REPORT_EXIT_FILM_DETAIL = 21;
        int REPORT_EXIT_PROMPT = 22;
        int REPORT_EXIT_WATCH_NOTICE = 23;

        int REPORT_VIDEO_START = 30;
        int REPORT_VIDEO_LOAD = 31;
        int REPORT_VIDEO_PLAY_PAUSE = 32;
        int REPORT_VIDEO_SEEK = 33;
        int REPORT_VIDEO_BLOCKED = 34;
        int REPORT_VIDEO_STREAM_SWITCH = 35;
        int REPORT_VIDEO_EXCEPTION = 36;
        int REPORT_VIDEO_EXIT = 37;

        int REPORT_ENTER_AD = 40;
        int REPORT_EXIT_AD = 41;

        int REPORT_PLAY_AD_START = 50;
        int REPORT_PLAY_AD_LOAD = 51;
        int REPORT_PLAY_AD_BLOCKED = 52;
        int REPORT_PLAY_AD_EXCEPTION = 53;
        int REPORT_PLAY_AD_EXIT = 54;

        int REPORT_ENTER_USER_CENTER = 60;
        int REPORT_EXIT_USER_CENTER = 61;
        int REPORT_TYPE_CLICK_USER_CENTER_TOP_UP = 62;
        int REPORT_TYPE_CLICK_USER_CENTER_VIP = 63;

        int REPORT_ENTER_EVENT = 70;
        int REPORT_EXIT_EVENT = 71;

        int REPORT_THIRD_PARTY_AD = 72;
        int REPORT_THIRD_EXPOSURE_PARTY_AD = 73;

        int VIEW_CODE_MAIN_ACTIVITY = -1;
        int VIEW_CODE_MAIN_FILM_LIST = 0;
        int VIEW_CODE_MAIN_RECOMMEND = 1;
        int VIEW_CODE_FILM_DETAIL = 2;
        int VIEW_CODE_FILM_LIB = 3;
        int VIEW_CODE_USER_CENTER = 4;
        int VIEW_CODE_REGISTER_VIP = 5;
        int VIEW_CODE_TOP_UP = 6;
        int VIEW_CODE_WATCH_RECORD = 7;
        int VIEW_CODE_TRADE_RECORD = 8;
        int VIEW_CODE_MESSAGE = 9;
        int VIEW_CODE_MY_ACCOUNT = 10;
        int VIEW_CODE_SETTING = 11;
        int VIEW_CODE_CUSTOMER_SERVICE = 12;
        int VIEW_CODE_ACCOUNT_INFO = 13;
        int VIEW_CODE_CREDIT_PAY = 14;
        int VIEW_CODE_ACTIVE_VIP = 15;
        int VIEW_CODE_ABOUT = 16;
        int VIEW_CODE_WATCH_NOTICE = 17;
        int VIEW_CODE_CHARGE_NOTICE = 18;
        int VIEW_CODE_PLAYER = 19;

        /* report parameter*/

        /** os version */
        String REPORT_PARAM_OS_VERSION = "report_param_os_version";
        /** version code */
        String REPORT_PARAM_VERSION_CODE = "report_param_version_code";
        /** version name */
        String REPORT_PARAM_VERSION_NAME = "report_param_version_name";
        /** network type */
        String REPORT_PARAM_NETWORK_TYPE = "report_param_network_type";
        /** mac */
        String REPORT_PARAM_MAC = "report_param_mac";
        /** wireless mac */
        String REPORT_PARAM_WIRELESS_MAC = "report_param_wireless_mac";
        /** bluetooth mac */
        String REPORT_PARAM_BLUETOOTH_MAC = "report_param_bluetooth_mac";
        /** sn */
        String REPORT_PARAM_SN = "report_param_sn";
        /** cpu id */
        String REPORT_PARAM_CPU_ID = "report_param_cpu_id";
        /** device id */
        String REPORT_PARAM_DEVICE_ID = "report_param_device_id";
        /** device name */
        String REPORT_PARAM_DEVICE_NAME = "report_param_device_name";
        /** device type */
        String REPORT_PARAM_DEVICE_TYPE = "report_param_device_type";
        /** memory */
        String REPORT_PARAM_DEVICE_MEMORY = "report_param_device_memory";
        /** storage */
        String REPORT_PARAM_DEVICE_STORAGE = "report_param_device_storage";
        /** screen density */
        String REPORT_PARAM_DEVICE_SCREEN_DENSITY =
                "report_param_device_screen_density";
        /** screen resolution */
        String REPORT_PARAM_DEVICE_SCREEN_RESOLUTION =
                "report_param_device_screen_resolution";
        /** screen size */
        String REPORT_PARAM_DEVICE_SCREEN_SCREEN_SIZE =
                "report_param_device_screen_screen_size";
        /** partner id */
        String REPORT_PARAM_PARTNER_ID = "report_param_partner_id";
        /** param exception type */
        String REPORT_PARAM_EXCEPTION_TYPE = "report_param_exception_type";
        /** param exception code */
        String REPORT_PARAM_EXCEPTION_CODE = "report_param_exception_code";
        /** param exception message */
        String REPORT_PARAM_EXCEPTION_MSG = "report_param_exception_msg";
        /** param exception level */
        String REPORT_PARAM_EXCEPTION_LEVEL = "report_param_exception_level";
        /** id */
        String REPORT_PARAM_ID = "report_param_id";
        /** name */
        String REPORT_PARAM_NAME = "report_param_name";
        /** status */
        String REPORT_PARAM_STATUS = "report_param_status";
        /** url */
        String REPORT_PARAM_URL = "report_param_url";
        /** duration */
        String REPORT_PARAM_DURATION = "report_param_duration";
        /** speed */
        String REPORT_PARAM_SPEED = "report_param_speed";
        /** type */
        String REPORT_PARAM_TYPE = "report_param_type";
        /** code */
        String REPORT_PARAM_CODE = "report_param_code";
        /** caller */
        String REPORT_PARAM_CALLER = "report_param_caller";
        /** destination */
        String REPORT_PARAM_DESTINATION = "report_param_destination";
        /** button */
        String REPORT_PARAM_BUTTON = "report_param_button";
        /** location */
        String REPORT_PARAM_LOCATION = "report_param_location";
        /** film id */
        String REPORT_PARAM_FILM_ID = "report_param_film_id";
        /** film name */
        String REPORT_PARAM_FILM_NAME = "report_param_film_name";
        /** film type */
        String REPORT_PARAM_FILM_TYPE = "report_param_film_type";
        /** film status */
        String REPORT_PARAM_FILM_STATUS = "report_param_film_status";
        /** value type */
        String REPORT_PARAM_VALUE_TYPE = "report_param_value_type";
        /** video type */
        String REPORT_PARAM_VIDEO_TYPE = "report_param_video_type";
        /** video watch type */
        String REPORT_PARAM_VIDEO_WATCH_TYPE = "report_param_video_watch_type";
        /** video definition */
        String REPORT_PARAM_VIDEO_DEFINITION = "report_param_video_definition";
        /** video switch to definition */
        String REPORT_PARAM_VIDEO_TO_DEFINITION =
                "report_param_video_to_definition";
        /** video duration */
        String REPORT_PARAM_VIDEO_DURATION = "report_param_video_duration";
        /** video ip */
        String REPORT_PARAM_VIDEO_IP = "report_param_video_ip";
        /** watch duration */
        String REPORT_PARAM_WATCH_DURATION = "report_param_watch_duration";
        /** pause duration */
        String REPORT_PARAM_PAUSE_DURATION = "report_param_pause_duration";
        /** buffer duration */
        String REPORT_PARAM_BUFFER_DURATION = "report_param_buffer_duration";
        /** play duration */
        String REPORT_PARAM_PLAY_DURATION = "report_param_play_duration";
        /** play progress */
        String REPORT_PARAM_PLAY_PROGRESS = "report_param_play_progress";
        /** play position */
        String REPORT_PARAM_PLAY_POSITION = "report_param_play_position";
        /** play error code */
        String REPORT_PARAM_PLAY_ERR_CODE = "report_param_play_err_code";
        /** play error message */
        String REPORT_PARAM_PLAY_ERR_MSG = "report_param_play_err_msg";
        /** AD id */
        String REPORT_PARAM_AD_ID = "report_param_ad_id";
        /** AD name */
        String REPORT_PARAM_AD_NAME = "report_param_ad_name";
        /** AD type */
        String REPORT_PARAM_AD_TYPE = "report_param_ad_type";
        /** AD location */
        String REPORT_PARAM_AD_LOCATION = "report_param_ad_location";
        /** AD progress */
        String REPORT_PARAM_AD_PROGRESS = "report_param_ad_progress";
        /** AD provider code */
        String REPORT_PARAM_AD_PROVIDER_CODE = "report_param_ad_provider_code";
        /** AD provider name */
        String REPORT_PARAM_AD_PROVIDER_NAME = "report_param_ad_provider_name";
        /** account balance */
        String REPORT_PARAM_ACCOUNT_BALANCE = "report_param_account_balance";
        /** order price */
        String REPORT_PARAM_ORDER_PRICE = "report_param_order_price";
        /** order serial */
        String REPORT_PARAM_ORDER_SERIAL = "report_param_order_serial";
        /** kdm version */
        String REPORT_PARAM_KDM_VERSION = "report_param_kdm_version";
        /** user status */
        String REPORT_PARAM_USER_STATUS = "report_param_user_status";
        /** video seek to type */
        String REPORT_PARAM_VIDEO_SEEK_TYPE = "report_param_video_seek_type";

        /** ad reportAdExposure */
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_ZONE_TYPE =
                "report_param_advert_exposure_ad_zone_type";
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_CODE = "report_param_advert_exposure_ad_code";
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_MATERIAL_CODE =
                "report_param_advert_exposure_ad_material_code";
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TIME =
                "report_param_advert_exposure_ad_show_time";
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_SHOW_TYPE =
                "report_param_advert_exposure_ad_show_type";
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_PKG_NAME =
                "report_param_advert_exposure_ad_pkg_name";
        String REPORT_PARAM_ADVERT_EXPOSURE_AD_ACTIVITY_NAME =
                "report_param_advert_exposure_ad_activity_name";
    }
}
