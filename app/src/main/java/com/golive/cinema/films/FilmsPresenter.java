package com.golive.cinema.films;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.films.domain.usecase.GetFilmListUseCase;
import com.golive.cinema.films.domain.usecase.GetFilmsUseCase;
import com.golive.cinema.util.EspressoIdlingResource;
import com.golive.network.entity.Film;
import com.golive.network.response.FilmListResponse;
import com.google.gson.Gson;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Observer;
import rx.Subscription;

/**
 * Created by Wangzj on 2016/7/12.
 */

public class FilmsPresenter extends BasePresenter<FilmsContract.View> implements
        FilmsContract.Presenter {
    //private final static String TAG = FilmsPresenter.class.getSimpleName();
    private final GetFilmsUseCase mGetTasks;
    private final GetFilmListUseCase mFilmListUseCase;
    private FilmsFilterType mCurrentFiltering = FilmsFilterType.ALL_FILMS;
    private final String DEFAULT_STR = "{\"error\":{\"type\":\"false\"}," +
            "\"data\":{\"content\":[{},{},{},{},{},{},{},{},{},{}]}}";

    private boolean mFirstLoad = true;

    public FilmsPresenter(@NonNull FilmsContract.View filmsView,
            @NonNull GetFilmsUseCase getFilms,
            @NonNull GetFilmListUseCase getFilmListUsecase) {
        checkNotNull(filmsView, "filmsView cannot be null!");
        this.mGetTasks = checkNotNull(getFilms, "getFilms cannot be null!");
        mFilmListUseCase = checkNotNull(getFilmListUsecase, "getFilmListUseCase cannot be null!");
        attachView(filmsView);
        filmsView.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        loadFilms(false);
    }

    @Override
    public void loadFilms(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadFilms(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    @Override
    public void loadFilms() {//获取新接口数据Json
        Subscription subscription = mFilmListUseCase.run(new GetFilmListUseCase.RequestValues())
                .subscribe(new Observer<GetFilmListUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadFilms, onError : ");
                        FilmsContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showFilms(new Gson().fromJson(DEFAULT_STR, FilmListResponse.class));
                    }

                    @Override
                    public void onNext(GetFilmListUseCase.ResponseValue responseValue) {
                        FilmsContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        FilmListResponse response = responseValue.getFilmListResponse();
                        if (response != null && response.isOk()) {
                            view.showFilms(response);
                        } else {
                            view.showFilms(
                                    new Gson().fromJson(DEFAULT_STR, FilmListResponse.class));
                        }
                    }
                });

        addSubscription(subscription);
    }

    @Override
    public void openFilmDetail(@NonNull String filmId) {
        checkNotNull(filmId);
        getView().showFilmDetailUi(filmId);
    }

    private void loadFilms(boolean forceUpdate, final boolean showLoadingUI) {
        Logger.d("loadFilms, forceUpdate : " + forceUpdate);
        if (showLoadingUI) {
            getView().setLoadingIndicator(true);
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        GetFilmsUseCase.RequestValues requestValue =
                new GetFilmsUseCase.RequestValues(forceUpdate, mCurrentFiltering);

        Subscription subscription = mGetTasks.run(requestValue)
                .subscribe(new Observer<GetFilmsUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();

                        // The view may not be able to handle UI updates anymore
                        if (!getView().isActive()) {
                            return;
                        }

                        if (showLoadingUI) {
                            getView().setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EspressoIdlingResource.decrement();
                        Logger.e(e, "loadFilms, onError : ");

                        // The view may not be able to handle UI updates anymore
                        if (!getView().isActive()) {
                            return;
                        }

                        if (showLoadingUI) {
                            getView().setLoadingIndicator(false);
                        }

                        getView().showLoadingFilmsError();
                    }

                    @Override
                    public void onNext(GetFilmsUseCase.ResponseValue response) {

                        // The view may not be able to handle UI updates anymore
                        if (!getView().isActive()) {
                            return;
                        }

                        List<Film> filmList = response.getFilmList();
                        processTasks(filmList);
                    }
                });
        addSubscription(subscription);
    }

    private void processTasks(List<Film> filmList) {
        if (null == filmList) {

            // Show a message indicating there are no films.
            processEmptyTasks();
        } else {

            // Show the list of films.
            getView().showFilms(filmList);

            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ON_NOW_FILMS:
                getView().showOnNowFilterLabel();
                break;
            case UPCOMING_TASKS:
                getView().showUpComingFilterLabel();
                break;
            case ENDING_TASKS:
                getView().showEndingFilterLabel();
                break;
            default:
                getView().showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTasks() {
        switch (mCurrentFiltering) {
            case ON_NOW_FILMS:
                getView().showNoOnNowFilms();
                break;
            case UPCOMING_TASKS:
                getView().showNoUpcomingFilms();
                break;
            case ENDING_TASKS:
                getView().showEndingFilms();
                break;
            default:
                getView().showNoFilms();
                break;
        }
    }

    @Override
    public void setFiltering(FilmsFilterType filterType) {
        this.mCurrentFiltering = filterType;
    }

    @Override
    public FilmsFilterType getFiltering() {
        return mCurrentFiltering;
    }
}
