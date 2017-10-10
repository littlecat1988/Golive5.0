package com.golive.cinema.user.myinfo;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.domain.usecase.GetAgreementUseCase;
import com.golive.network.entity.AllAgreement;
import com.initialjie.log.Logger;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Wangzj on 2017/3/6.
 */

public class PayServiceAgreementPresenter extends
        BasePresenter<PayServiceAgreementContract.View> implements
        PayServiceAgreementContract.Presenter {

    private final GetAgreementUseCase mGetAgreementUseCase;

    public PayServiceAgreementPresenter(@NonNull PayServiceAgreementContract.View view,
            @NonNull GetAgreementUseCase getAgreementUseCase) {
        mGetAgreementUseCase = checkNotNull(getAgreementUseCase,
                "GetAgreementUseCase can not be null!");
        attachView(checkNotNull(view, "view can not be null!"));
        view.setPresenter(this);
    }

    @Override
    public void getPayServiceAgreement(String type) {
        getView().setLoadingIndicator(true);
        Subscription subscription = mGetAgreementUseCase.run(
                new GetAgreementUseCase.RequestValues(type))
                .subscribe(new Subscriber<GetAgreementUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        PayServiceAgreementContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getPayServiceAgreement, onError : ");
                        PayServiceAgreementContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                            view.showPayServiceAgreement(null, null);
                        }
                    }

                    @Override
                    public void onNext(GetAgreementUseCase.ResponseValue responseValue) {
                        PayServiceAgreementContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        if (responseValue != null && responseValue.getAgreement() != null) {
                            AllAgreement agreement = responseValue.getAgreement();
                            view.showPayServiceAgreement(agreement.getTitle(),
                                    agreement.getContent());
                        }
                    }
                });
        addSubscription(subscription);
    }

}
