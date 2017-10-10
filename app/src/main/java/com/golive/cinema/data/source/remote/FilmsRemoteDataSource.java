package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.Film;
import com.golive.network.entity.FilmList;
import com.golive.network.entity.MainConfig;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.response.FilmListResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;


/**
 * Created by Wangzj on 2016/7/8.
 */

public class FilmsRemoteDataSource implements FilmsDataSource {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static FilmsRemoteDataSource INSTANCE;

    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    private FilmsRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }

    public static FilmsRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        checkNotNull(goLiveRestApi);
        checkNotNull(mainConfigDataSource);
        if (INSTANCE == null) {
            INSTANCE = new FilmsRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<Film> getFilm(@NonNull final String id) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Film>>() {
                    @Override
                    public Observable<Film> call(MainConfig mainConfig) {
                        String url = mainConfig.getMovieDetail();
                        return mGoLiveRestApi.getFilmDetail(url, id);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Film>());
    }

    @Override
    public Observable<Film> getFilmDetail(@NonNull String id) {
        return getFilm(id);
    }

    @Override
    public Observable<List<Film>> getFilms() {
        final String tags = "000";
        final String filmtype = "000";
        final String playtype = "2";
        final String lang = "";
        final String limit = "0";
        final String start = "0";
        final String type = "001";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String now = dateFormat.format(new Date());

        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<FilmList>>() {
                    @Override
                    public Observable<FilmList> call(MainConfig mainConfig) {
                        String url = mainConfig.getSynchronousmoivelist();
                        url =
                                "http://api3.golivetv.tv/golivetvAPI2/api2/synchronous-movieList"
                                        + ".action";
                        return mGoLiveRestApi.getFilmList(url, tags, filmtype, playtype, limit,
                                start, lang, now, type);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<FilmList>())
                .map(new Func1<FilmList, List<Film>>() {
                    @Override
                    public List<Film> call(FilmList filmList) {
//                        Logger.d("getFilms, filmList : "
//                                + filmList
//                                + ", filmList.getFilmList() : "
//                                + filmList.getFilmList());
                        return filmList.getFilmList();
                    }
                });
    }

    @Override
    public Observable<FilmListResponse> getFilmList(final String encryptionType) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<FilmListResponse>>() {
                    @Override
                    public Observable<FilmListResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetSyncRecommend();
                        //url = "http://183.60.142.151:8063/goliveAPI//api5/movie_syncRecommend
                        // .action";
                        return mGoLiveRestApi.getFilmList(url, encryptionType);
                    }
                });
    }

    @Override
    public void refreshFilms() {
        // Not required
    }

    @Override
    public void refreshFilmDetail(@NonNull String filmId) {
        // Not required
    }

    @Override
    public void saveFilm(@NonNull Film film) {
        // Not required
    }

    @Override
    public void saveFilmDetail(@NonNull Film film) {
        // Not required
    }

    @Override
    public void deleteFilm(@NonNull String filmId) {
        // Not required
    }

    @Override
    public void deleteFilmDetail(@NonNull String filmId) {
        // Not required
    }

    @Override
    public void deleteAllFilms() {
        // Not required
    }
}
