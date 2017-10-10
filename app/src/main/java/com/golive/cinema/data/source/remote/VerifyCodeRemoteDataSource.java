package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.VerifyCodeDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Response;
import com.golive.network.net.GoLiveRestApi;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/9.
 */

public class VerifyCodeRemoteDataSource implements VerifyCodeDataSource {

    private static VerifyCodeRemoteDataSource INSTANCE;
    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    private VerifyCodeRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        this.mGoLiveRestApi = goLiveRestApi;
        this.mMainConfigDataSource = mainConfigDataSource;
    }

    public static VerifyCodeRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        checkNotNull(goLiveRestApi);
        if (INSTANCE == null) {
            INSTANCE = new VerifyCodeRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Observable<Response> getVerifyCode(final String phone) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.getVerifyCode(mainConfig.getVerifycode(), phone);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<>());

    }
}
