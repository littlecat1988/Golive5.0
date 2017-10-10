package com.golive.cinema;


import android.util.SparseArray;

import com.golive.cinema.recommend.RecommendFragment;
import com.golive.cinema.theater.sync.TheaterSyncFragment;
import com.golive.cinema.topic.TopicFragment;
import com.golive.cinema.user.usercenter.UserCenterFragment;

import java.io.File;

/**
 * Created by Wangzj on 2016/9/18.
 */

public class Constants {

    public static final String[] SERVER_URLS = new String[]{
            "http://5api.test.golivetv.tv:5101/goliveAPI/api2/getMainConfig-getMainConfig.action",
            "http://huidu.api.golivetv.tv/goliveAPI/api2/getMainConfig-getMainConfig.action"
    };

    /* server URL */
    public static String APP_MAIN_CONFIG_URL =
            BuildConfig.MAIN_CONFIG_URL;
    //            "http://172.19.0.125:8081/goliveAPI/api2/getMainConfig-getMainConfig.action";
//            "http://211.99.241.12:5101/goliveAPI/api2/getMainConfig-getMainConfig.action";
//            "http://5api.test.golivetv.tv:5101/goliveAPI/api2/getMainConfig-getMainConfig.action";
//            "http://huidu.api.golivetv.tv/goliveAPI/api2/getMainConfig-getMainConfig.action";
//    "http://imax.api.golivetv.tv/goliveAPI/api2/getMainConfig-getMainConfig.action";
//    "http://api3.test.golivetv.tv:8089/golivetvAPI2/api2/getMainConfig-getMainConfig.action";

    /*  */
    public static final boolean PROD = BuildConfig.IS_PROD;

    /** cache film to local */
    public static final boolean FILM_CACHE_LOCAL = BuildConfig.FILM_CACHE_LOCAL;

    /* preference */
    public static final String APP_FILE_NAME = "GoLive";
    public static final String PREF_FILE_NAME = "GolivePref";
    public static final String PREF_SERVER_URL = "pref_server_url";
    public static final String PREF_SERVER_URL_REPORT = "pref_server_url_report";
    public static final String PREF_VERSIONCODE = "pref_versioncode";

    /* log */
    public static final boolean LOG_FORCE = BuildConfig.LOG_FORCE;
    public static final boolean LOG_TO_FILE = BuildConfig.LOG_TO_FILE;
    public static final String LOG_FILE_NAME = "app_log";
    public static final String CRASH_LOG_FILE_NAME = "crash_log";
    public static final String LOG_FILE_MAX_SIZE_S = "10MB";
    public static final long LOG_FILE_MAX_SIZE = 10L << 20;

    /* 升级判断：true为tcl升级判断，只判断是否升级，不下载apk，false下载自升级 */
    public static boolean isTCL = true;

    /* TCL app store package */
    public static final String TCL_APP_MARKET_PACKAGE = "com.tcl.appmarket";
    public static final String TCL_APP_MARKET2_PACKAGE = "com.tcl.appmarket2";
    public static final String TCL_HUAN_APP_STORE_PACKAGE = "com.huan.appstore";

    /* download */
    public static final String DOWNLOAD_FILE_NAME = APP_FILE_NAME + File.separator + "Download";
    public static final String DOWNLOAD_BACKUP = "download_backup";

    public static final String KEY_CHANGE_SERVER = "12344321";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /* report exception */
    public static final int REPORT_EXCEPTION_MSG_MAX_LENTH = 2 << 10;
    public static final int REPORT_HARDWARE_INF_DELAY_IN_MIN = 5;

    /* 从launcher快捷键进入apk */
    /** 启动类型键 */
    public static final String START_TYPE_KEY = "startTypeKey";
    /** 启动内容键 */
    public static final String START_CONTENT_KEY = "startContentKey";
    /** 启动院线入口 */
    public static final String START_TYPE_CINEMA = "2";
    public static final String START_TYPE_CINEMA_KILL_ON_EXIT = "21";

    /* Extra */
    public static final String EXTRA_EXIT = "extra_exit";
    public static final String EXTRA_RESTART = "extra_restart";
    public static final String EXTRA_SERVER_URL = "extra_server_url";
    public static final String EXTRA_FROM = "extra_from";
    public static final String EXTRA_FILM_ID = "extra_film_id";
    public static final String INTENT_FILM_ID = "intent_film_id"; // compatible for old version
    public static final String EXTRA_FILM_NAME = "extra_film_name";
    public static final String EXTRA_MEDIA_ID = "extra_media_id";
    public static final String EXTRA_MEDIA_URL = "extra_media_url";
    public static final String EXTRA_MEDIA_SIZE = "extra_media_size";
    public static final String EXTRA_FILE_PATH = "extra_file_path";
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_PRODUCT_TYPE = "extra_product_type";
    public static final String EXTRA_ENCRYPTION_TYPE = "extra_encryption_type";
    public static final String EXTRA_IS_ONLINE = "extra_is_online";
    public static final String EXTRA_QUANTITY = "extra_quantity";
    public static final String EXTRA_CURRENCY = "extra_currency";
    public static final String EXTRA_PRICE = "extra_price";
    public static final String EXTRA_CREDIT_PAY = "extra_credit_pay";
    public static final String EXTRA_CREDIT_EXPIRE_DATE = "extra_credit_expire_date";
    public static final String EXTRA_CREDIT_EXPIRE_BILL = "extra_credit_expire_bill";
    public static final String EXTRA_CREDIT_EXPIRE_LIMIT = "extra_credit_expire_limit";
    public static final String EXTRA_UPGRADE_URL = "extra_upgrade_url";
    public static final String EXTRA_UPGRADE_CODE = "extra_upgrade_code";
    public static final String EXTRA_BASE_PAGE_ID = "extra_base_page_id";

    /* Film detail */
    public static final String VIEW_TAG_PURCHASE = "tag_purchase";
    public static final String VIEW_TAG_PLAY = "tag_play";
    public static final String EXTRA_FILM_INTRODUCE = "extra_film_introduce";
    public static final String EXTRA_MEDIAS = "extra_medias";

    /* Film watch notice */
    public static final String EXTRA_NOTICE_TYPE = "extra_notice_type";
    public static final String EXTRA_LIMIT_TIME = "extra_limit_time";
    public static final long DEFAULT_WATCH_LIMIT_TIME = 48 * 3600000L;

    /* Film library */
    public static boolean INCLUDE_TOPICS_DEFAULT = false;
    public static final String EXTRA_INCLUDE_TOPICS = "extra_include_topics";
    public static final String EXTRA_SHOW_TOPICS = "extra_show_topics";

    /* Credit pay notice */
    public static final String EXTRA_LIMIT = "extra_limit";
    public static final String EXTRA_DEAD_LINE_DAYS = "extra_dead_line_days";

    /* download */
    public static final String EXTRA_REDOWNLOAD = "extra_redownload";
    public static final String EXTRA_STORAGES = "extra_storages";
    public static final String EXTRA_FILES = "extra_files";
    public static final String EXTRA_ERR_CODE = "extra_err_code";
    public static final String EXTRA_ERR_TITLE = "extra_err_title";
    public static final String EXTRA_ERR_MESSAGE = "extra_err_message";

    /* Player */
    public static final String PLAYER_INTENT_FILM_ID = "player_intent_film_id";
    public static final String PLAYER_INTENT_MEDIA_ID = "player_intent_media_id";
    public static final String PLAYER_INTENT_NAME = "player_intent_name";
    public static final String PLAYER_INTENT_PLAY_PROGRESS = "player_intent_play_progress";
    public static final String PLAYER_INTENT_BOOT_ADVERT = "player_intent_boot_image_advert";
    public static final String PLAYER_INTENT_URLS = "player_intent_urls";
    public static final String PLAYER_INTENT_RANKS = "player_intent_ranks";
    public static final String PLAYER_INTENT_ENCRYPTION_TYPE = "player_intent_encryption_type";
    /** no encryption */
    public static final String PLAYER_ENCRYPTION_TYPE_DEFAULT = "0";
    /** DivX */
    public static final String PLAYER_ENCRYPTION_TYPE_DIVX = "1";
    /** KDM */
    public static final String PLAYER_ENCRYPTION_TYPE_KDM = "2";
    /** Voole */
    public static final String PLAYER_ENCRYPTION_TYPE_VOOLE = "7";
    public static final String PLAYER_INTENT_FILM_ID_POSTER = "player_intent_film_poster";
    public static final String PLAYER_INTENT_FILM_MEDIA_TRAILER =
            "player_intent_film_media_trailer";
    public static final String PLAYER_INTENT_FILM_ADVERT_ALL = "player_intent_film_advert_all";
    /** advert */
    public static final String PLAYER_INTENT_MEDIA_ADVERT = "player_intent_advert";
    public static final String EXTRA_ADVERT_COUNTDOWN = "extra_advert_countdown";
    public static final String PLAYER_INTENT_FILM_ID_POSTER_COLOR =
            "player_intent_film_poster_color";
    public static final String PLAYER_INTENT_FILM_RANK = "player_intent_film_rank";
    public static final String PLAYER_INTENT_WAIT_FOR = "player_intent_media_type";
    public static final String ADVER_TYPE_VIDEO = "1";
    public static final String ADVER_TYPE_IMAGE = "2";
    public static final String ADVER_TYPE_COVER = "3";
    public static final String AD_TIME_TYPE_BEGIN = "1";
    public static final String AD_TIME_TYPE_MIDDLE = "2";
    public static final String AD_TIME_TYPE_END = "3";
    public static final String AD_TIME_TYPE_PAUSE = "4";
    /** 标清 */
    public static final String PLAY_MEDIA_RANK_CLARITY_STANDARD = "1";
    /** 高清 */
    public static final String PLAY_MEDIA_RANK_CLARITY_HIGH = "2";
    /** 超清 */
    public static final String PLAY_MEDIA_RANK_CLARITY_SUPER = "3";
    /* 1080P */
    public static final String PLAY_MEDIA_RANK_CLARITY_1080 = "4";

    /* upgrade */
    public static final String UPGRADE_FILE_NAME = "GoLive_cinema_upgrade.apk";
    /** 商店正常升级 */
    public static final int UPGRADE_TYPE_OPTIONAL_REMOTE = 0;
    /** 商店强制升级 */
    public static final int UPGRADE_TYPE_OPTIONAL_FORCE = 1;
    /** 无升级 */
    public static final int UPGRADE_TYPE_NO_UPGRADE = 2;
    /** 自升级正常升级 */
    public static final int UPGRADE_TYPE_AUTO_OPTIONAL_REMOTE = 3;
    /** 自升级强制升级 */
    public static final int UPGRADE_TYPE_AUTO_OPTIONAL_FORCE = 4;

    /* qr code pay */
    public static final String EXTRA_NORMAL_PAY_AMOUNT = "extra_normal_pay_amount";
    public static final String EXTRA_CREDIT_PAY_AMOUNT = "extra_credit_pay_amount";
    public static final String EXTRA_REFUND_REFUND_CREDIT = "extra_refund_refund_credit";
    public static final String EXTRA_CREDIT_PAY_DEADLINE = "extra_credit_pay_deadline";

    /* common dialog */
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_COUNTDOWN_TIME = "extra_countdown_time";

    /* Paid service agreement */
    public static final String PAID_SERVICE_AGREEMENT_TYPE = "paid_service_agreement_type";

    /* drainage */
    /** 引导进入零花钱方式 */
    public static final String PREF_DRAINAGE = "PREF_DRAINAGE";
    public static final String PREF_GUIDE_TYPE = "PREF_GUIDE_TYPE";
    /** 引导进入零花钱方式 */
    public static final int CHOSEEN_DURATION = 15;

    /* 跳转到广告的StartMode */
    /** 从首页跳转到零花钱 */
    public static final String ADVERT_STARTMODE_HOME = "001";
    /** 从影片详情页跳转到零花钱 */
    public static final String ADVERT_STARTMODE_DETAIL = "002";
    /** 我的钱包跳转到零花钱 */
    public static final String ADVERT_STARTMODE_WALLET = "003";
    /** 推荐引导进入零花钱 */
    public static final String ADVERT_STARTMODE_GUIDE_RECOMMEND = "004";
    /** 强制引导进入零花钱 */
    public static final String ADVERT_STARTMODE_GUIDE_FORCE = "005";
    /** 请求号 */
    public static final int ADVERT_REQUEST_CODE = 12345;

    /* advert(零花钱) */
    public static final String ADVERT_PACKAGE = "com.golive.advert";
    public static final String ADVERT_CLASS = "com.golive.advertlib.AdvertActivity";
    public static final String ADVERT_ENVIRONMENT = "golive_sync_environment";
    public static final String ADVERT_APKTYPE = "golive_sync_apktype";
    public static final String ADVERT_STARTMODE = "startMode";
    public static final String K_KEY_AD = "http://g.dtv.cn.miaozhen.com/x/k";
    public static final String P_KEY_AD = "p";
    /** 是否依赖广告工程 */
    public static final boolean ADVERT_DEPENDENCE = BuildConfig.INCLUDE_ADVERT_PROJ;
    /** 依赖的广告工程版本号 */
    public static final int ADVERT_DEPENDENCE_CODE = BuildConfig.ADVERT_PROJ_VERSION;

    /* 数娱广告开关 */
    public static final String AD_RESOURCE_DIGTAL_MEDIA = "1";
    public static final String AD_RESOURCE_GOLIVE = "0";
    public static final int MATERIAL_TYPE_IMAGE_DIGTAL_MEDIA = 2;
    public static final int MATERIAL_TYPE_VIDEO_DIGTAL_MEDIA = 3;
    public static final int AD_REQUEST_TYPE_BOOT = 1;
    public static final int AD_REQUEST_TYPE_PLAYER = 3;

    /* statistics watch_type：观看类型（1--首发在线、2--同步下载  3--同步在线）*/
    public final static String ONLINE = "1";
    public final static String DOWNLOAD_KDM = "2";
    public final static String ONLINE_KDM = "3";

    /* type：影片类型（1--正片、2--预告片）*/
    public final static String POSITIVE = "1";
    public final static String TRAILER = "2";

    public static final int SCALE_DURATION = 200;
    public static final float SCALE_FACTOR = 1.1f;

    public static final String FIRST_FOCUS_RECOMMEND = "first_focus_recommend";
    public static final String LAST_FOCUS_RECOMMEND = "last_focus_recommend";
    public static final String FIRST_FOCUS_THEATER = "first_focus_theater";
    public static final String LAST_FOCUS_THEATER = "last_focus_theater";
    public static final String FIRST_FOCUS_USER = "first_focus_user";
    public static final String LAST_FOCUS_USER = "last_focus_user";
    public static final String GOTO_PAGE_USER = "goto_page_user";
    public static final String GOTO_PAGE_THEATER = "goto_page_theater";

    /* init broadcast */
    public static final String INIT_SPLASH_BROADCAST_ACTION =
            "init_splash_activity_broadcast_action";
    public static final String INIT_SPLASH_EXIT_BROADCAST_ACTION =
            "init_splash_activity_exit_broadcast_action";
    public static final String INIT_NETWORK_RESTART_BROADCAST_ACTION =
            "init_network_restart_broadcast_action";

    public static final String EXIT_GUIDE_TYPE="guide_Type";

    public static final int PAGE_INDEX_THEATRE = 1;
    public static final int PAGE_INDEX_RECOMMEND = 2;
    public static final int PAGE_INDEX_FILM_LIB = 3;
    public static final int PAGE_INDEX_ADVERT = 4;
    public static final int PAGE_INDEX_USER_CENTER = 5;
    public static final int PAGE_INDEX_TOPIC = 6;

    /** default app page data */
//    public final static String DEFAULT_APP_PAGE =
//            "{\"error\":{\"type\":\"false\"},\"applicationPage\":"
//                    + "{\"basePage\":{\"id\":1,\"name\":\"公版基础页\",\"navigation\":{\"record\":6,"
//                    + "\"datas\":["
//                    + "{\"order\":1,\"title\":\"同步院线\",\"name\":\"同步院线\",\"actionContent\":\""
//                    + PAGE_INDEX_THEATRE + "\"},"
//                    + "{\"order\":2,\"title\":\"推荐\",\"name\":\"推荐\",\"actionContent\":\""
//                    + PAGE_INDEX_RECOMMEND + "\"},"
//                    + "{\"order\":3,\"title\":\"专题\",\"name\":\"专题\",\"actionContent\":\""
//                    + PAGE_INDEX_TOPIC + "\"},"
//                    + "{\"order\":4,\"title\":\"片库\",\"name\":\"片库\",\"actionContent\":\""
//                    + PAGE_INDEX_FILM_LIB + "\"},"
//                    + "{\"order\":5,\"title\":\"天天赚钱\",\"name\":\"天天赚钱\",\"actionContent\":\""
//                    + PAGE_INDEX_ADVERT + "\"},"
//                    + "{\"order\":6,\"title\":\"用户中心\",\"name\":\"用户中心\",\"actionContent\":\""
//                    + PAGE_INDEX_USER_CENTER + "\"}]}}}}";

    /** default app page data */
    public static final String DEFAULT_APP_PAGE =
            "{\"error\":{\"type\":\"false\"},\"applicationPage\":"
                    + "{\"basePage\":{\"id\":1,\"name\":\"公版基础页\","
                    + "\"navigation\":{\"record\":6,"
                    + "\"datas\":["
                    + "{\"order\":1,\"title\":\"同步院线\",\"name\":\"同步院线\","
                    + "\"actionContent\":\""
                    + PAGE_INDEX_THEATRE + "\"},"
                    + "{\"order\":2,\"title\":\"推荐\",\"name\":\"推荐\",\"actionContent\":\""
                    + PAGE_INDEX_RECOMMEND + "\"},"
                    + "{\"order\":3,\"title\":\"片库\",\"name\":\"片库\",\"actionContent\":\""
                    + PAGE_INDEX_FILM_LIB + "\"},"
                    + "{\"order\":4,\"title\":\"天天赚钱\",\"name\":\"天天赚钱\","
                    + "\"actionContent\":\""
                    + PAGE_INDEX_ADVERT + "\"},"
                    + "{\"order\":5,\"title\":\"用户中心\",\"name\":\"用户中心\","
                    + "\"actionContent\":\""
                    + PAGE_INDEX_USER_CENTER + "\"}]}}}}";

    /** default app page with topic data */
    public static final String DEFAULT_APP_PAGE_TOPIC =
            "{\"error\":{\"type\":\"false\"},\"applicationPage\":"
                    + "{\"basePage\":{\"id\":1,\"name\":\"公版基础页\","
                    + "\"navigation\":{\"record\":6,"
                    + "\"datas\":["
                    + "{\"order\":1,\"title\":\"同步院线\",\"name\":\"同步院线\","
                    + "\"actionContent\":\""
                    + PAGE_INDEX_THEATRE + "\"},"
                    + "{\"order\":2,\"title\":\"推荐\",\"name\":\"推荐\",\"actionContent\":\""
                    + PAGE_INDEX_RECOMMEND + "\"},"
                    + "{\"order\":3,\"title\":\"专题\",\"name\":\"专题\",\"actionContent\":\""
                    + PAGE_INDEX_TOPIC + "\"},"
                    + "{\"order\":4,\"title\":\"片库\",\"name\":\"片库\",\"actionContent\":\""
                    + PAGE_INDEX_FILM_LIB + "\"},"
                    + "{\"order\":5,\"title\":\"天天赚钱\",\"name\":\"天天赚钱\","
                    + "\"actionContent\":\""
                    + PAGE_INDEX_ADVERT + "\"},"
                    + "{\"order\":6,\"title\":\"用户中心\",\"name\":\"用户中心\","
                    + "\"actionContent\":\""
                    + PAGE_INDEX_USER_CENTER + "\"}]}}}}";

    public static final SparseArray<Object> CLASSES_MAPS = new SparseArray<>();//约定

    static {
        //约定对应关系
        CLASSES_MAPS.put(PAGE_INDEX_THEATRE, TheaterSyncFragment.class);//同步院线
        CLASSES_MAPS.put(PAGE_INDEX_RECOMMEND, RecommendFragment.class);//推荐
        CLASSES_MAPS.put(PAGE_INDEX_FILM_LIB, "");//片库
        CLASSES_MAPS.put(PAGE_INDEX_ADVERT, "");//天天赚钱
        CLASSES_MAPS.put(PAGE_INDEX_USER_CENTER, UserCenterFragment.class);//用户中心
        if (INCLUDE_TOPICS_DEFAULT) {
            CLASSES_MAPS.put(PAGE_INDEX_TOPIC, TopicFragment.class); // topic
        }
    }

    public static final String getAppPage() {
        return getAppPage(INCLUDE_TOPICS_DEFAULT);
    }

    public static final String getAppPage(boolean isTopicEnable) {
        String appPage;
        if (isTopicEnable) {
            CLASSES_MAPS.put(PAGE_INDEX_TOPIC, TopicFragment.class);
            appPage = DEFAULT_APP_PAGE_TOPIC;
        } else {
            CLASSES_MAPS.remove(PAGE_INDEX_TOPIC);
            appPage = DEFAULT_APP_PAGE;
        }
        return appPage;
    }

    /** Rest api error */
    public static final class RestApiError {
        /** 非法用户 */
        public static final int ERR_USER_ILLEGAL = 2003;
        /** 用户未登录 */
        public static final int ERR_USER_NOT_LOGIN = 2004;
        /** 会员信息不存在 */
        public static final int ERR_USER_NOT_EXIST = 2005;
        /** 设备信息不存在 */
        public static final int ERR_USER_DEVICE_NOT_EXIST = 2006;
        /** 密码错误 */
        public static final int ERR_USER_PW_WRONG = 2007;
        /** LiveKey值错误 */
        public static final int ERR_USER_LIVE_KEY = 2015;
    }

    public static class EventType {
        public final static String TAG_FINISH_ACTIVITY = "tag_finish_activity";
        public final static String TAG_CLEAR_CACHE = "tag_clear_cache";
        public final static String TAG_CHANGE_SERVER = "tag_change_server";
        public final static String TAG_AD_ENTER = "tag_ad_enter";
        public final static String TAG_AD_LEAVE = "tag_ad_leave";
        public final static String TAG_UPDATE_USER_INFO = "tag_update_user_info";
        public final static String TAG_UPDATE_WALLET = "tag_update_wallet";
    }
}
