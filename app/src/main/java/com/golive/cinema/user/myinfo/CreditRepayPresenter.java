package com.golive.cinema.user.myinfo;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.initialjie.log.Logger;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Mowl on 2016/11/25.
 */

public class CreditRepayPresenter extends BasePresenter<MyInfoContract.CreditRepayView> implements
        MyInfoContract.CreditRepayPresenter {

    @NonNull
    private final GetUserCreditWalletUseCase mGetUserCreditWalletUseCase;

    public CreditRepayPresenter(@NonNull MyInfoContract.CreditRepayView view,
            @NonNull GetUserCreditWalletUseCase getUserCreditWalletUseCase) {
        checkNotNull(view, "BuyVipView cannot be null!");

        this.mGetUserCreditWalletUseCase = checkNotNull(getUserCreditWalletUseCase,
                "GetUserCreditWalletUseCase can not be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getCreditWallet();
    }

    private void getCreditWallet() {
        Logger.d("getCreditWallet start");
        getView().setLoadingIndicator(true);
        Subscription subscription = mGetUserCreditWalletUseCase.run(
                new GetUserCreditWalletUseCase.RequestValues(true))
                .subscribe(new Subscriber<GetUserCreditWalletUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        MyInfoContract.CreditRepayView view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getCreditWallet onError : ");
                        MyInfoContract.CreditRepayView view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                        view.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(GetUserCreditWalletUseCase.ResponseValue responseValue) {
                        MyInfoContract.CreditRepayView view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.setCreditInfo(responseValue.getWallet());
                    }
                });
        addSubscription(subscription);
    }
}
