package com.golive.cinema.statistics.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Location;
import com.initialjie.log.Logger;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class ReportAppStartUseCase extends
        UseCase<ReportAppStartUseCase.RequestValues, ReportAppStartUseCase
                .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;
    private final GetLocationUseCase mGetLocationUseCase;
    private final BaseSchedulerProvider mschedulerProvider;

    public ReportAppStartUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource,
            @NonNull GetLocationUseCase getLocationUseCase) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
        mGetLocationUseCase = checkNotNull(getLocationUseCase);
        mschedulerProvider = checkNotNull(schedulerProvider);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {

        Observable<Location> getLocationObs = mGetLocationUseCase.run(
                new GetLocationUseCase.RequestValues())
                .onErrorReturn(new Func1<Throwable, GetLocationUseCase.ResponseValue>() {
                    @Override
                    public GetLocationUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getLocationObs, onErrorReturn : ");
                        return null;
                    }
                })
                .map(new Func1<GetLocationUseCase.ResponseValue, Location>() {
                    @Override
                    public Location call(GetLocationUseCase.ResponseValue responseValue) {
                        if (responseValue != null) {
                            return responseValue.getLocation();
                        }
                        return null;
                    }
                });

        return getLocationObs
                .observeOn(mschedulerProvider.io())
                .concatMap(new Func1<Location, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(Location location) {
                        String province = null;
                        String city = null;
                        if (location != null) {
                            province = location.getProvince();
                            city = location.getCity();
                        }
                        if (StringUtils.isNullOrEmpty(province)) {
                            province = "";
                        }
                        if (StringUtils.isNullOrEmpty(city)) {
                            city = "";
                        }
                        String kdmVersion = requestValues.kdmVersion;
                        if (StringUtils.isNullOrEmpty(kdmVersion)) {
                            kdmVersion = "";
                        }
                        return mStatisticsDataSource.reportAppStart(requestValues.caller,
                                requestValues.destination, requestValues.userStatus,
                                province, city, requestValues.netType, requestValues.osVersion,
                                requestValues.versionCode, kdmVersion, requestValues.duration);
                    }
                })
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String caller;
        private final String destination;
        private final String netType;
        private final String osVersion;
        private final String versionCode;
        private final String filmId;
        private final String userStatus;
        private final String kdmVersion;
        private final String duration;

        public RequestValues(String caller, String destination, String netType, String osVersion,
                String versionCode, String filmId, String userStatus, String kdmVersion,
                String duration) {
            this.caller = caller;
            this.destination = destination;
            this.netType = netType;
            this.osVersion = osVersion;
            this.versionCode = versionCode;
            this.filmId = filmId;
            this.userStatus = userStatus;
            this.kdmVersion = kdmVersion;
            this.duration = duration;
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
