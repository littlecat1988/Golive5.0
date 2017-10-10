package com.golive.cinema.recommend;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.data.source.remote.RecommendRemoteDataSource;
import com.golive.cinema.recommend.domain.usecase.GetRecommendUseCase;
import com.golive.network.response.RecommendResponse;
import com.google.gson.Gson;
import com.initialjie.log.Logger;

import rx.Observer;
import rx.Subscription;


/**
 * Created by Administrator on 2016/11/4.
 */

public class RecommendPresenter extends BasePresenter<RecommendContract.View> implements
        RecommendContract.Presenter {
    private final GetRecommendUseCase mGetRecommendUseCase;
    private final String DEFAULT_STR = "{\"error\":{\"type\":\"false\"," +
            "\"note\":\"\",\"notemsg\":\"\"},\"layout\":{\"items\":[" +
            "{\"location\":{\"x\":0,\"y\":0,\"w\":2,\"h\":2}}," +
            "{\"location\":{\"x\":0,\"y\":2,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":1,\"y\":2,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":2,\"y\":0,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":2,\"y\":1,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":2,\"y\":2,\"w\":2,\"h\":1}}," +
            "{\"location\":{\"x\":3,\"y\":0,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":3,\"y\":1,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":4,\"y\":0,\"w\":1,\"h\":3}}," +
            "{\"location\":{\"x\":5,\"y\":0,\"w\":2,\"h\":2}}," +
            "{\"location\":{\"x\":5,\"y\":2,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":6,\"y\":2,\"w\":1,\"h\":1}}," +
            "{\"location\":{\"x\":7,\"y\":0,\"w\":1,\"h\":3}}]}}";

    public RecommendPresenter(@NonNull RecommendContract.View view,
            @NonNull GetRecommendUseCase getRecommend) {
        checkNotNull(view, "RecommendView cannot be null!");
        mGetRecommendUseCase = checkNotNull(getRecommend, "getRecommendUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void loadRecommend(String pageId) {
        Subscription subscription = mGetRecommendUseCase.run(
                new GetRecommendUseCase.RequestValues(
                        RecommendRemoteDataSource.PAGE_RECOMMEND, pageId))
                .subscribe(new Observer<GetRecommendUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "load RecommendLayout onError : ");
                        RecommendContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        //getView().showError("获取推荐页数据异常!");
                        view.showView(
                                new Gson().fromJson(DEFAULT_STR, RecommendResponse.class));
                    }

                    @Override
                    public void onNext(GetRecommendUseCase.ResponseValue responseValue) {
                        RecommendContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        RecommendResponse response = responseValue.getResponse();
                        if (response != null && response.isOk()) {
                            view.showView(response);
                        } else {
                            //getView().showError(response.getError().getNotemsg());
                            view.showView(
                                    new Gson().fromJson(DEFAULT_STR, RecommendResponse.class));
                        }
                    }
                });
        addSubscription(subscription);
    }
}
