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

public class ReportVideoStreamSwitchUseCase extends
        UseCase<ReportVideoStreamSwitchUseCase.RequestValues,
                ReportVideoStreamSwitchUseCase
                        .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportVideoStreamSwitchUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportVideoStreamSwitch(requestValues.filmId,
                requestValues.filmName, requestValues.filmType, requestValues.watchType,
                requestValues.definition, requestValues.toDefinition, requestValues.watchDuration,
                requestValues.playDuration, requestValues.totalDuration, requestValues.playProgress,
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
        private final String filmType;
        private final String watchType;
        private final String definition;
        private final String toDefinition;
        private final String watchDuration;
        private final String playDuration;
        private final String totalDuration;
        private final String playProgress;
        private final String orderSerial;
        private final String valueType;

        public RequestValues(String filmId, String filmName, String filmType, String watchType,
                String definition, String toDefinition, String watchDuration, String playDuration,
                String totalDuration, String playProgress, String orderSerial, String valueType) {
            this.filmId = filmId;
            this.filmName = filmName;
            this.filmType = filmType;
            this.watchType = watchType;
            this.definition = definition;
            this.toDefinition = toDefinition;
            this.watchDuration = watchDuration;
            this.playDuration = playDuration;
            this.totalDuration = totalDuration;
            this.playProgress = playProgress;
            this.orderSerial = orderSerial;
            this.valueType = valueType;
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
