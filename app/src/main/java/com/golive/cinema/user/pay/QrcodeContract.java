package com.golive.cinema.user.pay;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

import java.util.Map;

/**
 * Created by Mowl on 2016/11/14.
 */

public class QrcodeContract {

    public interface View extends IBaseView<QrcodeContract.Presenter> {
        void setLoadingIndicator(boolean active);

        void showQrcode(Map<String, String> urlMap);
    }

    public interface Presenter extends IBasePresenter<QrcodeContract.View> {

    }

    public interface PayResultCallBack {
        void PayFinishExecute(int state, String log);
    }

    public static final int ALI_WECHAT_MODE_BOTH = 0;
    public static final int ALI_WECHAT_MODE_ALI = 1;
    public static final int ALI_WECHAT_MODE_WECHAT = 2;
}
