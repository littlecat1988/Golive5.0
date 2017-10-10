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

public class ReportEnterFilmDetailUseCase extends
        UseCase<ReportEnterFilmDetailUseCase.RequestValues,
                ReportEnterFilmDetailUseCase
                        .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportEnterFilmDetailUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportEnterFilmDetail(requestValues.filmId,
                requestValues.filmName, requestValues.filmStatus, requestValues.caller)
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
        private final String filmStatus;
        private final String caller;

        public RequestValues(String filmId, String filmName, String filmStatus, String caller) {
            this.filmId = filmId;
            this.filmName = filmName;
            this.filmStatus = filmStatus;
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
