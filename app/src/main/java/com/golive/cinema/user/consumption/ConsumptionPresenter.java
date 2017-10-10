package com.golive.cinema.user.consumption;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.consumption.domain.usecase.GetWalletOperationListUseCase;
import com.golive.network.entity.WalletOperationItem;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Administrator on 2016/10/31.
 */

public class ConsumptionPresenter extends BasePresenter<ConsumptionContract.View> implements
        ConsumptionContract.Presenter {
    private final GetWalletOperationListUseCase mGetListUseCase;

    public ConsumptionPresenter(@NonNull ConsumptionContract.View buyvipView,
            @NonNull GetWalletOperationListUseCase task) {
        checkNotNull(buyvipView, "BuyVipView cannot be null!");
        this.mGetListUseCase = checkNotNull(task, "getBuyVip cannot be null!");
        attachView(buyvipView);
        buyvipView.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        loadWalletOperationItems();
    }

    private void loadWalletOperationItems() {
        getView().setLoadingIndicator(true);
        Subscription subscription = mGetListUseCase.run(
                new GetWalletOperationListUseCase.RequestValues(true))
                .subscribe(new Subscriber<GetWalletOperationListUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        ConsumptionContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadWalletOperationItems onError : ");
                        ConsumptionContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                        view.showGetError(e.getMessage());
                    }

                    @Override
                    public void onNext(GetWalletOperationListUseCase.ResponseValue responseValue) {
                        ConsumptionContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        List<WalletOperationItem> getList =
                                responseValue.getWalletOperationItemList();
                        view.showGetListView(getList);
                    }
                });
        addSubscription(subscription);
    }
}