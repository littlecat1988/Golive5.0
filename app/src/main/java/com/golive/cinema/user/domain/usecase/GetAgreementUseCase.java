package com.golive.cinema.user.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.AllAgreement;

import rx.Observable;
import rx.functions.Func1;

public class GetAgreementUseCase extends
        UseCase<GetAgreementUseCase.RequestValues, GetAgreementUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetAgreementUseCase(@NonNull UserDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(dataSource, "UserDataSource cannot be null!");
    }

    @Override
    protected Observable<GetAgreementUseCase.ResponseValue> executeUseCase(
            GetAgreementUseCase.RequestValues requestValues) {
        return mUserDataSource.getAgreement(requestValues.type)
                .map(new Func1<AllAgreement, GetAgreementUseCase.ResponseValue>() {
                    @Override
                    public GetAgreementUseCase.ResponseValue call(AllAgreement agreement) {
                        return new GetAgreementUseCase.ResponseValue(agreement);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String type;

        public RequestValues(String type) {
            this.type = type;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final AllAgreement mAgreement;

        public ResponseValue(AllAgreement agreement) {
            mAgreement = agreement;
        }

        public AllAgreement getAgreement() {
            return mAgreement;
        }
    }
}

