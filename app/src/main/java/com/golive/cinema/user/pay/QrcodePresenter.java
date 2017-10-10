package com.golive.cinema.user.pay;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.pay.domain.usecase.GetPayUrlUseCase;
import com.initialjie.log.Logger;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Mowl on 2016/11/14.
 */

public class QrcodePresenter extends BasePresenter<QrcodeContract.View> implements
        QrcodeContract.Presenter {
    private final GetPayUrlUseCase getPayUrlUseCase;

    public QrcodePresenter(@NonNull QrcodeContract.View view,
            @NonNull GetPayUrlUseCase payurltask) {
        checkNotNull(view, "BuyVipView cannot be null!");
        this.getPayUrlUseCase = checkNotNull(payurltask, "payurltask cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getloadPayUrl();
    }

    private void getloadPayUrl() {
        Logger.d("loadPayUrl");
        Subscription subscription = getPayUrlUseCase.run(new GetPayUrlUseCase.RequestValues())
                .subscribe(new Subscriber<GetPayUrlUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getPayUrlUseCase onError");
                    }

                    @Override
                    public void onNext(GetPayUrlUseCase.ResponseValue responseValue) {
                        QrcodeContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.showQrcode(responseValue.getPayConfig());
                    }
                });
        addSubscription(subscription);
    }


}
