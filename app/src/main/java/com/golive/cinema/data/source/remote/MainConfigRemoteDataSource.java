package com.golive.cinema.data.source.remote;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.Constants;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.MainConfig;
import com.golive.network.net.GoLiveRestApi;

import rx.Observable;

/**
 * Created by Wangzj on 2016/7/13.
 */

public class MainConfigRemoteDataSource implements MainConfigDataSource {

    private static MainConfigRemoteDataSource INSTANCE;
    private final GoLiveRestApi mGoLiveRestApi;

    private MainConfigRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi) {
        mGoLiveRestApi = goLiveRestApi;
    }

    public static MainConfigRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi) {
        checkNotNull(goLiveRestApi);
        if (INSTANCE == null) {
            INSTANCE = new MainConfigRemoteDataSource(goLiveRestApi);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<MainConfig> getMainConfig() {
//        final String REQUEST_URL =
//                "http://api3.test.golivetv
// .tv:8089/golivetvAPI2/api2/getMainConfig-getMainConfig.action";
//                "http://183.60.142.151:8063/goliveAPI/api2/getMainConfig-getMainConfig.action";
//                "http://211.99.241.12:5101/goliveAPI/api2/getMainConfig-getMainConfig.action";
//                "http://183.60.142.151:8064/goliveAPI3/api2/getMainConfig-getMainConfig.action";
        return mGoLiveRestApi.getMainConfig(Constants.APP_MAIN_CONFIG_URL)
                .flatMap(new RestApiErrorCheckFlatMap<MainConfig>());
    }

    @Override
    public void saveMainConfig(MainConfig mainConfig) {

    }

    @Override
    public void refreshMainConfig() {

    }
}
