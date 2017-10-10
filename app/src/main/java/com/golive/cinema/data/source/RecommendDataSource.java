package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.response.MovieRecommendResponse;
import com.golive.network.response.RecommendResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/11/3.
 */

public interface RecommendDataSource {
    Observable<RecommendResponse> getRecommendData(int pageType, @NonNull String pageId,
            String encryptionType);

    Observable<MovieRecommendResponse> getMovieRecommendData(@NonNull String filmId,
            String encryptionType);
}
