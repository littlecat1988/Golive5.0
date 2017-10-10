package com.golive.cinema;

import static com.golive.cinema.Constants.ADVERT_REQUEST_CODE;
import static com.golive.cinema.Constants.ADVERT_STARTMODE_HOME;
import static com.golive.cinema.Constants.PAGE_INDEX_USER_CENTER;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.golive.cinema.advert.AdvertHelper;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.init.HomeContract;
import com.golive.cinema.init.HomePresenter;
import com.golive.cinema.init.SplashActivity;
import com.golive.cinema.init.dialog.AppInitializeDialog;
import com.golive.cinema.init.dialog.ExitAdvertDialog;
import com.golive.cinema.init.dialog.ExitRecommendDialog;
import com.golive.cinema.init.dialog.ExitVipDialog;
import com.golive.cinema.player.dialog.RecommendAlertDialog;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.buyvip.BuyVipActivity;
import com.golive.cinema.user.custom.CustomActivity;
import com.golive.cinema.user.topup.TopupActivity;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.DeviceUtils;
import com.golive.cinema.util.FragmentUtils;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Combo;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.Poster;
import com.golive.network.entity.UserInfo;
import com.golive.network.helper.SharedPreferencesHelper;
import com.golive.network.helper.UserInfoHelper;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.initialjie.hw.util.DeviceUtil;
import com.initialjie.log.Logger;

import java.io.Serializable;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Wangzj
 * @date 2016/6/29
 * @Description
 */
public class MainActivity extends BaseActivity implements HomeContract.View {

    //private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_FILM_DETAIL = 2;
    private static final int REQUEST_CODE_AD = 123;
    public static boolean isLeftKeyPressed;
    public static boolean isRightKeyPressed;
    private MainFragment mMainFragment;
    private RelativeLayout mainLayout;
    private ImageView mLoadingView;
    private HomeContract.Presenter mPresenter;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mIsExit;
    private boolean mOnPressBacked;
    private long mStartTime;
    private long mAdEnterTime = -1;
    private String mAdEnterFrom;
    private Runnable mRunOnResume;
    private long mLastTime = 0;
    //private long mFirstFocusTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("onCreate, pid : " + Process.myPid());
//        RxBus.get().register(this);
//        checkAndShowRecommendFilm();
        Intent intent = getIntent();
        mIsExit = intent.getBooleanExtra(Constants.EXTRA_EXIT, false);
        if (mIsExit) {
            Logger.w("exit");
            finish();
//            android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }

        // is not task root
        if (intent.getFlags() != 0 && !isTaskRoot()) {
            finish();
            return;
        }

        checkServerChange();

        setContentView(R.layout.activity_main);
        setHeaderViewVisible(true);
        initBroadcastReceiver();

        boolean initSplash = getMyApplication().isInitSplash();
        Logger.d("initSplash : " + initSplash);
        if (!initSplash) {
            startActivityForResult(new Intent(this, SplashActivity.class), REQUEST_CODE);
            return;
        }

        initMain();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("onResume");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Logger.d("onPostResume");
        if (mRunOnResume != null) {
            Runnable runOnResume = mRunOnResume;
            mRunOnResume = null;
            runOnResume.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d("onPause");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isLeftKeyPressed = (keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
        isRightKeyPressed = (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime < 160) {
                return true;
            }
            mLastTime = currentTime;
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            isLeftKeyPressed = false;
            isRightKeyPressed = false;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);

        switch (requestCode) {
            case REQUEST_CODE:
                if (RESULT_OK == resultCode) {
                    exitApp();
                }
                break;
            case REQUEST_CODE_FILM_DETAIL: // back from film detail
                if (RESULT_OK == resultCode) {
                    exitApp();
                }
                break;
            case ADVERT_REQUEST_CODE:
                if (mAdEnterTime >= 0) {
                    long adDuration = (System.currentTimeMillis() - mAdEnterTime) / 1000;
                    Logger.d("onActivityResult, advert exit, adDuration : " + adDuration
                            + ", mAdEnterFrom : " + mAdEnterFrom);
                    StatisticsHelper helper = StatisticsHelper.getInstance(getApplicationContext());
                    helper.reportExitAd(mAdEnterFrom, String.valueOf(adDuration));

                    // clean up
                    mAdEnterFrom = null;
                    mAdEnterTime = -1;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d("onNewIntent");

        mIsExit = intent.getBooleanExtra(Constants.EXTRA_EXIT, false);
        if (mIsExit) {
            Logger.w("exit");
            finish();
//            android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }

        boolean initSplash = getMyApplication().isInitSplash();
        Logger.d("onNewIntent, initSplash : " + initSplash);
        if (initSplash) {
            // only show after init
            checkAndShowRecommendFilm();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
//        RxBus.get().unregister(this);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
//        getMyApplication().setInitSplash(false);
        if (mIsExit) {
            boolean restart = getIntent().getBooleanExtra(Constants.EXTRA_RESTART, false);
            if (restart) {
                // start apk after delay
                PackageUtils.startApkDelay(this, getPackageName(), 50);
            }

            //kill process
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void showActivityImageView(final Poster poster) {
        Logger.d("showActivityImageView:");
        if (!isActive()) {
            return;
        }
        mainLayout.setVisibility(View.VISIBLE);
        if (poster != null && poster.getUrl() != null) {
            AppInitializeDialog aDialog = FragmentUtils.newFragment(AppInitializeDialog.class);
            Bundle bundle = new Bundle();
            bundle.putString("poster_url", poster.getUrl());
            bundle.putString("emerge_times", poster.getEmergetimes());
            bundle.putString("prompt_time", poster.getPrompt());
            bundle.putString("emerge_category", poster.getEmergecategory());
            aDialog.setArguments(bundle);
            aDialog.setAppInitializeCallback(new AppInitializeDialog.AppInitializeCallback() {
                @Override
                public void dialogDismiss(int cate) {
                    onIntentDialogDismiss(cate, poster);
                }
            });
            aDialog.show(getSupportFragmentManager(), "AppInitializeDialog");
        }
    }

    @Override
    public void setLoadingExitRecommendIndicator(boolean active) {
        if (mLoadingView != null) {
            AnimationDrawable mLoadingViewDrawable = (AnimationDrawable) mLoadingView.getDrawable();
            if (active) {
                mLoadingView.setVisibility(View.VISIBLE);
                if (mLoadingViewDrawable != null) {
                    mLoadingViewDrawable.start();
                }
            } else {
                mLoadingView.setVisibility(View.GONE);
                if (mLoadingViewDrawable != null) {
                    mLoadingViewDrawable.stop();
                }
            }
        }
    }
    @Override
    public void showVipMenuView(final Combo combo) {
        Logger.d("showVipMenuView++++++++++++++++++");
//        hideLoadingView();
        if (!isActive() || null == combo) {
            return;
        }
        if (!mOnPressBacked) {
            return;
        }
        mOnPressBacked = false;

        String vTag = ExitVipDialog.FRAGMENT_TAG;
        Fragment eFragment = getSupportFragmentManager().findFragmentByTag(vTag);
        if (eFragment != null && eFragment instanceof ExitVipDialog) {
            ExitVipDialog eDialog = (ExitVipDialog) eFragment;
            if (eDialog.isResumed()) {
                return;
            }
            eDialog.dismiss();
            FragmentUtils.removePreviousFragment(getSupportFragmentManager(), vTag);
        }

        final ExitVipDialog exitVipDialog = FragmentUtils.newFragment(ExitVipDialog.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ExitVipDialog.VIP_LIST_KEY, combo);
        exitVipDialog.setArguments(bundle);
        exitVipDialog.setExitAppCallback(new ExitVipDialog.ExitAppCallback() {
            @Override
            public void resultDismiss(Integer isExit) {
                switch (isExit) {
                    case ExitVipDialog.PAY_NO_MOVE:
                        Intent intent = new Intent(MainActivity.this, BuyVipActivity.class);
                        intent.putExtra(BuyVipActivity.REQUEST_CODE_EXTRA_KEY, true);
                        MainActivity.this.startActivityForResult(intent, REQUEST_CODE);
                        break;
                    case ExitVipDialog.PAY_NO_EXIT:
                        exitApp();
                        break;
                    case ExitVipDialog.PAY_SUCCESS:
                        //成功刷新下数据
                        Logger.d("ExitVipDialog++++++++++++++++++PAY_SUCCESS");
                        break;
                    default:
                        break;
                }
            }
        });
        exitVipDialog.show(getSupportFragmentManager(), vTag);
    }

    @Override
    public void showExitRecommendView(List<MovieRecommendFilm> recommendFilmList) {
        Logger.d("showExitRecommendView++++++++++++++++++");
//        hideLoadingView();
        if (!isActive()) {
            return;
        }

        if (!mOnPressBacked) {
            return;
        }
        mOnPressBacked = false;

        String eTag = ExitRecommendDialog.FRAGMENT_TAG;
        Fragment eFragment = getSupportFragmentManager().findFragmentByTag(eTag);
        if (eFragment != null && eFragment instanceof ExitRecommendDialog) {
            ExitRecommendDialog eDialog = (ExitRecommendDialog) eFragment;
            if (eDialog.isResumed()) {
                return;
            }
            eDialog.dismiss();
            FragmentUtils.removePreviousFragment(getSupportFragmentManager(), eTag);
        }

        final ExitRecommendDialog exitRecommendDialog = FragmentUtils.newFragment(
                ExitRecommendDialog.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(eTag, (Serializable) recommendFilmList);
        exitRecommendDialog.setArguments(bundle);
        exitRecommendDialog.setDismissListener(new ExitRecommendDialog.OnDismissListener() {
            @Override
            public void onDialogDismiss(boolean exit) {
                if (exit) {
                    exitApp();
                }
                exitRecommendDialog.dismiss();
            }
        });
        exitRecommendDialog.show(getSupportFragmentManager(), eTag);
    }

    @Override
    public void reportAppStart(@Nullable UserInfo userInfo) {
        String clickFrom = "0";
        String targetTo = "0";
        String networkType = "";

        ConnectivityManager cm = NetworkUtils.getConnectivityManager(this);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            switch (ni.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    networkType = "0";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    networkType = "1";
                    break;
                default:
                    break;
            }
        }

        String osVersion = android.os.Build.VERSION.RELEASE;
        String versionCode = "";
        try {
            int code = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionCode = String.valueOf(code);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String userStatus = "2";
        if (userInfo != null) {
            userStatus = userInfo.isVIP() ? "0" : "1";
        }
//        String kdmVersion = "";
//        StatisticsHelper.getInstance(this).reportAppStart(clickFrom, targetTo, networkType,
//                osVersion, versionCode, "", userStatus, kdmVersion);
        mStartTime = System.currentTimeMillis();
//        long duration = mFirstFocusTime - MyApplication.START_TIME;
        long duration = SplashActivity.FIST_SHOW_UP_TIME - MyApplication.START_TIME;
        Logger.d("reportAppStart, duration : " + duration);
        mPresenter.reportAppStart(clickFrom, targetTo, networkType, osVersion, versionCode, "",
                userStatus, String.valueOf(duration / 1000));
    }

    @Override
    public void reportHardwareInfo() {
        _reportHardwareInfo();
    }

    /**
     * 展示类型: 1- 打开APK； 2- 进入影片详情页； 3- 进入充值页； 4- 进入套餐购买页； 5- 进入精彩片花； 6- 进入我的信息；
     * 7- 进入我的钱包； 8- 进入问题反馈； 9- 进入帮助；
     */
    private void onIntentDialogDismiss(int cate, Poster poster) {
        Logger.d("onIntentDialogDismiss:" + cate);
        switch (cate) {
            case AppInitializeDialog.OPEN_DETAIL:
                if (poster != null) {
                    String filmId = poster.getMovieid();
                    if (!StringUtils.isNullOrEmpty(filmId)) {
                        showFilmDetailUI(filmId, false);
                    } else {
                        Toast.makeText(this, R.string.film_detail_missing_film,
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
//            case AppInitializeDialog.OPEN_FEEDBACK:
//                Intent it = new Intent(this, SettingActivity.class);
//                it.putExtra(Constant.INTENT_SETTING_ATY_KEY, Constant
// .INTENT_SETTING_ATY_FEEDBACK_VALUE);
//                startActivity(it);
//                break;
            case AppInitializeDialog.OPEN_HELP:
                this.startActivity(new Intent(this, CustomActivity.class));
                break;
            case AppInitializeDialog.OPEN_MYINFO:
                if (mMainFragment != null) {
                    mMainFragment.onPerformClickButton(PAGE_INDEX_USER_CENTER);
                }
                break;
//            case AppInitializeDialog.OPEN_MYWALLET:
//                break;
//            case AppInitializeDialog.OPEN_TRAILER:
//                break;
            case AppInitializeDialog.OPEN_CHARGE:
                this.startActivity(new Intent(this, TopupActivity.class));
                break;
            case AppInitializeDialog.OPEN_BUYVIP:
                this.startActivity(new Intent(this, BuyVipActivity.class));
                break;
            case AppInitializeDialog.OPEN_APK:
                // 使主页海报获取焦点
                sendBroadcast(new Intent("poster_default_focus"));
                break;
            default:
                break;
        }
    }

    @Override
    public void setPresenter(HomeContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return !isFinishing();
    }

    @Override
    public void onBackPressed() {
        if (null == mMainFragment || mMainFragment.getTabCount() <= 0) {
            exitApp();
            return;
        }

        switch (mMainFragment.getCurrentItem()) {
            case Integer.MAX_VALUE:
                return;
            case 0:
                break;
            default:
                mMainFragment.onPerformClickButton(0);
                return;
        }
        mOnPressBacked = true;
        if (mPresenter != null && NetworkUtils.isNetworkAvailable(this)) {
            mPresenter.exitGuide();
        } else {
//                hideLoadingView();
            exitApp();
        }
    }

    @Override
    public void showDrainage(String guideType) {
        final ExitAdvertDialog exitAdvertDialog = FragmentUtils.newFragment(
                ExitAdvertDialog.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXIT_GUIDE_TYPE, guideType);
        exitAdvertDialog.setArguments(bundle);
        exitAdvertDialog.setExitAppCallback(new ExitAdvertDialog.ExitAppCallback() {
            @Override
            public void resultDismiss(boolean isExit) {
//                if (exitAdvertDialog != null) {
//                    exitAdvertDialog.dismiss();
//                }
                if (!isExit) {
                    //跳转零花钱
                    Activity activity = MainActivity.this;
                    boolean ok = AdvertHelper.goAdvert(activity, REQUEST_CODE_AD,
                            ADVERT_STARTMODE_HOME);
                    if (!ok) {
                        Toast.makeText(activity, getString(R.string.advert_empty_tips),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
//                    MainActivity.super.onBackPressed();
                    exitApp();
                }
            }
        });
        exitAdvertDialog.show(getSupportFragmentManager(), ExitAdvertDialog.FRAGMENT_TAG);
    }

    private void initMain() {
        Logger.d("initMain");
        if (!isActive()) {
            return;
        }

        checkAndShowRecommendFilm();
        updateUserBar();
        // Create the fragment
        mMainFragment = new MainFragment();
        // Add new fragment
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mMainFragment,
                R.id.contentFrame);
        mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mainLayout.setVisibility(View.GONE);
        mLoadingView = (ImageView) findViewById(R.id.main_network_progress_bar);
        mLoadingView.setVisibility(View.GONE);
        String deviceId = DeviceUtil.getDeviceid(this);
        String mac = DeviceUtil.getMacAddress(this);
        Context context = getApplicationContext();
        HomePresenter presenter = new HomePresenter(this,
                Injection.provideActivityImageUseCase(context),
                Injection.provideExitDrainageUseCase(context),
                Injection.provideGuideTypeUseCase(context),
                Injection.provideExitComboUseCase(context),
                Injection.provideGetUserInfoUseCase(context),
                Injection.provideGetMovieRecommendUseCase(context),
                Injection.provideGetKdmVersionUseCase(context),
                Injection.provideStatisticsHelper(context),
                Injection.provideSchedulerProvider());
        Logger.d("deviceId : " + deviceId + ", mac : " + mac);
        initLoadData();
    }

    private void initLoadData() {
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    private void initBroadcastReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.INIT_SPLASH_BROADCAST_ACTION);
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!StringUtils.isNullOrEmpty(action)
                        && Constants.INIT_SPLASH_BROADCAST_ACTION.equals(action)) {
                    Logger.d("onReceive, init return");
                    MyApplication application = getMyApplication();
                    if (!application.isInitSplash()) {
                        application.setInitSplash(true);
                    }

                    if (isSafeToCommitFragment()) {
                        initMain();
                    } else {
                        mRunOnResume = new Runnable() {
                            @Override
                            public void run() {
                                initMain();
                            }
                        };
                    }
//                    if (mMainFragment != null && mMainFragment.getPresenter() != null) {
//                        mMainFragment.getPresenter().start();
//                    }
                } else if (!StringUtils.isNullOrEmpty(action)
                        && Constants.INIT_SPLASH_EXIT_BROADCAST_ACTION.equals(action)) {
                    exitApp();
                }
            }
        };
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    /**
     * check and show recommend film if need.
     */
    private boolean checkAndShowRecommendFilm() {
        Intent intent = getIntent();
        String startType = intent.getStringExtra(Constants.START_TYPE_KEY);
        String startContent = intent.getStringExtra(Constants.START_CONTENT_KEY);
        Logger.d("checkAndShowRecommendFilm, startType : " + startType + ", startContent : "
                + startContent);
        if (!StringUtils.isNullOrEmpty(startType)
                && startType.startsWith(Constants.START_TYPE_CINEMA)
                && !StringUtils.isNullOrEmpty(startContent)) {
            boolean killOnExit = Constants.START_TYPE_CINEMA_KILL_ON_EXIT.equals(startType);
            showFilmDetailUI(startContent, killOnExit);
            return true;
        }

        return false;
    }

    private synchronized void exitApp() {
        mOnPressBacked = false;
        Logger.d("exitApp");
        String duration = String.valueOf((System.currentTimeMillis() - mStartTime) / 1000);
        StatisticsHelper.getInstance(this).reportAppExit(duration);
        ActivityUtils.finishAllActivityExclude(this, MainActivity.class);
    }

    private void _reportHardwareInfo() {
        long sTime = System.currentTimeMillis();
        Context context = getApplicationContext();
        StatisticsHelper helper = StatisticsHelper.getInstance(context);

        String wireMac = DeviceUtil.getMacAddress(context);
        wireMac = StringUtils.getDefaultStringIfEmpty(wireMac);

        String wirelessMac = DeviceUtils.getWirelessMac(context);
        wirelessMac = StringUtils.getDefaultStringIfEmpty(wirelessMac);

        String bluetoothMac = DeviceUtils.getBlueToothMac(context);
        bluetoothMac = StringUtils.getDefaultStringIfEmpty(bluetoothMac);

        String sn = DeviceUtils.getSn();
        sn = StringUtils.getDefaultStringIfEmpty(sn);

        String cpuId = DeviceUtils.getCpuID();
        cpuId = StringUtils.getDefaultStringIfEmpty(cpuId);

        String deviceId = DeviceUtil.getDeviceid(context);
        if (StringUtils.isNullOrEmpty(deviceId)) {
            deviceId = DeviceUtils.getDeviceId(context);
            deviceId = StringUtils.getDefaultStringIfEmpty(deviceId);
        }

        String deviceName = Build.PRODUCT;
        String deviceType = DeviceUtil.getDeviceModel(context);
        String memory = Formatter.formatFileSize(context, DeviceUtils.getTotalMemory(context));

        long storageTotalSize = 0;
        List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();
        if (storageInfos != null && !storageInfos.isEmpty()) {
            for (StorageUtils.StorageInfo storageInfo : storageInfos) {
                storageTotalSize += StorageUtils.getTotalCapacity(storageInfo.path);
            }
        }
        String storage = Formatter.formatFileSize(context, storageTotalSize);
        String density = String.valueOf(DeviceUtils.getScreenDensity(context));
        String resolution = DeviceUtils.getScreenW(context) + "*" + DeviceUtils.getScreenH(context);
        String screenSize = "";

        long eTime = System.currentTimeMillis();
        Logger.d("_reportHardwareInfo,time : " + (eTime - sTime) + "ms, wireMac : " + wireMac
                + ", wirelessMac : " + wirelessMac + ", bluetoothMac : " + bluetoothMac + ", sn : "
                + sn + ", cpuId : " + cpuId + ", deviceId : " + deviceId + ", deviceName : "
                + deviceName + ", deviceType : " + deviceType + ", memory : " + memory
                + ", storage : " + storage + ", density : " + density + ", resolution : "
                + resolution + ", screenSize : " + screenSize);
        helper.reportHardwareInfo(wirelessMac, wireMac, bluetoothMac, sn, cpuId, deviceId,
                deviceName, deviceType, memory, storage, density, resolution, screenSize);
    }

    private void updateUserBar() {
        TextView userIdTv = (TextView) findViewById(R.id.golive_user_name);
        userIdTv.setText(UserInfoHelper.getUserId(this));
        ImageView userHeader = (ImageView) findViewById(R.id.status_user_header_iv);
        String headUrl = UserInfoHelper.getUserHeadUrl(this);
        if (!StringUtils.isNullOrEmpty(headUrl)) {
            Glide.with(this).load(headUrl).into(userHeader);
        }
    }

//    private void showLoadingView() {
//        if (mLoadingView != null) {
//            mLoadingView.setVisibility(View.VISIBLE);
//            AnimationDrawable mLoadingViewDrawable = (AnimationDrawable) mLoadingView
// .getDrawable();
//            if (mLoadingViewDrawable != null) {
//                mLoadingViewDrawable.start();
//            }
//        }
//    }
//
//    private void hideLoadingView() {
//        if (mLoadingView != null) {
//            mLoadingView.setVisibility(View.GONE);
//            AnimationDrawable mLoadingViewDrawable = (AnimationDrawable) mLoadingView
// .getDrawable();
//            if (mLoadingViewDrawable != null) {
//                mLoadingViewDrawable.stop();
//            }
//        }
//    }

    private void showFilmDetailUI(String filmId, boolean exitAfterShow) {
        FilmDetailActivity.jumpToFilmDetailActivity(this, filmId, VIEW_CODE_MAIN_ACTIVITY,
                exitAfterShow, exitAfterShow ? REQUEST_CODE_FILM_DETAIL : 0);
    }

    private void checkServerChange() {
        // get server url
        String key = Constants.PREF_SERVER_URL;
        String serverUrl = SharedPreferencesHelper.getString(this, Constants.PREF_FILE_NAME, key,
                null);
        // has server url
        if (!StringUtils.isNullOrEmpty(serverUrl)) {
            // change the server url
            Constants.APP_MAIN_CONFIG_URL = serverUrl;
            // remove it
            SharedPreferencesHelper.remove(this, Constants.PREF_FILE_NAME, key);
        }
    }

    private MyApplication getMyApplication() {
        return (MyApplication) getApplication();
    }

    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_AD_ENTER)}
    )
    public void onAdEnter(String from) {
        Logger.d("onAdEnter, from : " + from);
        mAdEnterTime = System.currentTimeMillis();
        mAdEnterFrom = from;
    }
}
