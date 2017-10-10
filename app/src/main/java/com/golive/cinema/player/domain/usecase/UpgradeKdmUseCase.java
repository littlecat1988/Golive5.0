package com.golive.cinema.player.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.MainConfig;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2017/1/5.
 */

public class UpgradeKdmUseCase extends
        UseCase<UpgradeKdmUseCase.RequestValues, UpgradeKdmUseCase.ResponseValue> {

    private final KdmDataSource mKdmDataSource;
    private final MainConfigDataSource mMainConfigDataSource;

    public UpgradeKdmUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull KdmDataSource kdmDataSource,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        super(schedulerProvider);
        mKdmDataSource = checkNotNull(kdmDataSource);
        mMainConfigDataSource = checkNotNull(mainConfigDataSource);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<KDMResCode>>() {
                    @Override
                    public Observable<KDMResCode> call(MainConfig mainConfig) {
                        if (mainConfig.isKdmEnable()) {
                            mKdmDataSource.notifyKdmReady();
                        } else {
                            return Observable.just(null);
                        }

                        return mKdmDataSource.upgradeKdm(requestValues.getUpgradeUrl());
                    }
                })
                .map(new Func1<KDMResCode, ResponseValue>() {
                    @Override
                    public ResponseValue call(KDMResCode kdmResCode) {
                        return new ResponseValue(kdmResCode);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String mUpgradeUrl;

        public RequestValues(String upgradeUrl) {
            mUpgradeUrl = upgradeUrl;
        }

        public String getUpgradeUrl() {
            return mUpgradeUrl;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final KDMResCode mKDMResCode;

        public ResponseValue(KDMResCode KDMResCode) {
            mKDMResCode = KDMResCode;
        }

        public KDMResCode getKDMResCode() {
            return mKDMResCode;
        }
    }
}
