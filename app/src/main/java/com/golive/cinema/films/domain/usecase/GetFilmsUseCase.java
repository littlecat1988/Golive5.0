package com.golive.cinema.films.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.films.FilmsFilterType;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Film;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/7/8.
 */

public class GetFilmsUseCase
        extends UseCase<GetFilmsUseCase.RequestValues, GetFilmsUseCase.ResponseValue> {
    //private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final FilmsDataSource mDataSource;

    public GetFilmsUseCase(@NonNull FilmsDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        if (requestValues.isForceUpdate()) {
            mDataSource.refreshFilms();
        }

        //SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        //String now = dateFormat.format(new Date());
        //String tags = "000";
        //String filmtype = "000";
        //String playtype = "2";
        //String lang = "";
        //String limit = "0";
        //String start = "0";
        //String type = "001";

        //return mDataSource.getFilms(tags, filmtype, playtype, limit, start, lang, now, type)
        return mDataSource.getFilms()
                .map(new Func1<List<Film>, ResponseValue>() {
                    @Override
                    public ResponseValue call(List<Film> films) {
                        //List<Task> tasksFiltered = taskFilter.filter(tasks);
                        return new ResponseValue(films);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final boolean mForceUpdate;
        private final FilmsFilterType mCurrentFiltering;

        public RequestValues(boolean mForceUpdate, FilmsFilterType mCurrentFiltering) {
            this.mForceUpdate = mForceUpdate;
            this.mCurrentFiltering = mCurrentFiltering;
        }

        public FilmsFilterType getCurrentFiltering() {
            return mCurrentFiltering;
        }

        public boolean isForceUpdate() {
            return mForceUpdate;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final List<Film> mFilmList;

        public ResponseValue(List<Film> mFilmList) {
            this.mFilmList = mFilmList;
        }

        public List<Film> getFilmList() {
            return mFilmList;
        }
    }
}
