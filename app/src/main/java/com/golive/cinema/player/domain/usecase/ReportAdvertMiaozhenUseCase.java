package com.golive.cinema.player.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.PlayerDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Response;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2017/1/3.
 */

public class ReportAdvertMiaozhenUseCase extends UseCase<ReportAdvertMiaozhenUseCase.RequestValues,
        ReportAdvertMiaozhenUseCase.ResponseValue> {

    private final PlayerDataSource mPlayerDataSource;

    public ReportAdvertMiaozhenUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull PlayerDataSource playerDataSource) {
        super(schedulerProvider);
        mPlayerDataSource = checkNotNull(playerDataSource, "playerDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mPlayerDataSource.reportAdvertMiaozhen(requestValues.movieid,
                requestValues.scaleddensity, requestValues.manufacturerId, requestValues.pvalue,
                requestValues.kvalue, requestValues.advertId, requestValues.type)
                .map(new Func1<Response, ResponseValue>() {
                    @Override
                    public ResponseValue call(Response response) {
                        return new ResponseValue(response);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String movieid;
        private final String scaleddensity;
        private final String manufacturerId;
        private final String pvalue;
        private final String kvalue;
        private final String advertId;
        private final String type;

        public RequestValues(String movieid, String scaleddensity, String manufacturerId,
                String pvalue, String kvalue, String advertId, String type) {
            this.movieid = movieid;
            this.scaleddensity = scaleddensity;
            this.manufacturerId = manufacturerId;
            this.pvalue = pvalue;
            this.kvalue = kvalue;
            this.advertId = advertId;
            this.type = type;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final Response mResponse;

        public ResponseValue(Response response) {
            mResponse = response;
        }

        public Response getResponse() {
            return mResponse;
        }
    }
}
