package com.golive.cinema.films.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.FilmListResponse;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/25.
 * 获取新接口数据（Json）
 */

public class GetFilmListUseCase extends
        UseCase<GetFilmListUseCase.RequestValues, GetFilmListUseCase.ResponseValue> {
    private final FilmsDataSource mDataSource;
    private final GetKdmInitUseCase mGetKdmInitUseCase;
    private final BaseSchedulerProvider mSchedulerProvider;

    public GetFilmListUseCase(@NonNull FilmsDataSource dataSource,
            @NonNull GetKdmInitUseCase getKdmInitUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
        mGetKdmInitUseCase = checkNotNull(getKdmInitUseCase, "GetKdmInitUseCase cannot be null!");
        mSchedulerProvider = schedulerProvider;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mGetKdmInitUseCase.run(new GetKdmInitUseCase.RequestValues(false))
                .observeOn(mSchedulerProvider.io())
                .flatMap(
                        new Func1<GetKdmInitUseCase.ResponseValue, Observable<FilmListResponse>>() {
                            @Override
                            public Observable<FilmListResponse> call(
                                    GetKdmInitUseCase.ResponseValue responseValue) {
                                String encryptionType = "0";
                                KDMResCode resCode = responseValue.getKDMResCode();
                                if (resCode != null
                                        && KDMResCode.RESCODE_OK == resCode.getResult()) {
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
                                return mDataSource.getFilmList(encryptionType);
                            }
                        })
                .map(new Func1<FilmListResponse, ResponseValue>() {
                    @Override
                    public ResponseValue call(FilmListResponse filmListResponse) {
                        return new ResponseValue(filmListResponse);
                    }
                });

    }

    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final FilmListResponse filmListResponse;

        public ResponseValue(FilmListResponse filmListResponse) {
            this.filmListResponse = filmListResponse;
        }

        public FilmListResponse getFilmListResponse() {
            return filmListResponse;
        }
    }
}