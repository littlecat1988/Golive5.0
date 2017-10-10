package com.golive.cinema.init;

import static com.golive.cinema.util.Preconditions.checkNotNull;
import static com.golive.network.response.ExitGuideResponse.FILM_RECOMMENDED_DRAINAGE;
import static com.golive.network.response.ExitGuideResponse.PIN_MONEY_DRAINAGE;
import static com.golive.network.response.ExitGuideResponse.VIP_PACKAGE_RECOMMENDATION;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.init.domain.usecase.ActivityImageUseCase;
import com.golive.cinema.init.domain.usecase.ExitComboUseCase;
import com.golive.cinema.init.domain.usecase.ExitDrainageUseCase;
import com.golive.cinema.init.domain.usecase.GuideTypeUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmVersionUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Combo;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.UserInfo;
import com.golive.network.response.ExitGuideResponse;
import com.initialjie.log.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/9.
 */

public class HomePresenter extends BasePresenter<HomeContract.View> implements
        HomeContract.Presenter {

    private static final int EXIT_NETWORK_DELAY_TIME = 4000;

    @NonNull
    private final ActivityImageUseCase mActivityImageUseCase;
    @NonNull
    private final ExitDrainageUseCase mExitDrainageUseCase;
    @NonNull
    private final GuideTypeUseCase mGuideTypeUseCase;
    @NonNull
    private final ExitComboUseCase mExitComboUseCase;
    @NonNull
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    @NonNull
    private final GetMovieRecommendUseCase mGetMovieRecommendUseCase;
    @NonNull
    private final GetKdmVersionUseCase mGetKdmVersionUseCase;
    @NonNull
    private final StatisticsHelper mStatisticsHelper;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;
    private GetMovieRecommendUseCase.ResponseValue mMovieRecommendCache;
    private Observable<GetMovieRecommendUseCase.ResponseValue> mGetMovieRecommendObs;
    private ExitGuideResponse mCachedExitGuideResponse;
    private Observable<ExitGuideResponse> mGetExitGuideObs;

    public HomePresenter(@NonNull HomeContract.View initView,
            @NonNull ActivityImageUseCase activityImageUseCase,
            @NonNull ExitDrainageUseCase exitDrainageUseCase,
            @NonNull GuideTypeUseCase guideTypeUseCase,
            @NonNull ExitComboUseCase exitComboUseCase,
            @NonNull GetUserInfoUseCase getUserInfoUseCase,
            @NonNull GetMovieRecommendUseCase movieRecommendUseCase,
            @NonNull GetKdmVersionUseCase getKdmVersionUseCase,
            @NonNull StatisticsHelper statisticsHelper,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(initView, "initView cannot be null!");
        this.mActivityImageUseCase = checkNotNull(activityImageUseCase,
                "activityImageUseCase cannot be null!");
        this.mExitDrainageUseCase = checkNotNull(exitDrainageUseCase,
                "exitDrainageUseCase cannot be null!");
        this.mGuideTypeUseCase = checkNotNull(guideTypeUseCase, "guideTypeUseCase cannot be null!");
        this.mExitComboUseCase = checkNotNull(exitComboUseCase, "exitComboUseCase cannot be null!");
        this.mGetUserInfoUseCase = checkNotNull(getUserInfoUseCase,
                "mGetUserInfoUseCase cannot be null!");
        this.mGetMovieRecommendUseCase = checkNotNull(movieRecommendUseCase,
                "GetMovieRecommendUseCase cannot be null!");
        mGetKdmVersionUseCase = checkNotNull(getKdmVersionUseCase,
                "getKdmVersionUseCase cannot be null!");
        mStatisticsHelper = checkNotNull(statisticsHelper, "StatisticsHelper cannot be null!");
        this.mSchedulerProvider = checkNotNull(schedulerProvider,
                "schedulerProvider cannot be null!");
        attachView(initView);
        initView.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getUserInfo();
        initHomeData();
        reportHardwareInfo();
    }

    @Override
    public void initHomeData() {
        Logger.d("getActivityImage : start");
        Subscription subscription = mActivityImageUseCase.run(
                new ActivityImageUseCase.RequestValues())
                .subscribe(new Observer<ActivityImageUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "initHomeData, onError : ");
                    }

                    @Override
                    public void onNext(ActivityImageUseCase.ResponseValue response) {
                        Logger.d("onNext");
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showActivityImageView(response.getPoster());
                    }
                });
        addSubscription(subscription);

    }

    @Override
    public void exitGuide() {
        //退出引流
        getView().setLoadingExitRecommendIndicator(true);
        Subscription subscription = getExitComboObs()
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Observer<ExitGuideResponse>() {
                    @Override
                    public void onCompleted() {
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingExitRecommendIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "exitGuide, onError : ");
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showExitRecommendView(null);
                        view.setLoadingExitRecommendIndicator(false);
                    }

                    @Override
                    public void onNext(ExitGuideResponse response) {
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        if (null == response) {
                            view.showExitRecommendView(null);
                            return;
                        }
                        //根据不同的type进行不同的展示
                        int type = response.getType();
                        switch (type) {
                            case FILM_RECOMMENDED_DRAINAGE:
                                List<MovieRecommendFilm> filmsList = response.getData().getList();
                                view.showExitRecommendView(filmsList);
                                break;
                            case VIP_PACKAGE_RECOMMENDATION:
                                Combo combo = response.getCombo();
                                if (TextUtils.isEmpty(combo.getVipProductId())) {
                                    view.showExitRecommendView(null);
                                } else {
                                    view.showVipMenuView(combo);
                                }
                                break;
                            case PIN_MONEY_DRAINAGE:
                                //零花钱引流
                                String guideType = null;
                                ExitGuideResponse.Drainage drainage = response.getDrainage();
                                if (drainage != null) {
                                    guideType = drainage.getGuidetype();
                                }
                                view.showDrainage(guideType);
                                break;
                            default:
                                view.showExitRecommendView(null);
                                break;
                        }
                    }
                });
        addSubscription(subscription);
    }

    @NonNull
    private Observable<ExitGuideResponse> getExitComboObs() {
        if (mCachedExitGuideResponse != null) {
            return Observable.just(mCachedExitGuideResponse);
        }

        if (null == mGetExitGuideObs) {
            synchronized (this) {
                if (null == mGetExitGuideObs) {
                    mGetExitGuideObs = mExitComboUseCase.run(
                            new ExitComboUseCase.RequestValues(false))
                            .doOnNext(new Action1<ExitComboUseCase.ResponseValue>() {
                                @Override
                                public void call(ExitComboUseCase.ResponseValue responseValue) {
                                    if (responseValue != null && responseValue.getResponse() != null
                                            && responseValue.getResponse().isOk()) {
                                        mCachedExitGuideResponse = responseValue.getResponse();
                                    }
                                }
                            })
                            .map(new Func1<ExitComboUseCase.ResponseValue, ExitGuideResponse>() {
                                @Override
                                public ExitGuideResponse call(
                                        ExitComboUseCase.ResponseValue responseValue) {
                                    if (responseValue != null) {
                                        return responseValue.getResponse();
                                    }
                                    return null;
                                }
                            })
                            .timeout(EXIT_NETWORK_DELAY_TIME, TimeUnit.MILLISECONDS)
                            .replay(1)
                            .refCount();
                }
            }
        }
        return mGetExitGuideObs;
    }

    @Override
    public void loadExitData() {
        getView().setLoadingExitRecommendIndicator(true);
        Observable<Object> objectObservable = mGetUserInfoUseCase.run(
                new GetUserInfoUseCase.RequestValues(false))
                .concatMap(new Func1<GetUserInfoUseCase.ResponseValue, Observable<?>>() {
                    @Override
                    public Observable<?> call(GetUserInfoUseCase.ResponseValue responseValue) {
                        if (responseValue != null) {
                            UserInfo userInfo = responseValue.getUserInfo();
                            if (userInfo != null && !userInfo.isVIP()) {
                                return mExitComboUseCase.run(new ExitComboUseCase.RequestValues(
                                        false));
                            } else {
                                return getRecommendMoviesObs();
                            }
                        }

                        return Observable.just(null);
                    }
                })
                .timeout(EXIT_NETWORK_DELAY_TIME, TimeUnit.MILLISECONDS);

        Subscription subscription = objectObservable
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingExitRecommendIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadExitData, onError : ");
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showExitRecommendView(null);
                        view.setLoadingExitRecommendIndicator(false);
                    }

                    @Override
                    public void onNext(Object responseValue) {
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        if (null == responseValue) {
                            view.showExitRecommendView(null);
                            return;
                        }

                        if (responseValue instanceof ExitComboUseCase.ResponseValue) {
                            ExitComboUseCase.ResponseValue value =
                                    (ExitComboUseCase.ResponseValue) responseValue;
                            if (value.getResponse() != null
                                    && value.getResponse().getCombo() != null) {
                                Combo combo = value.getResponse().getCombo();
                                // empty product id
                                if (StringUtils.isNullOrEmpty(combo.getVipProductId())) {
                                    view.showExitRecommendView(null);
                                } else {
                                    // show vip recommended view
                                    view.showVipMenuView(combo);
                                }
                            } else {
                                view.showExitRecommendView(null);
                            }
                        } else if (responseValue instanceof GetMovieRecommendUseCase
                                .ResponseValue) {
                            view.showExitRecommendView(
                                    ((GetMovieRecommendUseCase.ResponseValue) responseValue)
                                            .getContentList());
                        } else {
                            view.showExitRecommendView(null);
                        }
                    }
                });

        addSubscription(subscription);
    }

    private Observable<?> getRecommendMoviesObs() {
        if (mMovieRecommendCache != null) {
            return Observable.just(mMovieRecommendCache);
        }

        if (null == mGetMovieRecommendObs) {
            synchronized (this) {
                if (null == mGetMovieRecommendObs) {
                    mGetMovieRecommendObs = mGetMovieRecommendUseCase.run(
                            new GetMovieRecommendUseCase.RequestValues(""))
                            .doOnNext(new Action1<GetMovieRecommendUseCase.ResponseValue>() {
                                @Override
                                public void call(GetMovieRecommendUseCase.ResponseValue value) {
                                    mMovieRecommendCache = value;
                                }
                            })
                            .replay(1)
                            .refCount();
                }
            }
        }
        return mGetMovieRecommendObs;
    }

    @Override
    public void reportAppStart(final String caller, final String destination, final String netType,
            final String osVersion, final String versionCode, final String filmId,
            final String userStatus, final String duration) {
        Subscription subscription = mGetKdmVersionUseCase.run(
                new GetKdmVersionUseCase.RequestValues())
                .onErrorReturn(new Func1<Throwable, GetKdmVersionUseCase.ResponseValue>() {
                    @Override
                    public GetKdmVersionUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "GetKdmVersionUseCase, onErrorReturn : ");
                        return null;
                    }
                })
                .subscribe(new Subscriber<GetKdmVersionUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "reportAppStart, onError : ");
                    }

                    @Override
                    public void onNext(GetKdmVersionUseCase.ResponseValue responseValue) {
                        String kdmVersion = null;
                        if (responseValue != null && responseValue.getKDMResCode() != null) {
                            kdmVersion = responseValue.getKDMResCode().version.getVersion();
                        }
                        if (StringUtils.isNullOrEmpty(kdmVersion)) {
                            kdmVersion = "";
                        }
                        mStatisticsHelper.reportAppStart(caller, destination, netType, osVersion,
                                versionCode, filmId, userStatus, kdmVersion, duration);
                    }
                });
        addSubscription(subscription);
    }

    private void getUserInfo() {
        Subscription subscription = mGetUserInfoUseCase.run(
                new GetUserInfoUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetUserInfoUseCase.ResponseValue>() {
                    @Override
                    public GetUserInfoUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getUserInfo, onErrorReturn : ");
                        return null;
                    }
                })
                .subscribe(new Observer<GetUserInfoUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getUserInfo, onError : ");
                    }

                    @Override
                    public void onNext(GetUserInfoUseCase.ResponseValue responseValue) {
                        HomeContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        UserInfo userInfo = null;
                        if (responseValue != null) {
                            userInfo = responseValue.getUserInfo();
                        }
                        view.reportAppStart(userInfo);
                    }
                });
        addSubscription(subscription);
    }

    private void reportHardwareInfo() {
        Subscription subscription = Observable.just(null).delay(
                Constants.REPORT_HARDWARE_INF_DELAY_IN_MIN, TimeUnit.MINUTES)
                .observeOn(mSchedulerProvider.io())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "reportHardwareInfo, onError : ");
                    }

                    @Override
                    public void onNext(Object o) {
                        HomeContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.reportHardwareInfo();
                        }
                    }
                });
        addSubscription(subscription);
    }
}
