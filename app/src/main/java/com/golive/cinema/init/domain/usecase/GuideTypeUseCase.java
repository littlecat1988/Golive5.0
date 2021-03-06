package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.GuideTypeInfo;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/12/12.
 */

public class GuideTypeUseCase extends
        UseCase<GuideTypeUseCase.RequestValues, GuideTypeUseCase.ResponseValue> {


    @NonNull
    private final ServerInitDataSource mServerInitDataSource;

    public GuideTypeUseCase(@NonNull ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mServerInitDataSource = serverInitDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mServerInitDataSource.queryGuideTypeInfo()
                .map(new Func1<GuideTypeInfo, ResponseValue>() {
                    @Override
                    public ResponseValue call(GuideTypeInfo guideTypeInfo) {
                        return new ResponseValue(guideTypeInfo);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final GuideTypeInfo response;

        public ResponseValue(GuideTypeInfo response) {
            this.response = response;
        }

        public GuideTypeInfo getResponse() {
            return response;
        }
    }
}
