package com.golive.cinema.player;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.golive.cinema.Constants;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.advert.AdvertDialog;
import com.golive.cinema.advert.AdvertImageDialog;
import com.golive.cinema.advert.AdvertImagePauseDialog;
import com.golive.cinema.advert.AdvertMediaDialog;
import com.golive.cinema.player.controller.MediaControllerHolder;
import com.golive.cinema.player.controller.NativeMediaController;
import com.golive.cinema.player.dialog.PlayPreloadingDialog;
import com.golive.cinema.player.dialog.RecommendAlertDialog;
import com.golive.cinema.player.views.PlayerBusyingView;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.FragmentUtils;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.WatermarkHelper;
import com.golive.network.entity.Ad;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.hw.util.DeviceUtil;
import com.initialjie.log.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscriber;

/**
 * Base Fragment for playing medias which wrap common play business.
 * <p/>
 * Created by Wangzj on 2016/9/18.
 */
public abstract class BasePlayerFragment extends MvpFragment implements PlayerContract.View,
        SurfaceHolder.Callback {

    private final static String FRAG_TAG_LOADING = "frag_tag_loading";
    private final static String FRAG_TAG_RECOMMEND = "frag_tag_recommend";
    private final static String FRAG_TAG_AD_MEDIA = "frag_tag_ad_media";
    private final static String FRAG_TAG_AD_IMAGE = "frag_tag_ad_image";
    private final static String FRAG_TAG_AD_IMAGE_PAUSE = "frag_tag_ad_image_pause";

    private PlayerContract.Presenter mPresenter;

    /** 存放控制栏的容器 */
    private ViewGroup mViewContainer;

    /** 控制栏 */
    private NativeMediaController mNativeMediaController;

    private View mPlayerBusyView;
    private View mLoadingRecommendView;
    private ViewGroup mWaterMarkFl;

    private String mFilmId;
    private String mMediaName;
    private boolean mIsOnlinePlay;
    private boolean mSurfaceCreated;
    private boolean mIsTrailer;
    private boolean mOnPressBacked;
    private long mWatchDuration; //播放时长记录点
    private long mVideoLoadDuration; //开始播放缓冲时长记录点
    private long mVideoLoadBlockDuration; //播放缓冲时长记录点
    private long mPauseDuration;
    private List<MovieRecommendFilm> mRecommendFilmList;

    private BroadcastReceiver mBroadcastReceiver;
    private ExecutorService mExecutorService;
    private WatermarkHelper mWatermarkHelper;
    private boolean mStopByNetworkInterrupted;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.base_player_frag, container, false);
        mWaterMarkFl = (ViewGroup) rootView.findViewById(R.id.player_water_mark_fl);
        mLoadingRecommendView = rootView.findViewById(R.id.player_busy_view);
        setContainerView((ViewGroup) rootView.findViewById(R.id.player_container));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        registerReceiver();
        mWatchDuration = System.currentTimeMillis();
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        Logger.d("onDestroy");
        super.onDestroy();

        removeDialogs();
        unregisterReceiver();

        mWatchDuration = 0;
        mVideoLoadDuration = 0;
        mVideoLoadBlockDuration = 0;
        mOnPressBacked = false;

        if (mPlayerBusyView != null && mPlayerBusyView instanceof PlayerBusyingView) {
            ((PlayerBusyingView) mPlayerBusyView).stopSpeedProgressText();
            mPlayerBusyView.destroyDrawingCache();
        }

        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }

        if (mWatermarkHelper != null) {
            // stop last water mark
            mWatermarkHelper.stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.d("onSaveInstanceState");
    }

    /**
     * Set content view by layout id
     *
     * @param layoutResID layout id
     */
    protected void setContentView(int layoutResID) {
        if (null == getView()) {
            return;
        }

        ViewGroup contentParent = (ViewGroup) getView().findViewById(R.id.content);
        if (contentParent != null) {
            contentParent.removeAllViews();
            LayoutInflater.from(getContext()).inflate(layoutResID, contentParent);
        }
    }

    /**
     * Get error description
     *
     * @param errCode error code
     * @return error description
     */
    protected abstract String getErrorDescription(int errCode, int extra);

    private void init() {
        // 控制界面
        NativeMediaController nativeMediaController = new NativeMediaController(getActivity(),
                this);

        // 设置控制界面可获取焦点
//        nativeMediaController.setFocusable(true);
//        nativeMediaController.setFocusableInTouchMode(true);

        // 设置控制界面的控件集合
        MediaControllerHolder defaultHolder = MediaControllerHolder.getDefaultHolder(
                getActivity());
        nativeMediaController.setControllerHolder(defaultHolder);

        // 设置播放器操作类
        nativeMediaController.setPlayerOperation(getPresenter().getPlayerOperation());

        // 设置控制栏容器
        nativeMediaController.setAnchorView(getViewContainer());

        setNativeMediaController(nativeMediaController);

        mPlayerBusyView = defaultHolder.getPlayerBusyView();

        if (getPresenter() != null) {
            getPresenter().loadRecommendFilmList(getFilmId());
        }
    }

    private void registerReceiver() {
        if (null == mBroadcastReceiver) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (StringUtils.isNullOrEmpty(action)) {
                        return;
                    }

                    switch (action) {
                        case ConnectivityManager.CONNECTIVITY_ACTION:
                            // online play
                            if (isOnlinePlay()) {
                                // network is available
                                if (NetworkUtils.isNetworkAvailable(getContext())) {
                                    Logger.d("onReceive, network is available");
                                    // last time is stop by network interrupted
                                    if (isStopByNetworkInterrupted()) {
                                        // start player
                                        startPlayer(false);
                                    }
                                } else {
                                    Logger.d("onReceive, network is not available");
                                    // mark stop by network interrupted
                                    setStopByNetworkInterrupted(true);
                                    // stop player
                                    stopPlayer();
                                }
                            }
                            break;
                        case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
//                            stopPlayer();
                            removeDialogs();
                            break;
                        default:
                            break;
                    }
                }
            };
        }
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getContext().registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private void unregisterReceiver() {
        if (mBroadcastReceiver != null) {
            getContext().unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.d("surfaceCreated");
        setSurfaceCreated(true);
        startPlayer(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.d("surfaceChanged, format : " + format + ", width : "
                + width + ", height : " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.d("surfaceDestroyed");
        setSurfaceCreated(false);
        stopPlayer();
    }

    @Override
    public void showPlayerBusy(boolean busy) {
        if (mPlayerBusyView != null) {
            mPlayerBusyView.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void updatePausePlayUI() {
        if (isActive()) {
            getNativeMediaController().updatePausePlay();
        }
    }

    @Override
    public void updatePlayerProgress() {
        getNativeMediaController().requestUpdateProgress();
    }

    @Override
    protected PlayerContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(PlayerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showPlayerCompleted() {
        Logger.d("onPlayerCompleted");
        finishActivity();
    }

    @Override
    public void showPlayerBuffering(final boolean hasProgress, final int progress,
            final String speed, final boolean isBufferedSupport) {
        Logger.d("showPlayerBuffering, hasProgress : " + hasProgress + ", progress : " + progress
                + ", speed : " + speed + ", isBufferedSupport :" + isBufferedSupport);
        if (isActive()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getNativeMediaController().showPlayerBuffering(hasProgress, progress, speed,
                            isBufferedSupport);
                }
            });
        }
    }

    @Override
    public void updateBufferingPercent(int percent) {
        if (isActive()) {
            getNativeMediaController().updateBufferingPercent(percent);
        }
    }

    @Override
    public void showPlayerError(int errCode, int extra, String errMsg) {
        String errDesc = getErrorDescription(errCode, extra);
        if (StringUtils.isNullOrEmpty(errMsg)) {
            errMsg = errDesc;
        } else {
            if (!StringUtils.isNullOrEmpty(errDesc)) {
                errMsg = errDesc + ", " + errMsg;
            }
        }
        Logger.e("showPlayerError! errCode : " + errCode + ", extra : " + extra + ", errMsg : "
                + errMsg);

        if (isActive()) {
            String txt = getString(R.string.play_failed) + ", " + getString(
                    R.string.play_error_code) + " : " + errCode + ", " + extra;
            // has error description
            if (!StringUtils.isNullOrEmpty(errMsg)) {
                txt += ", " + getString(R.string.play_error_desc) + " : " + errMsg;
            }

            ToastUtils.showToast(getContext(), txt);
        }
    }

    @Override
    public boolean isReadyToPlay() {
        return isSurfaceCreated();
    }

    protected void startPlayer(boolean foreStart) {
        // reset mark
        setStopByNetworkInterrupted(false);

        // start
        if (mPresenter != null && (foreStart || isReadyToPlay())) {
            mPresenter.start();
        }
    }

    protected void stopPlayer() {
        if (mPresenter != null) {
            mPresenter.stopPlayer();
        }
    }

    private NativeMediaController getNativeMediaController() {
        return mNativeMediaController;
    }

    private void setNativeMediaController(NativeMediaController nativeMediaController) {
        mNativeMediaController = nativeMediaController;
    }

    private ViewGroup getViewContainer() {
        return mViewContainer;
    }

    /**
     * 存放控制栏的容器
     */
    private void setContainerView(@NonNull ViewGroup viewContainer) {
        mViewContainer = checkNotNull(viewContainer, "viewContainer cannot be null!");
    }

    private boolean isSurfaceCreated() {
        return mSurfaceCreated;
    }

    private void setSurfaceCreated(boolean surfaceCreated) {
        mSurfaceCreated = surfaceCreated;
    }

    @Override
    public Observable<Boolean> waitForPlayer(final boolean isKdm, final int rank) {
        if (!isKdm) {
            return Observable.just(true);
        }

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                showPlayPreloadingView(true, rank, false, isKdm, subscriber);
            }
        });
    }

    @Override
    public void setPlayingIndicator(boolean active, int rank, boolean force) {
        Logger.d("setPlayingIndicator, active : " + active + ", rank : " + rank);
        if (!isActive() || !FragmentUtils.allowShowFragmentNow(getActivity())) {
            return;
        }

        showPlayPreloadingView(active, rank, force, false, null);
    }

    @Override
    public void setLoadingAdvertIndicator(boolean active) {
        showPlayerBusy(active);
    }

    @Override
    public void setLoadingRecommendIndicator(boolean active) {
        if (null == mLoadingRecommendView) {
            return;
        }
        UIHelper.setViewVisibleOrGone(mLoadingRecommendView, active);
        Drawable background = mLoadingRecommendView.getBackground();
        if (background != null && background instanceof AnimationDrawable) {
            AnimationDrawable speedLoadingViewDrawable = (AnimationDrawable) background;
            if (active && mOnPressBacked) {
                speedLoadingViewDrawable.start();
            } else {
                speedLoadingViewDrawable.stop();
            }
        }
    }

    @Override
    public void showRecommendMovieList(List<MovieRecommendFilm> recommendFilmList) {
        if (!isActive()) {
            return;
        }

        this.mRecommendFilmList = recommendFilmList;
        if (!mOnPressBacked) {
            return;
        }
        mOnPressBacked = false;

        final PlayerOperation playerOperation = getPresenter().getPlayerOperation();
//        final boolean isPlaying = playerOperation.isInPlaybackState()
//                && PlayerState.STATE_PAUSED != playerOperation.getPlayerState();
        final boolean isPlaying = playerOperation.isPlaying();
        // if playing
        if (isPlaying) {
            // make sure the player is pause
            playerOperation.pausePlayer();
        }

        final String fragmentTag = FRAG_TAG_RECOMMEND;
        Fragment eFragment = getFragmentManager().findFragmentByTag(fragmentTag);
        if (eFragment != null && eFragment instanceof RecommendAlertDialog) {
            RecommendAlertDialog eDialog = (RecommendAlertDialog) eFragment;
            if (eDialog.isResumed()) {
                return;
            }
            eDialog.dismiss();
            removeFragment(fragmentTag);
        }

        if (isAdded() && FragmentUtils.allowShowFragmentNow(getActivity())) {
            final RecommendAlertDialog mRecommendAlertDialog = FragmentUtils.newFragment(
                    RecommendAlertDialog.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(fragmentTag, (Serializable) recommendFilmList);
            bundle.putString(Constants.PLAYER_INTENT_NAME, getMediaName());
            mRecommendAlertDialog.setArguments(bundle);
            mRecommendAlertDialog.setDismissListener(
                    new RecommendAlertDialog.OnDismissListener() {

                        @Override
                        public void onDialogDismiss(boolean exit) {
//                            mRecommendAlertDialog.dismiss();
                            if (exit) {
                                finishActivity();
                            } else {
//                                // if player is paused
//                                if (PlayerState.STATE_PAUSED == playerOperation.getPlayerState
// ()) {
//                                    // resume it
//                                    playerOperation.resumePlayer();
//                                }

                                // resume player
                                getPresenter().startOrResumePlayer();
                                showMediaController();
                            }
                        }
                    });
//            getNativeMediaController().showPlayerBuffering(true, 100, null, false);
            getNativeMediaController().hidePlayerBufferingView();
            removeFragment(fragmentTag);
            mRecommendAlertDialog.show(getFragmentManager(), fragmentTag);
        }
    }

    @Override
    public void showPlayerDuration(long duration) {
        if (!isTrailer()) {
            UserInfoHelper.savePlayDuration(getContext(), getFilmId(), duration);
        }
    }

    @Override
    public void showPlayingCurrentPosition(final int position) {
        if (!isTrailer() && mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }

                    // this call cost a few time, so proceed it in non-UI thread.
                    UserInfoHelper.setUserPlayCurrentPosition(getContext(), getFilmId(), position);
                }
            });
        }
    }

    @Override
    public void showPlayerSourceChanged(boolean isSourceChange) {
    }

    @Override
    public void showPlayingRankChanged(int rank) {
        UserInfoHelper.setUserPlayMediaRank(getActivity(), getFilmId(), rank);
    }

    @Override
    public Observable<Boolean> showAdvert(final Ad ad, final long currentPlayPosition) {
        Logger.d("showAdvert, currentPlayPosition : " + currentPlayPosition);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                // no advert
                if (null == ad || StringUtils.isNullOrEmpty(ad.getType())) {
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                    return;
                }

                AdvertDialog fragment = null;
                String tag = null;
                Bundle bundle = new Bundle();
                if (Constants.ADVER_TYPE_IMAGE.equals(ad.getType())) { // advert-image
                    String timeType = ad.getTimeType();
                    // is paused-ad
                    boolean isImagePauseAdvert = !StringUtils.isNullOrEmpty(timeType)
                            && Constants.AD_TIME_TYPE_PAUSE.equals(timeType);
                    tag = isImagePauseAdvert ? FRAG_TAG_AD_IMAGE_PAUSE
                            : FRAG_TAG_AD_IMAGE;
                    removeAdvertDialog(tag);
                    if (null == fragment) {
                        fragment = FragmentUtils.newFragment(
                                isImagePauseAdvert ? AdvertImagePauseDialog.class
                                        : AdvertImageDialog.class);
                    }
                } else if (Constants.ADVER_TYPE_VIDEO.equals(ad.getType())) { // advert-video
                    tag = FRAG_TAG_AD_MEDIA;
                    removeAdvertDialog(tag);
                    if (null == fragment) {
                        fragment = FragmentUtils.newFragment(AdvertMediaDialog.class);
                    }
                    bundle.putLong(Constants.PLAYER_INTENT_PLAY_PROGRESS, currentPlayPosition);
                }

                if (fragment != null && tag != null) {
                    if (fragment.isAdded() && fragment.isResumed()) {
                        subscriber.onNext(false);
                        subscriber.onCompleted();
                        return;
                    }

                    bundle.putInt(Constants.PLAYER_INTENT_BOOT_ADVERT,
                            Constants.AD_REQUEST_TYPE_PLAYER);
                    bundle.putString(Constants.PLAYER_INTENT_FILM_ID, getFilmId());
                    bundle.putString(Constants.PLAYER_INTENT_NAME, getMediaName());
                    bundle.putSerializable(Constants.PLAYER_INTENT_MEDIA_ADVERT, ad);
                    fragment.setArguments(bundle);
                    fragment.setOnAdvertCallback(new AdvertDialog.AdvertCallback() {
                        @Override
                        public void onExit(boolean isExit, boolean isAdvertFinish) {
//                            Logger.d("showAdvert, onExit");
                            if (isExit) {
                                if (subscriber != null && !subscriber.isUnsubscribed()) {
                                    subscriber.onCompleted();
                                }
                                // exit
                                finishActivity();
                                return;
                            }

                            if (subscriber != null && !subscriber.isUnsubscribed()) {
                                subscriber.onNext(!isExit && isAdvertFinish);
                                subscriber.onCompleted();
                            }
                        }
                    });
                    if (isAdded() && FragmentUtils.allowShowFragmentNow(getActivity())) {
                        removeFragment(tag);
                        // show fragment
                        FragmentUtils.addFragmentAllowingStateLoss(getFragmentManager(), fragment,
                                tag);
                    }
                } else {
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public void hidePauseAdvert() {
        removeAdvertDialog(FRAG_TAG_AD_IMAGE_PAUSE);
    }

    @Override
    public void setWaterMarkIndicator(boolean active) {
        if (mWatermarkHelper != null) {
            if (active) {
                mWatermarkHelper.start();
            } else {
                mWatermarkHelper.stop();
            }
        }
    }

    @Override
    public void setWaterMark(String showText, int showTime, int intervalTime) {
        if (mWatermarkHelper != null) {
            // stop last water mark
            mWatermarkHelper.stop();
        }
        mWaterMarkFl.removeAllViews();
        if (View.VISIBLE != mWaterMarkFl.getVisibility()) {
            mWaterMarkFl.setVisibility(View.VISIBLE);
        }
        mWatermarkHelper = new WatermarkHelper(showText, showTime, intervalTime);
        mWatermarkHelper.initView(getContext(), mWaterMarkFl);
    }

    @Override
    public long getSavePlayPosition() {
        return UserInfoHelper.getUserPlayCurrentPosition(getContext(), getFilmId());
    }

    @Override
    public String getUserId() {
        return UserInfoHelper.getUserId(getContext());
    }

    @Override
    public String getMacAddress() {
        return DeviceUtil.getMacAddress(getContext());
    }

    @Override
    public boolean isRetrySupport() {
        // default retry is support
        return true;
    }

    @Override
    public void showRetryPlayView(int retryTimes) {
        Logger.d("showRetryPlayView, retryTimes : " + retryTimes);
        Spanned text = Html.fromHtml(
                String.format(getString(R.string.player_txt_retry_times), retryTimes));
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void reportVideoStartStatistics(String trailer, String watchType, String rank,
            String source, String serial, String free) {
        StatisticsHelper.getInstance(getContext())
                .reportVideoStart(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        serial,
                        free,
                        source);
    }

    @Override
    public void reportVideoLoadStatistics(String trailer, String watchType, String rank,
            String videoUrl, String mediaIp, String serial, String free) {
        String speed = "";
        if (mPlayerBusyView != null && mPlayerBusyView instanceof PlayerBusyingView) {
            speed = ((PlayerBusyingView) mPlayerBusyView).getLocalSpeed();
        }

        long currentTime = System.currentTimeMillis();
        int bufferDuration = (int) ((currentTime - mVideoLoadDuration) / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoLoad(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        videoUrl,
                        String.valueOf(bufferDuration),
                        rank,
                        mediaIp,
                        serial,
                        free,
                        speed);
        mVideoLoadDuration = 0;
    }

    @Override
    public void reportVideoStartBlockStatistics() {
        mVideoLoadBlockDuration = System.currentTimeMillis();
    }

    @Override
    public void reportVideoBlockStatistics(String trailer, String watchType, String rank,
            String mediaIp, long playDuration, long totalDuration, long playProgress,
            String serial, String free) {
        long currentTime = System.currentTimeMillis();
        int bufferDuration = (int) ((currentTime - mVideoLoadBlockDuration) / 1000);
        if (mVideoLoadBlockDuration <= 0) {
            return;
        }

        int reportWatchDuration = (int) ((currentTime - mWatchDuration) / 1000);
        int reportTotalDuration = (int) (totalDuration / 1000);
        int reportPlayProgress = (int) (playProgress / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoBlock(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        mediaIp,
                        String.valueOf(bufferDuration),
                        String.valueOf(reportWatchDuration),
                        String.valueOf(playDuration),
                        String.valueOf(reportTotalDuration),
                        String.valueOf(reportPlayProgress),
                        serial,
                        free);
        mVideoLoadBlockDuration = 0;
    }

    @Override
    public void reportVideoStreamChangeStatistics(String trailer, String watchType, String rank,
            String toRank, long playDuration, long totalDuration, long playProgress, String serial,
            String free) {

        // 用户观看时长
        int reportWatchDuration = (int) ((System.currentTimeMillis() - mWatchDuration) / 1000);
        int reportTotalDuration = (int) (totalDuration / 1000);
        int reportPlayProgress = (int) (playProgress / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoStreamSwitch(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        toRank,
                        String.valueOf(reportWatchDuration),
                        String.valueOf(playDuration),
                        String.valueOf(reportTotalDuration),
                        String.valueOf(reportPlayProgress),
                        serial,
                        free);
    }

    @Override
    public void reportVideoPlayPauseStatistics() {
        mPauseDuration = System.currentTimeMillis();
    }

    @Override
    public void reportVideoPlayPauseResumeStatistics(String trailer, String watchType, String rank,
            long playDuration, long totalDuration, long playProgress, String serial, String free) {
        if (0 == mPauseDuration) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        int reportWatchDuration = (int) ((currentTime - mWatchDuration) / 1000);
        int reportTotalDuration = (int) (totalDuration / 1000);
        int reportPlayProgress = (int) (playProgress / 1000);
        int resumeDuration = (int) ((currentTime - mPauseDuration) / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoPlayPause(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        String.valueOf(resumeDuration),
                        String.valueOf(reportWatchDuration),
                        String.valueOf(playDuration),
                        String.valueOf(reportTotalDuration),
                        String.valueOf(reportPlayProgress),
                        serial,
                        free);
        mPauseDuration = 0;
    }

    @Override
    public void reportVideoExceptionStatistics(String trailer, String watchType, String rank,
            String errCode, String errMsg, long playDuration, long totalDuration, long playProgress,
            String serial, String free) {
        int reportWatchDuration = (int) ((System.currentTimeMillis() - mWatchDuration) / 1000);
        int reportTotalDuration = (int) (totalDuration / 1000);
        int reportPlayProgress = (int) (playProgress / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoException(getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        errCode,
                        errMsg,
                        String.valueOf(reportWatchDuration),
                        String.valueOf(playDuration),
                        String.valueOf(reportTotalDuration),
                        String.valueOf(reportPlayProgress),
                        serial,
                        free);
    }

    @Override
    public void reportVideoExitStatistics(String trailer, String watchType, String rank,
            long playDuration, long totalDuration, long playProgress, String serial,
            String free) {
        int reportWatchDuration = (int) ((System.currentTimeMillis() - mWatchDuration) / 1000);
        int reportTotalDuration = (int) (totalDuration / 1000);
        int reportPlayProgress = (int) (playProgress / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoExit(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        String.valueOf(reportWatchDuration),
                        String.valueOf(playDuration),
                        String.valueOf(reportTotalDuration),
                        String.valueOf(reportPlayProgress),
                        serial,
                        free);
    }

    @Override
    public void reportVideoSeekStatistics(String trailer, String watchType, String rank,
            long playProgress, long seekToPosition, String toType, String serial, String free) {
        int reportPlayProgress = (int) (playProgress / 1000);
        int reportSeekToPosition = (int) (seekToPosition / 1000);
        StatisticsHelper.getInstance(getContext())
                .reportVideoSeek(
                        getFilmId(),
                        getMediaName(),
                        trailer,
                        watchType,
                        rank,
                        String.valueOf(reportPlayProgress),
                        String.valueOf(reportSeekToPosition),
                        toType,
                        serial,
                        free);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.d("keyCode:" + keyCode);
        if (KeyEvent.ACTION_DOWN == event.getAction() && 0 == event.getRepeatCount()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (getNativeMediaController().isShowing()) {
                        getNativeMediaController().hide();
                        return true;
                    } else {
                        if (!mOnPressBacked) {
                            mOnPressBacked = true;
                        }

                        if (getPresenter() != null && !StringUtils.isNullOrEmpty(getFilmId())) {
                            getNativeMediaController().onKeyBackRecommend();
//                            // is playing
//                            if (getPresenter().isPlaying())
                            {
                                // pause player
                                getPresenter().pausePlayer();
                            }

                            if (mRecommendFilmList != null && !mRecommendFilmList.isEmpty()) {
                                showRecommendMovieList(mRecommendFilmList);
                            } else {
                                getPresenter().loadRecommendFilmList(getFilmId());
                            }
                        } else {
                            finishActivity();
                        }
                    }
                    break;

                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    showMediaController();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    showMediaController();
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void showPlayPreloadingView(boolean active, int rank, boolean force, boolean isKdm,
            final Subscriber<? super Boolean> subscriber) {
        Logger.d("showPlayPreloadingView, active : " + active + ", force : " + force + ", kdm : "
                + isKdm);
        final String fragmentTag = FRAG_TAG_LOADING;
        Fragment fragment = getFragmentManager().findFragmentByTag(fragmentTag);

        if (active) {
            mVideoLoadDuration = System.currentTimeMillis();
            if (fragment != null) {
                if (fragment != null && fragment instanceof PlayPreloadingDialog) {
                    PlayPreloadingDialog playPreloadingDialog = (PlayPreloadingDialog) fragment;
//                    playPreloadingDialog.release();
                }
                removeFragment(fragmentTag);
                fragment = null;
            }

            if (null == fragment) {
                PlayPreloadingDialog playPreloadingDialog = FragmentUtils.newFragment(
                        PlayPreloadingDialog.class);
                Bundle bundle = new Bundle();
                String filmName = getArguments().getString(Constants.PLAYER_INTENT_NAME);
                String posterUrl = getArguments().getString(Constants.PLAYER_INTENT_FILM_ID_POSTER);
                bundle.putString(Constants.PLAYER_INTENT_FILM_ID, getFilmId());
                bundle.putString(Constants.PLAYER_INTENT_NAME, filmName);
                bundle.putString(Constants.PLAYER_INTENT_FILM_ID_POSTER, posterUrl);
                bundle.putBoolean(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER, isTrailer());
                bundle.putInt(Constants.PLAYER_INTENT_FILM_RANK, rank);
                bundle.putBoolean(Constants.PLAYER_INTENT_WAIT_FOR, isKdm);
                int[] colorBg = getArguments().getIntArray(
                        Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR);
                bundle.putIntArray(Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR, colorBg);
                playPreloadingDialog.setArguments(bundle);
                playPreloadingDialog.setPreloadingDismissCallback(
                        new PlayPreloadingDialog.PreloadingDismissCallback() {
                            @Override
                            public void onSelectRestart(long seekPosition, boolean restart) {
                                Logger.d("seekPosition:" + seekPosition + ", restart : " + restart);
                                if (subscriber != null && !subscriber.isUnsubscribed()) {
                                    subscriber.onNext(restart);
                                }
                                if (getNativeMediaController() != null) {
                                    int position = restart ? 0 : (int) seekPosition;
                                    getNativeMediaController().seekToPosition(position);
                                }
                            }
                        });
                playPreloadingDialog.setOnDialogDismissListener(
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (subscriber != null && !subscriber.isUnsubscribed()) {
                                    subscriber.onCompleted();
                                }
                                showMediaController();
//                                getPresenter().resumePlayer();
                            }
                        });
                playPreloadingDialog.setOnDialogCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finishActivity();
                            }
                        });

//                removePreviousFragment(getFragmentManager(), fragmentTag);
                try {
                    FragmentUtils.addFragmentAllowingStateLoss(getFragmentManager(),
                            playPreloadingDialog, fragmentTag);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.w(e, "showPlayPreloadingView, Exception : ");
                }
            }
        } else {
            if (fragment != null && fragment instanceof PlayPreloadingDialog) {
                PlayPreloadingDialog playPreloadingDialog = (PlayPreloadingDialog) fragment;
                // force hide
                if (force) {
//                    playPreloadingDialog.release();
                    removeFragment(fragmentTag);
                } else {
//                    if (getNativeMediaController() != null) {
//                        getNativeMediaController().doPauseResume(false);
//                    }
                    if (getPresenter().isPlaying()) {
                        getPresenter().pausePlayer();
                    }
                    playPreloadingDialog.delayCloseLoading();
                }
            }
            showPlayingRankChanged(rank);
        }
    }

    private void showMediaController() {
        if (isAdded() && getNativeMediaController() != null) {
            getNativeMediaController().show();
        }
    }

    private void removeDialogs() {
        Logger.d("removeDialogs");
        //remove RecommendAlertDialog
        removeFragment(FRAG_TAG_RECOMMEND);

        //remove AdvertMediaDialog
        removeFragment(FRAG_TAG_AD_MEDIA);

        //remove AdvertImageDialog
        removeFragment(FRAG_TAG_AD_IMAGE);

        //remove AdvertImagePauseDialog
        removeFragment(FRAG_TAG_AD_IMAGE_PAUSE);

        //remove PlayPreloadingDialog
        final String tag = FRAG_TAG_LOADING;
        Fragment pFragment = getFragmentManager().findFragmentByTag(tag);
        if (pFragment != null && pFragment instanceof PlayPreloadingDialog) {
            PlayPreloadingDialog playPreloadingDialog = (PlayPreloadingDialog) pFragment;
//            playPreloadingDialog.release();
        }
        removeFragment(tag);
    }

    /**
     * remove advert fragment
     */
    private void removeAdvertDialog(String tag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment != null && fragment instanceof AdvertDialog) {
            AdvertDialog dialog = (AdvertDialog) fragment;
            dialog.onFinish();
//            if (isAdded()) {
//                dialog.dismiss();
//            }
        }
        removeFragment(tag);
    }

    private void removeFragment(String fragTag) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            FragmentUtils.removePreviousFragment(getFragmentManager(), fragTag);
            FragmentUtils.removePreviousFragment(getChildFragmentManager(), fragTag);
        }
    }

    /**
     * finish the activity
     */
    private void finishActivity() {
        // presenter exit
        PlayerContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.exit();
        }

        // finish activity
        Activity activity = getActivity();
        if (activity != null && isAdded()) {
            activity.finish();
        }
    }

    private String getMediaName() {
        return mMediaName;
    }

    protected void setMediaName(String mediaName) {
        this.mMediaName = mediaName;
    }

    protected String getFilmId() {
        return mFilmId;
    }

    protected void setFilmId(String filmId) {
        this.mFilmId = filmId;
    }

    public boolean isOnlinePlay() {
        return mIsOnlinePlay;
    }

    public void setOnlinePlay(boolean onlinePlay) {
        mIsOnlinePlay = onlinePlay;
    }

    private boolean isTrailer() {
        return mIsTrailer;
    }

    protected void setTrailer(boolean trailer) {
        mIsTrailer = trailer;
    }

    private boolean isStopByNetworkInterrupted() {
        return mStopByNetworkInterrupted;
    }

    private void setStopByNetworkInterrupted(boolean stopByNetworkInterrupted) {
        mStopByNetworkInterrupted = stopByNetworkInterrupted;
    }
}
