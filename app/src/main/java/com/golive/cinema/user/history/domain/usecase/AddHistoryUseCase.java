package com.golive.cinema.user.history.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.EditHistoryResult;
import com.golive.network.entity.Order;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mowl on 2016/11/22.
 */

public class AddHistoryUseCase extends
        UseCase<AddHistoryUseCase.RequestValues, AddHistoryUseCase.ResponseValue> {


    private final UserDataSource mUserDataSource;

    public AddHistoryUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<AddHistoryUseCase.ResponseValue> executeUseCase(
            AddHistoryUseCase.RequestValues requestValues) {

        return mUserDataSource.AddHistory(requestValues.getmSerial())
                .map(new Func1<EditHistoryResult, AddHistoryUseCase.ResponseValue>() {
                    @Override
                    public AddHistoryUseCase.ResponseValue call(EditHistoryResult result) {
                        return new AddHistoryUseCase.ResponseValue(result);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String mSerial;

        public RequestValues(String serial) {
            mSerial = serial;
        }

        public String getmSerial() {
            return mSerial;
        }

    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final Order mOrder;
        private final String needed;

        public ResponseValue(EditHistoryResult result) {
            mOrder = result.getOrder();
            needed = result.getNeeded();
        }

        public Order getHistoryOrder() {
            return mOrder;
        }

        public String getHistoryNeeded() {
            return needed;
        }
    }
}


