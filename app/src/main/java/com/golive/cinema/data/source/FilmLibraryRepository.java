package com.golive.cinema.data.source;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Administrator on 2016/11/22.
 */

public class FilmLibraryRepository implements FilmLibraryDataSource {

    private static FilmLibraryRepository INSTANCE = null;

    private final FilmLibraryDataSource mRemoteDataSource, mLocalDataSource;

    private FilmLibTabResponse mCachedFilmLibTabResponse;
    private final Map<String, FilmLibListResponse> mCachedFilmLibListTabMap;
    private boolean mIsCacheDirty;
    private Observable<FilmLibTabResponse> mGetFilmLibTabOsb;
    private final Map<String, Observable<FilmLibListResponse>> mFilmLibListObsMap;

    private FilmLibraryRepository(@NonNull FilmLibraryDataSource remoteDataSource,
            @NonNull FilmLibraryDataSource localDataSource) {
        mRemoteDataSource = checkNotNull(remoteDataSource);
        mLocalDataSource = checkNotNull(localDataSource);
        mCachedFilmLibListTabMap = new ConcurrentHashMap<>();
        mFilmLibListObsMap = new ConcurrentHashMap<>();
    }

    public static FilmLibraryRepository getInstance(FilmLibraryDataSource remoteDataSource,
            FilmLibraryDataSource localDataSource) {
        if (null == INSTANCE) {
            synchronized (FilmLibraryRepository.class) {
                if (null == INSTANCE) {
                    INSTANCE = new FilmLibraryRepository(remoteDataSource, localDataSource);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public synchronized Observable<FilmLibTabResponse> getFilmLibTab() {
        if (!isCacheDirty() && mCachedFilmLibTabResponse != null) {
            return Observable.just(mCachedFilmLibTabResponse);
        }

        if (null == getGetFilmLibTabOsb()) {
            Observable<FilmLibTabResponse> observable = mRemoteDataSource.getFilmLibTab()
                    .doOnNext(new Action1<FilmLibTabResponse>() {
                        @Override
                        public void call(FilmLibTabResponse filmLibTabResponse) {
                            mCachedFilmLibTabResponse = filmLibTabResponse;
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            setCacheDirty(false);
                        }
                    })
                    // cache 1
                    .replay(1)
                    //
                    .refCount();
            setGetFilmLibTabOsb(observable);
        }

        return getGetFilmLibTabOsb();
    }

    @Override
    public synchronized Observable<FilmLibListResponse> getFilmLibList(@NonNull final String tabId,
            String encryptionType) {
        FilmLibListResponse response = null;
        if (!isCacheDirty() && (response = getCachedFilmLibList(tabId)) != null) {
            return Observable.just(response);
        }

        Observable<FilmLibListResponse> observable = mFilmLibListObsMap.get(tabId);
        if (null == observable) {
            observable = mRemoteDataSource.getFilmLibList(tabId, encryptionType)
                    .doOnNext(new Action1<FilmLibListResponse>() {
                        @Override
                        public void call(FilmLibListResponse filmLibListResponse) {
                            if (mCachedFilmLibListTabMap != null && filmLibListResponse != null
                                    && filmLibListResponse.isOk()) {
                                // cache film lib list
                                mCachedFilmLibListTabMap.put(tabId, filmLibListResponse);
                            }
                        }
                    })
                    // cache 1
                    .replay(1)
                    //
                    .refCount();
            mFilmLibListObsMap.put(tabId, observable);
        }
        return observable;
    }

    @Override
    public void refreshFilmLibTab() {
        setCacheDirty(true);
        if (mCachedFilmLibListTabMap != null) {
            mCachedFilmLibListTabMap.clear();
        }
    }

    @Override
    public void refreshFilmLibList(String tabId) {
        if (mCachedFilmLibListTabMap != null) {
            mCachedFilmLibListTabMap.remove(tabId);
        }
    }

    private FilmLibListResponse getCachedFilmLibList(@NonNull String tabId) {
        if (mCachedFilmLibListTabMap != null) {
            return mCachedFilmLibListTabMap.get(tabId);
        }
        return null;
    }

    private synchronized boolean isCacheDirty() {
        return mIsCacheDirty;
    }

    private synchronized void setCacheDirty(boolean cacheDirty) {
        mIsCacheDirty = cacheDirty;
    }

    private synchronized Observable<FilmLibTabResponse> getGetFilmLibTabOsb() {
        return mGetFilmLibTabOsb;
    }

    private synchronized void setGetFilmLibTabOsb(Observable<FilmLibTabResponse> getFilmLibTabOsb) {
        mGetFilmLibTabOsb = getFilmLibTabOsb;
    }
}
