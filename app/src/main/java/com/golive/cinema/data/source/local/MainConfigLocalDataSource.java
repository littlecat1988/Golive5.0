package com.golive.cinema.data.source.local;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.network.entity.MainConfig;

import rx.Observable;

/**
 * Created by Wangzj on 2016/7/13.
 */

public class MainConfigLocalDataSource implements MainConfigDataSource {

    private static MainConfigLocalDataSource INSTANCE;

    private MainConfigLocalDataSource() {
    }

    public static MainConfigLocalDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainConfigLocalDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<MainConfig> getMainConfig() {
        return Observable.empty();
    }

    @Override
    public void saveMainConfig(MainConfig mainConfig) {

    }

    @Override
    public void refreshMainConfig() {

    }
}
