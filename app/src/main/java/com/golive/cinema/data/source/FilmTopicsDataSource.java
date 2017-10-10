package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.entity.FilmTopic;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2017/5/25.
 */

public interface FilmTopicsDataSource {

    Observable<FilmTopic> getFilmTopicDetail(@NonNull String filmTopicId);

    Observable<List<FilmTopic>> getRecommendFilmTopics();

    Observable<List<FilmTopic>> getOldFilmTopics();

}
