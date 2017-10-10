package com.golive.cinema.topic.details;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.topic.domain.usecase.GetFilmTopicDetailUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.FilmTopic;
import com.golive.network.entity.MovieRecommendFilm;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Subscriber;

/**
 * Created by Administrator on 2017/5/22.
 */

public class SpecialDetaliPresenter extends BasePresenter<SpecialDetailsContract.View> implements
        SpecialDetailsContract.Presenter {
    //    private  GetDetialUseCase mUseCase;
    private final GetFilmTopicDetailUseCase mGetFilmTopicDetailUseCase;

    public SpecialDetaliPresenter(@NonNull SpecialDetailsContract.View detailView,
            GetFilmTopicDetailUseCase getFilmTopicDetailUseCase) {
//        mUseCase = checkNotNull(useCase);
        mGetFilmTopicDetailUseCase = checkNotNull(getFilmTopicDetailUseCase);
        checkNotNull(detailView);
        attachView(detailView);
        detailView.setPresenter(this);
    }

    @Override
    public void loadTopicDetail(@NonNull final String detailId) {
        if (StringUtils.isNullOrEmpty(detailId)) {
            getView().showEmptyView();
            return;
        }

        addSubscription(mGetFilmTopicDetailUseCase.run(
                new GetFilmTopicDetailUseCase.RequestValues(detailId))
                .subscribe(new Subscriber<GetFilmTopicDetailUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Logger.e(throwable, "loadTopicDetail error : ");
                        SpecialDetailsContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.showErrorView(throwable.getMessage());
                        }
                    }

                    @Override
                    public void onNext(GetFilmTopicDetailUseCase.ResponseValue responseValue) {
                        SpecialDetailsContract.View view = getView();
                        if (view != null && view.isActive()) {
                            FilmTopic filmTopic = responseValue.getFilmTopic();
                            if (null == filmTopic || null == filmTopic.getMovies()
                                    || filmTopic.getMovies().isEmpty()) {
                                view.showEmptyView();
                                return;
                            }
                            List<MovieRecommendFilm> list = filmTopic.getMovies();
                            view.showDetailView(list, filmTopic.getBackgroundposter());
                        }
                    }
                }));
    }
}
