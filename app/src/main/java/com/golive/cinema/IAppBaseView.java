package com.golive.cinema;

import android.support.annotation.Nullable;

public interface IAppBaseView<T extends IBasePresenter> extends IBaseView<T> {
    void showError(int errorType, int errorCode, @Nullable String errorMsg);
}
