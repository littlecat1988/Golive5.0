package com.golive.cinema.init;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.Combo;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.Poster;
import com.golive.network.entity.UserInfo;

import java.util.List;

import rx.Observable;


/**
 * Created by chgang on 2016/11/9.
 */

public interface HomeContract {

    interface View extends IBaseView<HomeContract.Presenter> {
        void showActivityImageView(Poster poster);

        void setLoadingExitRecommendIndicator(boolean active);

        void showVipMenuView(Combo combo);

        void showExitRecommendView(List<MovieRecommendFilm> recommendFilmList);

        void reportAppStart(UserInfo userInfo);

        void reportHardwareInfo();
        void showDrainage(String guideType);
    }

    interface Presenter extends IBasePresenter<HomeContract.View> {
        void initHomeData();
        void exitGuide();
        void loadExitData();

        void reportAppStart(String caller, String destination, String netType, String osVersion,
                String versionCode, String filmId, String userStatus, String duration);
    }
}
