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

public class CheckDownloadFinishUseCase extends
        UseCase<CheckDownloadFinishUseCase.RequestValues, CheckDownloadFinishUseCase
                .ResponseValue> {

    private final DownloadDataSource mDownloadDataSource;

    public CheckDownloadFinishUseCase(
            @NonNull DownloadDataSource downloadDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDownloadDataSource = checkNotNull(downloadDataSource,
                "DownloadDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mDownloadDataSource.isDownloadFinished(requestValues.getFilmId(),
                requestValues.getMediaId())
                .map(new Func1<Boolean, ResponseValue>() {
                    @Override
                    public ResponseValue call(Boolean success) {
                        return new ResponseValue(success);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String filmId;
        private final String mediaId;

        public RequestValues(String filmId, String mediaId) {
            this.filmId = filmId;
            this.mediaId = mediaId;
        }

        public String getFilmId() {
            return filmId;
        }

        public String getMediaId() {
            return mediaId;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final boolean mIsFinished;

        public ResponseValue(boolean success) {
            mIsFinished = success;
        }

        public boolean isFinished() {
            return mIsFinished;
        }
    }
}
