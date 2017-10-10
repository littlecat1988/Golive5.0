package com.golive.cinema.statistics.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class ReportVideoStartUseCase extends
        UseCase<ReportVideoStartUseCase.RequestValues, ReportVideoStartUseCase
                .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportVideoStartUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportVideoStart(requestValues.filmId, requestValues.filmName,
                requestValues.definition, requestValues.filmType, requestValues.watchType,
                requestValues.orderSerial, requestValues.valueType, requestValues.caller)
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String filmId;
        private final String filmName;
        private final String definition;
        private final String filmType;
        private final String watchType;
        private final String orderSerial;
        private final String valueType;
        private final String caller;

        public RequestValues(String filmId, String filmName, String definition, String filmType,
                String watchType, String orderSerial, String valueType, String caller) {
            this.filmId = filmId;
            this.filmName = filmName;
            this.definition = definition;
            this.filmType = filmType;
            this.watchType = watchType;
            this.orderSerial = orderSerial;
            this.valueType = valueType;
            this.caller = caller;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final ResponseBody mResponseBody;

        public ResponseValue(ResponseBody responseBody) {
            mResponseBody = responseBody;
        }

        public ResponseBody getResponseBody() {
            return mResponseBody;
        }
    }
}
