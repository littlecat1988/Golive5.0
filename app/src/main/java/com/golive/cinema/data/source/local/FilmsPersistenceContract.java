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

import android.provider.BaseColumns;

/**
 * The contract used for the db to save the films locally.
 */
public final class FilmsPersistenceContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private FilmsPersistenceContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class FilmEntry implements BaseColumns {
        public static final String TABLE_NAME = "film";
        public static final String FILM_DETAIL_TABLE_NAME = "film_detail";
        public static final String COLUMN_NAME_ENTRY_ID = "filmId";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_INTRODUCTION = "introduction";
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_BIGPOSTER = "bigPoster";
        public static final String COLUMN_NAME_SMALLPOSTER = "smallPoster";
        public static final String COLUMN_NAME_DIRECTOR = "director";
        public static final String COLUMN_NAME_ACTORS = "actors";
        public static final String COLUMN_NAME_SCENARIST = "scenarist";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_CATEGORYNAME = "categoryName";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_STARTTIME = "starttime";
        public static final String COLUMN_NAME_ENDTIME = "endTime";
        public static final String COLUMN_NAME_ACTIVE = "active";
        public static final String COLUMN_NAME_AREA = "area";
        public static final String COLUMN_NAME_AREANAME = "areaName";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_VIPPRICE = "vipPrice";
        public static final String COLUMN_NAME_ONLINEPRICE = "onlinePrice";
        public static final String COLUMN_NAME_VIPONLINEPRICE = "vipOnlinePrice";
        public static final String COLUMN_NAME_DOWNLOADPRICE = "downloadPrice";
        public static final String COLUMN_NAME_VIPDOWNLOADPRICE = "vipDownloadPrice";
        public static final String COLUMN_NAME_MEDIAS = "medias";
        public static final String COLUMN_NAME_PREVUES = "prevues";
        public static final String COLUMN_NAME_COVERS = "covers";
    }
}
