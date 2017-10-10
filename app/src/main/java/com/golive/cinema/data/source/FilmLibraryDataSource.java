package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/11/22.
 */

public interface FilmLibraryDataSource {
    Observable<FilmLibTabResponse> getFilmLibTab();

    Observable<FilmLibListResponse> getFilmLibList(@NonNull String tabId, String encryptionType);

    void refreshFilmLibTab();

    void refreshFilmLibList(String tabId);
}
