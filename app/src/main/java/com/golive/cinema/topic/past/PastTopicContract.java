package com.golive.cinema.topic.past;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.FilmTopic;

import java.util.List;

/**
 * Created by Administrator on 2017/6/2.
 */

public class PastTopicContract {
    interface View extends IBaseView<PastTopicContract.Presenter> {
        void showErrorView(String errMsg);
        void showEmptyView();
        void showPostTopicView(List<FilmTopic> list);

    }
    interface Presenter extends IBasePresenter<PastTopicContract.View> {
        void getPastTopicList();
    }
}
