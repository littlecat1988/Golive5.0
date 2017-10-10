package com.golive.cinema.statistics.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Response;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class ReportAppExceptionUseCase extends
        UseCase<ReportAppExceptionUseCase.RequestValues, ReportAppExceptionUseCase.ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportAppExceptionUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportAppException(requestValues.exceptionType,
                requestValues.exceptionCode, requestValues.exceptionMsg,
                requestValues.exceptionLevel, requestValues.partnerId)
                .map(new Func1<Response, ResponseValue>() {
                    @Override
                    public ResponseValue call(Response response) {
                        return new ResponseValue(response);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String exceptionType;
        private final String exceptionCode;
        private final String exceptionMsg;
        private final String exceptionLevel;
        private final String partnerId;

        public RequestValues(String exceptionType, String exceptionCode, String exceptionMsg,
                String exceptionLevel, String partnerId) {
            this.exceptionType = exceptionType;
            this.exceptionCode = exceptionCode;
            this.exceptionMsg = exceptionMsg;
            this.exceptionLevel = exceptionLevel;
            this.partnerId = partnerId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final Response mResponse;

        public ResponseValue(Response responseBody) {
            mResponse = responseBody;
        }

        public Response getResponse() {
            return mResponse;
        }
    }
}
