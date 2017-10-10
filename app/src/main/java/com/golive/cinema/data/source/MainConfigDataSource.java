package com.golive.cinema.data.source;

import com.golive.network.entity.MainConfig;

import rx.Observable;

/**
 * Main entry point for accessing main-config data.
 * <p>
 * Created by Wangzj on 2016/7/13.
 */

public interface MainConfigDataSource {

    /**
     * Get an {@link rx.Observable} which will emit a {@link MainConfig}.
     */
    Observable<MainConfig> getMainConfig();

    void saveMainConfig(MainConfig mainConfig);

    /**
     * Force to refresh the cached MainConfig data.
     */
    void refreshMainConfig();
}
