package com.golive.cinema.data.source.local;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;

import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.MovieRecommendResponse;
import com.golive.network.response.RecommendResponse;

import rx.Observable;


/**
 * Created by Administrator on 2016/11/4.
 */

public class RecommendLocalDataSource implements RecommendDataSource {
    private static RecommendLocalDataSource INSTANCE;

    private RecommendLocalDataSource(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
    }

    public static RecommendLocalDataSource getInstance(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new RecommendLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    @Override
    public Observable<RecommendResponse> getRecommendData(int pageType, @NonNull String pageId,
            String encryptionType) {
        return Observable.empty();
    }

    @Override
    public Observable<MovieRecommendResponse> getMovieRecommendData(@NonNull String filmId,
            String encryptionType) {
        return Observable.empty();
    }
}
