package com.golive.cinema.films;

import android.support.annotation.NonNull;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.Film;
import com.golive.network.response.FilmListResponse;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 * Created by Wangzj on 2016/7/8.
 */
public interface FilmsContract {

    interface View extends IBaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showFilms(List<Film> films);

        void showFilms(FilmListResponse films);

        void showFilmDetailUi(@NonNull String filmId);

        void showLoadingFilmsError();

        void showAllFilterLabel();

        void showOnNowFilterLabel();

        void showUpComingFilterLabel();

        void showEndingFilterLabel();

        void showNoFilms();

        void showNoOnNowFilms();

        void showNoUpcomingFilms();

        void showEndingFilms();
    }

    interface Presenter extends IBasePresenter<View> {

        void loadFilms(boolean forceUpdate);

        void loadFilms();

        void openFilmDetail(@NonNull String filmId);

        void setFiltering(FilmsFilterType requestType);

        FilmsFilterType getFiltering();
    }
}