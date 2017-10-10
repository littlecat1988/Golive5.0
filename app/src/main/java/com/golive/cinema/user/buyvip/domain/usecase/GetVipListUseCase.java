package com.golive.cinema.user.buyvip.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.VipCombo;
import com.golive.network.response.VipComboResponse;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;


/**
 * Created by Mwling on 2016/10/31.
 */

public class GetVipListUseCase
        extends UseCase<GetVipListUseCase.RequestValues, GetVipListUseCase.ResponseValue> {

    private final UserDataSource mUserDataSource;

    public GetVipListUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<GetVipListUseCase.ResponseValue> executeUseCase(
            GetVipListUseCase.RequestValues requestValues) {

        if (requestValues.getRedo()) {
            mUserDataSource.redoGetVipPackageList();
        }
        return mUserDataSource.GetVipPackageListNews()
                .map(new Func1<VipComboResponse, ResponseValue>() {
                    @Override
                    public ResponseValue call(VipComboResponse vipPackageList) {
                        return new ResponseValue(vipPackageList);
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
        private final VipComboResponse mVipPackageList;

        public ResponseValue(VipComboResponse viplist) {
            mVipPackageList = viplist;
        }

        public List<VipCombo> getVipPackageList() {
            if (mVipPackageList != null) {
                if (mVipPackageList.getList() != null) {
                    return mVipPackageList.getList();
                }
            }
            return null;
        }

        public VipComboResponse getVipComboResponse() {
            return mVipPackageList;
        }
    }
}
