package com.golive.cinema.player;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.advert.domain.usecase.AdvertUseCase;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.user.history.domain.usecase.AddHistoryUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.util.DateHelper;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Ad;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.Order;
import com.golive.network.entity.UserInfo;
import com.golive.network.response.AdvertResponse;
import com.initialjie.log.Logger;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Wangzj on 2016/9/12.
 */

public class PlayerPresenter extends BasePresenter<PlayerContract.View> implements
        PlayerContract.Presenter {

    private final static int LOAD_RECOMMEND_TIMEOUT = 4000;
    private static final int MAX_RETRY_TIMES = 3;

    @NonNull
    private final GetMainConfigUseCase mGetMainConfigUseCase;
    @NonNull
    private final AddHistoryUseCase addHistoryUseCase;
    @NonNull
    private final GetMovieRecommendUseCase mGetMovieRecommendUseCase;
    @NonNull
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;
    @NonNull
    private final GetValidOrderUseCase mGetValidOrderUseCase;
    @NonNull
    private final AdvertUseCase mAdvertUseCase;

    private PlayerOperation mPlayerOperation;
    private final String mFilmId;
    private final String mWatchType;
    private final boolean mIsTrailer;

    /**
     * mPlayDuration用户纯观影时长
     */
    private long mUserPlayDuration;
    private Observable<GetMovieRecommendUseCase.ResponseValue> mGetRecommendMovies;

    /**
     * 广告资源
     */
    private List<Ad> mAdvertList;
    private AdvertResponse mAdvertCache;
    private Observable<AdvertResponse> mGetAdvertsTask;
    private boolean mIsExit;
    private Subscription mStartSubscription;

    /** retry play times */
    private int mRetryTimes;

    public PlayerPresenter(@NonNull PlayerContract.View view,
            @NonNull GetMainConfigUseCase getMainConfigUseCase,
            @NonNull GetMovieRecommendUseCase movieRecommendUseCase,
            @NonNull GetUserInfoUseCase getUserInfoUseCase,
            @NonNull AddHistoryUseCase addHistoryTask,
            @NonNull GetValidOrderUseCase getValidOrderUseCase,
            @NonNull AdvertUseCase advertUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider, @NonNull String filmId,
            String watchType, boolean isTrailer) {
        mFilmId = checkNotNull(filmId);
        mGetMainConfigUseCase = checkNotNull(getMainConfigUseCase,
                "getMainConfigUseCase cannot be null!");
        addHistoryUseCase = checkNotNull(addHistoryTask, "AddHistoryUseCase cannot be null!");
        mGetUserInfoUseCase = checkNotNull(getUserInfoUseCase,
                "getUserInfoUseCase cannot be null!");
        mGetMovieRecommendUseCase = checkNotNull(movieRecommendUseCase,
                "GetMovieRecommendUseCase cannot be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider,
                "BaseSchedulerProvider cannot be null!");
        mGetValidOrderUseCase = checkNotNull(getValidOrderUseCase,
                "GetValidOrderUseCase cannot be null!");
        mAdvertUseCase = checkNotNull(advertUseCase,
                "AdvertUseCase cannot be null!");
        mWatchType = watchType;
        mIsTrailer = isTrailer;
        attachView(checkNotNull(view, "playerView cannot be null!"));
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        Logger.d("start");

        releaseLoadingResource();

        // play advert && get water mark
        mStartSubscription = getPlayAdBeginObs().zipWith(getWaterMarkObs(),
                new Func2<Object, GetMainConfigUseCase.ResponseValue, Object>() {
                    @Override
                    public Object call(Object o, GetMainConfigUseCase.ResponseValue responseValue) {
                        return null;
                    }
                })
                // take the first one
                .first()
                .onErrorReturn(new Func1<Throwable, Object>() {
                    @Override
                    public Object call(Throwable throwable) {
                        Logger.w(throwable, "start, onErrorReturn : ");
                        return null;
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("start, onCompleted, isUnsubscribed : " + isUnsubscribed());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.w(e, "start, onError : ");
                    }

                    @Override
                    public void onNext(Object responseValue) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        Logger.d("start, onNext, isUnsubscribed : " + isUnsubscribed());
                        if (isUnsubscribed()) {
                            return;
                        }

                        // start player
                        startPlayer();
                    }
                });
        addSubscription(mStartSubscription);
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();
        stopLoadingStatus();
        stopPlayer();
    }

    private Observable<?> getPlayAdBeginObs() {
        return Observable.just(mIsTrailer)
                .concatMap(new Func1<Boolean, Observable<?>>() {
                    @Override
                    public Observable<?> call(Boolean aBoolean) {
                        // is trailer
                        if (aBoolean != null && aBoolean) {
                            // just return
                            return Observable.just(aBoolean);
                        }

                        // show adverts of wanted type
                        return showAdvertsObs(Constants.AD_TIME_TYPE_BEGIN);
                    }
                });
    }

    /**
     * release loading resource
     */
    private void releaseLoadingResource() {
        if (mStartSubscription != null && !mStartSubscription.isUnsubscribed()) {
            Logger.d("releaseLoadingResource");
            mStartSubscription.unsubscribe();
        }
    }

    @Override
    public void startPlayer() {
//        Logger.d("startPlayer!");
        PlayerContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        boolean readyToPlay = view.isReadyToPlay();
        Logger.d("startPlayer, ready to play ? " + readyToPlay);
        if (!readyToPlay) {
            return;
        }

        // is kdm?
        final boolean kdmPlayer = isKdmPlayer();
        addSubscription(Observable.just(kdmPlayer)
                .concatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        int rank = 0;
                        if (mPlayerOperation != null) {
                            rank = mPlayerOperation.getRank();
                        }
                        // wait for player
                        return view.waitForPlayer(aBoolean, rank);
                    }
                })
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "startPlayer, onError : ");
                    }

                    @Override
                    public void onNext(Boolean restart) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        // not trailer
                        if (!mIsTrailer) {
                            // last play pos < 0
                            if (getLastPlayPosition() < 0) {
                                // get last save play position
                                setLastPlayPosition((int) view.getSavePlayPosition());
                            }
                            // default seek to last position
                            int seekToPos = getLastPlayPosition();
                            // is kdm && restart play
                            if (kdmPlayer && restart != null && restart) {
                                seekToPos = 0;
                            }

                            // seek to last position
                            seekTo(seekToPos);
                        }

                        // start player
                        _startPlayer();
                    }
                }));
    }

    private void _startPlayer() {
        if (mPlayerOperation != null) {
            mPlayerOperation.startPlayer();
        }
    }

    @Override
    public void stopPlayer() {
        Logger.d("stopPlayer!");
        releaseLoadingResource();
        _stopPlayer();
    }

    private void _stopPlayer() {
        PlayerContract.View view = getView();
        if (view != null && view.isActive()) {
            // retry is support
            if (view.isRetrySupport()) {
                // reset
                resetRetryTimes();
            }
        }

        if (mPlayerOperation != null) {
            mPlayerOperation.stopPlayer();
        }
    }

    @Override
    public void pausePlayer() {
        if (mPlayerOperation != null) {
            mPlayerOperation.pausePlayer();
        }
    }

    @Override
    public void resumePlayer() {
        if (mPlayerOperation != null) {
            mPlayerOperation.resumePlayer();
        }
    }

    /**
     * Auto start or resume player
     */
    @Override
    public void startOrResumePlayer() {
        // is exit
        if (isExit()) {
            return;
        }

        PlayerOperation playerOperation = getPlayerOperation();
        if (!playerOperation.isInPlaybackState()) { // is not in playback state
            // start player
            startPlayer();
        } else if (PlayerState.STATE_PAUSED == playerOperation.getPlayerState()) { // is paused
            // just resume player
            resumePlayer();
        }
    }

    @Override
    public void seekTo(long msec) {
        if (mPlayerOperation != null) {
            int pos = (int) msec;
            pos = Math.max(0, pos);
            Logger.d("seekTo, position : " + pos);
            mPlayerOperation.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mPlayerOperation != null) {
            return mPlayerOperation.isPlaying();
        }
        return false;
    }

    @Override
    public void exit() {
        setExit(true);
        stopPlayer();
    }

    private synchronized boolean isExit() {
        return mIsExit;
    }

    private synchronized void setExit(boolean exit) {
        mIsExit = exit;
    }

//    private boolean mIsTrailer {
//        if (mPlayerOperation != null) {
//            return mPlayerOperation.mIsTrailer;
//        }
//        return false;
//    }

    private boolean isKdmPlayer() {
        return mWatchType != null && (Constants.ONLINE_KDM.equals(mWatchType)
                || Constants.DOWNLOAD_KDM.equals(mWatchType));
    }

    @Override
    public PlayerOperation getPlayerOperation() {
        return mPlayerOperation;
    }

    @Override
    public void setPlayerOperation(@NonNull PlayerOperation playerOperation) {
        mPlayerOperation = checkNotNull(playerOperation, "playerOperation cannot be null!");

        // set the player callback
        mPlayerOperation.setPlayerCallback(new BasePlayerCallback() {

            @Override
            public void onPlayerPrepared() {
                super.onPlayerPrepared();
                PlayerContract.View view = getView();
                if (view != null && view.isActive()) {
                    view.showPlayerDuration(getPlayerOperation().getDuration());
                    startLoadingStatus();
                }
            }

            @Override
            public void onPlayerStart() {
                super.onPlayerStart();
                PlayerContract.View view = getView();
                if (view != null && view.isActive()) {
                    view.updatePlayerProgress();
                    startLoadingStatus();
                }
            }

            @Override
            public void onPlayerResumed() {
                super.onPlayerResumed();
                startLoadingStatus();
            }

            @Override
            public void onPlayerPaused() {
                super.onPlayerPaused();
                stopLoadingStatus();
            }

            @Override
            public void onPlayerStopping() {
                super.onPlayerStopping();
                stopLoadingStatus();
                videoExitStatistics();
            }

            @Override
            public void onPlayerStopped() {
                super.onPlayerStopped();
                stopLoadingStatus();
            }

            @Override
            public void onPlayerCompleted() {
                super.onPlayerCompleted();
                stopLoadingStatus();

                PlayerContract.View view = getView();
                if (null == view || !view.isActive()) {
                    return;
                }

                Observable.just(mIsTrailer)
                        // filter not trailer
                        .filter(new Func1<Boolean, Boolean>() {
                            @Override
                            public Boolean call(Boolean aBoolean) {
                                return aBoolean != null && !aBoolean;
                            }
                        })
                        .doOnNext(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                PlayerContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                // show current pos to 0
                                view.showPlayingCurrentPosition(0);
                            }
                        })
                        .concatMap(new Func1<Boolean, Observable<?>>() {
                            @Override
                            public Observable<?> call(Boolean aBoolean) {
                                PlayerContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return Observable.empty();
                                }

                                // show advert of end-type
                                return showAdvertsObs(Constants.AD_TIME_TYPE_END);
                            }
                        })
                        // catch all error
                        .onErrorReturn(new Func1<Throwable, Object>() {
                            @Override
                            public Object call(Throwable throwable) {
                                Logger.w(throwable, "onPlayerCompleted, onErrorReturn : ");
                                return null;
                            }
                        })
                        .subscribe(new Subscriber<Object>() {
                            @Override
                            public void onCompleted() {
                                PlayerContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

//                                // show current pos to 0
//                                view.showPlayingCurrentPosition(0);

                                // show player complete
                                view.showPlayerCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.e(e, "onPlayerCompleted, onError : ");
                            }

                            @Override
                            public void onNext(Object o) {
                            }
                        });

//                mUserPlayDuration = 0;
            }

            @Override
            public void onPlayerBuffering(boolean hasProgress, int progress, String speed) {
                super.onPlayerBuffering(hasProgress, progress, speed);
                PlayerContract.View view = getView();
                if (view != null && view.isActive()) {
                    view.showPlayerBuffering(hasProgress, progress, speed,
                            mPlayerOperation.isBufferedSupport());

                    boolean visible = !(hasProgress && progress >= 100);
                    if (!visible || progress >= 100) {
                        //hide
                        videoPlayBlockendStatistics();
                    } else {
                        //show
                        view.reportVideoStartBlockStatistics();
                    }
                }
            }

            @Override
            public void onBufferingUpdate(int percent) {
                super.onBufferingUpdate(percent);
                PlayerContract.View view = getView();
                if (view != null && view.isActive()) {
                    view.updateBufferingPercent(percent);
                }
            }

            @Override
            public void onPlayerError(int err, int extra, String errMsg) {
                super.onPlayerError(err, extra, errMsg);
                PlayerContract.View view = getView();
                if (view != null && view.isActive()) {
                    boolean shouldShowError = true;
                    boolean readyToPlay = view.isReadyToPlay();
                    Logger.d("onPlayerError, ready to play ? " + readyToPlay);

                    // is ready to play && retry is support
                    if (readyToPlay && view.isRetrySupport()) {
                        // add try time
                        increaseRetryTimes();
                        // less than max retry times
                        if (getRetryTimes() < MAX_RETRY_TIMES) {
                            shouldShowError = false;
                            view.showRetryPlayView(getRetryTimes() + 1);
                            // start player
                            startPlayer();
                        }
                    }

                    if (shouldShowError) {
                        view.showPlayerError(err, extra, errMsg);
                    }
                }

                stopLoadingStatus();
            }
        });
    }

    /**
     * Base PlayerCallback for update common UI.
     */
    class BasePlayerCallback implements PlayerCallback {
        @Override
        public void onPlayerPreparing() {
            Logger.d("onPlayerPreparing");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                // not kdm
                if (!isKdmPlayer()) {
                    view.setPlayingIndicator(true, mPlayerOperation.getRank(), false);
                }
                view.updatePausePlayUI();
            }
        }

        @Override
        public void onPlayerPrepared() {
            Logger.d("onPlayerPrepared");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                // retry is support
                if (view.isRetrySupport()) {
                    // reset
                    resetRetryTimes();
                }

                videoPlayLoadStatistics();
                view.setPlayingIndicator(false, mPlayerOperation.getRank(), false);
                view.updatePausePlayUI();

                // kdm
                if (isKdmPlayer()) {
                    view.setWaterMarkIndicator(true);
                }

                Logger.d("onPlayerPrepared, startPlayer");
                // start player
                _startPlayer();
            }
        }

        @Override
        public void onPlayerStart() {
            Logger.d("onPlayerStart");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
                videoStartStatistics();
            }
        }

        @Override
        public void onPlayerPaused() {
            Logger.d("onPlayerPaused");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
                if (!mIsTrailer) {
                    showPauseAdvert();
                }
                view.reportVideoPlayPauseStatistics();
            }
        }

        @Override
        public void onPlayerResumed() {
            Logger.d("onPlayerResumed");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
                if (!mIsTrailer) {
                    view.hidePauseAdvert();
                }
                videoPlayPauseResumeStatistics();
            }
        }

        @Override
        public void onPlayerSeekTo(int msec) {
            Logger.d("onPlayerSeekTo, msec : " + msec);
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
                videoPlaySeekStatistics(msec);
            }
        }

        @Override
        public void onPlayerStopping() {
            Logger.d("onPlayerStopping");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
                if (isKdmPlayer()) {
                    view.setWaterMarkIndicator(false);
                }
            }
        }

        @Override
        public void onPlayerStopped() {
            Logger.d("onPlayerStopped");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
//                view.setPlayingIndicator(false, mPlayerOperation.getRank(), false);
                view.updatePausePlayUI();
            }
        }

        @Override
        public void onPlayerCompleted() {
            Logger.d("onPlayerCompleted");
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
            }

            stopPlayer();
        }

        @Override
        public void onPlayerBuffering(boolean hasProgress, int progress, String speed) {
            Logger.d(
                    "onPlayerBuffering, hasProgress : " + hasProgress + ", progress : " + progress);
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.updatePausePlayUI();
            }
        }

        @Override
        public void onBufferingUpdate(int percent) {
//            Logger.d("onBufferingUpdate, percent : " + percent);
        }

        @Override
        public void onPlayerError(int err, int extra, String errMsg) {
            Logger.e(
                    "onPlayerError, err : " + err + ", extra : " + extra + ", errMsg : " + errMsg);
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.setPlayingIndicator(false, mPlayerOperation.getRank(), true);
                view.updatePausePlayUI();
                videoPlayExceptStatistics(String.valueOf(err), errMsg);
            }
        }

        @Override
        public void onPlayerBusyChange(boolean busy) {
            Logger.d("onPlayerBusyChange, busy : " + busy);
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.showPlayerBusy(busy);
            }
        }

        @Override
        public void onPlayerRankChanged(boolean isRankChange, int rank, int toRank) {
            Logger.d("onPlayerRankChanged, isRankChange : " + isRankChange + ", rank : " + rank
                    + ", toRank : " + toRank);
            PlayerContract.View view = getView();
            if (view != null && view.isActive()) {
                view.showPlayerSourceChanged(isRankChange);
                view.showPlayingRankChanged(toRank);
                videoSwitchStreamStatistics(rank, toRank);
            }
        }
    }

    private Observable<?> showAdvertsObs(final String timeType) {
        // get adverts
        return getAdvertsObs()
                .observeOn(mSchedulerProvider.io())
                .concatMap(new Func1<AdvertResponse, Observable<Ad>>() {
                    @Override
                    public Observable<Ad> call(AdvertResponse advertResponse) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        // current advert list is empty && has advert response
                        if ((null == getAdvertList() || getAdvertList().isEmpty())
                                && advertResponse != null && !advertResponse.isGolive) {
                            setAdvertList(null);
                            // sort ad list
                            List<AdvertResponse.AdsBean> adBeanLists = sortAdList(advertResponse);
                            // convert advert
                            List<Ad> adList = convertAdvertToAd(adBeanLists);
                            if (adList != null && !adList.isEmpty()) {
                                setAdvertList(adList);
                            }
                        }

                        List<Ad> advertList = getAdvertList();
                        // current advert list is empty
                        if (null == advertList || advertList.isEmpty()) {
                            return Observable.empty();
                        }

                        // for each advert
                        return Observable.from(advertList);
                    }
                })
                // filter not-played advert
                .filter(new Func1<Ad, Boolean>() {
                    @Override
                    public Boolean call(Ad ad) {
                        String adTimeType = ad.getTimeType();
                        String type = ad.getType();
                        // ad has not been played && is the corresponding time && type
                        return !ad.isPlay()
                                && !StringUtils.isNullOrEmpty(adTimeType)
                                && timeType.equals(adTimeType)
                                && !StringUtils.isNullOrEmpty(type)
                                && !Constants.ADVER_TYPE_COVER.equals(type);
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(new Action1<Ad>() {
                    @Override
                    public void call(Ad ad) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        String type = ad.getType();
                        if (!StringUtils.isNullOrEmpty(type)) {
                            if (Constants.ADVER_TYPE_IMAGE.equals(type)) { // ad-image
                                // just pause player
                                pausePlayer();
                            } else if (Constants.ADVER_TYPE_VIDEO.equals(type)) { // ad-video
                                // stop player
                                _stopPlayer();
                            }
                        }
                    }
                })
                .concatMap(new Func1<Ad, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final Ad ad) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        // show advert
                        return showAdvertViewObs(view, ad);
                    }
                })
                // wait for all
                .toList();
    }

    @Override
    public void loadRecommendFilmList(final String filmId) {
        getView().setLoadingRecommendIndicator(true);
        Subscription subscription = getRecommendMovies(filmId)
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Observer<GetMovieRecommendUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingRecommendIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadRecommendFilmList, onError : ");
                        PlayerContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingRecommendIndicator(false);
                            view.showRecommendMovieList(null);
                        }
                    }

                    @Override
                    public void onNext(GetMovieRecommendUseCase.ResponseValue responseValue) {
                        PlayerContract.View view = getView();
                        if (view != null && view.isActive()) {
                            List<MovieRecommendFilm> contentList = null;
                            if (responseValue != null) {
                                contentList = responseValue.getContentList();
                            }
                            view.showRecommendMovieList(contentList);
                        }
                    }
                });
        addSubscription(subscription);
    }

    public List<Ad> getAdvertList() {
        return mAdvertList;
    }

    @Override
    public void setAdvertList(List<Ad> advertList) {
        mAdvertList = advertList;
    }

    private Observable<GetMovieRecommendUseCase.ResponseValue> getRecommendMovies(
            final String filmId) {
        if (null == mGetRecommendMovies) {
            synchronized (this) {
                if (null == mGetRecommendMovies) {
                    mGetRecommendMovies = getUserInfo(false)
                            .flatMap(new Func1<UserInfo, Observable<GetMovieRecommendUseCase
                                    .ResponseValue>>() {
                                @Override
                                public Observable<GetMovieRecommendUseCase.ResponseValue>
                                call(UserInfo userInfo) {
                                    return mGetMovieRecommendUseCase.run(
                                            new GetMovieRecommendUseCase.RequestValues(filmId));
                                }
                            })
                            .timeout(LOAD_RECOMMEND_TIMEOUT, TimeUnit.MILLISECONDS)
                            .onErrorReturn(
                                    new Func1<Throwable, GetMovieRecommendUseCase.ResponseValue>() {
                                        @Override
                                        public GetMovieRecommendUseCase.ResponseValue call(
                                                Throwable throwable) {
                                            Logger.w(throwable,
                                                    "getRecommendMovies, onErrorReturn : ");
                                            return null;
                                        }
                                    })
                            // cache 1
                            .replay(1)
                            //
                            .refCount();
                }
            }
        }
        return mGetRecommendMovies;
    }

    @Override
    public void addToHistory(final String orderSerial) {
        if (StringUtils.isNullOrEmpty(orderSerial)) {
            return;
        }

        AddHistoryUseCase.RequestValues Task = new AddHistoryUseCase.RequestValues(orderSerial);
        Subscription subscription = addHistoryUseCase.run(Task)
                .subscribe(new Subscriber<AddHistoryUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "AddHistoryUseCase onError:");
                    }

                    @Override
                    public void onNext(AddHistoryUseCase.ResponseValue responseValue) {
//                        Order order =responseValue.getHistoryOrder();
                    }
                });
        addSubscription(subscription);
    }

    private Observable<UserInfo> getUserInfo(boolean forceUpdate) {
        return mGetUserInfoUseCase.run(new GetUserInfoUseCase.RequestValues(forceUpdate))
                .map(new Func1<GetUserInfoUseCase.ResponseValue, UserInfo>() {
                    @Override
                    public UserInfo call(GetUserInfoUseCase.ResponseValue responseValue) {
                        return responseValue.getUserInfo();
                    }
                });
    }

    /*************************************************************************************/
    private int mLastPlayPosition = -1;
    private int mDuration;
    private boolean mIsBlocked = false;
    private static final int DELAY = 0;
    private static final int PERIOD = 1;
    private Subscription mTimerSubscription;

    private synchronized void stopLoadingStatus() {
        Logger.d("stopLoadingStatus");
        // reset
//        mLastPlayPosition = 0;
        mDuration = 0;
        mIsBlocked = false;

        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
            mTimerSubscription = null;
        }
    }

    private synchronized void startLoadingStatus() {
        Logger.d("startLoadingStatus");
        PlayerContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

//        if (mIsTrailer) {
//            return;
//        }

        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
            mTimerSubscription = null;
        }

        if (null == mTimerSubscription) {
            mTimerSubscription = Observable.interval(DELAY, PERIOD, TimeUnit.SECONDS)
                    .subscribeOn(mSchedulerProvider.io())
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.e(e, "startLoadingStatus, onError : ");
                        }

                        @Override
                        public void onNext(Long aLong) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return;
                            }

                            if (null == mPlayerOperation || !mPlayerOperation.isInPlaybackState()) {
                                return;
                            }

                            mDuration = mPlayerOperation.getDuration();
                            int currentPos = mPlayerOperation.getCurrentPosition();
                            // current pos != last pos
                            if (getLastPlayPosition() != currentPos && currentPos <= mDuration) {
                                if (mUserPlayDuration >= Long.MAX_VALUE) {
                                    mUserPlayDuration = 0;
                                }

                                // add user play time
                                ++mUserPlayDuration;

                                // save play position
                                view.showPlayingCurrentPosition(currentPos);

                                // not trailer
                                if (!mIsTrailer) {
                                    // check whether need to show advert based on current position
                                    showAdvertsOnPosition(currentPos);
                                }
                            }

                            // buffer detect not support
                            if (!mPlayerOperation.isBufferedSupport()) {
                                // current position not change && is playing
                                if (getLastPlayPosition() == currentPos
                                        && mPlayerOperation.isPlaying()) {
                                    mIsBlocked = true;
                                    // show buffer view
                                    view.showPlayerBuffering(true, 0, null, false);
                                } else {
                                    // last state is block
                                    if (mIsBlocked) {
                                        mIsBlocked = false;
                                        // hide buffer view
                                        view.showPlayerBuffering(true, 100, null, false);
                                    }
                                }
                            }

                            setLastPlayPosition(currentPos);
                        }
                    });
            addSubscription(mTimerSubscription);
        }
    }

    /**
     * check whether need to show advert based on current position
     */
    private void showAdvertsOnPosition(final int position) {
        List<Ad> advertList = getAdvertList();
        if (null == advertList || advertList.isEmpty()) {
            return;
        }

        // for each advert
        Subscription subscription = Observable.from(advertList)
                .filter(new Func1<Ad, Boolean>() {
                    @Override
                    public Boolean call(Ad ad) {
                        // empty play duration || empty type
                        if (null == ad || StringUtils.isNullOrEmpty(ad.getPlayDuration())
                                || StringUtils.isNullOrEmpty(ad.getType())) {
                            return false;
                        }

                        try {
                            // get advert show time
                            long seconds = DateHelper.convertDateToSecond(
                                    ad.getPlayDuration().trim());
                            String timeType = ad.getTimeType();
                            // current position && advert has not been played && advert-middle
                            return position / 1000 == seconds
                                    && !ad.isPlay()
                                    && !StringUtils.isNullOrEmpty(timeType)
                                    && Constants.AD_TIME_TYPE_MIDDLE.equals(timeType);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        return false;
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(new Action1<Ad>() {
                    @Override
                    public void call(Ad ad) {
                        if (isPlaying() && ad != null
                                && !StringUtils.isNullOrEmpty(ad.getType())) {
                            String type = ad.getType();
                            if (Constants.ADVER_TYPE_IMAGE.equals(type)) {  // advert-image
                                // pause player
                                pausePlayer();
                            } else if (Constants.ADVER_TYPE_VIDEO.equals(type)) {  // advert-video
                                // stop player
                                stopPlayer();
                            }
                        }
                    }
                })
                .concatMap(new Func1<Ad, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Ad ad) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        view.showPlayerSourceChanged(true);
                        // show advert
                        return showAdvertViewObs(view, ad);
                    }
                })
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        // start or resume player
                        startOrResumePlayer();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "showAdvertsOnPosition, onError : ");
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
//                        Logger.d("showAdvertsOnPosition, onNext aBoolean: " + aBoolean);
//                        if (aBoolean != null && aBoolean) {
//                            checkPlayingAdverts(position);
//                        }
                    }
                });
        addSubscription(subscription);
    }

    private Observable<Boolean> showAdvertViewObs(PlayerContract.View view, final Ad ad) {
        long currentPosition = getPlayerOperation().getCurrentPosition();
        return view.showAdvert(ad, currentPosition)
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean complete) {
                        // play this advert completely
                        if (complete != null && complete) {
                            // mark this advert has been played
                            ad.setPlay(true);
                        }
                    }
                });
    }

    /**
     * show advert of pause type
     */
    private void showPauseAdvert() {
        Logger.d("showPauseAdvert");
        PlayerContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

//        if (isPlaying()) {
//            view.showPlayAdvertPaused();
//            return;
//        }

        if (mPlayerOperation != null && mPlayerOperation.isPauseClick()) {
            mPlayerOperation.setPauseClick(false);
            List<Ad> mAdvertList = getAdvertList();
            if (null == mAdvertList || mAdvertList.isEmpty()) {
                return;
            }

            addSubscription(Observable.just(mAdvertList)
                    .concatMap(new Func1<List<Ad>, Observable<Ad>>() {
                        @Override
                        public Observable<Ad> call(List<Ad> adList) {
                            for (Ad ad : adList) {
                                String timeType = ad.getTimeType();
                                String type = ad.getType();
                                if (!StringUtils.isNullOrEmpty(timeType)
                                        && Constants.AD_TIME_TYPE_PAUSE.equals(timeType)
                                        && !StringUtils.isNullOrEmpty(type)
                                        && Constants.ADVER_TYPE_IMAGE.equals(type)
                                        && !StringUtils.isNullOrEmpty(ad.getUrl())) {
                                    // return it
                                    return Observable.just(ad);
                                }
                            }

                            return Observable.empty();
                        }
                    })
//                    .filter(new Func1<Ad, Boolean>() {
//                        @Override
//                        public Boolean call(Ad ad) {
//                            String timeType = ad.getTimeType();
//                            String type = ad.getType();
//                            // advert of type(pause or image)
//                            return !StringUtils.isNullOrEmpty(timeType)
//                                    && Constants.AD_TIME_TYPE_PAUSE.equals(timeType)
//                                    && !StringUtils.isNullOrEmpty(type)
//                                    && Constants.ADVER_TYPE_IMAGE.equals(type)
//                                    && !StringUtils.isNullOrEmpty(ad.getUrl());
//                        }
//                    })
//                    // take first one
//                    .first()
                    .observeOn(mSchedulerProvider.ui())
                    .concatMap(new Func1<Ad, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Ad ad) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive() || null == ad) {
                                return Observable.empty();
                            }

                            int playerState = getPlayerOperation().getPlayerState();
                            // if not paused
                            if (PlayerState.STATE_PAUSED != playerState) {
                                // exit
                                return Observable.empty();
                            }

                            // show advert
                            return showAdvertViewObs(view, ad);
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, Boolean>() {
                        @Override
                        public Boolean call(Throwable throwable) {
                            Logger.w(throwable, "showPauseAdvert, onErrorReturn : ");
                            return null;
                        }
                    })
                    .subscribeOn(mSchedulerProvider.io())
//                    // for sync operation
//                    .subscribeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
//                            Logger.d("onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.e(e, "showPauseAdvert, onError : ");
                        }

                        @Override
                        public void onNext(Boolean ok) {
//                            Logger.d("onNext, ok : " + ok);
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive() || null == ok) {
                                return;
                            }

                            // if player is paused
                            PlayerOperation playerOperation = getPlayerOperation();
                            if (PlayerState.STATE_PAUSED == playerOperation.getPlayerState()) {
                                // resume player
                                startOrResumePlayer();
                            }
                        }
                    }));
        }
    }

    /**
     * Get valid order if not trailer, or return a Observable which just emits single
     * <code>null<code/> item.
     */
    private Observable<Order> getValidOrderOrNullObs() {
        if (null == mPlayerOperation || StringUtils.isNullOrEmpty(mPlayerOperation.getFilmId())) {
            return Observable.empty();
        }

        if (mIsTrailer) {
            return Observable.just(null);
        }

        String filmId = mPlayerOperation.getFilmId();
        return getValidOrdersObs(filmId)
                .filter(new Func1<List<Order>, Boolean>() {
                    @Override
                    public Boolean call(List<Order> orderList) {
                        return orderList != null && !orderList.isEmpty();
                    }
                })
                .map(new Func1<List<Order>, Order>() {
                    @Override
                    public Order call(List<Order> orderList) {
                        for (Order order : orderList) {
                            if (order.isValid()) {
                                return order;
                            }
                        }
                        return null;
                    }
                })
                .observeOn(mSchedulerProvider.ui());
    }

    private Observable<List<Order>> getValidOrdersObs(final String filmId) {
        GetValidOrderUseCase.RequestValues requestValues = new GetValidOrderUseCase.RequestValues(
                filmId, Order.PRODUCT_TYPE_THEATRE);
        return mGetValidOrderUseCase.run(requestValues)
                .map(new Func1<GetValidOrderUseCase.ResponseValue, List<Order>>() {
                    @Override
                    public List<Order> call(GetValidOrderUseCase.ResponseValue responseValue) {
                        return responseValue.getOrders();
                    }
                });
    }

    private String getOrderFreed(String price) {
        if (StringUtils.isNullOrEmpty(price)) {
            return "0";
        }

        String free;
        try {
            BigDecimal filmPrice = new BigDecimal(price);
            if (0 == filmPrice.compareTo(BigDecimal.ZERO)) {
                //免费
                free = "0";
            } else {
                free = "1";
            }
        } catch (Exception e) {
            e.printStackTrace();
            free = "0";
        }
        return free;
    }

    private Observable<AdvertResponse> getAdvertsObs() {
        if (null == mPlayerOperation) {
            return Observable.empty();
        }

        if (mAdvertCache != null) {
            return Observable.just(mAdvertCache);
        }

        if (null == mGetAdvertsTask) {
            synchronized (this) {
                if (null == mGetAdvertsTask) {
                    // get valid orders
                    mGetAdvertsTask = getValidOrderOrNullObs()
                            .observeOn(mSchedulerProvider.io())
                            .concatMap(new Func1<Order, Observable<AdvertUseCase.ResponseValue>>() {
                                @Override
                                public Observable<AdvertUseCase.ResponseValue> call(Order order) {
                                    String free = "0";
                                    if (order != null && !StringUtils.isNullOrEmpty(
                                            order.getPrice())) {
                                        String getFree = getOrderFreed(order.getPrice());
                                        if ("0".equals(getFree)) {
                                            free = "1";
                                        } else {
                                            free = "2";
                                        }
                                    }

                                    // get advert
                                    String filmId = mPlayerOperation.getFilmId();
                                    String mediaName = mPlayerOperation.getMediaName();
                                    return mAdvertUseCase.run(new AdvertUseCase.RequestValues(
                                            Constants.AD_REQUEST_TYPE_PLAYER, filmId, mediaName,
                                            free));
                                }
                            })
                            .map(new Func1<AdvertUseCase.ResponseValue, AdvertResponse>() {
                                @Override
                                public AdvertResponse call(
                                        AdvertUseCase.ResponseValue responseValue) {
                                    return responseValue.getAdvertResponse();
                                }
                            })
                            .doOnNext(new Action1<AdvertResponse>() {
                                @Override
                                public void call(AdvertResponse advertResponse) {
                                    PlayerContract.View view = getView();
                                    if (null == view || !view.isActive()) {
                                        return;
                                    }

                                    if (advertResponse != null) {
                                        mAdvertCache = advertResponse;
                                    }
                                }
                            })
                            .replay(1)
                            .refCount();
                }
            }
        }

        return mGetAdvertsTask;
    }

    /**
     * get sorted advert list
     */
    private List<AdvertResponse.AdsBean> sortAdList(AdvertResponse advert) {
        List<AdvertResponse.AdsBean> ads = new ArrayList<>();
        if (null != advert) {
            List<AdvertResponse.AdsBean> adList = advert.getAds();
            if (null != adList) {
                ads.addAll(adList);

                Collections.sort(ads, new Comparator<AdvertResponse.AdsBean>() {
                    public int compare(AdvertResponse.AdsBean ad1, AdvertResponse.AdsBean ad2) {
                        List<Integer> start_timestamp_1 = ad1.getStart_timestamp();
                        List<Integer> start_timestamp_2 = ad2.getStart_timestamp();
                        if (start_timestamp_1 != null && !start_timestamp_1.isEmpty()
                                && start_timestamp_2 != null && !start_timestamp_2.isEmpty()) {
                            Integer one = start_timestamp_1.get(0);
                            Integer two = start_timestamp_2.get(0);
                            return one.compareTo(two);
                        }
                        return 0;
                    }
                });
            }

        }
        Logger.d("sortAdList= " + ads.size());
        return ads;
    }

    /**
     * convert advert
     */
    private List<Ad> convertAdvertToAd(List<AdvertResponse.AdsBean> adBeanList) {
        if (null == adBeanList) {
            return null;
        }
        List<Ad> adList = new ArrayList<>();
        for (AdvertResponse.AdsBean adBean : adBeanList) {
            List<Integer> start_timestamp = adBean.getStart_timestamp();
            List<AdvertResponse.MaterialsBean> materials = adBean.getMaterials();
            if (null == start_timestamp || start_timestamp.isEmpty() || null == materials
                    || materials.isEmpty()) {
                continue;
            }

            AdvertResponse.MaterialsBean materialsBean = materials.get(0);
            String material_code = materialsBean.getMaterial_code();
            Ad ad = new Ad();
            ad.setId(material_code);
            ad.setAdTitle(material_code);
            ad.setDuration(String.valueOf(adBean.getShow_time()));
            List<String> show_url = materialsBean.getShow_url();
            if (null != show_url && !show_url.isEmpty()) {
                ad.setReportUrl(show_url.get(0));
            }
            int res_type = materialsBean.getRes_type();
            if (Constants.MATERIAL_TYPE_IMAGE_DIGTAL_MEDIA == res_type) {
                AdvertResponse.ImageBean image = materialsBean.getImage();
                if (null != image && (null == image.getType() || !"gif".equals(image.getType()))) {
                    ad.setUrl(image.getUrl());
                    ad.setType(Constants.ADVER_TYPE_IMAGE);
                    int showTime = adBean.getShow_time();
                    if (0 == showTime) {
                        ad.setDuration("0");
                    } else {
                        ad.setDuration(String.valueOf(showTime));
                    }
                }
            } else if (Constants.MATERIAL_TYPE_VIDEO_DIGTAL_MEDIA == res_type) {
                ad.setUrl(materialsBean.getVideo());
                ad.setType(Constants.ADVER_TYPE_VIDEO);
                int showTime = adBean.getShow_time();
                if (0 == showTime) {
                    ad.setDuration("0");
                } else {
                    ad.setDuration(String.valueOf(showTime));
                }
            }

            int timeStamp = start_timestamp.get(0);
            if (0 == timeStamp) {
                ad.setTimeType(Constants.AD_TIME_TYPE_BEGIN);
            } else if (86400 == timeStamp) {
                ad.setTimeType(Constants.AD_TIME_TYPE_END);
            } else if (100001 == timeStamp) {
                ad.setTimeType(Constants.AD_TIME_TYPE_PAUSE);
            } else {
                ad.setTimeType(Constants.AD_TIME_TYPE_MIDDLE);
                String playDuration = DateHelper.formatGMTTime(timeStamp);
                ad.setPlayDuration(playDuration);
            }
            ad.setThirdAdvert(true);
            ad.setAdCode(adBean.getAd_code());
            ad.setMaterialCode(material_code);
            adList.add(ad);
        }
        return adList;
    }

    /**
     * get water mark
     */
    private Observable<GetMainConfigUseCase.ResponseValue> getWaterMarkObs() {
        return mGetMainConfigUseCase.run(new GetMainConfigUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetMainConfigUseCase.ResponseValue>() {
                    @Override
                    public GetMainConfigUseCase.ResponseValue call(Throwable throwable) {
                        Logger.e(throwable, "getWaterMarkObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetMainConfigUseCase.ResponseValue>() {
                    @Override
                    public void call(GetMainConfigUseCase.ResponseValue response) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive() || null == response
                                || null == response.getMainConfig()) {
                            return;
                        }

                        MainConfig mainConfig = response.getMainConfig();
                        String showTime = mainConfig.getWaterMarkShowTime();
                        String intervalTime = mainConfig.getWaterMarkSpaceTime();
                        if (!StringUtils.isNullOrEmpty(showTime) && !StringUtils.isNullOrEmpty(
                                intervalTime)) {
                            try {
                                String userId = view.getUserId();
                                String mac = view.getMacAddress();
                                view.setWaterMark(userId + " " + mac, Integer.parseInt(showTime),
                                        Integer.parseInt(intervalTime));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    /**
     * 开始播放
     */
    private void videoStartStatistics() {
        if (null == mPlayerOperation) {
            return;
        }

        final String quality = String.valueOf(mPlayerOperation.getRank());
        Subscription subscription = getValidOrderOrNullObs()
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Order>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.w(e, "videoStartStatistics, onError : ");
                    }

                    @Override
                    public void onNext(Order order) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                        String source = "0";
                        String orderSerial = "";
                        String isFree = "0";
                        if (order != null) {
                            orderSerial = order.getSerial();
                            isFree = getOrderFreed(order.getPrice());
                        }
                        view.reportVideoStartStatistics(type, mWatchType, quality, source,
                                orderSerial, isFree);
                    }
                });
        addSubscription(subscription);
    }

    /**
     * 开始播放缓冲结束
     */
    private void videoPlayLoadStatistics() {
        if (null == mPlayerOperation) {
            return;
        }

        final String quality = String.valueOf(mPlayerOperation.getRank());
        final String playUrl = mPlayerOperation.getPlayUrl();
        Subscription subscription = Observable.zip(getValidOrderOrNullObs(), getHostIpObs(playUrl),
                new Func2<Order, String, Pair<Order, String>>() {
                    @Override
                    public Pair<Order, String> call(Order order, String s) {
                        return new Pair<>(order, s);
                    }
                })
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Pair<Order, String>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.w(e, "videoPlayLoadStatistics, onError : ");
                    }

                    @Override
                    public void onNext(Pair<Order, String> pair) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                        String orderSerial = "";
                        String isFree = "0";
                        String ip = "";
                        if (pair != null) {
                            Order order = pair.first;
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }
                            if (pair.second != null) {
                                ip = StringUtils.getDefaultStringIfEmpty(pair.second);
                            }
                        }

                        view.reportVideoLoadStatistics(type, mWatchType, quality, playUrl, ip,
                                orderSerial, isFree);
                    }
                });
        addSubscription(subscription);
    }

    /**
     * 播放缓冲结束
     */
    private void videoPlayBlockendStatistics() {
        if (null == mPlayerOperation) {
            return;
        }

        final long totalDuration = mPlayerOperation.getDuration();
        final long playProgress = mPlayerOperation.getCurrentPosition();
        final String quality = String.valueOf(mPlayerOperation.getRank());
        final String playUrl = mPlayerOperation.getPlayUrl();
        final long userPlayDuration = mUserPlayDuration;
        Subscription subscription = Observable.zip(getValidOrderOrNullObs(), getHostIpObs(playUrl),
                new Func2<Order, String, Pair<Order, String>>() {
                    @Override
                    public Pair<Order, String> call(Order order, String s) {
                        return new Pair<>(order, s);
                    }
                })
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Pair<Order, String>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.w(e, "videoPlayBlockendStatistics, onError : ");
                    }

                    @Override
                    public void onNext(Pair<Order, String> pair) {
                        PlayerContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                        String orderSerial = "";
                        String isFree = "0";
                        String ip = "";
                        if (pair != null) {
                            Order order = pair.first;
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }
                            if (pair.second != null) {
                                ip = StringUtils.getDefaultStringIfEmpty(pair.second);
                            }
                        }

                        view.reportVideoBlockStatistics(type, mWatchType, quality, ip,
                                userPlayDuration, totalDuration, playProgress, orderSerial, isFree);
                    }
                });
        addSubscription(subscription);
    }

    /**
     * 切换码流
     */
    private void videoSwitchStreamStatistics(final int rank, final int toRank) {
        if (mPlayerOperation != null) {
            final long totalDuration = mPlayerOperation.getDuration();
            final long playProgress = mPlayerOperation.getCurrentPosition();
            final long userPlayDuration = mUserPlayDuration;
            Subscription subscription = getValidOrderOrNullObs()
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Order>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.w(e, "videoSwitchStreamStatistics, onError : ");
                        }

                        @Override
                        public void onNext(Order order) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return;
                            }

                            String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                            String orderSerial = "";
                            String isFree = "0";
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }

                            view.reportVideoStreamChangeStatistics(type, mWatchType,
                                    String.valueOf(rank), String.valueOf(toRank), userPlayDuration,
                                    totalDuration, playProgress, orderSerial, isFree);
                        }
                    });
            addSubscription(subscription);
        }

    }

    /**
     * 播放暂停
     */
    private void videoPlayPauseResumeStatistics() {
        if (mPlayerOperation != null) {
            final long totalDuration = mPlayerOperation.getDuration();
            final long playProgress = mPlayerOperation.getCurrentPosition();
            final String quality = String.valueOf(mPlayerOperation.getRank());
            final long userPlayDuration = mUserPlayDuration;
            Subscription subscription = getValidOrderOrNullObs()
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Order>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.w(e, "videoPlayPauseResumeStatistics, onError : ");
                        }

                        @Override
                        public void onNext(Order order) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return;
                            }

                            String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                            String orderSerial = "";
                            String isFree = "0";
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }

                            view.reportVideoPlayPauseResumeStatistics(type, mWatchType, quality,
                                    userPlayDuration, totalDuration, playProgress, orderSerial,
                                    isFree);
                        }
                    });
            addSubscription(subscription);
        }
    }

    /**
     * 播放快进快退
     */
    private void videoPlaySeekStatistics(final int msec) {
        if (mPlayerOperation != null) {
            final String quality = String.valueOf(mPlayerOperation.getRank());
            final long playProgress = mPlayerOperation.getSeekDuration();
            final String toType = mPlayerOperation.getToType();
            Subscription subscription = getValidOrderOrNullObs()
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Order>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.w(e, "videoPlaySeekStatistics, onError : ");
                        }

                        @Override
                        public void onNext(Order order) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return;
                            }

                            String orderSerial = "";
                            String isFree = "0";
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }
                            String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                            view.reportVideoSeekStatistics(type, mWatchType, quality, playProgress,
                                    msec, toType, orderSerial, isFree);
                        }
                    });
            addSubscription(subscription);
        }
    }

    /**
     * 播放异常
     */
    private void videoPlayExceptStatistics(final String errCode, final String errMsg) {
        if (mPlayerOperation != null) {
            final long totalDuration = mPlayerOperation.getDuration();
            final long playProgress = mPlayerOperation.getCurrentPosition();
            final String quality = String.valueOf(mPlayerOperation.getRank());
            final long userPlayDuration = mUserPlayDuration;
            Subscription subscription = getValidOrderOrNullObs()
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Order>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.w(e, "videoPlayExceptStatistics, onError : ");
                        }

                        @Override
                        public void onNext(Order order) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return;
                            }

                            String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                            String orderSerial = "";
                            String isFree = "0";
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }
                            view.reportVideoExceptionStatistics(type, mWatchType, quality, errCode,
                                    errMsg, userPlayDuration, totalDuration, playProgress,
                                    orderSerial, isFree);
                        }
                    });
            addSubscription(subscription);
        }
    }

    /**
     * 播放退出
     */
    private void videoExitStatistics() {
        if (mPlayerOperation != null) {
            final long totalDuration = mPlayerOperation.getDuration();
            final long playProgress = mPlayerOperation.getCurrentPosition();
            final String quality = String.valueOf(mPlayerOperation.getRank());
            final long userPlayDuration = mUserPlayDuration;
            Subscription subscription = getValidOrderOrNullObs()
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Order>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.w(e, "videoExitStatistics, onError : ");
                        }

                        @Override
                        public void onNext(Order order) {
                            PlayerContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return;
                            }

                            String type = mIsTrailer ? Constants.TRAILER : Constants.POSITIVE;
                            String orderSerial = "";
                            String isFree = "0";
                            if (order != null) {
                                orderSerial = order.getSerial();
                                isFree = getOrderFreed(order.getPrice());
                            }
                            view.reportVideoExitStatistics(type, mWatchType, quality,
                                    userPlayDuration, totalDuration, playProgress, orderSerial,
                                    getOrderFreed(isFree));
                        }
                    });
            addSubscription(subscription);
        }
    }

    private Observable<String> getHostIpObs(String url) {
        if (mPlayerOperation == null) {
            return Observable.empty();
        }

        return Observable.just(url)
                .concatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        if (mPlayerOperation.isMediaIpSupport()) {
                            return Observable.just(mPlayerOperation.getMediaIp());
                        }

                        String mediaIp = "";
                        try {
                            URL url = new URL(s);
                            String domainName = url.getHost();
                            InetAddress domainName_ip = InetAddress.getByName(domainName);
                            mediaIp = domainName_ip.getHostAddress();// 得到字符串形式的ip地址
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Observable.just(mediaIp);
                    }
                })
                .onErrorReturn(new Func1<Throwable, String>() {
                    @Override
                    public String call(Throwable throwable) {
                        Logger.w(throwable, "getHostIpObs, onErrorReturn : ");
                        return null;
                    }
                });
    }

    private int getLastPlayPosition() {
        return mLastPlayPosition;
    }

    private void setLastPlayPosition(int lastPosition) {
        mLastPlayPosition = lastPosition;
    }

    private int getRetryTimes() {
        return mRetryTimes;
    }

    private void setRetryTimes(int retryTimes) {
        mRetryTimes = retryTimes;
    }

    /**
     * 重试次数递增
     */
    private void increaseRetryTimes() {
        mRetryTimes++;
    }

    /**
     * 重试次数复位
     */
    private void resetRetryTimes() {
        setRetryTimes(0);
    }
}
