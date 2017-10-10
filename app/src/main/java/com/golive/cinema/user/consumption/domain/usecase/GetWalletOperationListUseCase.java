package com.golive.cinema.user.consumption.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.WalletOperationItem;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/10/31.
 */

public class GetWalletOperationListUseCase extends
        UseCase<GetWalletOperationListUseCase.RequestValues, GetWalletOperationListUseCase
                .ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetWalletOperationListUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<GetWalletOperationListUseCase.ResponseValue> executeUseCase(
            GetWalletOperationListUseCase.RequestValues requestValues) {

        if (requestValues.getRedo()) {
            mUserDataSource.redoGetWalletOperationList();
        }

        return mUserDataSource.GetWalletOperationList()
                .map(new Func1<List<WalletOperationItem>, GetWalletOperationListUseCase
                        .ResponseValue>() {
                    @Override
                    public GetWalletOperationListUseCase.ResponseValue call(
                            List<WalletOperationItem> WalletOperationItemList) {
                        return new GetWalletOperationListUseCase.ResponseValue(
                                WalletOperationItemList);
                    }
                });

    }

    public static final class RequestValues implements UseCase.RequestValues {
        private boolean redoMode = false;

        public RequestValues(boolean redo) {
            redoMode = redo;
        }

        public boolean getRedo() {
            return redoMode;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final List<WalletOperationItem> mList;

        public ResponseValue(List<WalletOperationItem> lists) {
            mList = lists;
        }

        public List<WalletOperationItem> getWalletOperationItemList() {
            return mList;
        }
    }
}
