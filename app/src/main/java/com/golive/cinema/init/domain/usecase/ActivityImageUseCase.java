package com.golive.cinema.init.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ActivityImage;
import com.golive.network.entity.Poster;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/9.
 */

public class ActivityImageUseCase extends
        UseCase<ActivityImageUseCase.RequestValues, ActivityImageUseCase.ResponseValue> {

    @NonNull
    private final ServerInitDataSource mServerInitDataSource;

    public ActivityImageUseCase(@NonNull ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mServerInitDataSource = serverInitDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            RequestValues requestValues) {
        return mServerInitDataSource.queryActivityPoster()
                .map(new Func1<ActivityImage, ResponseValue>() {
                    @Override
                    public ResponseValue call(ActivityImage activityImage) {
                        Poster poster = null;
                        if (activityImage != null && activityImage.getItemList() != null) {
                            if (activityImage.getItemList().size() > 0) {
                                poster = activityImage.getItemList().get(0);
                                poster.setEmergetimes(activityImage.getEmergetimes());
                                poster.setPrompt(activityImage.getPrompt());
                                return new ResponseValue(poster);
                            }
                        }
                        return new ResponseValue(poster);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final Poster poster;

        public ResponseValue(Poster poster) {
            this.poster = poster;
        }

        public Poster getPoster() {
            return poster;
        }
    }

}
