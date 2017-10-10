package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.entity.FilmTopic;
import com.paypal.android.sdk.T;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/10/27.
 */

public class FilmTopicsRepository implements FilmTopicsDataSource {

    private static FilmTopicsRepository INSTANCE = null;

    private final FilmTopicsDataSource mRemoteDataSource;
    private final FilmTopicsDataSource mLocalRemoteDataSource;

    public static FilmTopicsRepository getInstance(
            @NonNull FilmTopicsDataSource remoteDataSource,
            @NonNull FilmTopicsDataSource localDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new FilmTopicsRepository(remoteDataSource, localDataSource);
        }
        return INSTANCE;
    }

    public FilmTopicsRepository(FilmTopicsDataSource remoteDataSource,
            FilmTopicsDataSource localRemoteDataSource) {
        mRemoteDataSource = remoteDataSource;
        mLocalRemoteDataSource = localRemoteDataSource;
    }

    /**
     * Used to force {@link #getInstance(FilmTopicsDataSource, FilmTopicsDataSource)} to create a
     * new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<FilmTopic> getFilmTopicDetail(@NonNull String filmTopicId) {
        return mRemoteDataSource.getFilmTopicDetail(filmTopicId);
    }

    @Override
    public Observable<List<FilmTopic>> getRecommendFilmTopics() {
        return mRemoteDataSource.getRecommendFilmTopics();
    }

    @Override
    public Observable<List<FilmTopic>> getOldFilmTopics() {
        return mRemoteDataSource.getOldFilmTopics();
    }
}
