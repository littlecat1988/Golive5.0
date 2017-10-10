/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.golive.cinema;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import rx.Observable;

/**
 * Use cases are the entry points to the domain layer.
 *
 * @param <Q> the request type
 * @param <P> the response type
 */
public abstract class UseCase<Q extends UseCase.RequestValues, P extends UseCase.ResponseValue> {

    public final BaseSchedulerProvider mSchedulerProvider;

    public UseCase(@NonNull BaseSchedulerProvider schedulerProvider) {
        mSchedulerProvider = checkNotNull(schedulerProvider, "SchedulerProvider cannot be null!");
    }

    public Observable<P> run(Q requestValues) {
        return executeUseCase(requestValues)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui());
    }

    protected abstract Observable<P> executeUseCase(Q requestValues);

    /**
     * Data passed to a request.
     */
    public interface RequestValues {
    }

    /**
     * Data received from a request.
     */
    public interface ResponseValue {
    }
}
