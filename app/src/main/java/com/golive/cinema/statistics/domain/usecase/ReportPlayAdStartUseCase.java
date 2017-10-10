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

public class ReportPlayAdStartUseCase extends
        UseCase<ReportPlayAdStartUseCase.RequestValues,
                ReportPlayAdStartUseCase
                        .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportPlayAdStartUseCase(
            @NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportPlayAdStart(requestValues.adId, requestValues.adName,
                requestValues.adType, requestValues.adOwnerCode, requestValues.adOwnerName,
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
        private final String adOwnerCode;
        private final String adOwnerName;
        private final String adDefinition;
        private final String adUrl;
        private final String adLocation;
        private final String filmId;
        private final String filmName;
        private final String filmPlayProgress;

        public RequestValues(String adId, String adName, String adType, String adOwnerCode,
                String adOwnerName, String adDefinition, String adUrl, String adLocation,
                String filmId, String filmName, String filmPlayProgress) {
            this.adId = adId;
            this.adName = adName;
            this.adType = adType;
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
