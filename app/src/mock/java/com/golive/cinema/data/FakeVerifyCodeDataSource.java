package com.golive.cinema.data;

import com.golive.cinema.data.source.VerifyCodeDataSource;
import com.golive.network.entity.Response;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/9.
 */

public class FakeVerifyCodeDataSource implements VerifyCodeDataSource {

    private static FakeVerifyCodeDataSource INSTANCE;

    public static FakeVerifyCodeDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeVerifyCodeDataSource();
        }
        return INSTANCE;
    }

    private FakeVerifyCodeDataSource() {
    }

    @Override
    public Observable<Response> getVerifyCode(String phone) {
        return Observable.just(new Response());
    }

}
