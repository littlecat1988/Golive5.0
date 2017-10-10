package com.golive.cinema.recommend;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.response.RecommendResponse;

/**
 * Created by Administrator on 2016/11/4.
 */

public interface RecommendContract {

    interface View extends IBaseView<Presenter> {
        void showView(RecommendResponse response);

        void showError(String msg);
    }

    interface Presenter extends IBasePresenter<View> {
        void loadRecommend(String pageId);
    }
}