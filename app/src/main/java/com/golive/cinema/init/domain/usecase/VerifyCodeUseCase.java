package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.VerifyCodeDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Response;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/9.
 */

public class VerifyCodeUseCase extends
        UseCase<VerifyCodeUseCase.RequestValues, VerifyCodeUseCase.ResponseValue> {


    private final VerifyCodeDataSource mVerifyCodeDataSource;

    public VerifyCodeUseCase(@NonNull VerifyCodeDataSource verifyCodeDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mVerifyCodeDataSource = verifyCodeDataSource;
    }


    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mVerifyCodeDataSource.getVerifyCode(requestValues.getPhone())
                .map(new Func1<Response, ResponseValue>() {
                    @Override
                    public ResponseValue call(Response response) {
                        return new ResponseValue(response != null && response.isOk());
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String phone;

        public RequestValues(String phone) {
            this.phone = phone;
        }

        public String getPhone() {
            return phone;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final Boolean success;

        public ResponseValue(Boolean success) {
            this.success = success;
        }

        public Boolean getSuccess() {
            return success;
        }
    }

}
