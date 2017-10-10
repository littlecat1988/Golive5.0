package com.golive.cinema.init.dialog;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.purchase.domain.usecase.PurchaseUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Error;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Wallet;
import com.initialjie.log.Logger;

import java.math.BigDecimal;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2017/5/15.
 */

public class ExitVipPresenter extends BasePresenter<ExitVipContract.View> implements
        ExitVipContract.Presenter {

    @NonNull
    private final String mProductId;

    @Nullable
    private final String mProductName;

    private final PurchaseUseCase mPurchaseUseCase;

    private final GetUserCreditWalletUseCase mGetUserCreditWalletUseCase;

    public ExitVipPresenter(@NonNull String productId, String productName,
            @NonNull ExitVipContract.View view,
            @NonNull PurchaseUseCase purchaseUseCase,
            @NonNull GetUserCreditWalletUseCase getUserCreditWalletUseCase) {
        mProductId = checkNotNull(productId);
        mProductName = productName;
        mPurchaseUseCase = checkNotNull(purchaseUseCase);
        mGetUserCreditWalletUseCase = checkNotNull(getUserCreditWalletUseCase);
        attachView(checkNotNull(view));
        view.setPresenter(this);
    }

    @Override
    public void purchase() {
        getView().setPurchasingIndicator(true);
        Subscription subscription = mGetUserCreditWalletUseCase.run(
                new GetUserCreditWalletUseCase.RequestValues(false))
                .concatMap(
                        new Func1<GetUserCreditWalletUseCase.ResponseValue,
                                Observable<PurchaseUseCase.ResponseValue>>() {
                            @Override
                            public Observable<PurchaseUseCase.ResponseValue> call(
                                    GetUserCreditWalletUseCase.ResponseValue
                                            responseValue) {
                                ExitVipContract.View view = getView();
                                if (null == view || !view.isActive() || null == responseValue
                                        || null == responseValue.getWallet()) {
                                    return Observable.empty();
                                }

                                Wallet wallet = responseValue.getWallet();
                                String mCurrency = wallet.getCurrency();
                                boolean creditPay = false;
                                String balanceStr = wallet.getValue();
                                if (!StringUtils.isNullOrEmpty(balanceStr)) {
                                    BigDecimal balance = BigDecimal.ZERO;
                                    try {
                                        balance = new BigDecimal(balanceStr);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    // balance < 0
                                    if (BigDecimal.ZERO.compareTo(balance) > 0) {
                                        // use credit pay method
                                        creditPay = true;
                                    }
                                }

                                return mPurchaseUseCase.run(
                                        new PurchaseUseCase.RequestValues(mProductId,
                                                Order.PRODUCT_TYPE_VIP, "", "", false, 1,
                                                mCurrency, creditPay));
                            }
                        })
                .subscribe(new Subscriber<PurchaseUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        ExitVipContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setPurchasingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "purchase, onError : ");
                        ExitVipContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showPurchaseFailure(e.getMessage());
                        view.setPurchasingIndicator(false);
                    }

                    @Override
                    public void onNext(PurchaseUseCase.ResponseValue responseValue) {
                        ExitVipContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }

                        PayOrderResult payOrderResult = responseValue.getPayOrderResult();
                        // pay success
                        if (payOrderResult != null && StringUtils.isNullOrEmpty(
                                payOrderResult.getNeeded())) {
                            view.showPurchaseSuccess();
                        } else {
                            String errMsg = null;
                            if (payOrderResult != null && payOrderResult.getError() != null) {
                                Error error = payOrderResult.getError();
                                if (!StringUtils.isNullOrEmpty(error.getNote())) {
                                    errMsg = error.getNote();
                                    if (!StringUtils.isNullOrEmpty(error.getNotemsg())) {
                                        errMsg += ", " + error.getNotemsg();
                                    }
                                }
                            }
                            view.showPurchaseFailure(errMsg);
                        }

                    }
                });
        addSubscription(subscription);
    }
}