package com.golive.cinema.filmdetail.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Film;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class GetFilmDetailUseCase extends
        UseCase<GetFilmDetailUseCase.RequestValues, GetFilmDetailUseCase.ResponseValue> {

    private final FilmsDataSource mFilmsDataSource;

    public GetFilmDetailUseCase(@NonNull FilmsDataSource filmsDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mFilmsDataSource = checkNotNull(filmsDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mFilmsDataSource
                .getFilmDetail(requestValues.getFilmId())
                .map(new Func1<Film, ResponseValue>() {
                    @Override
                    public ResponseValue call(Film film) {
                        return new ResponseValue(film);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mFilmId;

        public RequestValues(@NonNull String filmId) {
            mFilmId = checkNotNull(filmId, "filmId cannot be null!");
        }

        public String getFilmId() {
            return mFilmId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Film mFilm;

        public ResponseValue(@NonNull Film film) {
            mFilm = checkNotNull(film, "film cannot be null!");
        }

        public Film getFilm() {
            return mFilm;
        }
    }
}
