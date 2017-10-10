package com.golive.cinema.data.source;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.network.response.MovieRecommendResponse;
import com.golive.network.response.RecommendResponse;

import rx.Observable;


/**
 * Created by Administrator on 2016/11/4.
 */

public class RecommendRepository implements RecommendDataSource {

    private static RecommendRepository INSTANCE = null;
    private final RecommendDataSource mRemoteDataSource, mLocalDataSource;

    private RecommendRepository(@NonNull RecommendDataSource recommendRemoteDataSource,
            @NonNull RecommendDataSource recommendLocalDataSource) {
        mRemoteDataSource = checkNotNull(recommendRemoteDataSource);
        mLocalDataSource = checkNotNull(recommendLocalDataSource);
    }

    public static RecommendRepository getInstance(RecommendDataSource remoteDataSource,
            RecommendDataSource localDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new RecommendRepository(remoteDataSource, localDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<RecommendResponse> getRecommendData(int pageType,
            @NonNull String pageId, String encryptionType) {
        return mRemoteDataSource.getRecommendData(pageType, pageId, encryptionType);
    }

    @Override
    public Observable<MovieRecommendResponse> getMovieRecommendData(@NonNull String filmId,
            String encryptionType) {
        return mRemoteDataSource.getMovieRecommendData(filmId, encryptionType);
    }
}
