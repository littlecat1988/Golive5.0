package com.golive.cinema;

import android.app.Service;

/**
 * Created by Wangzj on 2016/9/28.
 */

public abstract class MvpService<P extends IBasePresenter> extends Service {
    protected abstract P getPresenter();

    @Override
    public void onDestroy() {
        super.onDestroy();
        P presenter = getPresenter();
        if (presenter != null) {
            presenter.detachView();
            presenter.unsubscribe();
        }
    }
}
