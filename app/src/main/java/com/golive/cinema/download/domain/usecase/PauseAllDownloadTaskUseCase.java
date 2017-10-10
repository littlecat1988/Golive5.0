package com.golive.cinema.download.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class PauseAllDownloadTaskUseCase extends
        UseCase<PauseAllDownloadTaskUseCase.RequestValues, PauseAllDownloadTaskUseCase
                .ResponseValue> {

    private final DownloadDataSource mDownloadDataSource;

    public PauseAllDownloadTaskUseCase(
            @NonNull DownloadDataSource downloadDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDownloadDataSource = checkNotNull(downloadDataSource,
                "DownloadDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mDownloadDataSource.pauseAllDownloadTask()
                .map(new Func1<Void, ResponseValue>() {
                    @Override
                    public ResponseValue call(Void aVoid) {
                        return new ResponseValue(true);
                    }
                });
    }


    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final boolean mSuccess;

        public ResponseValue(boolean success) {
            mSuccess = success;
        }

        public boolean isSuccess() {
            return mSuccess;
        }
    }
}
