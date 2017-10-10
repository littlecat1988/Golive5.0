package com.golive.cinema.user.usercenter.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.FinanceMessage;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/10.
 */

public class FinanceMessageUseCase extends
        UseCase<FinanceMessageUseCase.RequestValues, FinanceMessageUseCase.ResponseValue> {

    @NonNull
    private final UserDataSource mUserDataSource;

    public FinanceMessageUseCase(
            @NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mUserDataSource = userDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mUserDataSource.queryUserFinanceMessage()
                .map(new Func1<FinanceMessage, ResponseValue>() {
                    @Override
                    public ResponseValue call(FinanceMessage financeMessage) {
                        return new ResponseValue(financeMessage);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private boolean redoMode = false;

        public RequestValues(boolean redo) {
            redoMode = redo;
        }

        public boolean getRedo() {
            return redoMode;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final FinanceMessage financeMessage;

        public ResponseValue(FinanceMessage financeMessage) {
            this.financeMessage = financeMessage;
        }

        public FinanceMessage getFinanceMessage() {
            return financeMessage;
        }
    }


}
