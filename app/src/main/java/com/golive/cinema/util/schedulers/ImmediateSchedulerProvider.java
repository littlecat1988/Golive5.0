package com.golive.cinema.util.schedulers;

import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Implementation of the {@link BaseSchedulerProvider} making all {@link Scheduler}s immediate.
 */
public class ImmediateSchedulerProvider implements BaseSchedulerProvider {

    @NonNull
    @Override
    public Scheduler computation() {
        return getImmediateScheduler();
    }

    @NonNull
    @Override
    public Scheduler newThread() {
        return getImmediateScheduler();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return getImmediateScheduler();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return getImmediateScheduler();
    }

    private Scheduler getImmediateScheduler() {
        return Schedulers.immediate();
    }
}
