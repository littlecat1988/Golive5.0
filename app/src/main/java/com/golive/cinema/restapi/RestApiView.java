package com.golive.cinema.restapi;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

/**
 * Created by Wangzj on 2016/10/26.
 */

public interface RestApiView<T extends IBasePresenter> extends IBaseView<T> {

    /**
     * Show RestApi Exception
     *
     * @param errType Error type
     * @param errCode Error code
     * @param errMsg  Error message
     */
    void showRestApiException(String errType, String errCode, String errMsg);
}
