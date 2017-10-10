package com.golive.cinema.topic;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.topic.domain.usecase.GetRecommendFilmTopicsUseCase;
import com.golive.network.entity.FilmTopic;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Subscriber;

/**
 * Created by Wangzj on 2017/5/23.
 */

public class TopicPresenter extends BasePresenter<TopicContract.View> implements
        TopicContract.Presenter {

    @NonNull
    private final GetRecommendFilmTopicsUseCase mGetRecommendFilmTopicsUseCase;

    public TopicPresenter(@NonNull TopicContract.View view,
            @NonNull GetRecommendFilmTopicsUseCase getRecommendFilmTopicsUseCase) {
        mGetRecommendFilmTopicsUseCase = checkNotNull(getRecommendFilmTopicsUseCase);
        attachView(checkNotNull(view));
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        loadRecommendTopics();
    }

    private void loadRecommendTopics() {
        addSubscription(mGetRecommendFilmTopicsUseCase.run(
                new GetRecommendFilmTopicsUseCase.RequestValues())
                .subscribe(new Subscriber<GetRecommendFilmTopicsUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadRecommendTopics, onError : ");
                    }

                    @Override
                    public void onNext(GetRecommendFilmTopicsUseCase.ResponseValue responseValue) {
//                        // get topics
//                        List<FilmTopic> filmTopics = new ArrayList<>();
//                        for (int i = 0; i < 10; i++) {
//                            filmTopics.add(new FilmTopic());
//                        }

                        TopicContract.View view = getView();
                        if (view != null && view.isActive()) {
                            List<FilmTopic> filmTopics = responseValue.getFilmTopics();
                            // show topics
                            view.showTopics(filmTopics, true);
                        }
                    }
                }));

    }
}
