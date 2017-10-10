package com.golive.cinema.data.source.local;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmLibraryDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/11/22.
 */

public class FilmLibraryLocalDataSource implements FilmLibraryDataSource {
    private static FilmLibraryLocalDataSource INSTANCE;

    private FilmLibraryLocalDataSource(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
    }

    public static FilmLibraryLocalDataSource getInstance(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new FilmLibraryLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    @Override
    public Observable<FilmLibTabResponse> getFilmLibTab() {
        return Observable.empty();
    }

    @Override
    public Observable<FilmLibListResponse> getFilmLibList(@NonNull String tabId,
            String encryptionType) {
        return Observable.empty();
    }

    @Override
    public void refreshFilmLibTab() {

    }

    @Override
    public void refreshFilmLibList(String tabId) {

    }
}
