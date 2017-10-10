package com.golive.cinema.user.topup;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.user.topup.domain.usecase.TopupPriceListUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.network.entity.ClientService;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;


/**
 * Created by Administrator on 2016/10/31.
 */

public class TopupPresenter extends BasePresenter<TopupContract.View> implements
        TopupContract.Presenter {
    private final TopupPriceListUseCase mTopupPriceListUseCase;
    private final GetUserWalletUseCase mGetUserWalletUseCase;
    //    private GetMainConfigUseCase mMainUseCase;
    private final GetClientServiceUseCase mGetClientServiceUseCase;

    public TopupPresenter(@NonNull TopupContract.View view,
            @NonNull TopupPriceListUseCase topupPriceListUseCase,
            @NonNull GetUserWalletUseCase GetUserWalletTask,
            @NonNull GetClientServiceUseCase getClientServicetask
    ) {//@NonNull GetMainConfigUseCase cfgtask
        checkNotNull(view, "BuyVipView cannot be null!");
        this.mTopupPriceListUseCase = checkNotNull(topupPriceListUseCase,
                "mTopupPriceListUseCase cannot be null!");
        this.mGetUserWalletUseCase = checkNotNull(GetUserWalletTask,
                "mGetUserWalletUseCase cannot be null!");
        this.mGetClientServiceUseCase = checkNotNull(getClientServicetask,
                "mGetClientServiceUseCase cannot be null!");
//        this.mMainUseCase = checkNotNull(cfgtask, "payurltask cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();

        getView().setLoadingIndicator(true);
        Subscription subscription = Observable.zip(getTopUpListObs(), getWalletObs(),
                getClientServiceObs(),
                new Func3<TopupPriceListUseCase.ResponseValue, GetUserWalletUseCase
                        .ResponseValue, GetClientServiceUseCase.ResponseValue, Object>() {
                    @Override
                    public Object call(TopupPriceListUseCase.ResponseValue responseValue,
                            GetUserWalletUseCase.ResponseValue responseValue2,
                            GetClientServiceUseCase.ResponseValue responseValue3) {
                        return null;
                    }
                })
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        TopupContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "topup, onError : ");
                        TopupContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                        view.showLoadingFailed(e.getMessage());
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
        addSubscription(subscription);
    }

    private Observable<TopupPriceListUseCase.ResponseValue> getTopUpListObs() {
        return mTopupPriceListUseCase.run(new TopupPriceListUseCase.RequestValues())
                .doOnNext(new Action1<TopupPriceListUseCase.ResponseValue>() {
                    @Override
                    public void call(TopupPriceListUseCase.ResponseValue responseValue) {
                        TopupContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.showResultListView(responseValue.getTopupPriceList());
                    }
                });
    }

    private Observable<GetUserWalletUseCase.ResponseValue> getWalletObs() {
        return mGetUserWalletUseCase.run(new GetUserWalletUseCase.RequestValues(true))
                .onErrorReturn(new Func1<Throwable, GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public GetUserWalletUseCase.ResponseValue call(Throwable throwable) {
                        Logger.e(throwable, "getWalletObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public void call(GetUserWalletUseCase.ResponseValue responseValue) {
                        TopupContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setWalletInfo(responseValue.getWallet());
                    }
                });
    }

    private Observable<GetClientServiceUseCase.ResponseValue> getClientServiceObs() {
        return mGetClientServiceUseCase.run(new GetClientServiceUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public GetClientServiceUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getClientServiceObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public void call(GetClientServiceUseCase.ResponseValue responseValue) {
                        TopupContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue
                                || null == responseValue.getClientService()) {
                            return;
                        }
                        ClientService clientService = responseValue.getClientService();
                        String phone = clientService.getServicePhone5();
                        if (phone != null) {
                            view.setServicePhoneInfo(phone);
                        }
                    }
                });
    }

    @Override
    public void getUserWallet() {
        addSubscription(getWalletObs()
                .subscribe(new Subscriber<GetUserWalletUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getUserWallet, onError : ");
                    }

                    @Override
                    public void onNext(GetUserWalletUseCase.ResponseValue responseValue) {
                    }
                }));
    }
}
