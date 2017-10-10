package com.golive.cinema.order.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/20.
 */

public class RefreshOrderUseCase extends
        UseCase<RefreshOrderUseCase.RequestValues, RefreshOrderUseCase.ResponseValue> {

    private final OrdersDataSource mDataSource;

    public RefreshOrderUseCase(@NonNull OrdersDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mDataSource.refreshOrder(requestValues.orderSerial)
                .map(new Func1<Boolean, ResponseValue>() {
                    @Override
                    public ResponseValue call(Boolean aBoolean) {
                        return new ResponseValue(aBoolean);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String orderSerial;

        public RequestValues(String orderSerial) {
            this.orderSerial = orderSerial;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final boolean success;

        public ResponseValue(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
