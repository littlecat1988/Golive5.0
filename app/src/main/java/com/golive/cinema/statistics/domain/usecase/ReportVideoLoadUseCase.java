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

public class ReportVideoLoadUseCase extends
        UseCase<ReportVideoLoadUseCase.RequestValues, ReportVideoLoadUseCase
                .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportVideoLoadUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportVideoLoad(requestValues.filmId, requestValues.filmName,
                requestValues.filmType, requestValues.watchType, requestValues.mediaUrl,
                requestValues.mediaIp, requestValues.definition, requestValues.bufferDuration,
                requestValues.serial_no, requestValues.valueType, requestValues.speed)
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
        private final String mediaUrl;
        private final String mediaIp;
        private final String definition;
        private final String bufferDuration;
        private final String serial_no;
        private final String valueType;
        private final String speed;

        public RequestValues(String filmId, String filmName, String filmType, String watchType,
                String mediaUrl, String mediaIp, String definition, String bufferDuration,
                String serial_no, String valueType, String speed) {
            this.filmId = filmId;
            this.filmName = filmName;
            this.filmType = filmType;
            this.watchType = watchType;
            this.mediaUrl = mediaUrl;
            this.mediaIp = mediaIp;
            this.definition = definition;
            this.bufferDuration = bufferDuration;
            this.serial_no = serial_no;
            this.valueType = valueType;
            this.speed = speed;
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
