package com.golive.cinema.purchase.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.order.domain.usecase.CreateOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayCreditOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayOrderUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/20.
 */

public class PurchaseUseCase extends
        UseCase<PurchaseUseCase.RequestValues, PurchaseUseCase.ResponseValue> {

    private final CreateOrderUseCase mCreateOrderUseCase;
    private final PayOrderUseCase mPayOrderUseCase;
    private final PayCreditOrderUseCase mPayCreditOrderUseCase;

    public PurchaseUseCase(@NonNull CreateOrderUseCase createOrderUseCase,
            @NonNull PayOrderUseCase payOrderUseCase,
            @NonNull PayCreditOrderUseCase payCreditOrderUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mCreateOrderUseCase = checkNotNull(createOrderUseCase,
                "CreateOrderUseCase cannot be null!");
        mPayOrderUseCase = checkNotNull(payOrderUseCase,
                "PayOrderUseCase cannot be null!");
        mPayCreditOrderUseCase = checkNotNull(payCreditOrderUseCase,
                "PayCreditOrderUseCase cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {

        // #1. create a order
        // #2. pay the order
        // #3. get ticket?
//        String productType = requestValues.isOnline() ? Order.PRODUCT_TYPE_THEATRE_ONLINE
//                : Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;

        // #1. create a order
        String productType = requestValues.mProductType;
        // vip monthly
        if (!StringUtils.isNullOrEmpty(productType)
                && Order.PRODUCT_TYPE_VIP_MONTHLY.equals(productType)) {
            productType = Order.PRODUCT_TYPE_VIP;
        }
        CreateOrderUseCase.RequestValues values = new CreateOrderUseCase.RequestValues(
                requestValues.mProductId, productType, requestValues.mMediaId,
                requestValues.mEncryptionType, requestValues.mQuantity, requestValues.mCurrency);
        return mCreateOrderUseCase.run(values)
                // #2. pay the order
                .flatMap(
                        new Func1<CreateOrderUseCase.ResponseValue, Observable<PayOrderResult>>() {
                            @Override
                            public Observable<PayOrderResult> call(
                                    CreateOrderUseCase.ResponseValue o) {
                                Order order = o.getOrder();
                                String orderSerial = order.getSerial();
                                if (requestValues.mCreditPay) {
                                    return mPayCreditOrderUseCase.run(
                                            new PayCreditOrderUseCase.RequestValues(orderSerial))
                                            .map(new Func1<PayCreditOrderUseCase.ResponseValue,
                                                    PayOrderResult>() {
                                                @Override
                                                public PayOrderResult call(
                                                        PayCreditOrderUseCase.ResponseValue
                                                                responseValue) {
                                                    return responseValue.getPayOrderResult();
                                                }
                                            });
                                } else {
                                    return mPayOrderUseCase.run(
                                            new PayOrderUseCase.RequestValues(orderSerial))
                                            .map(new Func1<PayOrderUseCase.ResponseValue,
                                                    PayOrderResult>() {
                                                @Override
                                                public PayOrderResult call(
                                                        PayOrderUseCase.ResponseValue
                                                                responseValue) {
                                                    return responseValue.getPayOrderResult();
                                                }
                                            });
                                }
                            }
                        })
                // #3. get ticket?
//                .flatMap(new Func1<PayOrderUseCase.ResponseValue, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(PayOrderUseCase.ResponseValue responseValue) {
//                        return null;
//                    }
//                })
                .map(new Func1<PayOrderResult, ResponseValue>() {
                    @Override
                    public ResponseValue call(PayOrderResult payOrderResult) {
                        return new ResponseValue(payOrderResult);
                    }
                })
                ;

    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mProductId;
        private final String mProductType;
        private final String mMediaId;
        private final String mEncryptionType;
        private final boolean mIsOnline;
        private final int mQuantity;
        private final String mCurrency;
        private final boolean mCreditPay;

        public RequestValues(String productId, String productType, String mediaId,
                String encryptionType, boolean isOnline, int quantity, String currency,
                boolean creditPay) {
            mProductId = productId;
            mProductType = productType;
            mMediaId = mediaId;
            mEncryptionType = encryptionType;
            mIsOnline = isOnline;
            mQuantity = quantity;
            mCurrency = currency;
            mCreditPay = creditPay;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final PayOrderResult mPayOrderResult;

        public ResponseValue(PayOrderResult payOrderResult) {
            mPayOrderResult = payOrderResult;
        }

        public PayOrderResult getPayOrderResult() {
            return mPayOrderResult;
        }
    }
}
