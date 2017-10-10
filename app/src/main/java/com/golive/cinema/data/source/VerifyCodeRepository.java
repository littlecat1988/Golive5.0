package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.entity.Response;

import rx.Observable;

/**
 * Created by Wangzj on 2017/4/25.
 */

public class VerifyCodeRepository implements VerifyCodeDataSource {

    private static VerifyCodeRepository INSTANCE;
    private final VerifyCodeDataSource mDataSource;

    public static VerifyCodeRepository getInstance(@NonNull VerifyCodeDataSource dataSource) {
        if (INSTANCE == null) {
            INSTANCE = new VerifyCodeRepository(dataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(VerifyCodeDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    private VerifyCodeRepository(VerifyCodeDataSource dataSource) {
        mDataSource = dataSource;
    }

    @Override
    public Observable<Response> getVerifyCode(String phone) {
        return mDataSource.getVerifyCode(phone);
    }
}
