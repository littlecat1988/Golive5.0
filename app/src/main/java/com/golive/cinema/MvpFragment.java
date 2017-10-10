package com.golive.cinema;

/**
 * Created by Wangzj on 2016/9/28.
 */

public abstract class MvpFragment<P extends IBasePresenter> extends BaseFragment {
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
