package com.golive.cinema.user.usercenter.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Wallet;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/20.
 */

public class GetUserCreditWalletUseCase extends
        UseCase<GetUserCreditWalletUseCase.RequestValues, GetUserCreditWalletUseCase
                .ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetUserCreditWalletUseCase(@NonNull UserDataSource dataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(dataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {

        if (requestValues.isForceUpdate()) {
            mUserDataSource.refreshCreditWallet();
        }

        return mUserDataSource.getCreditWallet()
                .map(new Func1<Wallet, ResponseValue>() {
                    @Override
                    public ResponseValue call(Wallet wallet) {
                        return new ResponseValue(wallet);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final boolean mForceUpdate;

        public RequestValues(boolean forceUpdate) {
            mForceUpdate = forceUpdate;
        }

        public boolean isForceUpdate() {
            return mForceUpdate;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final Wallet mWallet;

        public ResponseValue(Wallet wallet) {
            mWallet = wallet;
        }

        public Wallet getWallet() {
            return mWallet;
        }

    }
}
