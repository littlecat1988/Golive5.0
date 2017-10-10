package com.golive.cinema.order.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Order;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/20.
 */

public class CreateOrderUseCase extends
        UseCase<CreateOrderUseCase.RequestValues, CreateOrderUseCase.ResponseValue> {

    private final OrdersDataSource mDataSource;

    public CreateOrderUseCase(@NonNull OrdersDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {

        // #1. create order
        return mDataSource.createOrder(requestValues.productId, requestValues.mediaId,
                requestValues.encryptionType, requestValues.productType,
                String.valueOf(requestValues.quantity))
                .map(new Func1<Order, ResponseValue>() {
                    @Override
                    public ResponseValue call(Order order) {
                        return new ResponseValue(order);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String productId;
        private final String mediaId;
        private final String encryptionType;
        private final String productType;
        private final int quantity;
        private final String currency;

        public RequestValues(String productId, String productType, String mediaId,
                String encryptionType, int quantity, String currency) {
            this.productId = productId;
            this.productType = productType;
            this.mediaId = mediaId;
            this.encryptionType = encryptionType;
            this.quantity = quantity;
            this.currency = currency;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final Order mOrder;

        public ResponseValue(Order order) {
            mOrder = order;
        }

        public Order getOrder() {
            return mOrder;
        }
    }
}
