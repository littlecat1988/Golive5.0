package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.ExitGuideResponse;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/12/15.
 */

public class ExitComboUseCase extends
        UseCase<ExitComboUseCase.RequestValues, ExitComboUseCase.ResponseValue> {

    @NonNull
    private final UserDataSource mUserDataSource;
    @NonNull
    private final GetKdmInitUseCase mGetKdmInitUseCase;

    public ExitComboUseCase(@NonNull UserDataSource userDataSource,
            @NonNull GetKdmInitUseCase mGetKdmInitUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mUserDataSource = userDataSource;
        this.mGetKdmInitUseCase=mGetKdmInitUseCase;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
//        if (requestValues.forceUpdate) {
//            mUserDataSource.refreshRecommendCombo();
//        }
        return mGetKdmInitUseCase.run(new GetKdmInitUseCase.RequestValues(false))
                .observeOn(mSchedulerProvider.io())
                .flatMap(
                        new Func1<GetKdmInitUseCase.ResponseValue, Observable<ExitGuideResponse>>() {
                            @Override
                            public Observable<ExitGuideResponse> call(
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
                                return mUserDataSource.queryExitGuideResponse(encryptionType);
                            }
                        })
                .map(new Func1<ExitGuideResponse, ResponseValue>() {
                    @Override
                    public ResponseValue call(ExitGuideResponse exitGuideResponse) {
                        return new ResponseValue(exitGuideResponse);
                    }
                });
//        return mUserDataSource.queryRecommendComboInfo()
//                .map(new Func1<RecommendComboResponse, ResponseValue>() {
//                    @Override
//                    public ResponseValue call(RecommendComboResponse recommendComboResponse) {
//                        return new ResponseValue(recommendComboResponse);
//                    }
//                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final boolean forceUpdate;

        public RequestValues(boolean forceUpdate) {
            this.forceUpdate = forceUpdate;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final ExitGuideResponse response;

        public ResponseValue(ExitGuideResponse response) {
            this.response = response;
        }

        public ExitGuideResponse getResponse() {
            return response;
        }
    }

}
