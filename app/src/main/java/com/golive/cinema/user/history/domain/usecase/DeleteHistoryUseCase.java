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
 * Created by Mowl on 2016/11/8.
 */

public class DeleteHistoryUseCase extends
        UseCase<DeleteHistoryUseCase.RequestValues, DeleteHistoryUseCase.ResponseValue> {


    private final UserDataSource mUserDataSource;

    public DeleteHistoryUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<DeleteHistoryUseCase.ResponseValue> executeUseCase(
            DeleteHistoryUseCase.RequestValues requestValues) {

        return mUserDataSource.DeleteHistory(requestValues.getmSerial(), requestValues.getmTitle())
                .map(new Func1<EditHistoryResult, ResponseValue>() {
                    @Override
                    public DeleteHistoryUseCase.ResponseValue call(EditHistoryResult result) {
                        return new DeleteHistoryUseCase.ResponseValue(result);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String mSerial;
        private final String mTitle;

        public RequestValues(String serial, String title) {
            mSerial = serial;
            mTitle = title;
        }

        public String getmSerial() {
            return mSerial;
        }

        public String getmTitle() {
            return mTitle;
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


