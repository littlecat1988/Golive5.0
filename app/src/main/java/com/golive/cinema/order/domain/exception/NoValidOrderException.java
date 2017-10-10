package com.golive.cinema.order.domain.exception;

/**
 * Created by Wangzj on 2016/10/28.
 */

public class NoValidOrderException extends Exception {
    private final String mProductId;
    private final String mMediaId;

    public NoValidOrderException(String productId, String mediaId) {
        mProductId = productId;
        mMediaId = mediaId;
    }

    public NoValidOrderException(String detailMessage, String productId, String mediaId) {
        super(detailMessage);
        mProductId = productId;
        mMediaId = mediaId;
    }

    public NoValidOrderException(String detailMessage, Throwable throwable, String productId,
            String mediaId) {
        super(detailMessage, throwable);
        mProductId = productId;
        mMediaId = mediaId;
    }

    public NoValidOrderException(Throwable throwable, String productId, String mediaId) {
        super(throwable);
        mProductId = productId;
        mMediaId = mediaId;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getMediaId() {
        return mMediaId;
    }
}
