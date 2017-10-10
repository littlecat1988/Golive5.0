package com.golive.cinema.data.source;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.LruCache;

import com.golive.cinema.Constants;
import com.golive.network.entity.Film;
import com.golive.network.response.FilmListResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/7/8.
 */
public class FilmsRepository implements FilmsDataSource {
    private static final int MAX_CACHE_SIZE = 30;
    private static FilmsRepository INSTANCE = null;

    private final FilmsDataSource mRemoteDataSource;

    private final FilmsDataSource mLocalDataSource;

    /**
     * Cached films for film list.
     */
//    private Map<String, Film> mCachedFilms;

    private final LruCache<String, Film> mCachedFilms;

    /**
     * Cached films for film detail info.
     */
//    private Map<String, Film> mCachedFilmDetails;

    private final LruCache<String, Film> mCachedFilmDetails;

    private final Map<String, Boolean> mCachedFilmDetailIsDirty;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This
     * variable
     * has package local visibility so it can be accessed from tests.
     */
    private boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private FilmsRepository(@NonNull FilmsDataSource filmsRemoteDataSource,
            @NonNull FilmsDataSource filmsLocalDataSource) {
        mRemoteDataSource = checkNotNull(filmsRemoteDataSource);
        mLocalDataSource = checkNotNull(filmsLocalDataSource);

//        mCachedFilms = new LinkedHashMap<>();
        mCachedFilms = new LruCache<>(MAX_CACHE_SIZE);
//        mCachedFilmDetails = new LinkedHashMap<>();
        mCachedFilmDetails = new LruCache<>(MAX_CACHE_SIZE);
        mCachedFilmDetailIsDirty = new LinkedHashMap<>();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param remoteDataSource the backend data source
     * @param localDataSource  the device storage data source
     * @return the {@link FilmsRepository} instance
     */
    public static FilmsRepository getInstance(FilmsDataSource remoteDataSource,
            FilmsDataSource localDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new FilmsRepository(remoteDataSource, localDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(FilmsDataSource, FilmsDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<Film> getFilm(@NonNull String id) {

        checkNotNull(id);

        // if cache is not dirty.
        if (!mCacheIsDirty) {
            final Film cachedFilm = getCachedFilmById(id);

            // Respond immediately with cache if available
            if (cachedFilm != null) {
                return Observable.just(cachedFilm);
            }
        }

        Observable<Film> remoteFilm = getFilmWithIdFromRemoteRepository(id);

        // cache is dirty || not allow to use local cache
        if (mCacheIsDirty || !Constants.FILM_CACHE_LOCAL) {
            // If the cache is dirty we need to fetch new data from the network.
            return remoteFilm;
        } else {
            // Query the local storage if available. If not, query the network.
            Observable<Film> localFilm = getFilmWithIdFromLocalRepository(id);
            return Observable.concat(localFilm, remoteFilm).first();
        }
    }

    @Override
    public Observable<Film> getFilmDetail(@NonNull String id) {
        checkNotNull(id);

        // if cache is not dirty.
        boolean isCachedDirty = false;
        Boolean isDirty;
        if (mCachedFilmDetailIsDirty != null
                && (isDirty = mCachedFilmDetailIsDirty.get(id)) != null) {
            isCachedDirty = isDirty;
        }

        if (!isCachedDirty) {
            final Film cachedFilm = getCachedFilmDetailById(id);

            // Respond immediately with cache if available
            if (cachedFilm != null) {
                return Observable.just(cachedFilm);
            }
        }

        Observable<Film> remoteFilm = getFilmDetailWithIdFromRemoteRepository(id);

        // cache is dirty || not allow to use local cache
        if (isCachedDirty || !Constants.FILM_CACHE_LOCAL) {
            // If the cache is dirty we need to fetch new data from the network.
            return remoteFilm;
        } else {
            // Query the local storage if available. If not, query the network.
            Observable<Film> localFilm = getFilmDetailWithIdFromLocalRepository(id);
            return Observable.concat(localFilm, remoteFilm).filter(new Func1<Film, Boolean>() {
                @Override
                public Boolean call(Film film) {
                    return film != null;
                }
            }).first();
        }
    }

    @Override
    public Observable<List<Film>> getFilms() {
        // Respond immediately with cache if available and not dirty
        if (!mCacheIsDirty && mCachedFilms != null && mCachedFilms.size() > 0) {
            return Observable.from(mCachedFilms.snapshot().values()).toList();
        }

        Observable<List<Film>> remoteFilms = getAndSaveRemoteFilms();

        // cache is dirty || not allow to use local cache
        if (mCacheIsDirty || !Constants.FILM_CACHE_LOCAL) {
            // If the cache is dirty we need to fetch new data from the network.
            return remoteFilms;
        } else {
            // Query the local storage if available. If not, query the network.
            Observable<List<Film>> localFilms = getAndCacheLocalFilms();
            return Observable.concat(localFilms, remoteFilms)
                    .filter(new Func1<List<Film>, Boolean>() {
                        @Override
                        public Boolean call(List<Film> films) {
                            return films != null && !films.isEmpty();
                        }
                    })
                    .first();
        }
    }

    @Override
    public void refreshFilms() {
        mCacheIsDirty = true;
    }

    @Override
    public void refreshFilmDetail(@NonNull String filmId) {
        if (mCachedFilmDetails != null) {
            mCachedFilmDetails.remove(filmId);
        }

        if (mCachedFilmDetailIsDirty != null) {
            mCachedFilmDetailIsDirty.put(filmId, true);
        }
    }

    @Override
    public void saveFilm(@NonNull Film film) {
        checkNotNull(film);
        mRemoteDataSource.saveFilm(film);
        mLocalDataSource.saveFilm(film);
        // Do in memory cache update to keep the app UI up to date
        mCachedFilms.put(film.getReleaseid(), film);
    }

    @Override
    public void saveFilmDetail(@NonNull Film film) {
        checkNotNull(film);
        mRemoteDataSource.saveFilmDetail(film);
        mLocalDataSource.saveFilmDetail(film);
        // Do in memory cache update to keep the app UI up to date
        mCachedFilmDetails.put(film.getReleaseid(), film);
    }

    @Override
    public void deleteFilm(@NonNull String filmId) {
        mRemoteDataSource.deleteFilm(checkNotNull(filmId));
        mLocalDataSource.deleteFilm(checkNotNull(filmId));
        mCachedFilms.remove(filmId);
    }

    @Override
    public void deleteFilmDetail(@NonNull String filmId) {
        mRemoteDataSource.deleteFilmDetail(checkNotNull(filmId));
        mLocalDataSource.deleteFilmDetail(checkNotNull(filmId));
        mCachedFilmDetails.remove(filmId);
    }

    @Override
    public void deleteAllFilms() {
        mRemoteDataSource.deleteAllFilms();
        mLocalDataSource.deleteAllFilms();

        if (mCachedFilms != null) {
//            mCachedFilms.clear();
            mCachedFilms.evictAll();
        }

        if (mCachedFilmDetails != null) {
//            mCachedFilmDetails.clear();
            mCachedFilmDetails.evictAll();
        }
    }

    @Override
    public Observable<FilmListResponse> getFilmList(String encryptionType) {
        return mRemoteDataSource.getFilmList(encryptionType);
    }

    /**
     * Get cached film by id.
     */
    @Nullable
    private Film getCachedFilmById(@NonNull String id) {
        checkNotNull(id);

        Film film = null;
        if (mCachedFilms != null && mCachedFilms.size() > 0) {
            film = mCachedFilms.get(id);
        }
        return film;
    }

    /**
     * Get cached film by id.
     */
    @Nullable
    private Film getCachedFilmDetailById(@NonNull String id) {
        checkNotNull(id);

        Film film = null;
        if (mCachedFilmDetails != null && mCachedFilmDetails.size() > 0) {
            film = mCachedFilmDetails.get(id);
        }
        return film;
    }

    @NonNull
    private Observable<Film> getFilmWithIdFromLocalRepository(@NonNull String id) {
        return mLocalDataSource.getFilm(id)
                .filter(new Func1<Film, Boolean>() {
                    @Override
                    public Boolean call(Film film) {
                        return film != null;
                    }
                })
                .doOnNext(new Action1<Film>() {
                    @Override
                    public void call(Film film) {
                        mCachedFilms.put(film.getReleaseid(), film);
                    }
                })
                .first();
    }

    @NonNull
    private Observable<Film> getFilmDetailWithIdFromLocalRepository(@NonNull String id) {
        return mLocalDataSource.getFilmDetail(id)
                .doOnNext(new Action1<Film>() {
                    @Override
                    public void call(Film film) {
                        if (film != null) {
                            mCachedFilmDetails.put(film.getReleaseid(), film);
                        }
                    }
                })
                .first();
    }

    @NonNull
    private Observable<Film> getFilmWithIdFromRemoteRepository(@NonNull String id) {
        return mRemoteDataSource.getFilm(id)
                .doOnNext(new Action1<Film>() {
                    @Override
                    public void call(Film film) {
                        mLocalDataSource.saveFilm(film);
                        mCachedFilms.put(film.getReleaseid(), film);
                    }
                });
    }

    @NonNull
    private Observable<Film> getFilmDetailWithIdFromRemoteRepository(@NonNull final String id) {
        return mRemoteDataSource.getFilmDetail(id)
                .doOnNext(new Action1<Film>() {
                    @Override
                    public void call(Film film) {
                        if (film != null) {
                            mLocalDataSource.saveFilmDetail(film);
                            mCachedFilmDetails.put(film.getReleaseid(), film);
                        }
                    }
                }).doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (mCachedFilmDetailIsDirty != null) {
                            mCachedFilmDetailIsDirty.remove(id);
                        }
                    }
                });
    }

    @NonNull
    private Observable<List<Film>> getAndCacheLocalFilms() {
        return mLocalDataSource.getFilms()
                .flatMap(new Func1<List<Film>, Observable<List<Film>>>() {
                    @Override
                    public Observable<List<Film>> call(List<Film> films) {
                        if (null == films || films.isEmpty()) {
                            return Observable.empty();
                        }
                        return Observable.from(films)
                                .doOnNext(new Action1<Film>() {
                                    @Override
                                    public void call(Film Film) {
                                        mCachedFilms.put(Film.getReleaseid(), Film);
                                    }
                                })
                                .toList();
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mCacheIsDirty = false;
                    }
                });
    }

    @NonNull
    private Observable<List<Film>> getAndSaveRemoteFilms() {
        return mRemoteDataSource.getFilms()
                //mRemoteDataSource.getFilms(tags, filmtype, playtype, limit, start, lang,
                // nowdate, type)
                .flatMap(new Func1<List<Film>, Observable<Film>>() {
                    @Override
                    public Observable<Film> call(List<Film> films) {
                        if (films != null && !films.isEmpty()) {
                            return Observable.from(films);
                        } else {
                            return Observable.empty();
                        }
                    }
                })
                .doOnNext(new Action1<Film>() {
                    @Override
                    public void call(Film film) {
                        mLocalDataSource.saveFilm(film);
                        mCachedFilms.put(film.getReleaseid(), film);
                    }
                })
                .toList()
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mCacheIsDirty = false;
                    }
                });
    }

    private void refreshCache(List<Film> Films) {
//        mCachedFilms.clear();
        mCachedFilms.evictAll();
        for (Film film : Films) {
            mCachedFilms.put(film.getReleaseid(), film);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Film> Films) {
        mLocalDataSource.deleteAllFilms();
        for (Film Film : Films) {
            mLocalDataSource.saveFilm(Film);
        }
    }

    @VisibleForTesting
    public LruCache<String, Film> getCachedFilmDetails() {
        return mCachedFilmDetails;
    }

    @VisibleForTesting
    public LruCache<String, Film> getCachedFilms() {
        return mCachedFilms;
    }
}