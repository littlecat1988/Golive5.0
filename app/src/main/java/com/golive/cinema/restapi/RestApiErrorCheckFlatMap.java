package com.golive.cinema.restapi;

import com.golive.cinema.restapi.exception.RestApiException;
import com.golive.network.entity.Error;
import com.golive.network.entity.Response;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/10/24.
 */

public class RestApiErrorCheckFlatMap<T extends Response> implements Func1<T, Observable<T>> {

//    private static volatile RestApiErrorCheckFlatMap INSTANCE;
//
//    public static RestApiErrorCheckFlatMap getInstance() {
//        if (null == INSTANCE) {
//            INSTANCE = new RestApiErrorCheckFlatMap();
//        }
//        return INSTANCE;
//    }

    public RestApiErrorCheckFlatMap() {
    }

    @Override
    public Observable<T> call(T t) {
        if (t != null && !t.isOk()) {
            Error error = t.getError();
            RestApiException exception = new RestApiException(error.getType(), error.getNote(),
                    error.getNotemsg(), error.getServertime());
            exception.setObject(t);
            return Observable.error(exception);
        } else {
            return Observable.just(t);
        }
    }
}
