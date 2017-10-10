package com.golive.cinema.user.history;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.user.history.domain.usecase.DeleteHistoryUseCase;
import com.golive.cinema.user.history.domain.usecase.GetHistoryListUseCase;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Administrator on 2016/10/31.
 */

public class HistoryPresenter extends BasePresenter<HistoryContract.View> implements
        HistoryContract.Presenter {
    private final GetHistoryListUseCase getListUseCase;
    private final DeleteHistoryUseCase deleteUseCase;
    private final GetMovieRecommendUseCase mGetMovieRecommendUseCase;

    public HistoryPresenter(@NonNull HistoryContract.View view,
            @NonNull GetHistoryListUseCase getlistTask,
            @NonNull DeleteHistoryUseCase deleteTask,
            @NonNull GetMovieRecommendUseCase getMovieRecommendTask) {
        checkNotNull(view, "BuyVipView cannot be null!");
        this.getListUseCase = checkNotNull(getlistTask, "GetHistoryListUseCase cannot be null!");
        this.deleteUseCase = checkNotNull(deleteTask, "deleteUseCase cannot be null!");
        this.mGetMovieRecommendUseCase = checkNotNull(getMovieRecommendTask,
                "deleteUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        loadHistory();
    }

    private void loadHistory() {
        Logger.d("loadHistory start");
        getView().setLoadingIndicator(true);
        Observable<GetHistoryListUseCase.ResponseValue> getHistListObs = getListUseCase.run(
                new GetHistoryListUseCase.RequestValues(true));
        Observable<GetMovieRecommendUseCase.ResponseValue> getMovieRecommObs =
                getMovieRecommendObs();
        Subscription subscription = getHistListObs.zipWith(getMovieRecommObs,
                new Func2<GetHistoryListUseCase.ResponseValue, GetMovieRecommendUseCase
                        .ResponseValue, Pair<GetHistoryListUseCase.ResponseValue,
                        GetMovieRecommendUseCase.ResponseValue>>() {
                    @Override
                    public Pair<GetHistoryListUseCase.ResponseValue, GetMovieRecommendUseCase
                            .ResponseValue> call(GetHistoryListUseCase.ResponseValue responseValue,
                            GetMovieRecommendUseCase.ResponseValue response2) {
                        return new Pair<>(responseValue, response2);
                    }
                })
                .subscribe(
                        new Subscriber<Pair<GetHistoryListUseCase.ResponseValue,
                                GetMovieRecommendUseCase.ResponseValue>>() {
                            @Override
                            public void onCompleted() {
                                HistoryContract.View view = getView();
                                if (view != null && view.isActive()) {
                                    view.setLoadingIndicator(false);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.e(e, "loadHistory, onError : ");
                                HistoryContract.View view = getView();
                                if (view != null && view.isActive()) {
                                    view.setLoadingIndicator(false);
                                    view.showResultError(e.getMessage());
                                }
                            }

                            @Override
                            public void onNext(Pair<GetHistoryListUseCase.ResponseValue,
                                    GetMovieRecommendUseCase.ResponseValue> responsePair) {
                                GetHistoryListUseCase.ResponseValue response1 = responsePair.first;
                                GetMovieRecommendUseCase.ResponseValue response2 =
                                        responsePair.second;
                                HistoryContract.View view = getView();
                                if (view != null && view.isActive()) {
                                    if (response1 != null) {
                                        view.showResultListView(response1.getHistoryList());
                                    }
                                    if (response2 != null) {
                                        view.showMovieRecommend(response2.getContentList());
                                    }
                                }
                            }
                        });
        addSubscription(subscription);
    }

    public void deleteHistory(final int position, HistoryFilm film) {
        getView().setLoadingIndicator(true);
        DeleteHistoryUseCase.RequestValues Task = new DeleteHistoryUseCase.RequestValues(
                film.getOrderSerial(), "Remove film:" + film.getName());
        Subscription subscription = deleteUseCase.run(Task)
                .subscribe(new Subscriber<DeleteHistoryUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("deleteHistory onCompleted");
                        HistoryContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "deleteHistory onError : ");
                        HistoryContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                            reFlashDelete(position, false);
                        }
                    }

                    @Override
                    public void onNext(DeleteHistoryUseCase.ResponseValue responseValue) {
                        Logger.d("deleteHistory onNext");
                        HistoryContract.View view = getView();
                        if (view != null && view.isActive()) {
//                            Order order = responseValue.getHistoryOrder();
                            reFlashDelete(position, true);
                        }
                    }
                });
        addSubscription(subscription);
    }

    private void reFlashDelete(int position, boolean sucess) {
        getView().reFlashDelete(position, sucess);
    }

    private Observable<GetMovieRecommendUseCase.ResponseValue> getMovieRecommendObs() {
        return mGetMovieRecommendUseCase.run(new GetMovieRecommendUseCase.RequestValues(""))
                .onErrorReturn(new Func1<Throwable, GetMovieRecommendUseCase.ResponseValue>() {
                    @Override
                    public GetMovieRecommendUseCase.ResponseValue call(Throwable throwable) {
                        Logger.e(throwable, "getGetMovieRecommend onErrorReturn : ");
                        return null;
                    }
                });
    }
}
