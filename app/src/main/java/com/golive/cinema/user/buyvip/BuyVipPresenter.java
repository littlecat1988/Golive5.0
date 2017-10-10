package com.golive.cinema.user.buyvip;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipListUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipMonthlyStatusUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Order;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipCombo;
import com.golive.network.entity.VipMonthlyResult;
import com.golive.network.entity.Wallet;
import com.initialjie.log.Logger;

import java.math.BigDecimal;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;


/**
 * Created by Administrator on 2016/10/31.
 */

public class BuyVipPresenter extends BasePresenter<BuyVipContract.View> implements
        BuyVipContract.Presenter {
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    private final GetVipListUseCase mGetVipListUseCase;
    private final GetUserWalletUseCase mGetUserWalletUseCase;
    @NonNull
    private final GetUserCreditWalletUseCase mGetUserCreditWalletUseCase;
    private final GetVipMonthlyStatusUseCase mGetVipMonthlyStatusUseCase;

    public BuyVipPresenter(@NonNull BuyVipContract.View buyvipView,
            @NonNull GetVipListUseCase task,
            @NonNull GetUserWalletUseCase getUserWalletUseCase,
            @NonNull GetUserCreditWalletUseCase getUserCreditWalletUseCase,
            @NonNull GetUserInfoUseCase getUserInfoUseCase,
            GetVipMonthlyStatusUseCase getVipMonthlyStatusUseCase) {
        checkNotNull(buyvipView, "BuyVipView cannot be null!");
        this.mGetVipListUseCase = checkNotNull(task, "getBuyVip cannot be null!");
        this.mGetUserWalletUseCase = checkNotNull(getUserWalletUseCase,
                "GetUserWalletUseCase can not be null!");
        this.mGetUserCreditWalletUseCase = checkNotNull(getUserCreditWalletUseCase,
                "GetUserCreditWalletUseCase can not be null!");
        mGetUserInfoUseCase = checkNotNull(getUserInfoUseCase,
                "get user info usecase can not be null!");
        mGetVipMonthlyStatusUseCase = checkNotNull(getVipMonthlyStatusUseCase,
                "getVipMonthlyStatusUseCase can not be null!");
        attachView(buyvipView);
        buyvipView.setPresenter(this);
    }

    @Override
    public void loadVipPackages(boolean updateUserInfo) {
        getView().setLoadingIndicator(true);
        // get user info && get vip package list
        Subscription subscription = Observable.zip(
                mGetUserInfoUseCase.run(new GetUserInfoUseCase.RequestValues(updateUserInfo))
                        .doOnNext(new Action1<GetUserInfoUseCase.ResponseValue>() {
                            @Override
                            public void call(GetUserInfoUseCase.ResponseValue responseValue) {
                                BuyVipContract.View view = getView();
                                if (view != null && view.isActive() && responseValue != null) {
                                    view.showUserInfo(responseValue.getUserInfo());
                                }
                            }
                        }),
                mGetVipListUseCase.run(new GetVipListUseCase.RequestValues(true)),
                new Func2<GetUserInfoUseCase.ResponseValue, GetVipListUseCase.ResponseValue,
                        Pair<GetUserInfoUseCase.ResponseValue, GetVipListUseCase.ResponseValue>>() {
                    @Override
                    public Pair<GetUserInfoUseCase.ResponseValue, GetVipListUseCase
                            .ResponseValue> call(GetUserInfoUseCase.ResponseValue responseValue,
                            GetVipListUseCase.ResponseValue responseValue2) {
                        return new Pair<>(responseValue, responseValue2);
                    }
                })
                .subscribe(
                        new Subscriber<Pair<GetUserInfoUseCase.ResponseValue, GetVipListUseCase
                                .ResponseValue>>() {
                            @Override
                            public void onCompleted() {
                                BuyVipContract.View view = getView();
                                if (view != null && view.isActive()) {
                                    view.setLoadingIndicator(false);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.e(e, "loadVipPackages onError : ");
                                BuyVipContract.View view = getView();
                                if (view != null && view.isActive()) {
                                    view.setLoadingIndicator(false);
                                }
                            }

                            @Override
                            public void onNext(
                                    Pair<GetUserInfoUseCase.ResponseValue, GetVipListUseCase
                                            .ResponseValue> pair) {
                                Logger.d("loadVipPackages onNext");
                                BuyVipContract.View view = getView();
                                if (null == view || !view.isActive() || null == pair) {
                                    return;
                                }

                                List<VipCombo> packList = pair.second.getVipPackageList();
                                boolean isVip = false;
                                if (pair.first != null) {
                                    UserInfo userInfo = pair.first.getUserInfo();
                                    isVip = userInfo != null && userInfo.isVIP();
                                }
                                view.showVipListView(packList, isVip);
                            }
                        });
        addSubscription(subscription);
    }

    @Override
    public void getTheUserWallet() {
        Subscription subscription = mGetUserWalletUseCase.run(
                new GetUserWalletUseCase.RequestValues(true))
                .subscribe(new Subscriber<GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getTheUserWallet onError : ");
                    }

                    @Override
                    public void onNext(GetUserWalletUseCase.ResponseValue responseValue) {
                        BuyVipContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setWalletInfo(responseValue.getWallet());
                            getCreditWallet();
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void purchaseVip(final VipCombo vipCombo) {
        getView().setLoadingIndicator(true);
        // get user credit wallet
        Subscription subscription = mGetUserCreditWalletUseCase.run(
                new GetUserCreditWalletUseCase.RequestValues(true))
//                .onErrorReturn(new Func1<Throwable, GetUserCreditWalletUseCase.ResponseValue>() {
//                    @Override
//                    public GetUserCreditWalletUseCase.ResponseValue call(Throwable throwable) {
//                        return null;
//                    }
//                })
                .flatMap(new Func1<GetUserCreditWalletUseCase.ResponseValue, Observable<?>>() {
                    @Override
                    public Observable<?> call(GetUserCreditWalletUseCase.ResponseValue
                            responseValue) {
                        BuyVipContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        Wallet wallet = responseValue.getWallet();
                        view.setCreditInfo(wallet);
                        boolean creditExpired = wallet.isCreditExpired();
                        // credit expired
                        if (creditExpired) {
                            int deadLineDays = 0;
                            BigDecimal creditBill = BigDecimal.ZERO;
                            BigDecimal creditLimit = BigDecimal.ZERO;

                            if (!StringUtils.isNullOrEmpty(wallet.getCreditDeadLineDays())) {
                                try {
                                    deadLineDays = Integer.parseInt(wallet
                                            .getCreditDeadLineDays());
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!StringUtils.isNullOrEmpty(wallet.getValue())) {
                                try {
                                    creditBill = new BigDecimal(wallet.getValue());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!StringUtils.isNullOrEmpty(wallet.getCreditLine())) {
                                try {
                                    creditLimit = new BigDecimal(wallet.getCreditLine());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            // show credit expired
                            view.showCreditPayExpired(deadLineDays, creditBill.abs()
                                            .doubleValue(),
                                    creditLimit.doubleValue());
                            // end
                            return Observable.empty();
                        }

                        String payMode = vipCombo.getPayMode();
                        // vip monthly
                        if (!StringUtils.isNullOrEmpty(payMode)
                                && VipCombo.PAY_MODE_CONTINUOUS_MONTHLY.equals(payMode)) {
                            // get vip monthly status
                            return getVipMonthlyStatusObs(vipCombo)
                                    .flatMap(new Func1<VipMonthlyResult, Observable<?>>() {
                                        @Override
                                        public Observable<?> call(VipMonthlyResult result) {
                                            BuyVipContract.View view = getView();
                                            if (null == view || !view.isActive()) {
                                                return Observable.empty();
                                            }

                                            // has sign vip monthly!
                                            if (result != null && result.isOk()
                                                    && !StringUtils.isNullOrEmpty(
                                                    result.getStatus())) {
                                                // show vip monthly repeat
                                                view.showPurchaseVipMonthlyRepeat();
                                                // end
                                                return Observable.empty();
                                            }

                                            return Observable.just(result);
                                        }
                                    });
                        }

                        return Observable.just(true);
                    }
                })
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        BuyVipContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "purchaseVip onError : ");
                        BuyVipContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                            view.showPurchaseVipError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Object responseValue) {
                        BuyVipContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        // show purchase vip view
                        view.showPurchaseVip(vipCombo);
                    }
                });
        addSubscription(subscription);
    }

    private Observable<VipMonthlyResult> getVipMonthlyStatusObs(VipCombo vipCombo) {
        return mGetVipMonthlyStatusUseCase.run(new GetVipMonthlyStatusUseCase
                .RequestValues(vipCombo.getId(), Order.PRODUCT_TYPE_VIP))
                .onErrorReturn(new Func1<Throwable, GetVipMonthlyStatusUseCase.ResponseValue>() {
                    @Override
                    public GetVipMonthlyStatusUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w("getVipMonthlyStatusObs, onErrorReturn :");
                        return null;
                    }
                })
                .map(new Func1<GetVipMonthlyStatusUseCase.ResponseValue, VipMonthlyResult>() {
                    @Override
                    public VipMonthlyResult call(
                            GetVipMonthlyStatusUseCase.ResponseValue responseValue) {
                        if (responseValue != null) {
                            return responseValue.getVipMonthlyResult();
                        }
                        return null;
                    }
                });
    }

    private void getCreditWallet() {
        Logger.d("getCreditWallet start");
        Subscription subscription = mGetUserCreditWalletUseCase.run(
                new GetUserCreditWalletUseCase.RequestValues(true))
                .subscribe(new Subscriber<GetUserCreditWalletUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getCreditWallet onError : ");
                    }

                    @Override
                    public void onNext(GetUserCreditWalletUseCase.ResponseValue responseValue) {
                        BuyVipContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setCreditInfo(responseValue.getWallet());
                        }
                    }
                });
        addSubscription(subscription);
    }
}
