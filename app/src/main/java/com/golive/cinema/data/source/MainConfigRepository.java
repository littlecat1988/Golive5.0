package com.golive.cinema.data.source;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.network.entity.MainConfig;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/7/13.
 */

public class MainConfigRepository implements MainConfigDataSource {

    private static MainConfigRepository INSTANCE = null;

    private final MainConfigDataSource mLocalDataSource;
    private final MainConfigDataSource mRemoteDataSource;
    private MainConfig mCachedMainConfig;
    private boolean mMainConfigCacheIsDirty;
    private Observable<MainConfig> mRemoteTask;

    private MainConfigRepository(MainConfigDataSource mRemoteDataSource,
            MainConfigDataSource mLocalDataSource) {
        this.mLocalDataSource = mLocalDataSource;
        this.mRemoteDataSource = mRemoteDataSource;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param remoteDataSource the backend data source
     * @param localDataSource  the device storage data source
     * @return the {@link MainConfigRepository} instance
     */
    public static MainConfigRepository getInstance(MainConfigDataSource remoteDataSource,
            MainConfigDataSource localDataSource) {
        if (null == INSTANCE) {
            synchronized (MainConfigRepository.class) {
                if (null == INSTANCE) {
                    INSTANCE = new MainConfigRepository(localDataSource, remoteDataSource);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(MainConfigDataSource, MainConfigDataSource)} to create a
     * new instance next time it's called.
     */
    public static void destroyInstance() {
        Logger.d("destroyInstance");
        if (INSTANCE != null) {
            INSTANCE.refreshMainConfig();
            INSTANCE = null;
        }
    }

    @Override
    public rx.Observable<MainConfig> getMainConfig() {
        final MainConfig mainConfig = getMainConfigFromCache();

        // Respond immediately with cache if available
        if (mainConfig != null) {
            return rx.Observable.just(mainConfig);
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        rx.Observable<MainConfig> localTask =
                mLocalDataSource.getMainConfig()
                        .doOnNext(new Action1<MainConfig>() {
                            @Override
                            public void call(MainConfig mainConfig) {
                                if (mainConfig != null) {
                                    setMainConfigCache(mainConfig);
                                }
                            }
                        });
        if (null == mRemoteTask) {
            synchronized (this) {
                if (null == mRemoteTask) {
                    mRemoteTask = mRemoteDataSource.getMainConfig()
                            .doOnNext(new Action1<MainConfig>() {
                                @Override
                                public void call(MainConfig mainConfig) {
                                    mLocalDataSource.saveMainConfig(mainConfig);
                                    mMainConfigCacheIsDirty = false;
                                    setMainConfigCache(mainConfig);
                                }
                            })
                            .replay(1)
                            .refCount();
                }
            }
        }

        return rx.Observable.concat(localTask, mRemoteTask).filter(
                new Func1<MainConfig, Boolean>() {
                    @Override
                    public Boolean call(MainConfig mainConfig) {
                        return mainConfig != null;
                    }
                }).first();
    }

    public void saveMainConfig(@NonNull MainConfig mainConfig) {
        checkNotNull(mainConfig);
        mLocalDataSource.saveMainConfig(mainConfig);
        mRemoteDataSource.saveMainConfig(mainConfig);

        // Do in memory cache update to keep the app UI up to date
        setMainConfigCache(mainConfig);
    }

    @Override
    public synchronized void refreshMainConfig() {
        mMainConfigCacheIsDirty = true;
        mRemoteTask = null;
    }

    @Nullable
    private synchronized MainConfig getMainConfigFromCache() {

        // cache
        if (!mMainConfigCacheIsDirty && mCachedMainConfig != null) {
            return mCachedMainConfig;
        }

        return null;
    }

    private synchronized void setMainConfigCache(MainConfig mainConfig) {
        mCachedMainConfig = mainConfig;
    }
}
