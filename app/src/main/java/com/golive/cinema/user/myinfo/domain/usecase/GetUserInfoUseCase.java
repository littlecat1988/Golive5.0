package com.golive.cinema.user.myinfo.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.UserInfo;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/10/31.
 */

public class GetUserInfoUseCase extends
        UseCase<GetUserInfoUseCase.RequestValues, GetUserInfoUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetUserInfoUseCase(@NonNull UserDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(dataSource, "UserDataSource cannot be null!");
    }

    @Override
    protected Observable<GetUserInfoUseCase.ResponseValue> executeUseCase(
            GetUserInfoUseCase.RequestValues requestValues) {

        if (requestValues.isForceUpdate()) {
            mUserDataSource.refreshUserInfo();
        }

        return mUserDataSource.getUserInfo()
                .map(new Func1<UserInfo, GetUserInfoUseCase.ResponseValue>() {
                    @Override
                    public GetUserInfoUseCase.ResponseValue call(UserInfo userInfo) {
                        return new GetUserInfoUseCase.ResponseValue(userInfo);
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
        private final UserInfo mUserInfo;

        public ResponseValue(UserInfo userInfo) {
            mUserInfo = userInfo;
        }

        public UserInfo getUserInfo() {
            return mUserInfo;
        }
    }
}
