package com.golive.cinema.user.buyvip.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.order.domain.usecase.CreateOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayCreditOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayOrderUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mowl on 2016/11/19.
 */

public class PurchaseVipUseCase extends
        UseCase<PurchaseVipUseCase.RequestValues, PurchaseVipUseCase.ResponseValue> {
    private final CreateOrderUseCase mCreateOrderUseCase;
    private final PayOrderUseCase mPayOrderUseCase;
    private final PayCreditOrderUseCase mPayCreditOrderUseCase;


    public PurchaseVipUseCase(@NonNull CreateOrderUseCase createOrderUseCase,
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
    protected Observable<PurchaseVipUseCase.ResponseValue> executeUseCase(
            final PurchaseVipUseCase.RequestValues requestValues) {

        // #1. create a order
        // #2. pay the order

        CreateOrderUseCase.RequestValues getOrderRequest = new CreateOrderUseCase.RequestValues(
                requestValues.getProductId(),
                requestValues.getProductType(),
                "",
                "",
                requestValues.getQuantity(),
                requestValues.getCurrency());

        return getCreateOrderResult(getOrderRequest)
                .flatMap(new Func1<Order, Observable<PayOrderResult>>() {
                    @Override
                    public Observable<PayOrderResult> call(Order order) {
                        String orderSerial = order.getSerial();
                        Logger.d("CreateOrderUseCase result order.getSerial=" + orderSerial);
                        return getPayOrderResult(order.getSerial());
                    }
                })
                .map(new Func1<PayOrderResult, PurchaseVipUseCase.ResponseValue>() {
                    @Override
                    public PurchaseVipUseCase.ResponseValue call(PayOrderResult payOrderResult) {
                        return new PurchaseVipUseCase.ResponseValue(payOrderResult);
                    }
                });

    }

    // #1. create a order
    private Observable<Order> getCreateOrderResult(
            CreateOrderUseCase.RequestValues getOrderRequest) {
        return mCreateOrderUseCase.run(getOrderRequest)
                .map(new Func1<CreateOrderUseCase.ResponseValue, Order>() {
                    @Override
                    public Order call(CreateOrderUseCase.ResponseValue o) {
                        return o.getOrder();
                    }
                });
    }

    // #2. pay the order
    private Observable<PayOrderResult> getPayOrderResult(String orderSerial) {
        return mPayOrderUseCase.run(new PayOrderUseCase.RequestValues(orderSerial))
                .map(new Func1<PayOrderUseCase.ResponseValue, PayOrderResult>() {
                    @Override
                    public PayOrderResult call(PayOrderUseCase.ResponseValue responseValue) {
                        return responseValue.getPayOrderResult();
                    }
                });
    }


    public static final class RequestValues implements UseCase.RequestValues {

        private final String mProductId;
        private final String mProductType;
        private final int mQuantity;
        private final String mCurrency;
        private final boolean mCrediaPay;

        //quantity :数量, currency:币种
        public RequestValues(String productId, String productType,
                int quantity, String currency, boolean crediaPay) {
            mProductId = productId;
            this.mProductType = productType;
            mQuantity = quantity;
            mCurrency = currency;
            mCrediaPay = crediaPay;
        }

        public String getProductId() {
            return mProductId;
        }


        public int getQuantity() {
            return mQuantity;
        }

        public String getCurrency() {
            return mCurrency;
        }

        public String getProductType() {
            return mProductType;
        }


        public boolean isCrediaPay() {
            return mCrediaPay;
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
