package com.golive.cinema.filmlibrary.dimain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.FilmLibraryDataSource;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.FilmLibListResponse;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/14.
 */

public class GetFilmLibListUseCase extends
        UseCase<GetFilmLibListUseCase.RequestValues, GetFilmLibListUseCase.ResponseValue> {

    private final FilmLibraryDataSource mFilmLibraryDataSource;
    private final GetKdmInitUseCase mGetKdmInitUseCase;
    private final BaseSchedulerProvider mSchedulerProvider;
    private static String encryptionType;

    public GetFilmLibListUseCase(@NonNull FilmLibraryDataSource filmLibraryDataSource,
            @NonNull GetKdmInitUseCase getKdmInitUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mFilmLibraryDataSource = checkNotNull(filmLibraryDataSource,
                "filmLibraryDataSource cannot be null!");
        mGetKdmInitUseCase = checkNotNull(getKdmInitUseCase, "GetKdmInitUseCase cannot be null!");
        mSchedulerProvider = schedulerProvider;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        if (encryptionType == null) {
            return mGetKdmInitUseCase.run(new GetKdmInitUseCase.RequestValues(false))
                    .observeOn(mSchedulerProvider.io())
                    .flatMap(new Func1<GetKdmInitUseCase.ResponseValue,
                            Observable<FilmLibListResponse>>() {
                        @Override
                        public Observable<FilmLibListResponse> call(
                                GetKdmInitUseCase.ResponseValue responseValue) {
                            encryptionType = "0";
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
                            return mFilmLibraryDataSource.getFilmLibList(requestValues.getTabId(),
                                    encryptionType);
                        }
                    })
                    .map(new Func1<FilmLibListResponse, ResponseValue>() {
                        @Override
                        public ResponseValue call(FilmLibListResponse filmLibListResponse) {
                            return new ResponseValue(filmLibListResponse);
                        }
                    });
        } else {
            return mFilmLibraryDataSource.getFilmLibList(requestValues.getTabId(), encryptionType)
                    .map(new Func1<FilmLibListResponse, ResponseValue>() {
                        @Override
                        public ResponseValue call(FilmLibListResponse filmLibListResponse) {
                            return new ResponseValue(filmLibListResponse);
                        }
                    });
        }
    }

    public static class RequestValues implements UseCase.RequestValues {
        private String tabId;

        public RequestValues(String tabId) {
            this.tabId = tabId;
        }

        public String getTabId() {
            return tabId;
        }

        public void setTabId(String tabId) {
            this.tabId = tabId;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final FilmLibListResponse response;

        public ResponseValue(FilmLibListResponse response) {
            this.response = response;
        }

        public FilmLibListResponse getResponse() {
            return response;
        }
    }
}
