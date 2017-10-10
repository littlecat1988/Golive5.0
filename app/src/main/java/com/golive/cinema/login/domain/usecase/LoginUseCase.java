package com.golive.cinema.login.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Login;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/7/25.
 */

public class LoginUseCase extends UseCase<LoginUseCase.RequestValues, LoginUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public LoginUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mUserDataSource = checkNotNull(userDataSource, "FilmsDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mUserDataSource.login(requestValues.getUserId(), requestValues.getPw(),
                requestValues.getStatus(), requestValues.getBranchType(),
                requestValues.getKdmVersion(), requestValues.getKdmPlatform())
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
        private final String status;
        private final String branchType;
        private final String kdmVersion;
        private final String kdmPlatform;

        public RequestValues(String userId, String pw, String status, String branchtype,
                String kdmVersion, String kdmPlatform) {
            this.branchType = branchtype;
            this.userId = userId;
            this.pw = pw;
            this.status = status;
            this.kdmVersion = kdmVersion;
            this.kdmPlatform = kdmPlatform;
        }

        public String getBranchType() {
            return branchType;
        }

        public String getKdmPlatform() {
            return kdmPlatform;
        }

        public String getKdmVersion() {
            return kdmVersion;
        }

        public String getPw() {
            return pw;
        }

        public String getStatus() {
            return status;
        }

        public String getUserId() {
            return userId;
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
