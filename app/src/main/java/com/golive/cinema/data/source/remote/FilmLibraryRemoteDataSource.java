package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmLibraryDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.MainConfig;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/22.
 */

public class FilmLibraryRemoteDataSource implements FilmLibraryDataSource {

    private static FilmLibraryRemoteDataSource INSTANCE;
    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    private FilmLibraryRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }

    public static FilmLibraryRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        checkNotNull(goLiveRestApi);
        checkNotNull(mainConfigDataSource);
        if (INSTANCE == null) {
            INSTANCE = new FilmLibraryRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Observable<FilmLibTabResponse> getFilmLibTab() {
        return mMainConfigDataSource.getMainConfig().flatMap(
                new Func1<MainConfig, Observable<FilmLibTabResponse>>() {
                    @Override
                    public Observable<FilmLibTabResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetFilmLibrary();
                        //url = "http://183.60.142.151:8063/goliveAPI//api5/movie_filmLibrary
                        // .action";
                        return mGoLiveRestApi.queryFilmLibTab(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<FilmLibTabResponse>());
    }

    @Override
    public Observable<FilmLibListResponse> getFilmLibList(@NonNull final String tabId,
            final String encryptionType) {
        return mMainConfigDataSource.getMainConfig().flatMap(
                new Func1<MainConfig, Observable<FilmLibListResponse>>() {
                    @Override
                    public Observable<FilmLibListResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetLibraryMovie();
                        //url = "http://183.60.142.151:8063/goliveAPI//api5/movie_libraryMovie
                        // .action";
                        return mGoLiveRestApi.queryFilmLibList(url, tabId, encryptionType);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<FilmLibListResponse>());
    }

    @Override
    public void refreshFilmLibTab() {

    }

    @Override
    public void refreshFilmLibList(String tabId) {

    }
}
