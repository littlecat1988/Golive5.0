package com.golive.cinema.topic.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmTopicsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.FilmTopic;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class GetFilmTopicDetailUseCase extends
        UseCase<GetFilmTopicDetailUseCase.RequestValues, GetFilmTopicDetailUseCase.ResponseValue> {

    private final FilmTopicsDataSource mTopicsDataSource;

    public GetFilmTopicDetailUseCase(@NonNull FilmTopicsDataSource filmTopicsDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mTopicsDataSource = checkNotNull(filmTopicsDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mTopicsDataSource.getFilmTopicDetail(requestValues.mFilmTopicId)
                .map(new Func1<FilmTopic, ResponseValue>() {
                    @Override
                    public ResponseValue call(FilmTopic filmTopic) {
                        return new ResponseValue(filmTopic);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        public final String mFilmTopicId;

        public RequestValues(String filmTopicId) {
            mFilmTopicId = filmTopicId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final FilmTopic mFilmTopic;

        public ResponseValue(FilmTopic filmTopic) {
            mFilmTopic = filmTopic;
        }

        public FilmTopic getFilmTopic() {
            return mFilmTopic;
        }
    }
}
