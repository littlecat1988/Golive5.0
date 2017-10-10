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

public class ReportHardwareInfoUseCase extends
        UseCase<ReportHardwareInfoUseCase.RequestValues, ReportHardwareInfoUseCase
                .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;

    public ReportHardwareInfoUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mStatisticsDataSource.reportHardwareInfo(requestValues.wirelessMac,
                requestValues.wireMac, requestValues.bluetoothMac, requestValues.sn,
                requestValues.cpuId, requestValues.deviceId, requestValues.deviceName,
                requestValues.deviceType, requestValues.memory, requestValues.storage,
                requestValues.density, requestValues.resolution, requestValues.screenSize)
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        final String wirelessMac;
        final String wireMac;
        final String bluetoothMac;
        final String sn;
        final String cpuId;
        final String deviceId;
        final String deviceName;
        final String deviceType;
        final String memory;
        final String storage;
        final String density;
        final String resolution;
        final String screenSize;

        public RequestValues(String wirelessMac, String wireMac, String bluetoothMac, String sn,
                String cpuId, String deviceId, String deviceName, String deviceType,
                String memory, String storage, String density, String resolution,
                String screenSize) {
            this.wirelessMac = wirelessMac;
            this.wireMac = wireMac;
            this.bluetoothMac = bluetoothMac;
            this.sn = sn;
            this.cpuId = cpuId;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.deviceType = deviceType;
            this.memory = memory;
            this.storage = storage;
            this.density = density;
            this.resolution = resolution;
            this.screenSize = screenSize;
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
