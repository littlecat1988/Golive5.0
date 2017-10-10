package com.golive.cinema.user.usercenter;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.data.source.remote.RecommendRemoteDataSource;
import com.golive.cinema.recommend.domain.usecase.GetRecommendUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserHeadUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.network.response.RecommendResponse;
import com.google.gson.Gson;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;

/**
 * Created by Mowl on 2016/11/15.
 */

public class UserCenterPresenter extends BasePresenter<UserCenterContract.View> implements
        UserCenterContract.Presenter {
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    private final GetUserWalletUseCase mGetUserWalletUseCase;
    private final GetRecommendUseCase mGetTemplateCase;
    private final GetUserHeadUseCase mGetUserHeadUseCase;
    private final String DEFAULT_STR =
            "{\"error\":{\"type\":\"false\",\"notemsg\":\"\"},\"layout\":{\"items\":[" +
                    "{\"location\":{\"x\":0,\"y\":0,\"w\":1,\"h\":1},\"actionContent\":\"1\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":0,\"y\":1,\"w\":2,\"h\":1},\"actionContent\":\"3\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":1,\"y\":0,\"w\":1,\"h\":1},\"actionContent\":\"2\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":2,\"y\":0,\"w\":1,\"h\":1},\"actionContent\":\"4\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":2,\"y\":1,\"w\":2,\"h\":1},\"actionContent\":\"6\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":3,\"y\":0,\"w\":1,\"h\":1},\"actionContent\":\"5\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":4,\"y\":0,\"w\":1,\"h\":1},\"actionContent\":\"7\","
                    + "\"actionType\":2},"
                    +
                    "{\"location\":{\"x\":4,\"y\":1,\"w\":1,\"h\":1},\"actionContent\":\"8\","
                    + "\"actionType\":2}]}}";

    public UserCenterPresenter(@NonNull UserCenterContract.View view,
            @NonNull GetRecommendUseCase getTemplateTask,
            @NonNull GetUserInfoUseCase getUserInfoTask,
            @NonNull GetUserWalletUseCase GetUserWalletTask,
            @NonNull GetUserHeadUseCase GetUserHeadTask) {
        checkNotNull(view, "View cannot be null!");
        this.mGetTemplateCase = checkNotNull(getTemplateTask,
                "mGetTemplateUseCase cannot be null!");
        this.mGetUserInfoUseCase = checkNotNull(getUserInfoTask,
                "mGetUserInfoUseCase cannot be null!");
        this.mGetUserWalletUseCase = checkNotNull(GetUserWalletTask,
                "mGetUserWalletUseCase cannot be null!");
        this.mGetUserHeadUseCase = checkNotNull(GetUserHeadTask,
                "mGetUserHeadUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        Subscription subscription = Observable.zip(getUserInfoObs(true), getUserWalletObs(true),
                getUserHeadObs(true),
                new Func3<GetUserInfoUseCase.ResponseValue, GetUserWalletUseCase.ResponseValue,
                        GetUserHeadUseCase.ResponseValue, Object>() {
                    @Override
                    public Object call(GetUserInfoUseCase.ResponseValue responseValue,
                            GetUserWalletUseCase.ResponseValue responseValue2,
                            GetUserHeadUseCase.ResponseValue responseValue3) {
                        return null;
                    }
                })
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "onError : ");
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void getTheUserWallet(boolean forceUpdate) {
        addSubscription(getUserWalletObs(forceUpdate).subscribe());
    }

    @Override
    public void getTemplateData(String pageId) {// 获取模板
        Subscription subscription = mGetTemplateCase.run(
                new GetRecommendUseCase.RequestValues(
                        RecommendRemoteDataSource.PAGE_USER_CENTER, pageId))
                .subscribe(new Observer<GetRecommendUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getTemplateData onError : ");
                        UserCenterContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        //view.showGetTemplateFailed(e.getMessage());

                        view.showTemplateView(
                                new Gson().fromJson(DEFAULT_STR, RecommendResponse.class));
                    }

                    @Override
                    public void onNext(GetRecommendUseCase.ResponseValue responseValue) {
                        UserCenterContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        RecommendResponse response = responseValue.getResponse();
                        if (response != null && response.isOk()) {
                            view.showTemplateView(response);
                        } else {
                            //view.showGetTemplateFailed(response.getError().getNotemsg());
                            view.showTemplateView(
                                    new Gson().fromJson(DEFAULT_STR, RecommendResponse.class));
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void getUserInfo(boolean forceUpdate) {
        addSubscription(getUserInfoObs(forceUpdate).subscribe());
    }

    @Override
    public void getUserHead(boolean forceUpdate) {
        addSubscription(getUserHeadObs(forceUpdate).subscribe());
    }

    private Observable<GetUserInfoUseCase.ResponseValue> getUserInfoObs(boolean forceUpdate) {
        return mGetUserInfoUseCase.run(new GetUserInfoUseCase.RequestValues(forceUpdate))
                .onErrorReturn(new Func1<Throwable, GetUserInfoUseCase.ResponseValue>() {
                    @Override
                    public GetUserInfoUseCase.ResponseValue call(Throwable throwable) {
                        Logger.e(throwable, "getUserWalletObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserInfoUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserInfoUseCase.ResponseValue responseValue) {
                        UserCenterContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setUserInfo(responseValue.getUserInfo());
                    }
                });
    }

    private Observable<GetUserWalletUseCase.ResponseValue> getUserWalletObs(boolean forceUpdate) {
        return mGetUserWalletUseCase.run(new GetUserWalletUseCase.RequestValues(forceUpdate))
                .onErrorReturn(new Func1<Throwable, GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public GetUserWalletUseCase.ResponseValue call(Throwable throwable) {
                        Logger.e(throwable, "getUserWalletObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserWalletUseCase.ResponseValue responseValue) {
                        UserCenterContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setWalletInfo(responseValue.getWallet());
                    }
                });
    }

    private Observable<GetUserHeadUseCase.ResponseValue> getUserHeadObs(boolean forceUpdate) {
        return mGetUserHeadUseCase.run(new GetUserHeadUseCase.RequestValues(forceUpdate))
                .onErrorReturn(new Func1<Throwable, GetUserHeadUseCase.ResponseValue>() {
                    @Override
                    public GetUserHeadUseCase.ResponseValue call(Throwable throwable) {
                        Logger.e(throwable, "getUserHeadObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserHeadUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserHeadUseCase.ResponseValue responseValue) {
                        UserCenterContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.showUserHead(responseValue.getUserHead());
                    }
                });
    }
}