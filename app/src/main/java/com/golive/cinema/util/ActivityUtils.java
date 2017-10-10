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

package com.golive.cinema.util;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;

import com.golive.cinema.Constants;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code containerViewId}. The
     * operation is performed by the {@code fragmentManager}.
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
            @NonNull Fragment fragment, int containerViewId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerViewId, fragment);
        transaction.commit();
    }

    /**
     * The {@code fragment} is added to the container view with id {@code containerViewId}. The
     * operation is performed by the {@code fragmentManager}.
     *
     * @param fragmentManager fragmentManager
     * @param fragment        fragment
     * @param containerViewId container view with id
     * @param tag             Optional tag name for the fragment
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
            @NonNull Fragment fragment, int containerViewId, @Nullable String tag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerViewId, fragment, tag);
        transaction.commit();
    }

    public static void removeFragmentToActivity(@NonNull FragmentManager fragmentManager,
            @NonNull Fragment fragment) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);//add(frameId, fragment);
        transaction.commit();
    }

    /**
     * Finish all activity exclude the wanted activity
     */
    public static void finishAllActivityExclude(@NonNull Context context, Class cls) {
        // Clear all activities and start new task
        Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.EXTRA_EXIT, true);
        context.startActivity(intent);
    }

    /**
     * Finish all activity exclude the wanted activity
     */
    public static void finishAllActivityAndRestart(@NonNull Context context, Class cls) {
        // Clear all activities and start new task
        Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.EXTRA_EXIT, true);
        intent.putExtra(Constants.EXTRA_RESTART, true);
        context.startActivity(intent);
    }
}
