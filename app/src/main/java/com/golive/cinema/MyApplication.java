package com.golive.cinema;

import static com.golive.cinema.Constants.LOG_FILE_MAX_SIZE_S;
import static com.golive.cinema.Constants.LOG_FORCE;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.golive.cinema.data.source.DownloadRepository;
import com.golive.cinema.data.source.FilmLibraryRepository;
import com.golive.cinema.data.source.FilmsRepository;
import com.golive.cinema.data.source.KdmRepository;
import com.golive.cinema.data.source.MainConfigRepository;
import com.golive.cinema.data.source.OrdersRepository;
import com.golive.cinema.data.source.PlayerRepository;
import com.golive.cinema.data.source.RecommendRepository;
import com.golive.cinema.data.source.ServerInitRepository;
import com.golive.cinema.data.source.StatisticsRepository;
import com.golive.cinema.data.source.UserRepository;
import com.golive.cinema.data.source.VerifyCodeRepository;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.DeviceTypeRuntimeCheck;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.network.helper.SharedPreferencesHelper;
import com.golive.network.helper.UserInfoHelper;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.initialjie.log.FileLoggingTree;
import com.initialjie.log.Logger;
import com.initialjie.log.Settings;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.voole.tvutils.BaseApplication;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.List;

/**
 * Created by Wangzj on 2016/7/8.
 */

@ReportsCrashes(/*formUri = "http://yourserver.com/yourscript",*/
        reportSenderFactoryClasses = {CrashSenderFactory.class},
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MyApplication extends BaseApplication {
    private static final String TAG = MyApplication.class.getSimpleName();
    public static long START_TIME;
    private static Application gContext;
    //    private static DeviceConfig gDeviceConfig;

    private boolean mIsInitSplash = false;

    private String mLogPath;
    private String mLogDir;
    private boolean mLogPrivateFile;
    private FileLoggingTree mFileLoggingTree;
    private RefWatcher mRefWatcher;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext");
        //Avoiding the 64K Limit
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        long sTime = System.currentTimeMillis();
        START_TIME = sTime;
        gContext = this;
        init();
        long eTime = System.currentTimeMillis();
        Logger.d("onCreate, time : " + (eTime - sTime) + "ms");
    }

    private void init() {
        Log.d(TAG, "init");
        initCrashHandler();
        initLog();

        // init LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
//        LeakCanary.install(this);
        mRefWatcher = installLeakCanary();

        initPlayer();

        DeviceTypeRuntimeCheck.isTV(this);

        // register RxBus
        RxBus.get().register(this);

        // Get default device config
//        gDeviceConfig = DeviceConfig.getDefaultDeviceConfig(this);
    }

    /**
     * Initialize the player
     */
    private void initPlayer() {
//        long sTime = System.currentTimeMillis();
//        VPlay vPlay = VPlay.GetInstance();
//        vPlay.initApp(this);
//        long eTime = System.currentTimeMillis();
//        Logger.d("initPlayer, time : " + (eTime - sTime) + "ms");
    }

    /**
     * Initialize the crash handler
     */
    private void initCrashHandler() {
//        mLogDir = getLogDir(null);
//        String path = new File(mLogDir, Constants.LOG_FILE_NAME + "-latest.txt")
// .getAbsolutePath();
//
//        // Create an ConfigurationBuilder. It is prepopulated with values specified via
// annotation.
//        // Set any additional value of the builder and then use it to construct an
//        // ACRAConfiguration.
//        ACRAConfiguration config = null;
//        try {
//            config = new ConfigurationBuilder(this)
//                    .setApplicationLogFile(path)
//                    .setApplicationLogFileLines(100)
//                    .build();
//        } catch (ACRAConfigurationException e) {
//            e.printStackTrace();
//        }
//
//        if (config != null) {
//            // The following line triggers the initialization of ACRA
//            ACRA.init(this, config);
//        }

        ACRA.init(this);
    }

    /**
     * Initialize the log configure
     */
    private void initLog() {

        Settings settings = getLogSettings();
        // initialize log
        com.initialjie.log.Logger.initialize(settings);

        if (Constants.LOG_TO_FILE) {

            // initialize file log
            initFileLog(null);

            registerReceiver();
        }
    }

    /**
     * initialize file log
     */
    private void initFileLog(String ignorePath) {

        // has configure file log before
        if (mFileLoggingTree != null) {
            // stop logging
            mFileLoggingTree.stop();
            // remove the file log
            com.initialjie.log.Logger.uproot(mFileLoggingTree);
        }
        mFileLoggingTree = null;

        mLogDir = getLogDir(ignorePath);
        String logFileName = Constants.LOG_FILE_NAME;
        Log.d(TAG, "initFileLog, mLogDir : " + mLogDir + ", logFileName : " + logFileName);

        Settings settings = getLogSettings();

        // file log
        mFileLoggingTree = new FileLoggingTree(settings, mLogDir, logFileName, LOG_FILE_MAX_SIZE_S);
        // start
        mFileLoggingTree.start();
        // plant file log
        Logger.plant(mFileLoggingTree);
    }

    private Settings getLogSettings() {
        return new Settings()
                .setShowMethodLink(true)
                .setShowThreadInfo(true)
                .setMethodOffset(0)
                .setLogPriority(LOG_FORCE || BuildConfig.DEBUG ? Log.VERBOSE : Log.ASSERT);
    }

    @NonNull
    private String getLogDir(@Nullable String ignorePath) {
        String logDir = null;
        List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();
        if (storageInfos != null && !storageInfos.isEmpty()) {
            int size = storageInfos.size();
            long availableCapacity = 0;
            for (StorageUtils.StorageInfo storageInfo : storageInfos) {
                String path = storageInfo.path;
//                // file not exist
//                if (!new File(path).exists()) {
//                    continue;
//                }
                // not "sdcard" storage && not the ignore path
                if (!path.contains("sdcard") &&
                        (StringUtils.isNullOrEmpty(ignorePath) || !ignorePath.startsWith(path))) {
                    long capacity = StorageUtils.getAvailableCapacity(path);
                    if (capacity > availableCapacity) {
                        availableCapacity = capacity;
                        logDir = path;
                    }
                    break;
                }
            }

            if (!StringUtils.isNullOrEmpty(logDir)) {
                Log.d(TAG, "getLogDir, use storage : " + logDir);
            }
        }

        if (StringUtils.isNullOrEmpty(logDir)) {
            // get default storage
            String state = Environment.getExternalStorageState();
            // is mounted
            if (!StringUtils.isNullOrEmpty(state) && Environment.MEDIA_MOUNTED.equals(state)) {
                logDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                Log.d(TAG, "getLogDir, use external storage : " + logDir);
            }
        }

        if (StringUtils.isNullOrEmpty(logDir)) {
            mLogPrivateFile = true;
            logDir = getFilesDir().getAbsolutePath();
            Log.d(TAG, "getLogDir, use private dir : " + logDir);
        } else {
            mLogPrivateFile = false;
        }

        logDir = new File(logDir, Constants.APP_FILE_NAME).getAbsolutePath();

        return logDir;
    }

    private void registerReceiver() {
        MountReceiver receiver = new MountReceiver(new MountReceiver.OnMountStateChangeListener() {
            @Override
            public void onMountStateChange(String path, MountReceiver.MountState state) {
                Log.d(TAG, "onMountStateChange, path : " + path + ", state : " + state);
                if (!Constants.LOG_TO_FILE) {
                    return;
                }

                boolean initLog = false;
                String ignorePath = null;

                if (MountReceiver.MountState.isRemove(state)) {    // remove
                    //  remove the log file device
                    if (!StringUtils.isNullOrEmpty(mLogDir) && mLogDir.startsWith(path)) {
                        initLog = true;
                        ignorePath = path;
                    }
                } else if (MountReceiver.MountState.Mount == state) { // mount
                    Log.d(TAG, "mLogPrivateFile : " + mLogPrivateFile);
                    // has log to private data path
                    if (mLogPrivateFile) {
                        initLog = true;
                    }
                }

                if (initLog) {
                    // initialize file log again
                    initFileLog(ignorePath);
                }
            }
        });
        // register file mount receiver
        receiver.registerReceiver(this);
    }

    /**
     * clear caches
     */
    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_CLEAR_CACHE)}
    )
    public void clearCaches(Object obj) {
        Logger.d("clearCaches");
        // clear user info cache
        UserInfoHelper.clearUserInfoCache(this);
        // clear data source cache
        MainConfigRepository.destroyInstance();
        ServerInitRepository.destroyInstance();
        UserRepository.destroyInstance();
        FilmsRepository.destroyInstance();
        FilmLibraryRepository.destroyInstance();
        RecommendRepository.destroyInstance();
        OrdersRepository.destroyInstance();
        DownloadRepository.destroyInstance();
        KdmRepository.destroyInstance();
        PlayerRepository.destroyInstance();
        StatisticsRepository.destroyInstance();
        VerifyCodeRepository.destroyInstance();
    }

    /**
     * Change server
     * <P/>
     * Notice: This will invalidate caches and restart activity from the very beginning.
     *
     * @param url server url
     */
    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_CHANGE_SERVER)}
    )
    public void changeServer(String url) {
        Logger.d("changeServer, url : " + url);
        // set main config url
        Constants.APP_MAIN_CONFIG_URL = url;

        // clear cache
        clearCaches(null);
        StatisticsHelper.destroyInstance();
        StatisticsHelper.getInstance(this).refresh(url);
//        // set init false
//        setInitSplash(false);

        // save server url temporarily
        SharedPreferencesHelper.putString(this, Constants.PREF_FILE_NAME, Constants.PREF_SERVER_URL,
                url);

        RxBus.get().post(Constants.EventType.TAG_FINISH_ACTIVITY, new Object());

        // finish all activity and restart
        ActivityUtils.finishAllActivityAndRestart(this, MainActivity.class);
    }

    protected RefWatcher installLeakCanary() {
        return RefWatcher.DISABLED;
    }

    public static Context getContext() {
        return gContext;
    }

//    public static DeviceConfig getDeviceConfig() {
//        return gDeviceConfig;
//    }

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication application = (MyApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }

    public synchronized boolean isInitSplash() {
        return mIsInitSplash;
    }

    public synchronized void setInitSplash(boolean initSplash) {
        mIsInitSplash = initSplash;
    }
}
