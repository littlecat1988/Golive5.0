package com.golive.cinema.filmlibrary.dimain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmLibraryDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.FilmLibTabResponse;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/14.
 */

public class GetFilmLibTabUseCase extends
        UseCase<GetFilmLibTabUseCase.RequestValues, GetFilmLibTabUseCase.ResponseValue> {

    private final FilmLibraryDataSource mDataSource;

    public GetFilmLibTabUseCase(@NonNull FilmLibraryDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {

        return mDataSource.getFilmLibTab().map(new Func1<FilmLibTabResponse, ResponseValue>() {
            @Override
            public ResponseValue call(FilmLibTabResponse filmLibTabResponse) {
                return new ResponseValue(filmLibTabResponse);
            }
        });
    }

    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final FilmLibTabResponse response;

        public ResponseValue(FilmLibTabResponse response) {
            this.response = response;
        }

        public FilmLibTabResponse getResponse() {
            return response;
        }
    }
}
