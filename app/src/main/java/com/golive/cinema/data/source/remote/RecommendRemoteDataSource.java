package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.MainConfig;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.response.MovieRecommendResponse;
import com.golive.network.response.RecommendResponse;

import rx.Observable;
import rx.functions.Func1;


/**
 * Created by Administrator on 2016/11/4.
 */

public class RecommendRemoteDataSource implements RecommendDataSource {
    private static RecommendRemoteDataSource INSTANCE;
    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;
    public final static int PAGE_RECOMMEND = 1;
    public final static int PAGE_USER_CENTER = 2;

    private RecommendRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }

    public static RecommendRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        checkNotNull(goLiveRestApi);
        checkNotNull(mainConfigDataSource);
        if (INSTANCE == null) {
            INSTANCE = new RecommendRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }


    @Override
    public Observable<RecommendResponse> getRecommendData(final int pageType,
            @NonNull final String pageId, final String encryptionType) {
        return mMainConfigDataSource.getMainConfig().flatMap(
                new Func1<MainConfig, Observable<RecommendResponse>>() {
                    @Override
                    public Observable<RecommendResponse> call(MainConfig mainConfig) {
                        String url = null;
                        switch (pageType) {
                            case PAGE_RECOMMEND:
                                url = mainConfig.getGetRecommendLayout();
                                //url = "http://183.60.142
                                // .151:8063/goliveAPI//api5/layout_getRecommendLayout.action";
                                break;
                            case PAGE_USER_CENTER:
                                url = mainConfig.getGetUserCenterLayout();
                                //url = "http://183.60.142
                                // .151:8063/goliveAPI//api5/layout_getUserCenterLayout.action";
                                break;
                            default:
                                break;
                        }
                        return mGoLiveRestApi.queryRecommend(url, pageId, encryptionType);
                    }
                });
    }

    @Override
    public Observable<MovieRecommendResponse> getMovieRecommendData(@NonNull final String filmId,
            final String encryptionType) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<MovieRecommendResponse>>() {
                    @Override
                    public Observable<MovieRecommendResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetMovieRecommend();
                        return mGoLiveRestApi.queryMovieRecommend(url, filmId,
                                encryptionType);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<MovieRecommendResponse>());
    }


}
