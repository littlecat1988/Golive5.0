package com.golive.cinema.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2016/8/30.
 */

public class GsonUtils {

    @Nullable
    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return fromJsonArray(gson, json, clazz);
    }

    @Nullable
    public static <T> List<T> fromJsonArray(@NonNull Gson gson, @NonNull String json,
            @NonNull Class<T> clazz) {
        List<T> lst = null;
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            if (null == lst) {
                lst = new ArrayList<>();
            }
            lst.add(gson.fromJson(elem, clazz));
        }
        return lst;
    }
}
