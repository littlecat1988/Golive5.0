package com.golive.cinema.user.history.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.HistoryMovie;
import com.golive.network.response.HistoryResponse;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/10/31.
 */

public class GetHistoryListUseCase extends
        UseCase<GetHistoryListUseCase.RequestValues, GetHistoryListUseCase.ResponseValue> {


    private final UserDataSource mUserDataSource;

    public GetHistoryListUseCase(@NonNull UserDataSource userDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mUserDataSource = checkNotNull(userDataSource, "dataSource cannot be null!");
    }

    @Override
    protected Observable<GetHistoryListUseCase.ResponseValue> executeUseCase(
            GetHistoryListUseCase.RequestValues requestValues) {

        if (requestValues.getRedo()) {
            mUserDataSource.redoGetHistoryList();
        }

        return mUserDataSource.GetHistoryListNews("", "2", "", "")
                .map(new Func1<HistoryResponse, GetHistoryListUseCase.ResponseValue>() {
                    @Override
                    public GetHistoryListUseCase.ResponseValue call(HistoryResponse lists) {
                        return new GetHistoryListUseCase.ResponseValue(lists);
                    }
                });

//        return mUserDataSource.GetHistoryList("","2","","")
//                .map(new Func1<List<Order>, GetHistoryListUseCase.ResponseValue>() {
//                    @Override
//                    public GetHistoryListUseCase.ResponseValue call(List<Order> lists) {
//                        return new GetHistoryListUseCase.ResponseValue(lists);
//                    }
//                });
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

        private final HistoryResponse mList;

        public ResponseValue(HistoryResponse lists) {
            mList = lists;
        }

        public List<HistoryMovie> getHistoryList() {
            if (mList != null) {
                return mList.getList();
            }
            return null;
        }
//        private final List<Order> mList;
//
//        public ResponseValue(List<Order> lists) {
//            mList = lists;
//        }
//
//        public List<Order> getHistoryList() {
//            return mList;
//        }
    }
}


