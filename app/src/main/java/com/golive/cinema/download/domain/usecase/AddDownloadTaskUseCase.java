package com.golive.cinema.download.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import java.util.Arrays;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class AddDownloadTaskUseCase extends
        UseCase<AddDownloadTaskUseCase.RequestValues, AddDownloadTaskUseCase
                .ResponseValue> {

    private final DownloadDataSource mDownloadDataSource;

    public AddDownloadTaskUseCase(
            @NonNull DownloadDataSource downloadDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDownloadDataSource = checkNotNull(downloadDataSource,
                "DownloadDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mDownloadDataSource.addDownloadTask(requestValues.getFilmId(),
                requestValues.getMediaId(), requestValues.getSavePath(), requestValues.getReserve(),
                requestValues.getUrls(), requestValues.getFileSizes())
                .map(new Func1<Boolean, ResponseValue>() {
                    @Override
                    public ResponseValue call(Boolean aBoolean) {
                        return new ResponseValue(aBoolean);
                    }
                });
    }


    public static class RequestValues implements UseCase.RequestValues {
        private final String filmId;
        private final String mediaId;
        private final String savePath;
        private final String reserve;
        private final String[] mUrls;
        private final long[] mFileSizes;

        public RequestValues(String filmId, String mediaId, String savePath, String reserve,
                String[] urls, long[] fileSizes) {
            this.filmId = filmId;
            this.mediaId = mediaId;
            this.savePath = savePath;
            this.reserve = reserve;
            this.mUrls = new String[urls.length];
            System.arraycopy(urls, 0, mUrls, 0, urls.length);
            this.mFileSizes = Arrays.copyOf(fileSizes, fileSizes.length);
        }

        public String getFilmId() {
            return filmId;
        }

        public String getMediaId() {
            return mediaId;
        }

        public String getSavePath() {
            return savePath;
        }

        public String getReserve() {
            return reserve;
        }

        public String[] getUrls() {
            return mUrls;
        }

        public long[] getFileSizes() {
            return mFileSizes;
        }
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
