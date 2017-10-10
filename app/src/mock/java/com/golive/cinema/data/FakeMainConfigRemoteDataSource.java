package com.golive.cinema.data;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.network.entity.MainConfig;

import rx.Observable;

/**
 * Created by Wangzj on 2016/7/13.
 */

public class FakeMainConfigRemoteDataSource implements MainConfigDataSource {

    private MainConfig mMainConfig;

    private static FakeMainConfigRemoteDataSource INSTANCE;

    private FakeMainConfigRemoteDataSource() {
        mMainConfig = new MainConfig();
    }

    public static FakeMainConfigRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeMainConfigRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<MainConfig> getMainConfig() {
        return Observable.just(mMainConfig);
    }

    @Override
    public void saveMainConfig(MainConfig mainConfig) {

    }

    @Override
    public void refreshMainConfig() {

    }
}
