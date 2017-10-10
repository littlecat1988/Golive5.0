package com.golive.cinema.filmlibrary;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;

import java.util.List;

/**
 * Created by Administrator on 2016/11/14.
 */

public interface FilmLibraryContract {
    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

//        void showTab(FilmLibTabResponse tab);

        void showTabs(List<FilmLibTabResponse.Content> contents);

        void showFilm();

        void showFilms(String tabId, List<FilmLibListResponse.Content> filmContents);

        void showError(String errMsg);

        void showLoadFilmsByIdFailed(String tabId, String errMsg);
    }

    interface Presenter extends IBasePresenter<View> {
//        void loadTab();

        /**
         * load films by tag id
         *
         * @param tabId tag id
         */
        void loadFilmsByTabId(String tabId);

//        FilmLibListResponse getFilmList(int index);
    }
}