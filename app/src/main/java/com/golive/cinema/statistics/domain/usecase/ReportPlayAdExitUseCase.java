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

public class ReportPlayAdExitUseCase extends
        UseCase<ReportPlayAdExitUseCase.RequestValues,
                ReportPlayAdExitUseCase
                        .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportPlayAdExitUseCase(
            @NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportPlayAdExit(requestValues.adId, requestValues.adName,
                requestValues.adType, requestValues.bufferDuration, requestValues.adDuration,
                requestValues.adProgress, requestValues.adOwnerCode, requestValues.adOwnerName,
                requestValues.adDefinition, requestValues.adUrl, requestValues.adLocation,
                requestValues.filmId, requestValues.filmName, requestValues.filmPlayProgress)
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String adId;
        private final String adName;
        private final String adType;
        private final String bufferDuration;
        private final String adDuration;
        private final String adProgress;
        private final String adOwnerCode;
        private final String adOwnerName;
        private final String adDefinition;
        private final String adUrl;
        private final String adLocation;
        private final String filmId;
        private final String filmName;
        private final String filmPlayProgress;

        public RequestValues(String adId, String adName, String adType, String bufferDuration,
                String adDuration, String adProgress, String adOwnerCode, String adOwnerName,
                String adDefinition, String adUrl, String adLocation, String filmId,
                String filmName, String filmPlayProgress) {
            this.adId = adId;
            this.adName = adName;
            this.adType = adType;
            this.bufferDuration = bufferDuration;
            this.adDuration = adDuration;
            this.adProgress = adProgress;
            this.adOwnerCode = adOwnerCode;
            this.adOwnerName = adOwnerName;
            this.adDefinition = adDefinition;
            this.adUrl = adUrl;
            this.adLocation = adLocation;
            this.filmId = filmId;
            this.filmName = filmName;
            this.filmPlayProgress = filmPlayProgress;
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
