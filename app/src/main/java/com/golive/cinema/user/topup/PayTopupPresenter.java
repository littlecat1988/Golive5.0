package com.golive.cinema.user.topup;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;

/**
 * Created by Mowl on 2016/11/14.
 */

public class PayTopupPresenter extends BasePresenter<PayTopupContract.View> implements
        PayTopupContract.Presenter {

    public PayTopupPresenter(@NonNull PayTopupContract.View view) {
        checkNotNull(view, "view cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }
}
