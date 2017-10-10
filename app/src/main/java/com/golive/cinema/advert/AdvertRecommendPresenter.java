package com.golive.cinema.advert;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.player.domain.usecase.ReportAdvertMiaozhenUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.UserInfo;
import com.golive.network.helper.Md5Helper;
import com.initialjie.log.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by chgang on 2017/1/9.
 */

public class AdvertRecommendPresenter extends BasePresenter<AdvertRecommendContract.View> implements
        AdvertRecommendContract.Presenter {
    private final static int LOAD_RECOMMEND_TIMEOUT = 4000;
    @NonNull
    private final GetMovieRecommendUseCase mGetMovieRecommendUseCase;
    @NonNull
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    @NonNull
    private final ReportAdvertMiaozhenUseCase mReportAdvertMiaozhenUseCase;
    private Observable<GetMovieRecommendUseCase.ResponseValue> mGetRecommendMovies;

    public AdvertRecommendPresenter(@NonNull AdvertRecommendContract.View view,
            @NonNull GetUserInfoUseCase getUserInfoUseCase,
            @NonNull GetMovieRecommendUseCase movieRecommendUseCase,
            @NonNull ReportAdvertMiaozhenUseCase reportAdvertMiaozhenUseCase) {
        checkNotNull(view, "AdvertRecommendView cannot be null!");
        this.mGetUserInfoUseCase = checkNotNull(getUserInfoUseCase,
                "getUserInfoUseCase cannot be null!");
        this.mGetMovieRecommendUseCase = checkNotNull(movieRecommendUseCase,
                "GetMovieRecommendUseCase cannot be null!");
        this.mReportAdvertMiaozhenUseCase = checkNotNull(reportAdvertMiaozhenUseCase,
                "reportAdvertMiaozhenUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void loadRecommendFilmList(final String filmId) {
        getView().setLoadingRecommendIndicator(true);
        Subscription subscription = getRecommendMovies(filmId)
                .subscribe(new Observer<GetMovieRecommendUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        AdvertRecommendContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingRecommendIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadRecommendFilmList, onError : ");
                        AdvertRecommendContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingRecommendIndicator(false);
                            view.showRecommendMovieList(null);
                        }
                    }

                    @Override
                    public void onNext(GetMovieRecommendUseCase.ResponseValue responseValue) {
                        AdvertRecommendContract.View view = getView();
                        if (view != null && view.isActive() && responseValue != null) {
                            List<MovieRecommendFilm> contentList = responseValue.getContentList();
                            view.showRecommendMovieList(contentList);
                        }
                    }
                });
        addSubscription(subscription);
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

    private Observable<UserInfo> getUserInfo(boolean forceUpdate) {
        return mGetUserInfoUseCase.run(new GetUserInfoUseCase.RequestValues(forceUpdate))
                .map(new Func1<GetUserInfoUseCase.ResponseValue, UserInfo>() {
                    @Override
                    public UserInfo call(GetUserInfoUseCase.ResponseValue responseValue) {
                        return responseValue.getUserInfo();
                    }
                });
    }

    @Override
    public void reportAdvertMiaozhen(String filmId, String advertId, String adReportUrl,
            @Nullable String manufacturerId, @Nullable String mac, String scaleddensity) {
        final String type = "1";
        String reportUrl = adReportUrl.replace("_MAC_", Md5Helper.calculateMd5(mac));
        if (!StringUtils.isNullOrEmpty(reportUrl)) {
            HashMap map = StringUtils.getReportParams(reportUrl, Constants.K_KEY_AD,
                    Constants.P_KEY_AD);
            if (map != null) {
                Object k_key = map.get(Constants.K_KEY_AD);
                Object p_key = map.get(Constants.P_KEY_AD);
                if (k_key != null && p_key != null) {
                    addSubscription(mReportAdvertMiaozhenUseCase.run(
                            new ReportAdvertMiaozhenUseCase.RequestValues(filmId, scaleddensity,
                                    manufacturerId, k_key.toString(), p_key.toString(), advertId,
                                    type))
                            .subscribe(new Subscriber<ReportAdvertMiaozhenUseCase.ResponseValue>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.e(e, "mReportAdvertMiaozhenUseCase, onError:");
                                }

                                @Override
                                public void onNext(
                                        ReportAdvertMiaozhenUseCase.ResponseValue responseValue) {
                                }
                            }));
                }
            }
        }
    }
}
