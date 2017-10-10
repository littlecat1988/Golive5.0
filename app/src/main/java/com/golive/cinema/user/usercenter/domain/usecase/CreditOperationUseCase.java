package com.golive.cinema.user.usercenter.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.CreditOperation;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/10.
 */

public class CreditOperationUseCase extends
        UseCase<CreditOperationUseCase.RequestValues, CreditOperationUseCase.ResponseValue> {

    @NonNull
    private final UserDataSource mUserDataSource;

    public CreditOperationUseCase(
            @NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mUserDataSource = userDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        if (requestValues.getRedo()) {
            mUserDataSource.redoUserCreditOperation();
        }
        return mUserDataSource.queryUserCreditOperation()
                .map(new Func1<CreditOperation, ResponseValue>() {
                    @Override
                    public ResponseValue call(CreditOperation creditOperation) {
                        return new ResponseValue(creditOperation);
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

        private final CreditOperation creditOperation;

        public ResponseValue(CreditOperation creditOperation) {
            this.creditOperation = creditOperation;
        }

        public CreditOperation getCreditOperation() {
            return creditOperation;
        }
    }


}
