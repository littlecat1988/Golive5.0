package com.golive.cinema.data;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.response.RecommendResponse;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/5.
 */

public class FakeRecommendDataSource implements RecommendDataSource {
    private static FakeRecommendDataSource INSTANCE;

    private List<MovieRecommendFilm> mMovieRecommendFilms;

    public static FakeRecommendDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeRecommendDataSource();
        }
        return INSTANCE;
    }

    private FakeRecommendDataSource() {
        mMovieRecommendFilms = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MovieRecommendFilm film = new MovieRecommendFilm();
            film.setName("film " + i);
            film.setBigposter("http://img.lanrentuku.com/img/allimg/1605/1464490533194.jpg");
            mMovieRecommendFilms.add(film);
        }
    }

    @Override
    public Observable<RecommendResponse> getRecommendData(@NonNull int pageType,
            @NonNull String pageId, String encryptionType) {
        return Observable.empty();
    }

    @Override
    public Observable<List<MovieRecommendFilm>> getMovieRecommendData(@NonNull String filmId,
            @NonNull String filmKdm, String encryptionType) {
        return Observable.just(mMovieRecommendFilms);
    }
}
