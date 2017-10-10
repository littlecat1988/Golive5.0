package com.golive.cinema.order.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.PayOrderResult;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/20.
 */

public class PayCreditOrderUseCase extends
        UseCase<PayCreditOrderUseCase.RequestValues, PayCreditOrderUseCase.ResponseValue> {

    private final OrdersDataSource mDataSource;

    public PayCreditOrderUseCase(@NonNull OrdersDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {

        // #1. create order
        return mDataSource.payCreditOrder(requestValues.getOrderSerial())
                .map(
                        new Func1<PayOrderResult, ResponseValue>() {
                            @Override
                            public ResponseValue call(PayOrderResult payOrderResult) {
                                return new ResponseValue(payOrderResult);
                            }
                        });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String orderSerial;

        public RequestValues(String orderSerial) {
            this.orderSerial = orderSerial;
        }

        public String getOrderSerial() {
            return orderSerial;
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
