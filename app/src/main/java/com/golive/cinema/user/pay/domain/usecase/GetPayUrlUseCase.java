package com.golive.cinema.user.pay.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.util.Log;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.MainConfig;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mowl on 2016/11/14.
 */

public class GetPayUrlUseCase
        extends UseCase<GetPayUrlUseCase.RequestValues, GetPayUrlUseCase.ResponseValue> {

    private final MainConfigDataSource mMainConfigDataSource;


    public GetPayUrlUseCase(@NonNull MainConfigDataSource mainConfigDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mMainConfigDataSource = checkNotNull(mainConfigDataSource,
                "MainConfigDataSource cannot be null!");

    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mMainConfigDataSource.getMainConfig().map(new Func1<MainConfig, ResponseValue>() {
            @Override
            public ResponseValue call(MainConfig mainConfig) {
                return new ResponseValue(mainConfig);
            }
        });
    }


    public static class RequestValues implements UseCase.RequestValues {

    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final MainConfig mMainConfig;
        private final Map<String, String> payMap;

        public ResponseValue(MainConfig mMainConfig) {
            this.mMainConfig = mMainConfig;

            this.payMap = getPayUrl(mMainConfig);
        }

        public MainConfig getMainConfig() {
            return mMainConfig;
        }

        public Map<String, String> getPayConfig() {
            return payMap;
        }

        public Map<String, String> getPayUrl(MainConfig cfg) {
            String URL_QUERYAUTHUSER = cfg.getQueryauthuser();
            String URL_GETQRCODE = cfg.getApplyauth();
            String URL_ALIPAY_RECHARGE = cfg.getPayalipay();
            String URL_ALIPAY_PAYSTATUS = cfg.getQueryalipay();

            String URL_UNION_GETPAYNO = cfg.getGetpayno();
            String URL_UNION_PAYTRANSANDBIND = cfg.getPaytransandbind_a();
            String URL_UNION_PAYTRANSFORBIND = cfg.getPaytransforbind_a();
            String URL_PAYPAL_VERIFYRESULT = cfg.getVerifypayresult();
            String URL_CREATETOPUPORDER = cfg.getCreatetopuporder();

            String URL_QUERYWEIXINPAYURL = cfg.getQueryweixinpayurl();
            String URL_QUERYWEIXINPAYSTATUS = cfg.getQueryweixinpaystatus();

            String URL_QUERYCARD = cfg.getQuerycard();
            String URL_USECARD = cfg.getUsecard();

            String URL_CREATEORDER = cfg.getCreateorder();
            String URL_GETALIPAYINFO = cfg.getGetalipayinfo();
            String URL_REPORTOPERATE = cfg.getReportmemberoperate();
            String URL_PAYNOTIFY = cfg.getPaynotify();
            String URL_CREATECOOCAAPAY = cfg.getCreatecoocaapay();
            String URL_CREATESJWEIXINPAY = cfg.getCreatesjweixinpay();
            String URL_ALIPAY_QRCODE = cfg.getGetalipayinfo4qrcode();
            String URL_QUERYORDER = cfg.getQueryorder();

            String URL_QUERYCREDIT = cfg.getQueryCreditwallet();
            String URL_USECREDIT = cfg.getPayOrderByCredit();

            /**
             * 2016/11/22 支付宝/微信 二码合一url
             */
            String URL_QUERYPAYURL = cfg.getGetQrpayUrl();
            String URL_QUERYORDERSTATUS = cfg.getQueryQrpayStatus();

            /* 连续包月 */
            String URL_QUERYPAYURL_MONTH = cfg.getContinueMonthSignUrl();
            String URL_QUERYQRPAYSTATUS = cfg.getContinueMonthQueryUrl();

            Map<String, String> map = new HashMap<>();

            /**
             * 2016/11/22 支付宝/微信 二码合一url
             */
            map.put("URL_QUERYPAYURL", URL_QUERYPAYURL);//请求后台返回混合二维码地址
            map.put("URL_QUERYORDERSTATUS", URL_QUERYORDERSTATUS);//轮询混合二维码状态地址


            map.put("URL_QUERYAUTHUSER", URL_QUERYAUTHUSER);
            map.put("URL_GETQRCODE", URL_GETQRCODE);
            map.put("URL_ALIPAY_RECHARGE", URL_ALIPAY_RECHARGE);
            map.put("URL_ALIPAY_PAYSTATUS", URL_ALIPAY_PAYSTATUS);

            map.put("URL_UNION_GETPAYNO", URL_UNION_GETPAYNO);
            map.put("URL_UNION_PAYTRANSANDBIND", URL_UNION_PAYTRANSANDBIND);
            map.put("URL_UNION_PAYTRANSFORBIND", URL_UNION_PAYTRANSFORBIND);
            map.put("URL_PAYPAL_VERIFYRESULT", URL_PAYPAL_VERIFYRESULT);
            map.put("URL_CREATETOPUPORDER", URL_CREATETOPUPORDER);

            map.put("URL_QUERYWEIXINPAYURL", URL_QUERYWEIXINPAYURL);
            map.put("URL_QUERYWEIXINPAYSTATUS", URL_QUERYWEIXINPAYSTATUS);

            map.put("URL_QUERYCARD", URL_QUERYCARD);
            map.put("URL_USECARD", URL_USECARD);

            map.put("URL_CREATEORDER", URL_CREATEORDER);
            map.put("URL_GETALIPAYINFO", URL_GETALIPAYINFO);
            map.put("URL_REPORTOPERATE", URL_REPORTOPERATE);
            map.put("URL_PAYNOTIFY", URL_PAYNOTIFY);
            map.put("URL_CREATECOOCAAPAY", URL_CREATECOOCAAPAY);
            map.put("URL_CREATESJWEIXINPAY", URL_CREATESJWEIXINPAY);
            map.put("URL_ALIPAY_QRCODE", URL_ALIPAY_QRCODE);
            map.put("URL_QUERYORDER", URL_QUERYORDER);

            map.put("URL_QUERYCREDIT", URL_QUERYCREDIT);
            map.put("URL_USECREDIT", URL_USECREDIT);

            map.put("URL_QUERYPAYURL_MONTH", URL_QUERYPAYURL_MONTH);
            map.put("URL_QUERYQRPAYSTATUS", URL_QUERYQRPAYSTATUS);

            Log.d("GetPayUrlUseCase", "map=" + map.get("URL_ALIPAY_RECHARGE"));
            return map;
        }

    }
}