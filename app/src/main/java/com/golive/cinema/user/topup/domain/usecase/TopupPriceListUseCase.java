package com.golive.cinema.user.topup.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.TopupRechargeItem;
import com.golive.network.response.TopupRechargeResponse;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;


/**
 * Created by Mwling on 2016/10/31.
 */

public class TopupPriceListUseCase extends
        UseCase<TopupPriceListUseCase.RequestValues, TopupPriceListUseCase.ResponseValue> {


    private final UserDataSource mUserDataSource;

    public TopupPriceListUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<TopupPriceListUseCase.ResponseValue> executeUseCase(
            TopupPriceListUseCase.RequestValues requestValues) {


        return mUserDataSource.GetTopupPriceListNews()
                .map(new Func1<TopupRechargeResponse, TopupPriceListUseCase.ResponseValue>() {
                    @Override
                    public TopupPriceListUseCase.ResponseValue call(TopupRechargeResponse lists) {
                        return new TopupPriceListUseCase.ResponseValue(lists);
                    }
                });
    }

    public static final class RequestValues implements UseCase.RequestValues {
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final TopupRechargeResponse mList;

        public ResponseValue(TopupRechargeResponse lists) {
            mList = lists;
        }

        public List<TopupRechargeItem> getTopupPriceList() {
            return mList.getPriceList();
        }
    }
}
