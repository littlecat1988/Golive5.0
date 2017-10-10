package com.golive.cinema.download.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/29.
 */

public class DownloadFileUseCase extends
        UseCase<DownloadFileUseCase.RequestValues, DownloadFileUseCase.ResponseValue> {

    private final DownloadDataSource mDownloadDataSource;

    public DownloadFileUseCase(@NonNull DownloadDataSource downloadDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDownloadDataSource = checkNotNull(downloadDataSource,
                "DownloadDataSource can not be Null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mDownloadDataSource.downloadFile(requestValues.getStartPos(), requestValues.getUrl())
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final long mStartPos;
        private final String mUrl;

        public RequestValues(String url) {
            this(0, url);
        }

        public RequestValues(long startPos, String url) {
            mStartPos = startPos;
            mUrl = url;
        }

        private long getStartPos() {
            return mStartPos;
        }

        private String getUrl() {
            return mUrl;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final ResponseBody mResponseBody;

        public ResponseValue(ResponseBody responseBody) {
            mResponseBody = responseBody;
        }

        public ResponseBody getResponseBody() {
            return mResponseBody;
        }
    }
}
