package com.golive.cinema.data.source.local;

import static com.golive.cinema.util.GsonUtils.fromJsonArray;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.data.source.local.FilmsPersistenceContract.FilmEntry;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Cover;
import com.golive.network.entity.Film;
import com.golive.network.entity.Media;
import com.golive.network.entity.Video;
import com.golive.network.response.FilmListResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/7/8.
 */

public class FilmsLocalDataSource implements FilmsDataSource {

    private static FilmsLocalDataSource INSTANCE;
    private final Gson mGson;
    private final BriteDatabase mDatabaseHelper;
    private final Func1<Cursor, Film> mTaskMapperFunction;

    private FilmsLocalDataSource(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");

        FilmsDbHelper dbHelper = new FilmsDbHelper(context);
        mGson = new GsonBuilder().create();
        SqlBrite sqlBrite = SqlBrite.create();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mTaskMapperFunction = new Func1<Cursor, Film>() {
            @Override
            public Film call(Cursor c) {
                return getFilmFromCursor(c);
            }
        };
    }


    public static FilmsLocalDataSource getInstance(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new FilmsLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<Film> getFilm(@NonNull String id) {
        return getFilm(FilmEntry.TABLE_NAME, id);
    }

    @Override
    public Observable<Film> getFilmDetail(@NonNull String id) {
        return getFilm(FilmEntry.FILM_DETAIL_TABLE_NAME, id);
    }

    @Override
    public Observable<List<Film>> getFilms() {
//        String[] projection = {
//                FilmEntry.COLUMN_NAME_ENTRY_ID,
//                FilmEntry.COLUMN_NAME_TITLE,
//                FilmEntry.COLUMN_NAME_INTRODUCTION
//        };
//        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection),
//                FilmEntry.TABLE_NAME);
        String sql = String.format("SELECT %s FROM %s", "*", FilmEntry.TABLE_NAME);
        return mDatabaseHelper.createQuery(FilmEntry.TABLE_NAME, sql)
                .mapToList(mTaskMapperFunction);
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
        checkNotNull(film);
        ContentValues values = getInsertContentValues(film);
        mDatabaseHelper.insert(FilmEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


    @Override
    public void saveFilmDetail(@NonNull Film film) {
        checkNotNull(film);
        ContentValues values = getInsertContentValues(film);
        mDatabaseHelper.insert(FilmEntry.FILM_DETAIL_TABLE_NAME, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void deleteFilm(@NonNull String filmId) {
        String selection = FilmEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {filmId};
        mDatabaseHelper.delete(FilmEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public void deleteFilmDetail(@NonNull String filmId) {
        String selection = FilmEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {filmId};
        mDatabaseHelper.delete(FilmEntry.FILM_DETAIL_TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public void deleteAllFilms() {
        mDatabaseHelper.delete(FilmEntry.TABLE_NAME, null);
        mDatabaseHelper.delete(FilmEntry.FILM_DETAIL_TABLE_NAME, null);
    }

    @Override
    public Observable<FilmListResponse> getFilmList(String encryptionType) {
        return Observable.empty();
    }

    @NonNull
    private Observable<Film> getFilm(@NonNull String tableName, @NonNull String id) {
        checkNotNull(id);
        checkNotNull(tableName);
//        String[] projection = {
//                FilmEntry.COLUMN_NAME_ENTRY_ID,
//                FilmEntry.COLUMN_NAME_TITLE,
//                FilmEntry.COLUMN_NAME_INTRODUCTION
//        };
//        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?",
//                TextUtils.join(",", projection), tableName,
//                FilmEntry.COLUMN_NAME_ENTRY_ID);
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?", "*", tableName,
                FilmEntry.COLUMN_NAME_ENTRY_ID);
        return mDatabaseHelper.createQuery(tableName, sql, id)
                .mapToOneOrDefault(mTaskMapperFunction, null);
//                .mapToOne(mTaskMapperFunction);
    }

    private ContentValues getInsertContentValues(@NonNull Film film) {
        checkNotNull(film);

        ContentValues values = new ContentValues();
        values.put(FilmEntry.COLUMN_NAME_ENTRY_ID, film.getReleaseid());
        values.put(FilmEntry.COLUMN_NAME_TITLE, film.getName());
        values.put(FilmEntry.COLUMN_NAME_INTRODUCTION, film.getIntroduction());
        values.put(FilmEntry.COLUMN_NAME_SCORE, film.getScore());
        values.put(FilmEntry.COLUMN_NAME_BIGPOSTER, film.getBigposter());
        values.put(FilmEntry.COLUMN_NAME_SMALLPOSTER, film.getBigposter());
        values.put(FilmEntry.COLUMN_NAME_SCENARIST, film.getScenarist());
        values.put(FilmEntry.COLUMN_NAME_CATEGORY, film.getCategory());
        values.put(FilmEntry.COLUMN_NAME_CATEGORYNAME, film.getCategoryname());
        values.put(FilmEntry.COLUMN_NAME_DURATION, film.getDuration());
        values.put(FilmEntry.COLUMN_NAME_STARTTIME, film.getStarttime());
        values.put(FilmEntry.COLUMN_NAME_ENDTIME, film.getEndtime());
        values.put(FilmEntry.COLUMN_NAME_ACTIVE, film.getActive());
        values.put(FilmEntry.COLUMN_NAME_AREA, film.getArea());
        values.put(FilmEntry.COLUMN_NAME_AREANAME, film.getAreaname());
        values.put(FilmEntry.COLUMN_NAME_PRICE, film.getPrice());
        values.put(FilmEntry.COLUMN_NAME_VIPPRICE, film.getVipprice());
        values.put(FilmEntry.COLUMN_NAME_ONLINEPRICE, film.getOnlineprice());
        values.put(FilmEntry.COLUMN_NAME_VIPONLINEPRICE, film.getViponlineprice());
        values.put(FilmEntry.COLUMN_NAME_DOWNLOADPRICE, film.getDownloadprice());
        values.put(FilmEntry.COLUMN_NAME_VIPDOWNLOADPRICE, film.getVipdownloadprice());

        List<Media> medias = film.getMedias();
        if (medias != null && !medias.isEmpty()) {
            String mediasJson = mGson.toJson(medias);
            if (!StringUtils.isNullOrEmpty(mediasJson)) {
                values.put(FilmEntry.COLUMN_NAME_MEDIAS, mediasJson);
            }
        }

        List<Video> prevues = film.getVideosList();
        if (prevues != null && !prevues.isEmpty()) {
            String prevuesJson = mGson.toJson(prevues);
            if (!StringUtils.isNullOrEmpty(prevuesJson)) {
                values.put(FilmEntry.COLUMN_NAME_PREVUES, prevuesJson);
            }
        }

        List<Cover> covers = film.getCovers();
        if (covers != null && !covers.isEmpty()) {
            String coversJson = mGson.toJson(covers);
            if (!StringUtils.isNullOrEmpty(coversJson)) {
                values.put(FilmEntry.COLUMN_NAME_COVERS, coversJson);
            }
        }

        return values;
    }

    @NonNull
    private Film getFilmFromCursor(@NonNull Cursor c) {
        checkNotNull(c);

        String itemId =
                c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_TITLE));
        String description =
                c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_INTRODUCTION));
        String score =
                c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_SCORE));
        String bigPoster = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_BIGPOSTER));
        String smalloster = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_SMALLPOSTER));
        String director = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_DIRECTOR));
        String scenarist = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_SCENARIST));
        String category = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_CATEGORY));
        String categoryName = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_CATEGORYNAME));
        String duration = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_DURATION));
        String starttime = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_STARTTIME));
        String endTime = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_ENDTIME));
        String active = c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_ACTIVE));
        String area = c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_AREA));
        String areaName = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_AREANAME));
        String price = c.getString(c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_PRICE));
        String vipPrice = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_VIPPRICE));
        String onlinePrice = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_ONLINEPRICE));
        String vipOnlinePrice = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_VIPONLINEPRICE));
        String downloadPrice = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_DOWNLOADPRICE));
        String vipDownloadPrice = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_VIPDOWNLOADPRICE));
        String mediasJson = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_MEDIAS));
        String prevuesJson = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_PREVUES));
        String coversJson = c.getString(
                c.getColumnIndexOrThrow(FilmEntry.COLUMN_NAME_COVERS));

        Film film = new Film();
        film.setReleaseid(itemId);
        film.setName(title);
        film.setIntroduction(description);
        film.setScore(score);
        film.setBigposter(bigPoster);
        film.setSmallposter(smalloster);
        film.setDirector(director);
        film.setScenarist(scenarist);
        film.setCategory(category);
        film.setCategoryname(categoryName);
        film.setDuration(duration);
        film.setStarttime(starttime);
        film.setEndtime(endTime);
        film.setActive(active);
        film.setArea(area);
        film.setAreaname(areaName);
        film.setPrice(price);
        film.setVipprice(vipPrice);
        film.setOnlineprice(onlinePrice);
        film.setViponlineprice(vipOnlinePrice);
        film.setDownloadprice(downloadPrice);
        film.setVipdownloadprice(vipDownloadPrice);

        List<Media> mediaList = null;
        List<Video> videoList = null;
        List<Cover> coverList = null;
        if (!StringUtils.isNullOrEmpty(mediasJson)) {
            mediaList = fromJsonArray(mGson, mediasJson, Media.class);
        }
        if (!StringUtils.isNullOrEmpty(prevuesJson)) {
            videoList = fromJsonArray(mGson, prevuesJson, Video.class);
        }
        if (!StringUtils.isNullOrEmpty(coversJson)) {
            coverList = fromJsonArray(mGson, coversJson, Cover.class);
        }
        film.setMedias(mediaList);
        film.setVideosList(videoList);
        film.setCoverList(coverList);

        return film;
    }
}
