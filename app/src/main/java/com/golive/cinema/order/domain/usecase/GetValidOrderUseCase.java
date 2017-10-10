package com.golive.cinema.order.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Order;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class GetValidOrderUseCase extends
        UseCase<GetValidOrderUseCase.RequestValues, GetValidOrderUseCase.ResponseValue> {

    private final OrdersDataSource mDataSource;

    public GetValidOrderUseCase(@NonNull OrdersDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {

        return mDataSource.getValidOrders(requestValues.getProductId(),
                requestValues.getProductType())
                .map(
                        new Func1<List<Order>, ResponseValue>() {
                            @Override
                            public ResponseValue call(List<Order> orders) {
                                return new ResponseValue(orders);
                            }
                        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String productId;
        private final String productType;

        public RequestValues(@NonNull String productId, @NonNull String productType) {
            this.productId = productId;
            this.productType = productType;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductType() {
            return productType;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final List<Order> mOrders;

        public ResponseValue(List<Order> orders) {
            mOrders = orders;
        }

        public List<Order> getOrders() {
            return mOrders;
        }
    }
}
