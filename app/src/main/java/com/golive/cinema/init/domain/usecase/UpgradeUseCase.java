package com.golive.cinema.init.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Upgrade;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/2.
 */

public class UpgradeUseCase extends
        UseCase<UpgradeUseCase.RequestValues, UpgradeUseCase.ResponseValue> {

    private final ServerInitDataSource mServerInitDataSource;

    public UpgradeUseCase(ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mServerInitDataSource = checkNotNull(serverInitDataSource,
                "ServerInitDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mServerInitDataSource.upgrade(requestValues.getVersionCode(),
                requestValues.getVersionName())
                .map(new Func1<Upgrade, ResponseValue>() {
                    @Override
                    public ResponseValue call(Upgrade upgrade) {
                        return new ResponseValue(upgrade);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {

        private final String versionCode;
        private final String versionName;

        public RequestValues(String versionCode, String versionName) {
            this.versionCode = versionCode;
            this.versionName = versionName;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public String getVersionName() {
            return versionName;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final Upgrade upgrade;

        public ResponseValue(Upgrade upgrade) {
            this.upgrade = upgrade;
        }

        public Upgrade getUpgrade() {
            return upgrade;
        }
    }

}
