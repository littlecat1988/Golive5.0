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

package com.golive.cinema.data.source.local;

import static com.golive.cinema.data.source.local.FilmsPersistenceContract.FilmEntry
        .FILM_DETAIL_TABLE_NAME;
import static com.golive.cinema.data.source.local.FilmsPersistenceContract.FilmEntry.TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.initialjie.log.Logger;

public class FilmsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_NAME = "Films.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String BOOLEAN_TYPE = " INTEGER";

    private static final String COMMA_SEP = ", ";

    private static final String TEXT_TYPE_SEP = TEXT_TYPE + COMMA_SEP;

    private static final String BOOLEAN_TYPE_SEP = BOOLEAN_TYPE + COMMA_SEP;

    private static final String SQL_CREATE_ENTRIES =
            getCreateTable(FilmsPersistenceContract.FilmEntry.TABLE_NAME);

    private static final String SQL_CREATE_FILM_DETAIL_ENTRIES =
            getCreateTable(FilmsPersistenceContract.FilmEntry.FILM_DETAIL_TABLE_NAME);

//    private static final String DATABASE_ALTER_3 = "ALTER TABLE "
//            + TABLE_TEAM + " ADD COLUMN " + COLUMN_STADIUM + " string;";


    public FilmsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.d("onCreate, Version : " + DATABASE_VERSION);
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_FILM_DETAIL_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d("onUpgrade, oldVersion : " + oldVersion + ", newVersion : " + newVersion);

        if (oldVersion < 3) {
            // drop older version( < 3 )
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + FILM_DETAIL_TABLE_NAME);
            onCreate(db);
        } else if (oldVersion < 4) {
//            db.execSQL(DATABASE_ALTER_3);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d("onDowngrade, oldVersion : " + oldVersion + ", newVersion : " + newVersion);
        // Not required as at version 1
    }

    @NonNull
    private static String getCreateTable(String tableName) {
        return "CREATE TABLE "
                + tableName
                + " ( "
                +
                FilmsPersistenceContract.FilmEntry._ID
                + TEXT_TYPE
                + " PRIMARY KEY "
                + COMMA_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_ENTRY_ID
                + TEXT_TYPE
                + " UNIQUE "
                + COMMA_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_TITLE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_INTRODUCTION
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_SCORE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_BIGPOSTER
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_SMALLPOSTER
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_DIRECTOR
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_ACTORS
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_SCENARIST
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_CATEGORY
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_CATEGORYNAME
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_DURATION
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_STARTTIME
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_ENDTIME
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_ACTIVE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_AREA
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_AREANAME
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_PRICE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_VIPPRICE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_ONLINEPRICE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_VIPONLINEPRICE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_DOWNLOADPRICE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_VIPDOWNLOADPRICE
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_MEDIAS
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_PREVUES
                + TEXT_TYPE_SEP
                +
                FilmsPersistenceContract.FilmEntry.COLUMN_NAME_COVERS
                + TEXT_TYPE
                +
                "  ) ";
    }
}
