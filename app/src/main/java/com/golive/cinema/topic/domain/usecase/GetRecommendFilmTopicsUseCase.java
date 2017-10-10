package com.golive.cinema.topic.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmTopicsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.FilmTopic;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class GetRecommendFilmTopicsUseCase extends
        UseCase<GetRecommendFilmTopicsUseCase.RequestValues, GetRecommendFilmTopicsUseCase
                .ResponseValue> {

    private final FilmTopicsDataSource mTopicsDataSource;

    public GetRecommendFilmTopicsUseCase(@NonNull FilmTopicsDataSource filmTopicsDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mTopicsDataSource = checkNotNull(filmTopicsDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mTopicsDataSource.getRecommendFilmTopics()
                .map(new Func1<List<FilmTopic>, ResponseValue>() {
                    @Override
                    public ResponseValue call(List<FilmTopic> filmTopics) {
                        return new ResponseValue(filmTopics);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final List<FilmTopic> mFilmTopics;

        public ResponseValue(List<FilmTopic> film) {
            mFilmTopics = film;
        }

        public List<FilmTopic> getFilmTopics() {
            return mFilmTopics;
        }
    }
}
