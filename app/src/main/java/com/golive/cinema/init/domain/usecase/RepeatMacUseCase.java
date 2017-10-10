package com.golive.cinema.init.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.RepeatMac;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/3.
 */

public class RepeatMacUseCase extends
        UseCase<RepeatMacUseCase.RequestValues, RepeatMacUseCase.ResponseValue> {


    private final ServerInitDataSource mServerInitDataSource;

    public RepeatMacUseCase(ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mServerInitDataSource = checkNotNull(serverInitDataSource,
                "ServerInitDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {

        return mServerInitDataSource.queryRepeatMacStatus(requestValues.getPhone())
                .map(new Func1<RepeatMac, ResponseValue>() {
                    @Override
                    public ResponseValue call(RepeatMac repeatMac) {
                        return new ResponseValue(repeatMac);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String phone;

        public RequestValues(String phone) {
            this.phone = phone;
        }

        public String getPhone() {
            return phone;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final RepeatMac repeatMac;

        public ResponseValue(RepeatMac repeatMac) {
            this.repeatMac = repeatMac;
        }

        public RepeatMac getRepeatMac() {
            return repeatMac;
        }
    }
}
