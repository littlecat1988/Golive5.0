package com.golive.cinema.filmlibrary;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.filmlibrary.dimain.usecase.GetFilmLibListUseCase;
import com.golive.cinema.filmlibrary.dimain.usecase.GetFilmLibTabUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/14.
 */

public class FilmLibraryPresenter extends BasePresenter<FilmLibraryContract.View> implements
        FilmLibraryContract.Presenter {

    private final GetFilmLibTabUseCase mGetFilmLibTabUseCase;
    private final GetFilmLibListUseCase mGetFilmLibListUseCase;
    private List<FilmLibTabResponse.Content> mFilmLibTabs;
    private final Map<String, List<FilmLibListResponse.Content>> mFilmLibListResponseMap2 =
            new HashMap<>();

    public FilmLibraryPresenter(@NonNull FilmLibraryContract.View view,
            @NonNull GetFilmLibTabUseCase getFilmLibTabUseCase,
            @NonNull GetFilmLibListUseCase getFilmLibListUseCase) {
        checkNotNull(view, "filmLibraryView cannot be null!");
        mGetFilmLibTabUseCase = checkNotNull(getFilmLibTabUseCase,
                "mGetFilmLibTabUseCase cannot be null!");
        mGetFilmLibListUseCase = checkNotNull(getFilmLibListUseCase,
                "mGetFilmLibListUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        loadFilmLibrary();
    }

    private void loadFilmLibrary() {
        getView().setLoadingIndicator(true);
        addSubscription(
                // load film library tabs
                getFilmLibTabContentsObs()
                        .doOnNext(new Action1<List<FilmLibTabResponse.Content>>() {
                            @Override
                            public void call(List<FilmLibTabResponse.Content> contents) {
                                FilmLibraryContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                // show tab
                                view.showTabs(contents);
                            }
                        })
                        .concatMap(new Func1<List<FilmLibTabResponse.Content>,
                                Observable<FilmLibTabResponse.Content>>() {
                            @Override
                            public Observable<FilmLibTabResponse.Content> call(
                                    List<FilmLibTabResponse.Content> contents) {
                                FilmLibraryContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return Observable.empty();
                                }

                                if (null == contents || contents.isEmpty()) {
                                    return Observable.empty();
                                }

                                // for-each tab
                                return Observable.from(contents);
                            }
                        })
                        .concatMap(new Func1<FilmLibTabResponse.Content,
                                Observable<List<FilmLibListResponse.Content>>>() {
                            @Override
                            public Observable<List<FilmLibListResponse.Content>> call(
                                    FilmLibTabResponse.Content content) {
                                String id = content.getId();
                                // get film list of the tab-id
                                return getFilmLibListObs(id)
                                        .onErrorReturn(new Func1<Throwable, List<FilmLibListResponse
                                                .Content>>() {
                                            @Override
                                            public List<FilmLibListResponse.Content> call(
                                                    Throwable t) {
                                                Logger.w(t, "getFilmLibListObs, onErrorReturn : ");
                                                return null;
                                            }
                                        });
                            }
                        })
                        .subscribe(new Subscriber<List<FilmLibListResponse.Content>>() {
                            @Override
                            public void onCompleted() {
                                FilmLibraryContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                view.setLoadingIndicator(false);
                                // show film
                                view.showFilm();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.e(e, "loadFilmLibrary, onError : ");
                                FilmLibraryContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                view.setLoadingIndicator(false);
                                view.showError(e.getMessage());
                            }

                            @Override
                            public void onNext(List<FilmLibListResponse.Content> response) {
                            }
                        }));
    }

    @Override
    public void loadFilmsByTabId(final String tabId) {
        loadFilmsByIdInternal(tabId);
    }

    private void loadFilmsByIdInternal(final String tabId) {
        Logger.d("loadFilmsByIdInternal, tabId : " + tabId);
        if (StringUtils.isNullOrEmpty(tabId)) {
            return;
        }

        List<FilmLibListResponse.Content> contents;
        if (mFilmLibListResponseMap2 != null && (contents = mFilmLibListResponseMap2.get(tabId))
                != null) {
            // show film
            getView().showFilms(null, contents);
            return;
        }

        getView().setLoadingIndicator(true);
        addSubscription(getFilmLibListObs(tabId)
                .subscribe(new Subscriber<List<FilmLibListResponse.Content>>() {
                    @Override
                    public void onCompleted() {
                        FilmLibraryContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadFilmsByIdInternal, onError : ");
                        FilmLibraryContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        view.setLoadingIndicator(false);
                        view.showLoadFilmsByIdFailed(tabId, e.getMessage());
                    }

                    @Override
                    public void onNext(List<FilmLibListResponse.Content> contents) {
                        FilmLibraryContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        view.showFilms(tabId, contents);
                    }
                }));
    }

    private Observable<List<FilmLibTabResponse.Content>> getFilmLibTabContentsObs() {
        if (mFilmLibTabs != null) {
            return Observable.just(mFilmLibTabs);
        }

        return mGetFilmLibTabUseCase.run(new GetFilmLibTabUseCase.RequestValues())
                .map(new Func1<GetFilmLibTabUseCase.ResponseValue, List<FilmLibTabResponse
                        .Content>>() {
                    @Override
                    public List<FilmLibTabResponse.Content> call(GetFilmLibTabUseCase
                            .ResponseValue responseValue) {
                        FilmLibraryContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return null;
                        }

                        FilmLibTabResponse response = responseValue.getResponse();
                        if (response != null && response.isOk() && response.getData() != null
                                && response.getData().getContent() != null) {
                            mFilmLibTabs = response.getData().getContent();
                        }

                        return mFilmLibTabs;
                    }
                });
    }

    private Observable<List<FilmLibListResponse.Content>> getFilmLibListObs(
            @NonNull final String tabId) {
        List<FilmLibListResponse.Content> responseValue;
        if (mFilmLibListResponseMap2 != null && (responseValue = mFilmLibListResponseMap2.get(
                tabId))
                != null) {
            return Observable.just(responseValue);
        }

        return mGetFilmLibListUseCase.run(new GetFilmLibListUseCase.RequestValues(tabId))
                .map(new Func1<GetFilmLibListUseCase.ResponseValue, List<FilmLibListResponse
                        .Content>>() {
                    @Override
                    public List<FilmLibListResponse.Content> call(
                            GetFilmLibListUseCase.ResponseValue responseValue) {
                        if (responseValue != null && responseValue.getResponse() != null &&
                                responseValue.getResponse().isOk()) {
                            FilmLibListResponse.Data data = responseValue.getResponse().getData();
                            if (data != null && data.getContent() != null) {
                                List<FilmLibListResponse.Content> contents = new ArrayList<>(
                                        data.getContent());
                                sortFilms(contents);
                                mFilmLibListResponseMap2.put(tabId, contents);
                                return contents;
                            }
                        }

                        return null;
                    }
                });
    }

    private void sortFilms(List<FilmLibListResponse.Content> contents) {
        //"影片"排序，第4,5,6个放到列表最后
        if (contents.size() > 7) {
            contents.add(contents.get(6));
            contents.add(contents.get(5));
            contents.add(contents.get(4));
            contents.remove(4);
            contents.remove(4);
            contents.remove(4);
        }
    }

}