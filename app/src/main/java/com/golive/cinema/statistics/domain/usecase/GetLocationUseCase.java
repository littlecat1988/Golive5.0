package com.golive.cinema.statistics.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Location;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class GetLocationUseCase extends UseCase<GetLocationUseCase.RequestValues, GetLocationUseCase
        .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public GetLocationUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.getLocation()
                .map(new Func1<Location, ResponseValue>() {
                    @Override
                    public ResponseValue call(Location location) {
                        return new ResponseValue(location);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final Location mLocation;

        public ResponseValue(Location location) {
            mLocation = location;
        }

        public Location getLocation() {
            return mLocation;
        }
    }
}
