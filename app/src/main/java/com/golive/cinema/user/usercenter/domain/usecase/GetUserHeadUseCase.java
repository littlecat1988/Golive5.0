package com.golive.cinema.user.usercenter.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.UserHead;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mowl on 2016/11/30.
 */

public class GetUserHeadUseCase extends
        UseCase<GetUserHeadUseCase.RequestValues, GetUserHeadUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetUserHeadUseCase(@NonNull UserDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(dataSource, "UserDataSource cannot be null!");
    }

    @Override
    protected Observable<GetUserHeadUseCase.ResponseValue> executeUseCase(
            GetUserHeadUseCase.RequestValues requestValues) {

        if (requestValues.isForceUpdate()) {
//                mUserDataSource.refreshUserInfo();
        }

        return mUserDataSource.GetUserHead()
                .map(new Func1<UserHead, GetUserHeadUseCase.ResponseValue>() {
                    @Override
                    public GetUserHeadUseCase.ResponseValue call(UserHead info) {
                        return new GetUserHeadUseCase.ResponseValue(info);
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
        private final UserHead mInfo;

        public ResponseValue(UserHead info) {
            mInfo = info;
        }

        public UserHead getUserHead() {
            return mInfo;
        }
    }
}

