package com.golive.cinema.topic.details;

import android.support.annotation.NonNull;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

/**
 * Created by Administrator on 2017/5/22.
 */

public class SpecialDetailsContract {
    interface View extends IBaseView<Presenter> {
        void showErrorView(String errMsg);
        void showEmptyView();
        void showDetailView(List<MovieRecommendFilm> list,String url);

    }
    interface Presenter extends IBasePresenter<View> {
        void loadTopicDetail(@NonNull final String detailId);
    }
}
