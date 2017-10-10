package com.golive.cinema.http.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.HttpDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2017/2/22.
 */

public class HttpGetUseCase extends
        UseCase<HttpGetUseCase.RequestValues, HttpGetUseCase.ResponseValue> {

    private final HttpDataSource mHttpDataSource;

    public HttpGetUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull HttpDataSource httpDataSource) {
        super(schedulerProvider);
        mHttpDataSource = checkNotNull(httpDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mHttpDataSource.get(requestValues.mUrl)
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String mUrl;

        public RequestValues(String url) {
            mUrl = url;
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
