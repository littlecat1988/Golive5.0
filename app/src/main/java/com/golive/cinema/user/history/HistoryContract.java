package com.golive.cinema.user.history;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.HistoryMovie;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

/**
 * Created by Administrator on 2016/10/31.
 */

public class HistoryContract {


    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showResultListView(List<HistoryMovie> lists);

        void showResultError(String errorCode);

        void reFlashDelete(int position, boolean seccuss);

        void showMovieRecommend(List<MovieRecommendFilm> content);
    }

    interface Presenter extends IBasePresenter<View> {
        void deleteHistory(final int position, HistoryFilm film);
    }
}
