package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.ApplicationPageResponse;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/19.
 */

public class AppPageUseCase extends
        UseCase<AppPageUseCase.RequestValues, AppPageUseCase.ResponseValue> {

    @NonNull
    private final ServerInitDataSource mServerInitDataSource;

    public AppPageUseCase(@NonNull ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mServerInitDataSource = serverInitDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mServerInitDataSource.queryApplicationPageAction()
                .map(new Func1<ApplicationPageResponse, ResponseValue>() {
                    @Override
                    public ResponseValue call(ApplicationPageResponse applicationPageResponse) {
                        return new ResponseValue(applicationPageResponse);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final ApplicationPageResponse response;

        public ResponseValue(ApplicationPageResponse response) {
            this.response = response;
        }

        public ApplicationPageResponse getResponse() {
            return response;
        }
    }

}
