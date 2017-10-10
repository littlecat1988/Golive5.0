package com.golive.cinema.download.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.initialjie.download.aidl.DownloadTaskInfo;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class GetDownloadTaskInfoUseCase extends
        UseCase<GetDownloadTaskInfoUseCase.RequestValues, GetDownloadTaskInfoUseCase
                .ResponseValue> {

    private final DownloadDataSource mDownloadDataSource;

    public GetDownloadTaskInfoUseCase(
            @NonNull DownloadDataSource downloadDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDownloadDataSource = checkNotNull(downloadDataSource,
                "DownloadDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mDownloadDataSource.getDownloadTaskInfo(requestValues.getFilmId(),
                requestValues.getMediaId())
                .map(new Func1<DownloadTaskInfo, ResponseValue>() {
                    @Override
                    public ResponseValue call(DownloadTaskInfo downloadTaskInfo) {
                        return new ResponseValue(downloadTaskInfo);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
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

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final DownloadTaskInfo mDownloadTaskInfo;

        public ResponseValue(DownloadTaskInfo downloadTaskInfo) {
            mDownloadTaskInfo = downloadTaskInfo;
        }

        public DownloadTaskInfo getDownloadTaskInfo() {
            return mDownloadTaskInfo;
        }
    }
}
