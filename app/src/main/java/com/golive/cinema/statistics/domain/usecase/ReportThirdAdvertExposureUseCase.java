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
 * Created by chgang on 2017/3/8.
 */

public class ReportThirdAdvertExposureUseCase extends
        UseCase<ReportThirdAdvertExposureUseCase.RequestValues, ReportThirdAdvertExposureUseCase
                .ResponseValue> {

    private final StatisticsDataSource mStatisticsDataSource;

    public ReportThirdAdvertExposureUseCase(
            @NonNull StatisticsDataSource statisticsDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mStatisticsDataSource.reportAdThirdExposure(requestValues.adType,
                requestValues.adCode, requestValues.materialCode,
                requestValues.showTime, requestValues.showType,
                requestValues.pkgName, requestValues.activityName)
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final int adType;
        private final String adCode;
        private final String materialCode;
        private final String showTime;
        private final String showType;
        private final String pkgName;
        private final String activityName;

        public RequestValues(int adType, String adCode, String materialCode,
                String showTime, String showType, String pkgName, String activityName) {
            this.adType = adType;
            this.adCode = adCode;
            this.materialCode = materialCode;
            this.showTime = showTime;
            this.showType = showType;
            this.pkgName = pkgName;
            this.activityName = activityName;
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
