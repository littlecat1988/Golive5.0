package com.golive.cinema.recommend.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.response.MovieRecommendResponse;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mowl on 2016/11/23.
 */

public class GetMovieRecommendUseCase extends
        UseCase<GetMovieRecommendUseCase.RequestValues, GetMovieRecommendUseCase.ResponseValue> {

    private final RecommendDataSource mDataSource;
    private final GetKdmInitUseCase mGetKdmInitUseCase;
    private final BaseSchedulerProvider mSchedulerProvider;

    public GetMovieRecommendUseCase(@NonNull RecommendDataSource dataSource,
            @NonNull GetKdmInitUseCase getKdmInitUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
        mGetKdmInitUseCase = checkNotNull(getKdmInitUseCase, "GetKdmInitUseCase cannot be null!");
        mSchedulerProvider = schedulerProvider;
    }

    @Override
    protected Observable<GetMovieRecommendUseCase.ResponseValue> executeUseCase(
            final GetMovieRecommendUseCase.RequestValues requestValues) {
        return mGetKdmInitUseCase.run(new GetKdmInitUseCase.RequestValues(false))
                .observeOn(mSchedulerProvider.io())
                .flatMap(new Func1<GetKdmInitUseCase.ResponseValue,
                        Observable<MovieRecommendResponse>>() {
                    @Override
                    public Observable<MovieRecommendResponse> call(
                            GetKdmInitUseCase.ResponseValue responseValue) {
                        String encryptionType = "0";
                        KDMResCode resCode = responseValue.getKDMResCode();
                        if (resCode != null && KDMResCode.RESCODE_OK == resCode.getResult()) {
                            switch (resCode.init.getType()) {
                                case KDMPlayer.ONLINE:
                                    encryptionType = "1";
                                    break;
                                case KDMPlayer.DOWNLOAD:
                                    encryptionType = "2";
                                    break;
                                case KDMPlayer.BOTH:
                                    encryptionType = "3";
                                    break;
                                default:
                                    break;

                            }
                        }
                        return mDataSource.getMovieRecommendData(requestValues.getFilmId(),
                                encryptionType);
                    }
                })
                .map(new Func1<MovieRecommendResponse, GetMovieRecommendUseCase
                        .ResponseValue>() {
                    @Override
                    public GetMovieRecommendUseCase.ResponseValue call(
                            MovieRecommendResponse recommendResponse) {
                        if (recommendResponse != null
                                && recommendResponse.getData() != null
                                && recommendResponse.getData().getContent() != null
                                && !recommendResponse.getData().getContent().isEmpty()) {
                            return new GetMovieRecommendUseCase.ResponseValue(
                                    recommendResponse.getData().getContent());
                        } else {
                            return new GetMovieRecommendUseCase.ResponseValue(null);
                        }
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private String filmId;

        public RequestValues(String filmId) {
            this.filmId = filmId;
        }

        public String getFilmId() {
            return filmId;
        }

        public void setFilmId(String filmId) {
            this.filmId = filmId;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final List<MovieRecommendFilm> contentList;

        public ResponseValue(List<MovieRecommendFilm> contentList) {
            this.contentList = contentList;
        }

        public List<MovieRecommendFilm> getContentList() {
            return contentList;
        }
    }
}
