package com.golive.cinema.recommend.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.RecommendResponse;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;


/**
 * Created by Administrator on 2016/11/3.
 */

public class GetRecommendUseCase extends
        UseCase<GetRecommendUseCase.RequestValues, GetRecommendUseCase.ResponseValue> {

    private final RecommendDataSource mDataSource;
    private final GetKdmInitUseCase mGetKdmInitUseCase;
    private final BaseSchedulerProvider mSchedulerProvider;

    public GetRecommendUseCase(@NonNull RecommendDataSource dataSource,
            @NonNull GetKdmInitUseCase getKdmInitUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
        mGetKdmInitUseCase = checkNotNull(getKdmInitUseCase, "GetKdmInitUseCase cannot be null!");
        mSchedulerProvider = schedulerProvider;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mGetKdmInitUseCase.run(new GetKdmInitUseCase.RequestValues(false))
                .observeOn(mSchedulerProvider.io())
                .flatMap(
                        new Func1<GetKdmInitUseCase.ResponseValue, Observable<RecommendResponse>>
                                () {
                            @Override
                            public Observable<RecommendResponse> call(
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
                                return mDataSource.getRecommendData(requestValues.getPageType(),
                                        requestValues.getBasePageId(), encryptionType);
                            }
                        })
                .map(new Func1<RecommendResponse, ResponseValue>() {
                    @Override
                    public ResponseValue call(RecommendResponse recommendResponse) {
                        return new ResponseValue(recommendResponse);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private int pageType;
        private String basePageId;

        public RequestValues(int pageType, String basePageId) {
            this.pageType = pageType;
            this.basePageId = basePageId;
        }

        public int getPageType() {
            return pageType;
        }

        public void setPageType(int pageType) {
            this.pageType = pageType;
        }

        public String getBasePageId() {
            return basePageId;
        }

        public void setBasePageId(String basePageId) {
            this.basePageId = basePageId;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final RecommendResponse response;

        public ResponseValue(RecommendResponse response) {
            this.response = response;
        }

        public RecommendResponse getResponse() {
            return response;
        }
    }
}
