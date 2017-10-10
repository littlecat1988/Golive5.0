package com.golive.cinema.topic.past;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.topic.domain.usecase.GetOldFilmTopicsUseCase;
import com.golive.network.entity.FilmTopic;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Administrator on 2017/6/2.
 */

public class PastTopicPresenter extends BasePresenter<PastTopicContract.View> implements
        PastTopicContract.Presenter {
    private GetOldFilmTopicsUseCase mGetOldFilmTopicsUseCase;

    public PastTopicPresenter(@NonNull PastTopicContract.View detailView,
            @NonNull GetOldFilmTopicsUseCase useCase) {
        checkNotNull(detailView);
        this.mGetOldFilmTopicsUseCase = checkNotNull(useCase);
        attachView(detailView);
        detailView.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getPastTopicList();
    }

    @Override
    public void getPastTopicList() {
        //获取往期专题
        Subscription subscription = mGetOldFilmTopicsUseCase.run(
                new GetOldFilmTopicsUseCase.RequestValues())
                .subscribe(new Subscriber<GetOldFilmTopicsUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getPastTopicList, onError : ");
                        PastTopicContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.showErrorView(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(GetOldFilmTopicsUseCase.ResponseValue responseValue) {
                        PastTopicContract.View view = getView();
                        if (view != null && view.isActive()) {
                            //后期要转成集合
                            List<FilmTopic> list = responseValue.getFilmTopics();
                            if (list == null || list.isEmpty()) {
                                view.showEmptyView();
                                return;
                            }
                            //更新数据
                            view.showPostTopicView(list);
                        }

                    }
                });
        addSubscription(subscription);
    }
}
