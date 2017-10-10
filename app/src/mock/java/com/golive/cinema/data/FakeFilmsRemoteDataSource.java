/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.golive.cinema.data;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.network.entity.Film;
import com.golive.network.entity.Media;
import com.golive.network.response.FilmListResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeFilmsRemoteDataSource implements FilmsDataSource {
    private static final String TAG = FakeFilmsRemoteDataSource.class.getSimpleName();
    private static FakeFilmsRemoteDataSource INSTANCE;

    private static final Map<String, Film> FILMS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeFilmsRemoteDataSource() {
        Film film;
        List<Media> medias = new ArrayList<>();
        Media media;

        media = new Media();
        String media_id = "media_id_1";
        media.setId(media_id);
        media.setName(media_id);
        media.setRankname("高清");
        media.setType(Media.MEDIA_TYPE_ONLINE);

        // Kdm online
        media.setUrl("http://cdn-kdm.golivetv.tv/KDM/Music_4M");
        media.setName("urn:uuid:a257a2ce-20fd-4c9d-89df-f1bb644bf6bc");
        media.setEncryption(Media.TYPE_KDM);

//        media.setUrl("http://cdn-3.golivetv.tv/NoCrypt/2015-09-22/shiergongmin/shiergongmin2400
// .mp4");
//        media.setEncryption(Media.TYPE_NO_ENCRYPT);
        medias.add(media);

        media = new Media();
        media_id = "media_id_2";
        media.setId(media_id);
        media.setName(media_id);
        media.setRankname("标清");
        media.setType(Media.MEDIA_TYPE_DOWNLOAD);
        media.setUrl(
                "http://kdm01.cp44.ott.cibntv"
                        + ".net/KDM/CurseOfDemon_4M/CurseOfDemon_4M-4D2E4AAC96CA12CA106DAA8C141C528A.xml");
//                "http://cdn-kdm.golivetv
// .tv/KDM/CJ_20150707/CJ_CRIFST_2K_13min_Encrypt-11A7BAABDD06D41203B4B70FAFE30530.xml");
        media.setEncryption(Media.TYPE_NO_ENCRYPT);
        medias.add(media);

        String price = "5";
        String vipPrice = "1";
        for (int i = 0; i < 10; i++) {
            film = new Film();
            String id = String.valueOf(i);
            film.setReleaseid(id);
            film.setName("test film " + id);
            film.setIntroduction("introduce introduce introduce introduce");
            film.setPrice(price);
            film.setOnlineprice(price);
            film.setDownloadprice(price);
            film.setVipprice(vipPrice);
            film.setViponlineprice(vipPrice);
            film.setVipdownloadprice(vipPrice);
            film.setMedias(medias);
            FILMS_SERVICE_DATA.put(id, film);
        }
    }

    public static FakeFilmsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeFilmsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<Film> getFilm(@NonNull String id) {
        Film task = FILMS_SERVICE_DATA.get(id);
        return Observable.just(task);
    }

    @Override
    public Observable<Film> getFilmDetail(@NonNull String id) {
        return getFilm(id);
    }

    @Override
    public Observable<List<Film>> getFilms() {
        Collection<Film> values = FILMS_SERVICE_DATA.values();
//        com.initialjie.log.Logger.d("getFilms, values : " + values);
        return Observable.from(values).toList();
    }

    @Override
    public void refreshFilms() {
        // Not required because the {@link FilmsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void refreshFilmDetail(@NonNull String filmId) {
        // Not required
    }

    @Override
    public void saveFilm(@NonNull Film film) {
        // Not required because the {@link FilmsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
        FILMS_SERVICE_DATA.put(film.getReleaseid(), film);
    }

    @Override
    public void saveFilmDetail(@NonNull Film film) {
        saveFilm(film);
    }

    @Override
    public void deleteFilm(@NonNull String filmId) {
        FILMS_SERVICE_DATA.remove(filmId);
    }

    @Override
    public void deleteFilmDetail(@NonNull String filmId) {
        deleteFilm(filmId);
    }

    @Override
    public void deleteAllFilms() {
        FILMS_SERVICE_DATA.clear();
    }

    @Override
    public Observable<FilmListResponse> getFilmList(String encryptionType) {
        return Observable.empty();
    }
}
