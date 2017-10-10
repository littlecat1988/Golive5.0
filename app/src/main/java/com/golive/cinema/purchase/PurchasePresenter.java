package com.golive.cinema.purchase;

import static com.golive.cinema.purchase.PurchaseContract.QR_CODE_PAY_TYPE_REFUND;
import static com.golive.cinema.purchase.PurchaseContract.QR_CODE_PAY_TYPE_TOP_UP;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.order.OrderManager;
import com.golive.cinema.purchase.domain.usecase.PurchaseUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.EspressoIdlingResource;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.Error;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Wallet;
import com.initialjie.log.Logger;

import java.math.BigDecimal;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Wangzj on 2016/11/11.
 */

public class PurchasePresenter extends BasePresenter<PurchaseContract.View> implements
        PurchaseContract.Presenter {

    @NonNull
    private final GetUserWalletUseCase mGetUserWalletUseCase;

    @NonNull
    private final GetUserCreditWalletUseCase mGetUserCreditWalletUseCase;

    @NonNull
    private final PurchaseUseCase mPurchaseFilmUseCase;

    private final GetClientServiceUseCase mGetClientServiceUseCase;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @NonNull
    private final String mProductId;

    @Nullable
    private final String mProductName;

    @NonNull
    private final String mProductType;

    @Nullable
    private final String mMediaId;

    private final String mPrice;

    private final boolean mCreditPay;

    @Nullable
    private final String mEncryptionType;

    private final boolean mIsOnline;

    private final int mQuantity;

    public PurchasePresenter(@NonNull PurchaseContract.View view, @NonNull String productId,
            @NonNull String productType, @Nullable String productName, @Nullable String mediaId,
            @Nullable String encryptionType, boolean isOnline, String price, int quantity,
            boolean creditPay, @NonNull GetUserWalletUseCase getUserWalletUseCase,
            @NonNull GetUserCreditWalletUseCase getUserCreditWalletUseCase,
            @NonNull PurchaseUseCase purchaseFilmUseCase,
            @NonNull GetClientServiceUseCase getClientServiceUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {

        mProductId = productId;
        mProductType = productType;
        mProductName = productName;
        mMediaId = mediaId;
        mEncryptionType = encryptionType;
        mIsOnline = isOnline;
        mQuantity = quantity;
        mPrice = price;
        mCreditPay = creditPay;

        mGetUserWalletUseCase = checkNotNull(getUserWalletUseCase,
                "GetUserWalletUseCase can not be null!");
        mGetUserCreditWalletUseCase = checkNotNull(getUserCreditWalletUseCase,
                "GetUserCreditWalletUseCase can not be null!");
        mPurchaseFilmUseCase = checkNotNull(purchaseFilmUseCase,
                "PurchaseUseCase can not be null!");
        mGetClientServiceUseCase = checkNotNull(getClientServiceUseCase,
                "getClientServiceUseCase can not be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider, "SchedulerProvider can not be null!");

        attachView(checkNotNull(view, "PurchaseContract View cannot be null!"));
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        // load purchase detail
        loadPurchaseDetail();
    }

    @Override
    public void loadPurchaseDetail() {

        getView().setLoadingIndicator(true);
        EspressoIdlingResource.increment();

        Subscription subscription = Observable.zip(getClientServiceObs(), getCheckWalletResultObs(),
                new Func2<ClientService, CheckWalletResult, Pair<ClientService,
                        CheckWalletResult>>() {
                    @Override
                    public Pair<ClientService, CheckWalletResult> call(ClientService clientService,
                            CheckWalletResult checkWalletResult) {
                        return new Pair<>(clientService, checkWalletResult);
                    }
                })
                .subscribe(new Subscriber<Pair<ClientService, CheckWalletResult>>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                            view.showGetPayInfoSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadPurchaseDetail, onError : ");
                        EspressoIdlingResource.decrement();
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                            view.showGetPayInfoFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Pair<ClientService, CheckWalletResult> resultPair) {
                        PurchaseContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        ClientService service = resultPair.first;
                        if (service != null) {
                            // show customer service
                            view.showCustomerService(service.getServicePhone5(), service.getQQ());
                        }

                        CheckWalletResult checkWalletResult = resultPair.second;
                        // update the view
                        updateView(checkWalletResult);
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void purchase() {

        getView().setPurchasingIndicator(true);
        EspressoIdlingResource.increment();

        /*
         * #1. get user wallet
         * #2. compare user wallet balance to the pay price
         * #2.1. if not enough, then show qr code to pay and when it pay success, then update the
         *  view and try to purchase again
         * #2.2. else, purchase it
         */

        // #1. get user wallet
        final Observable<Wallet> getWalletObs = getWalletObservable();

        // #2. compare user wallet balance to the pay price
        final Observable<CheckWalletResult> checkWalletResultObs = getWalletObs
                .map(new Func1<Wallet, CheckWalletResult>() {
                    @Override
                    public CheckWalletResult call(Wallet wallet) {
                        return getCheckWalletResult(wallet);
                    }
                });

        Subscription subscription = checkWalletResultObs
                // switch to UI thread
                .observeOn(mSchedulerProvider.ui())
                .flatMap(new Func1<CheckWalletResult, Observable<PurchaseUseCase
                        .ResponseValue>>() {
                    @Override
                    public Observable<PurchaseUseCase.ResponseValue> call(
                            CheckWalletResult checkWalletResult) {
                        final Wallet wallet = checkWalletResult.mWallet;
                        if (checkWalletResult.mCanPurchase) { // can purchase
                            // #2.2. balance enough, purchase it
                            return getPurchaseObservable(wallet.getCurrency(), mCreditPay);
                        } else {
                            // #2.1. if not enough, then show qr code to top tup
                            PurchaseContract.View view = getView();
                            if (null == view || !view.isActive()) {
                                return Observable.empty();
                            }

                            return view.showQrCodePayUI(QR_CODE_PAY_TYPE_TOP_UP,
//                                    checkWalletResult.mNeedPay,
                                    new BigDecimal(mPrice).doubleValue(),
                                    checkWalletResult.mWalletBalance,
                                    checkWalletResult.mCreditWalletBalance, 0)
                                    // filter qr code pay success
                                    .filter(new Func1<Boolean, Boolean>() {
                                        @Override
                                        public Boolean call(Boolean aBoolean) {
                                            return aBoolean != null && aBoolean;
                                        }
                                    })
                                    // check user wallet balance again
                                    .flatMap(
                                            new Func1<Boolean, Observable<CheckWalletResult>>() {
                                                @Override
                                                public Observable<CheckWalletResult> call(
                                                        Boolean aBoolean) {
                                                    return checkWalletResultObs.doOnNext(
                                                            new Action1<CheckWalletResult>() {
                                                                @Override
                                                                public void call(CheckWalletResult
                                                                        result) {
                                                                    // update the view
                                                                    updateView(result);
                                                                }
                                                            });
                                                }
                                            })
//                                    // filter balance enough
//                                    .filter(new Func1<CheckWalletResult, Boolean>() {
//                                        @Override
//                                        public Boolean call(CheckWalletResult checkWalletResult) {
//                                            return checkWalletResult != null
//                                                    && checkWalletResult.mCanPurchase;
//                                        }
//                                    })
                                    // continue purchase it
                                    .flatMap(new Func1<CheckWalletResult,
                                            Observable<PurchaseUseCase
                                                    .ResponseValue>>() {
                                        @Override
                                        public Observable<PurchaseUseCase
                                                .ResponseValue> call(
                                                CheckWalletResult result) {
                                            boolean creditPay = mCreditPay;
                                            // not credit pay
                                            if (!mCreditPay) {
                                                BigDecimal balance = BigDecimal.ZERO;
                                                String balanceStr = result.mWallet.getValue();
                                                Logger.d("purchase, after qr code pay, balance : "
                                                        + balanceStr);
                                                if (!StringUtils.isNullOrEmpty(balanceStr)) {
                                                    try {
                                                        balance = new BigDecimal(balanceStr);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                // balance < 0
                                                if (BigDecimal.ZERO.compareTo(balance) > 0) {
                                                    // use credit pay method
                                                    creditPay = true;
                                                    Logger.w(
                                                            "purchase, change to use credit pay "
                                                                    + "method");
                                                }
                                            }
                                            return getPurchaseObservable(wallet.getCurrency(),
                                                    creditPay);
                                        }
                                    });
                        }
                    }
                })
                .subscribe(new Subscriber<PurchaseUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPurchasingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "purchase, onError : ");
                        EspressoIdlingResource.decrement();
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPurchasingIndicator(false);
                            view.showPurchaseFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(PurchaseUseCase.ResponseValue responseValue) {
                        PurchaseContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        PayOrderResult payOrderResult = responseValue.getPayOrderResult();
                        // pay success
                        if (payOrderResult != null && StringUtils.isNullOrEmpty(
                                payOrderResult.getNeeded())) {
                            view.showPurchaseSuccess(payOrderResult.getOrder(),
                                    payOrderResult.getFinanceOrder());
                            OrderManager.getInstance().addOrder(mProductId,
                                    payOrderResult.getOrder());
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


    @Override
    public void topUp() {
        getView().showTopUpUI();
    }

    @Override
    public void refundCredit() {

        getView().setRefundingCreditIndicator(true);
        EspressoIdlingResource.increment();

        /**
         * #1. check the wallet
         * #2. calculate the amount to refund the credit
         * #3. show the qr code to refund credit
         * #4. check the wallet again and then refresh the view
         */

        // #1. get the wallet
        Subscription subscription = getWalletObservable()
                .flatMap(new Func1<Wallet, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Wallet wallet) {

                        PurchaseContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        String walletBalanceStr = wallet.getValue();
                        String creditLineStr = wallet.getCreditLine();

                        BigDecimal walletBalance = BigDecimal.ZERO;
                        BigDecimal creditLine = BigDecimal.ZERO;

                        if (!StringUtils.isNullOrEmpty(walletBalanceStr)) {
                            try {
                                walletBalance = new BigDecimal(walletBalanceStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (!StringUtils.isNullOrEmpty(creditLineStr)) {
                            try {
                                creditLine = new BigDecimal(creditLineStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // credit limit <= 0 || user wallet balance >= 0
                        if (BigDecimal.ZERO.compareTo(creditLine) >= 0
                                || BigDecimal.ZERO.compareTo(walletBalance) <= 0) {
                            // return ok
                            return Observable.just(true);
                        }

                        // #2. calculate the amount to refund the credit
                        double needPay = walletBalance.abs().doubleValue();

                        // #3. show the qr code to refund credit
                        return view.showQrCodePayUI(QR_CODE_PAY_TYPE_REFUND, 0, 0,
                                creditLine.doubleValue(), needPay);

                    }
                })
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean != null && aBoolean;
                    }
                })
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setRefundingCreditIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "refundCredit, onError : ");
                        EspressoIdlingResource.decrement();
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setRefundingCreditIndicator(false);
                            view.showRefundFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Boolean wallet) {
                        PurchaseContract.View view = getView();
                        if (view != null && view.isActive()) {

                            // show refund success
                            view.showRefundSuccess();

                            // #4. check the wallet again and then refresh the view
                            loadPurchaseDetail();
                        }
                    }
                });
        addSubscription(subscription);
    }

    private void updateView(CheckWalletResult result) {

        PurchaseContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        if (null == result) {
            return;
        }

        BigDecimal walletBalance = new BigDecimal(Double.toString(result.mWalletBalance));
        BigDecimal creditWalletBalance = new BigDecimal(
                Double.toString(result.mCreditWalletBalance));

        BigDecimal price = new BigDecimal(mPrice);
        BigDecimal payAmount = price.compareTo(walletBalance) > 0 ? walletBalance : price;

        // show normal pay amount
        view.showPayAmount(payAmount.doubleValue());

        if (mCreditPay) {
            BigDecimal creditNeedPay = BigDecimal.ZERO;

            // user wallet balance is not enough
            if (price.compareTo(walletBalance) > 0) {
                // calculate credit need pay
                creditNeedPay = price.subtract(walletBalance);
            }

            creditNeedPay = creditNeedPay.compareTo(creditWalletBalance) < 0 ? creditNeedPay
                    : creditWalletBalance;

            // show credit pay amount
            view.showCreditPayAmount(creditNeedPay.doubleValue());

            // show credit balance
            view.showCreditBalance(creditWalletBalance.doubleValue());

            // show credit pay dead line
            view.showCreditPayDeadline(result.mCreditWalletDeadline);

            // current credit wallet balance < credit wallet limit, it means credit pay had been
            // used before.
            boolean hasUseCredit = new BigDecimal(result.mCreditWalletLimit).compareTo(
                    creditWalletBalance) > 0;

            // set refund credit view visible
            view.setRefundCreditVisible(hasUseCredit);
        }

        double needPay = result.mNeedPay;
        // need pay
        BigDecimal needPayDecimal = new BigDecimal(needPay);
        if (needPayDecimal.compareTo(BigDecimal.ZERO) > 0) { // need more to pay
            boolean purchaseViewVisible;
//            if (mCreditPay) { // credit pay
//                // set purchase view not visible
//                view.setPurchaseVisible(false);
//            } else { // normal pay
//            }
            purchaseViewVisible = !mCreditPay;
            view.setPurchaseVisible(purchaseViewVisible);
            view.showBalanceEnough(false);
        } else { // wallet enough
            view.setPurchaseVisible(true);
            view.showBalanceEnough(true);
        }

        // show need pay
        view.showNeedForPay(needPay);
    }

    private Observable<Wallet> getWalletObservable() {
        return Observable.just(mCreditPay)
                // get user wallet
                .flatMap(new Func1<Boolean, Observable<Wallet>>() {
                    @Override
                    public Observable<Wallet> call(Boolean aBoolean) {
                        if (aBoolean) { // credit pay
                            return mGetUserCreditWalletUseCase.run(
                                    // need refresh wallet
                                    new GetUserCreditWalletUseCase.RequestValues(true))
                                    .map(new Func1<GetUserCreditWalletUseCase.ResponseValue,
                                            Wallet>() {
                                        @Override
                                        public Wallet call(
                                                GetUserCreditWalletUseCase.ResponseValue
                                                        responseValue) {
                                            return responseValue.getWallet();
                                        }
                                    });
                        } else { // normal pay
                            return mGetUserWalletUseCase.run(
                                    // need refresh wallet
                                    new GetUserWalletUseCase.RequestValues(true))
                                    .map(new Func1<GetUserWalletUseCase.ResponseValue, Wallet>() {
                                        @Override
                                        public Wallet call(
                                                GetUserWalletUseCase.ResponseValue responseValue) {
                                            return responseValue.getWallet();
                                        }
                                    });
                        }
                    }
                });
    }

    private Observable<CheckWalletResult> getCheckWalletResultObs() {
        return getWalletObservable()
                .map(new Func1<Wallet, CheckWalletResult>() {
                    @Override
                    public CheckWalletResult call(Wallet wallet) {
                        return getCheckWalletResult(wallet);
                    }
                });
    }

    @NonNull
    private Observable<PurchaseUseCase.ResponseValue> getPurchaseObservable(String currency,
            boolean creditPay) {
        final PurchaseUseCase.RequestValues requestValues =
                new PurchaseUseCase.RequestValues(mProductId, mProductType, mMediaId,
                        mEncryptionType, mIsOnline, mQuantity, currency, creditPay);
        return mPurchaseFilmUseCase.run(requestValues);
    }

    private Observable<ClientService> getClientServiceObs() {
        return mGetClientServiceUseCase.run(new GetClientServiceUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public GetClientServiceUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "GetClientServiceUseCase, onErrorReturn : ");
                        return null;
                    }
                })
                .map(new Func1<GetClientServiceUseCase.ResponseValue, ClientService>() {
                    @Override
                    public ClientService call(GetClientServiceUseCase.ResponseValue responseValue) {
                        if (responseValue != null) {
                            return responseValue.getClientService();
                        }
                        return null;
                    }
                });
    }

    @NonNull
    private CheckWalletResult getCheckWalletResult(Wallet wallet) {
        String walletBalanceStr = wallet.getValue();
        String creditLineStr = wallet.getCreditLine();

        BigDecimal walletBalance = BigDecimal.ZERO;
        BigDecimal creditLine = BigDecimal.ZERO;
        int creditDeadLine = -1;

        if (!StringUtils.isNullOrEmpty(walletBalanceStr)) {
            try {
                walletBalance = new BigDecimal(walletBalanceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!StringUtils.isNullOrEmpty(creditLineStr)) {
            try {
                creditLine = new BigDecimal(creditLineStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!StringUtils.isNullOrEmpty(wallet.getCreditDeadLineDays())) {
            try {
                creditDeadLine = Integer.parseInt(wallet.getCreditDeadLineDays());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BigDecimal walletAmount = BigDecimal.ZERO;
        if (walletBalance != null) {
            // make sure wallet balance >= 0
            walletAmount = BigDecimal.ZERO.compareTo(walletBalance) < 0
                    ? walletBalance : BigDecimal.ZERO;
        }

        // all balance sum
        BigDecimal walletBalanceSum;
        if (mCreditPay) { // credit pay
            walletBalanceSum = creditLine.add(walletBalance);
        } else { // normal pay
            walletBalanceSum = walletAmount;
        }

        BigDecimal price = new BigDecimal(mPrice);
        BigDecimal needPay = BigDecimal.ZERO;

        // balance is not enough
        if (walletBalanceSum.compareTo(price) < 0) {
            needPay = price.subtract(walletBalanceSum);
        } else {
        }

        BigDecimal creditBalance = creditLine;
        // if user wallet balance is negative
        if (BigDecimal.ZERO.compareTo(walletBalance) > 0) {
            creditBalance = creditLine.add(walletBalance);
        }

        boolean canPurchase = BigDecimal.ZERO.compareTo(needPay) >= 0;
        return new CheckWalletResult(canPurchase, needPay.doubleValue(), wallet,
                walletAmount.doubleValue(), creditBalance.doubleValue(),
                creditLine.doubleValue(), creditDeadLine);
    }

    private class CheckWalletResult {
        private final boolean mCanPurchase;
        private final double mNeedPay;
        private final Wallet mWallet;
        private final double mWalletBalance;
        private final double mCreditWalletBalance;
        private final double mCreditWalletLimit;
        private final int mCreditWalletDeadline;

        public CheckWalletResult(boolean canPurchase, double needPay, Wallet wallet,
                double walletBalance, double creditWalletBalance, double creditWalletLimit,
                int creditWalletDeadline) {
            mCanPurchase = canPurchase;
            mNeedPay = needPay;
            mWallet = wallet;
            this.mWalletBalance = walletBalance;
            this.mCreditWalletBalance = creditWalletBalance;
            mCreditWalletLimit = creditWalletLimit;
            mCreditWalletDeadline = creditWalletDeadline;
        }
    }
}