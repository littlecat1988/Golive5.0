package com.golive.cinema.data.source.remote;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.FilmTopicsDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.FilmTopic;
import com.golive.network.entity.MainConfig;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.response.FilmTopicsResponse;
import com.golive.network.response.SpecialDetailResponse;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/1.
 */

public class FilmTopicsRemoteDataSource implements FilmTopicsDataSource {

    private static FilmTopicsRemoteDataSource INSTANCE = null;

    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;
    private List<FilmTopic> mCachedPastFilmTopics;

    public static FilmTopicsRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new FilmTopicsRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    private FilmTopicsRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }


    @Override
    public Observable<FilmTopic> getFilmTopicDetail(@NonNull final String filmTopicId) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<SpecialDetailResponse>>() {
                    @Override
                    public Observable<SpecialDetailResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getTopicDetail();
                        return mGoLiveRestApi.getFilmTopicDetail(url, filmTopicId);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<SpecialDetailResponse>())
                .map(new Func1<SpecialDetailResponse, FilmTopic>() {
                    @Override
                    public FilmTopic call(SpecialDetailResponse specialDetailResponse) {
                        if (specialDetailResponse != null
                                && specialDetailResponse.getData() != null) {
                            return specialDetailResponse.getData().getFilmTopic();
                        }
                        return null;
                    }
                });
    }

    @Override
    public Observable<List<FilmTopic>> getRecommendFilmTopics() {
        return getFilmTopicsInternal(true);
    }

    @Override
    public Observable<List<FilmTopic>> getOldFilmTopics() {
        if (mCachedPastFilmTopics != null && !mCachedPastFilmTopics.isEmpty()) {
            return Observable.just(mCachedPastFilmTopics);
        }

        return getFilmTopicsInternal(false);
    }

    private Observable<List<FilmTopic>> getFilmTopicsInternal(final boolean recommendOrOldTopics) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<FilmTopicsResponse>>() {
                    @Override
                    public Observable<FilmTopicsResponse> call(MainConfig mainConfig) {
                        if (recommendOrOldTopics) {
//                        String url =
// "http://172.19.0.125:8081/goliveAPI/api5/movie_recommendSpecialtopic.action";
                            String url = mainConfig.getRecommendTopics();
                            return mGoLiveRestApi.getRecommendFilmTopics(url);
                        } else {
                            //                        String url =
// "http://172.19.0.125:8081/goliveAPI/api5/movie_pastSpecialtopic.action";
                            String url = mainConfig.getPastTopics();
                            return mGoLiveRestApi.getOldFilmTopics(url);
                        }
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<FilmTopicsResponse>())
                .map(new Func1<FilmTopicsResponse, List<FilmTopic>>() {
                    @Override
                    public List<FilmTopic> call(FilmTopicsResponse filmTopicsResponse) {
                        if (filmTopicsResponse != null && filmTopicsResponse.getData() != null) {
                            List<FilmTopic> topics = filmTopicsResponse.getData().getContent();
                            if (!recommendOrOldTopics) {
                                mCachedPastFilmTopics = topics;
                            }
                            return topics;
                        }
                        return null;
                    }
                });
    }
}
