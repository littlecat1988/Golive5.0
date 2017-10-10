package com.golive.cinema.topic;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.FilmTopic;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 * Created by Wangzj on 2017/5/23.
 */

public interface TopicContract {
    interface View extends IBaseView<TopicContract.Presenter> {
        /**
         * show film topics
         *  @param recommendFilms film topics
         * @param hasMoreTopics  has more film topics?
         */
        void showTopics(List<FilmTopic> recommendFilms, boolean hasMoreTopics);
    }

    interface Presenter extends IBasePresenter<View> {

    }
}
