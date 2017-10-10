package com.golive.cinema.user.myinfo;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipListUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipMonthlyStatusUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserHeadUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Order;
import com.golive.network.entity.VipCombo;
import com.golive.network.entity.VipMonthlyResult;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func5;

/**
 * Created by Administrator on 2016/10/31.
 */

public class MyInfoPresenter extends BasePresenter<MyInfoContract.View> implements
        MyInfoContract.Presenter {
    @NonNull
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    @NonNull
    private final GetUserWalletUseCase mGetUserWalletUseCase;
    private final GetUserHeadUseCase mGetUserHeadUseCase;
    @NonNull
    private final GetUserCreditWalletUseCase mGetUserCreditWalletUseCase;
    private final GetVipMonthlyStatusUseCase mGetVipMonthlyStatusUseCase;
    private final GetVipListUseCase mGetVipListUseCase;

    public MyInfoPresenter(@NonNull MyInfoContract.View view,
            @NonNull GetUserInfoUseCase getUserInfoTask,
            @NonNull GetUserWalletUseCase GetUserWalletTask,
            @NonNull GetUserCreditWalletUseCase getUserCreditWalletUseCase,
            @NonNull GetUserHeadUseCase GetUserHeadTask,
            @NonNull GetVipMonthlyStatusUseCase getVipMonthlyStatusUseCase,
            @NonNull GetVipListUseCase mGetVipListUseCase) {
        checkNotNull(view, "BuyVipView cannot be null!");
        this.mGetUserInfoUseCase = checkNotNull(getUserInfoTask,
                "mGetUserInfoUseCase cannot be null!");
        this.mGetUserWalletUseCase = checkNotNull(GetUserWalletTask,
                "mGetUserWalletUseCase cannot be null!");
        this.mGetUserHeadUseCase = checkNotNull(GetUserHeadTask,
                "mGetUserHeadUseCase cannot be null!");
        this.mGetUserCreditWalletUseCase = checkNotNull(getUserCreditWalletUseCase,
                "GetUserCreditWalletUseCase can not be null!");
        this.mGetVipMonthlyStatusUseCase = checkNotNull(getVipMonthlyStatusUseCase,
                "getVipMonthlyStatusUseCase can not be null!");
        this.mGetVipListUseCase = checkNotNull(mGetVipListUseCase,
                "getVipListUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getView().setLoadingIndicator(true);
        Subscription subscription = Observable.zip(getUserInfoObs(), getUserHeadObs(),
                getWalletObs(), getCreditWalletObs(), getVipListObs(),
                new Func5<GetUserInfoUseCase.ResponseValue, GetUserHeadUseCase.ResponseValue,
                        GetUserWalletUseCase.ResponseValue, GetUserCreditWalletUseCase
                        .ResponseValue, Object, Object>() {
                    @Override
                    public Object call(GetUserInfoUseCase.ResponseValue responseValue,
                            GetUserHeadUseCase.ResponseValue responseValue2,
                            GetUserWalletUseCase.ResponseValue responseValue3,
                            GetUserCreditWalletUseCase.ResponseValue responseValue4, Object o) {
                        return null;
                    }
                }
        ).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                MyInfoContract.View view = getView();
                if (null == view || !view.isActive()) {
                    return;
                }
                view.setLoadingIndicator(false);
            }

            @Override
            public void onError(Throwable e) {
                Logger.e(e, "get info, onError : ");
                MyInfoContract.View view = getView();
                if (null == view || !view.isActive()) {
                    return;
                }
                view.setLoadingIndicator(false);
            }

            @Override
            public void onNext(Object o) {
            }
        });

        addSubscription(subscription);
    }

    private Observable<GetUserInfoUseCase.ResponseValue> getUserInfoObs() {
        return mGetUserInfoUseCase.run(
                new GetUserInfoUseCase.RequestValues(false))
                .doOnNext(new Action1<GetUserInfoUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserInfoUseCase.ResponseValue responseValue) {
                        MyInfoContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setUserInfo(responseValue.getUserInfo());
                    }
                });
    }

    private Observable<GetUserWalletUseCase.ResponseValue> getWalletObs() {
        return mGetUserWalletUseCase.run(new GetUserWalletUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public GetUserWalletUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getWalletObs onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserWalletUseCase.ResponseValue responseValue) {
                        MyInfoContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setWalletInfo(responseValue.getWallet());
                    }
                });
    }

    private Observable<GetUserCreditWalletUseCase.ResponseValue> getCreditWalletObs() {
        return mGetUserCreditWalletUseCase.run(
                new GetUserCreditWalletUseCase.RequestValues(true))
                .onErrorReturn(new Func1<Throwable, GetUserCreditWalletUseCase.ResponseValue>() {
                    @Override
                    public GetUserCreditWalletUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getCreditWalletObs onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserCreditWalletUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserCreditWalletUseCase.ResponseValue responseValue) {
                        MyInfoContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setCreditInfo(responseValue.getWallet());
                    }
                });
    }

    private Observable<GetUserHeadUseCase.ResponseValue> getUserHeadObs() {
        return mGetUserHeadUseCase.run(new GetUserHeadUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetUserHeadUseCase.ResponseValue>() {
                    @Override
                    public GetUserHeadUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getUserHeadObs onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserHeadUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserHeadUseCase.ResponseValue responseValue) {
                        MyInfoContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.showUserHead(responseValue.getUserHead());
                    }
                });
    }

    private Observable<GetVipMonthlyStatusUseCase.ResponseValue> getVipMonthlyStatusObs(
            String vipMonthId) {
        return mGetVipMonthlyStatusUseCase.run(new GetVipMonthlyStatusUseCase
                .RequestValues(vipMonthId, Order.PRODUCT_TYPE_VIP))
                .onErrorReturn(new Func1<Throwable, GetVipMonthlyStatusUseCase.ResponseValue>() {
                    @Override
                    public GetVipMonthlyStatusUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getVipMonthlyStatusObs, onErrorReturn :");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetVipMonthlyStatusUseCase.ResponseValue>() {
                    @Override
                    public void call(GetVipMonthlyStatusUseCase.ResponseValue responseValue) {
                        MyInfoContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        VipMonthlyResult vipMonthlyResult = null;
                        if (responseValue != null) {
                            vipMonthlyResult = responseValue.getVipMonthlyResult();
                        }
                        view.setVipMonthlyInfo(vipMonthlyResult);
                    }
                });
    }

    private Observable<Object> getVipListObs() {
        return Observable.just(UserPublic.vipMonthId)
                .concatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(String s) {
                        if (!StringUtils.isNullOrEmpty(s)) {
                            return getVipMonthlyStatusObs(s);
                        }
                        return mGetVipListUseCase.run(new GetVipListUseCase.RequestValues(true))
                                .concatMap(new Func1<GetVipListUseCase.ResponseValue,
                                        Observable<?>>() {
                                    @Override
                                    public Observable<?> call(
                                            GetVipListUseCase.ResponseValue value) {
                                        MyInfoContract.View view = getView();
                                        if (null == view || !view.isActive()
                                                || null == value) {
                                            return Observable.empty();
                                        }

                                        List<VipCombo> packList = value.getVipPackageList();
                                        if (packList != null && !packList.isEmpty()) {
                                            for (VipCombo combo : packList) {
                                                String payMode = combo.getPayMode();
                                                if (!StringUtils.isNullOrEmpty(payMode)
                                                        && VipCombo.PAY_MODE_CONTINUOUS_MONTHLY
                                                        .equals(payMode)) {
                                                    UserPublic.vipMonthId = combo.getId();
                                                    return getVipMonthlyStatusObs(combo.getId());
                                                }
                                            }
                                        }
                                        return Observable.empty();
                                    }
                                });
                    }
                })
                .onErrorReturn(new Func1<Throwable, Object>() {
                    @Override
                    public Object call(Throwable throwable) {
                        Logger.w(throwable, "getVipListObs, onErrorReturn :");
                        return null;
                    }
                });
    }
}
