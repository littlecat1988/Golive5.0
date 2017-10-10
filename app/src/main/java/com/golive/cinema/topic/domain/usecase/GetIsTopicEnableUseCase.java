package com.golive.cinema.topic.domain.usecase;


import static com.golive.cinema.Constants.PAGE_INDEX_TOPIC;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.init.domain.usecase.AppPageUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.response.ApplicationPageResponse;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class GetIsTopicEnableUseCase extends
        UseCase<GetIsTopicEnableUseCase.RequestValues, GetIsTopicEnableUseCase.ResponseValue> {

    private final AppPageUseCase mAppPageUseCase;

    public GetIsTopicEnableUseCase(@NonNull AppPageUseCase pageUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mAppPageUseCase = checkNotNull(pageUseCase, "dataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {
        return mAppPageUseCase.run(new AppPageUseCase.RequestValues())
                .map(new Func1<AppPageUseCase.ResponseValue, ResponseValue>() {
                    @Override
                    public ResponseValue call(AppPageUseCase.ResponseValue responseValue) {
                        Boolean isEnable = isTopicEnable(responseValue);
                        return new ResponseValue(isEnable);
                    }
                });
    }

    @NonNull
    private Boolean isTopicEnable(AppPageUseCase.ResponseValue responseValue) {
        if (responseValue != null && responseValue.getResponse() != null) {
            ApplicationPageResponse response = responseValue.getResponse();
            if (response != null && response.isOk() && response.getApplicationPage() != null
                    && response.getApplicationPage().getBasePage() != null
                    && response.getApplicationPage().getBasePage().getNavigation() != null) {
                List<ApplicationPageResponse.Data> datas =
                        response.getApplicationPage().getBasePage().getNavigation().getDatas();
                final String topicStr = String.valueOf(PAGE_INDEX_TOPIC);
                for (ApplicationPageResponse.Data data : datas) {
                    String action = data.getActionContent();
                    if (!StringUtils.isNullOrEmpty(action) && topicStr.equals(action)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static final class RequestValues implements UseCase.RequestValues {
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final Boolean mIsEnable;

        public ResponseValue(Boolean isEnable) {
            mIsEnable = isEnable;
        }

        public Boolean getEnable() {
            return mIsEnable;
        }
    }
}
