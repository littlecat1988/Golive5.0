package com.golive.cinema.init.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.MainConfig;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/10/27.
 */

public class GetMainConfigUseCase extends
        UseCase<GetMainConfigUseCase.RequestValues, GetMainConfigUseCase.ResponseValue> {

    private final MainConfigDataSource mMainConfigDataSource;

    public GetMainConfigUseCase(@NonNull MainConfigDataSource mainConfigDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mMainConfigDataSource = checkNotNull(mainConfigDataSource,
                "MainConfigDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        if (requestValues.mForceUpdate) {
            mMainConfigDataSource.refreshMainConfig();
        }
        return mMainConfigDataSource.getMainConfig().map(new Func1<MainConfig, ResponseValue>() {
            @Override
            public ResponseValue call(MainConfig mainConfig) {
                return new ResponseValue(mainConfig);
            }
        });
    }


    public static class RequestValues implements UseCase.RequestValues {
        private final boolean mForceUpdate;

        public RequestValues(boolean forceUpdate) {
            mForceUpdate = forceUpdate;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final MainConfig mMainConfig;

        public ResponseValue(MainConfig mMainConfig) {
            this.mMainConfig = mMainConfig;
        }

        public MainConfig getMainConfig() {
            return mMainConfig;
        }
    }
}
