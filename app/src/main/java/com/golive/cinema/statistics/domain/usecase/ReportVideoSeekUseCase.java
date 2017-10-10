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

public class ReportVideoSeekUseCase extends
        UseCase<ReportVideoSeekUseCase.RequestValues, ReportVideoSeekUseCase
                .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportVideoSeekUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportVideoSeek(requestValues.filmId, requestValues.filmName,
                requestValues.filmType, requestValues.watchType, requestValues.definition,
                requestValues.playProgress, requestValues.toPosition, requestValues.toType,
                requestValues.orderSerial, requestValues.valueType)
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
        private final String playProgress;
        private final String toPosition;
        private final String orderSerial;
        private final String valueType;
        private final String toType;

        public RequestValues(String filmId, String filmName, String definition, String filmType,
                String watchType, String playProgress, String toPosition, String toType,
                String orderSerial, String valueType) {
            this.filmId = filmId;
            this.filmName = filmName;
            this.definition = definition;
            this.filmType = filmType;
            this.watchType = watchType;
            this.playProgress = playProgress;
            this.toPosition = toPosition;
            this.orderSerial = orderSerial;
            this.valueType = valueType;
            this.toType = toType;
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
