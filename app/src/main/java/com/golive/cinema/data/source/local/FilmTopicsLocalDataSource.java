package com.golive.cinema.data.source.local;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmTopicsDataSource;
import com.golive.network.entity.FilmTopic;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/1.
 */

public class FilmTopicsLocalDataSource implements FilmTopicsDataSource {

    private static FilmTopicsLocalDataSource INSTANCE = null;

    public static FilmTopicsLocalDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FilmTopicsLocalDataSource();
        }
        return INSTANCE;
    }

    private FilmTopicsLocalDataSource() {
    }


    @Override
    public Observable<FilmTopic> getFilmTopicDetail(@NonNull String filmTopicId) {
        return Observable.empty();
    }

    @Override
    public Observable<List<FilmTopic>> getRecommendFilmTopics() {
        return Observable.empty();
    }

    @Override
    public Observable<List<FilmTopic>> getOldFilmTopics() {
        return Observable.empty();
    }
}
