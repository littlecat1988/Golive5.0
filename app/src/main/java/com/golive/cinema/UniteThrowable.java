package com.golive.cinema;

import android.support.annotation.Nullable;

import com.golive.cinema.player.kdm.KdmException;
import com.golive.network.NoNetworkException;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import retrofit2.adapter.rxjava.HttpException;

/**
 * Created by Wangzj on 2017/6/7.
 */

public class UniteThrowable extends Exception {

    public final int errorType;
    public final int errorCode;
    @Nullable
    public final String errorMsg;

    public UniteThrowable(Throwable throwable) {
        this(throwable, -1, -1);
    }

    public UniteThrowable(Throwable throwable, int errorType) {
        this(throwable, errorType, -1);
    }

    public UniteThrowable(Throwable throwable, int errorType, int errorCode) {
        this(throwable, errorType, errorCode, null);
    }

    public UniteThrowable(Throwable throwable, int errorType, int errorCode,
            @Nullable String errorMsg) {
        super(throwable);
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static UniteThrowable handleException(Throwable e) {
        UniteThrowable ex;
        if (e instanceof NoNetworkException) {
            ex = new UniteThrowable(e, ErrorType.NETWORK_ERROR);
        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new UniteThrowable(e, ErrorType.HTTP_ERROR, httpException.code());
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new UniteThrowable(e, ErrorType.SSL_ERROR);
        } else if (e instanceof JsonParseException
                || e instanceof JSONException) {
            ex = new UniteThrowable(e, ErrorType.PARSE_ERROR);
        } else if (e instanceof KdmException) {
            KdmException exception = (KdmException) e;
            ex = new UniteThrowable(e, ErrorType.KDM_ERROR, exception.getKdmResCode().getErrno());
        } else {
            ex = new UniteThrowable(e, ErrorType.UNKNOWN);
        }

        return ex;
    }

    /**
     * 约定异常
     */
    public static final class ErrorType {

        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000000;

        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1000001;

        /**
         * 网络异常
         */
        public static final int NETWORK_ERROR = 1000002;

        /**
         * HTTP协议错误
         */
        public static final int HTTP_ERROR = 1000003;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1000004;

        /**
         * KDM错误
         */
        public static final int KDM_ERROR = 1000005;
    }

}
