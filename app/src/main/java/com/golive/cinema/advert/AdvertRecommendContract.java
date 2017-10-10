package com.golive.cinema.advert;

import android.support.annotation.Nullable;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

/**
 * Created by chgnag on 2017/1/9.
 */

public interface AdvertRecommendContract {

    interface View extends IBaseView<Presenter> {
        void showRecommendMovieList(List<MovieRecommendFilm> recommendFilmList);

        void setLoadingRecommendIndicator(boolean active);
    }


    interface Presenter extends IBasePresenter<View> {
        /**
         * recommend movie source
         */
        void loadRecommendFilmList(String filmId);

        /**
         * report advert to server
         */
        void reportAdvertMiaozhen(String filmId, String advertId, String adReportUrl,
                @Nullable String manufacturerId, @Nullable String mac, String scaleddensity);
    }
}
