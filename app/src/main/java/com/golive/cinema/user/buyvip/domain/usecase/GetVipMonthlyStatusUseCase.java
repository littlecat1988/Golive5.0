package com.golive.cinema.user.buyvip.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.VipMonthlyResult;

import rx.Observable;
import rx.functions.Func1;

public class GetVipMonthlyStatusUseCase
        extends
        UseCase<GetVipMonthlyStatusUseCase.RequestValues, GetVipMonthlyStatusUseCase
                .ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetVipMonthlyStatusUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<GetVipMonthlyStatusUseCase.ResponseValue> executeUseCase(
            GetVipMonthlyStatusUseCase.RequestValues requestValues) {
        return mUserDataSource.getVipMonthlyStatus(requestValues.id, requestValues.type)
                .map(new Func1<VipMonthlyResult, ResponseValue>() {
                    @Override
                    public ResponseValue call(VipMonthlyResult vipMonthlyResult) {
                        return new ResponseValue(vipMonthlyResult);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String id;
        private final String type;

        public RequestValues(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final VipMonthlyResult mVipMonthlyResult;

        public ResponseValue(VipMonthlyResult vipMonthlyResult) {
            mVipMonthlyResult = vipMonthlyResult;
        }

        public VipMonthlyResult getVipMonthlyResult() {
            return mVipMonthlyResult;
        }
    }
}
