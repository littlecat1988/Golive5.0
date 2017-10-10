package com.golive.cinema.player.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import rx.Observable;

/**
 * Created by Wangzj on 2017/1/5.
 */

public class NotifyKdmReadyUseCase extends
        UseCase<NotifyKdmReadyUseCase.RequestValues, NotifyKdmReadyUseCase.ResponseValue> {

    private final KdmDataSource mKdmDataSource;

    public NotifyKdmReadyUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull KdmDataSource kdmDataSource) {
        super(schedulerProvider);
        mKdmDataSource = checkNotNull(kdmDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        mKdmDataSource.notifyKdmReady();
        return Observable.just(new ResponseValue());
    }

    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {
    }
}
