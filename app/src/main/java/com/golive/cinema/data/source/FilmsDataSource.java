package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.entity.Film;
import com.golive.network.entity.FilmList;
import com.golive.network.response.FilmListResponse;

import java.util.List;

import rx.Observable;

/**
 * Main entry point for accessing films data.
 * <p>
 * Created by Wangzj on 2016/7/8.
 */
public interface FilmsDataSource {

    /**
     * Get an {@link rx.Observable} which will emit a {@link Film}.
     *
     * @param id The film id used to retrieve film data.
     */
    Observable<Film> getFilm(@NonNull final String id);

    /**
     * Get an {@link rx.Observable} which will emit a {@link Film}.
     *
     * @param id The film id used to retrieve film data.
     */
    Observable<Film> getFilmDetail(@NonNull final String id);

    /**
     * Get an {@link rx.Observable} which will emit a {@link FilmList}.
     */
    Observable<List<Film>> getFilms();
    //Observable<List<Film>> getFilms(@NonNull String tags, @NonNull String filmtype,
    //    @NonNull String playtype, @NonNull String limit, @NonNull String start, @NonNull String
    // lang,
    //    @NonNull String nowdate, @NonNull String type);

    /**
     * Force to refresh the cached film data.
     */
    void refreshFilms();

    void refreshFilmDetail(@NonNull String filmId);

    /**
     * Save the film.
     */
    void saveFilm(@NonNull Film film);

    /**
     * Save the film info.
     */
    void saveFilmDetail(@NonNull Film film);

    /**
     * Delete the film.
     */
    void deleteFilm(@NonNull String filmId);

    /**
     * Delete the film detail
     */
    void deleteFilmDetail(@NonNull String filmId);

    /**
     * Delete all films.
     */
    void deleteAllFilms();

    Observable<FilmListResponse> getFilmList(String encryptionType);

//    Observable<Login> getLogin(@NonNull String userid, @NonNull String pw, @NonNull String status,
//            @NonNull String branchtype,
//            @NonNull String kdmVersion, @NonNull String kdmPlatform);
//
//    /**
//     * Force to refresh the cached login data.
//     */
//    void refreshLogin();
}
