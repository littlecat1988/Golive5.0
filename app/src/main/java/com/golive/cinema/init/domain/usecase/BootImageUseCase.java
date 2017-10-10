package com.golive.cinema.init.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.BootImage;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/1.
 */

public class BootImageUseCase extends
        UseCase<BootImageUseCase.RequestValues, BootImageUseCase.ResponseValue> {

    private ServerInitDataSource mServerInitDataSource;

    public BootImageUseCase(@NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
    }

    public BootImageUseCase(@NonNull ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider baseSchedulerProvider) {
        this(baseSchedulerProvider);
        this.mServerInitDataSource = checkNotNull(serverInitDataSource,
                "ServerInitDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mServerInitDataSource.queryBootImage()
                .map(new Func1<BootImage, ResponseValue>() {
                    @Override
                    public ResponseValue call(BootImage bootImage) {
                        return new ResponseValue(bootImage);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final BootImage bootImage;

        public ResponseValue(BootImage bootImage) {
            this.bootImage = bootImage;
        }

        public BootImage getBootImage() {
            return bootImage;
        }
    }
}
