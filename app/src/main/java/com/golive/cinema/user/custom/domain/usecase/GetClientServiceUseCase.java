package com.golive.cinema.user.custom.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ClientService;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mowl on 2016/11/29.
 */

public class GetClientServiceUseCase extends
        UseCase<GetClientServiceUseCase.RequestValues, GetClientServiceUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetClientServiceUseCase(@NonNull UserDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(dataSource, "UserDataSource cannot be null!");
    }

    @Override
    protected Observable<GetClientServiceUseCase.ResponseValue> executeUseCase(
            GetClientServiceUseCase.RequestValues requestValues) {

        if (requestValues.isForceUpdate()) {
//                mUserDataSource.refreshUserInfo();
        }

        return mUserDataSource.GetClientService()
                .map(new Func1<ClientService, GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public GetClientServiceUseCase.ResponseValue call(ClientService info) {
                        return new GetClientServiceUseCase.ResponseValue(info);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final boolean mForceUpdate;

        public RequestValues(boolean forceUpdate) {
            mForceUpdate = forceUpdate;
        }

        public boolean isForceUpdate() {
            return mForceUpdate;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final ClientService mInfo;

        public ResponseValue(ClientService info) {
            mInfo = info;
        }

        public ClientService getClientService() {
            return mInfo;
        }
    }
}
