package com.golive.cinema.player.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.KDMServerVersion;
import com.golive.network.entity.MainConfig;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2017/1/5.
 */

public class GetKdmServerVersionUseCase extends
        UseCase<GetKdmServerVersionUseCase.RequestValues, GetKdmServerVersionUseCase
                .ResponseValue> {

    private final KdmDataSource mKdmDataSource;
    private final GetKdmVersionUseCase mGetKdmVersionUseCase;
    private final MainConfigDataSource mMainConfigDataSource;
    private final BaseSchedulerProvider mSchedulerProvider;

    public GetKdmServerVersionUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull KdmDataSource kdmDataSource,
            @NonNull GetKdmVersionUseCase getKdmVersionUseCase,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        super(schedulerProvider);
        mKdmDataSource = checkNotNull(kdmDataSource);
        mGetKdmVersionUseCase = checkNotNull(getKdmVersionUseCase);
        mMainConfigDataSource = checkNotNull(mainConfigDataSource);
        mSchedulerProvider = schedulerProvider;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return
                mMainConfigDataSource.getMainConfig()
                        .concatMap(new Func1<MainConfig, Observable<KDMServerVersion>>() {
                            @Override
                            public Observable<KDMServerVersion> call(MainConfig mainConfig) {
                                if (mainConfig.isKdmEnable()) {
                                    mKdmDataSource.notifyKdmReady();
                                } else {
                                    return Observable.just(null);
                                }
                                return getKDMServerVersionObservable();
                            }
                        })
                        .map(new Func1<KDMServerVersion, ResponseValue>() {
                            @Override
                            public ResponseValue call(KDMServerVersion kdmServerVersion) {
                                return new ResponseValue(kdmServerVersion);
                            }
                        });
    }

    private Observable<KDMServerVersion> getKDMServerVersionObservable() {
        return mGetKdmVersionUseCase.run(new GetKdmVersionUseCase.RequestValues())
                .observeOn(mSchedulerProvider.io())
                .concatMap(
                        new Func1<GetKdmVersionUseCase.ResponseValue,
                                Observable<KDMServerVersion>>() {
                            @Override
                            public Observable<KDMServerVersion> call(
                                    GetKdmVersionUseCase.ResponseValue responseValue) {
                                String version = null;
                                String platform = null;
                                KDMResCode kdmResCode = responseValue.getKDMResCode();
                                if (kdmResCode != null && kdmResCode.version != null) {
                                    version = kdmResCode.version.getVersion();
                                    platform = kdmResCode.version.getPlatform();
                                }
                                version = StringUtils.getDefaultStringIfEmpty(version);
                                platform = StringUtils.getDefaultStringIfEmpty(platform);
                                return mKdmDataSource.getKdmServerVersion(version, platform);
                            }
                        });
    }

    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final KDMServerVersion mKDMServerVersion;

        public ResponseValue(KDMServerVersion KDMServerVersion) {
            mKDMServerVersion = KDMServerVersion;
        }

        public KDMServerVersion getKDMServerVersion() {
            return mKDMServerVersion;
        }
    }
}
