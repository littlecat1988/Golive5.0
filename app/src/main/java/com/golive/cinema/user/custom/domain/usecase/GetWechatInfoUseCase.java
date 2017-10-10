package com.golive.cinema.user.custom.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.WechatInfo;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/10/31.
 */

public class GetWechatInfoUseCase extends
        UseCase<GetWechatInfoUseCase.RequestValues, GetWechatInfoUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetWechatInfoUseCase(@NonNull UserDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(dataSource, "UserDataSource cannot be null!");
    }

    @Override
    protected Observable<GetWechatInfoUseCase.ResponseValue> executeUseCase(
            GetWechatInfoUseCase.RequestValues requestValues) {

        if (requestValues.isForceUpdate()) {
//                mUserDataSource.refreshUserInfo();
        }

        return mUserDataSource.GetWechatInfo()
                .map(new Func1<WechatInfo, GetWechatInfoUseCase.ResponseValue>() {
                    @Override
                    public GetWechatInfoUseCase.ResponseValue call(WechatInfo info) {
                        return new GetWechatInfoUseCase.ResponseValue(info);
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
        private final WechatInfo mInfo;

        public ResponseValue(WechatInfo info) {
            mInfo = info;
        }

        public WechatInfo getWechatInfo() {
            return mInfo;
        }
    }
}
