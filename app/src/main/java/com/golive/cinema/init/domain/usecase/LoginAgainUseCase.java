package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Login;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/9.
 */

public class LoginAgainUseCase extends
        UseCase<LoginAgainUseCase.RequestValues, LoginAgainUseCase.ResponseValue> {


    @NonNull
    private final UserDataSource mUserDataSource;


    public LoginAgainUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mUserDataSource = userDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mUserDataSource.loginAgain(requestValues.getUserId(), requestValues.getPw(),
                requestValues.getPhone(), requestValues.getVerifyCode(),
                requestValues.getStatus(), requestValues.getLogontype(),
                requestValues.getBranchType(), requestValues.getKdmVersion(),
                requestValues.getKdmPlatform())
                .map(new Func1<Login, ResponseValue>() {
                    @Override
                    public ResponseValue call(Login login) {
                        return new ResponseValue(login);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String userId;
        private final String pw;
        private final String phone;
        private final String verifyCode;
        private final String status;
        private final String logontype;
        private final String branchType;
        private final String kdmVersion;
        private final String kdmPlatform;

        public RequestValues(String userId, String pw, String phone, String verifyCode,
                String status, String logontype, String branchType, String kdmVersion,
                String kdmPlatform) {
            this.userId = userId;
            this.pw = pw;
            this.phone = phone;
            this.verifyCode = verifyCode;
            this.status = status;
            this.logontype = logontype;
            this.branchType = branchType;
            this.kdmVersion = kdmVersion;
            this.kdmPlatform = kdmPlatform;
        }

        public String getUserId() {
            return userId;
        }

        public String getPw() {
            return pw;
        }

        public String getPhone() {
            return phone;
        }

        public String getVerifyCode() {
            return verifyCode;
        }

        public String getStatus() {
            return status;
        }

        public String getLogontype() {
            return logontype;
        }

        public String getBranchType() {
            return branchType;
        }

        public String getKdmVersion() {
            return kdmVersion;
        }

        public String getKdmPlatform() {
            return kdmPlatform;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final Login mLogin;

        public ResponseValue(Login login) {
            mLogin = login;
        }

        public Login getLogin() {
            return mLogin;
        }
    }

}
