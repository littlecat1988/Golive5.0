package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.DrainageInfo;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/12/12.
 */

public class ExitDrainageUseCase extends
        UseCase<ExitDrainageUseCase.RequestValues, ExitDrainageUseCase.ResponseValue> {

    @NonNull
    private final ServerInitDataSource mServerInitDataSource;

    public ExitDrainageUseCase(@NonNull ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mServerInitDataSource = serverInitDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mServerInitDataSource.queryDrainageInfo()
                .map(new Func1<DrainageInfo, ResponseValue>() {
                    @Override
                    public ResponseValue call(DrainageInfo drainageInfo) {
                        return new ResponseValue(drainageInfo);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final DrainageInfo response;

        public ResponseValue(DrainageInfo response) {
            this.response = response;
        }

        public DrainageInfo getResponse() {
            return response;
        }
    }


}
