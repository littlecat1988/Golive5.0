package com.golive.cinema.util;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by Administrator on 2016/9/23.
 */

public class FragmentUtils {

    /**
     * 创建一个fragment
     */
    public static <T extends Fragment> T newFragment(Class<T> tClass) {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static void replace(FragmentManager fragmentManager, int containerViewId,
            Fragment fragment, String tag) {
        if (fragment == null) {
            return;
        }
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment, tag)
//                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public static void hide(FragmentManager fragmentManager, String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            fragmentManager.beginTransaction().hide(fragment).commitAllowingStateLoss();
        }
    }

    public static boolean isHidden(FragmentManager fragmentManager, String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            return true;
        }

        return fragment.isHidden();
    }

    /**
     * Remove previous fragment if exist.
     *
     * @param fragmentManager fragment manager
     * @param fragTag         fragment tag
     */
    public static void removePreviousFragment(@NonNull FragmentManager fragmentManager,
            @NonNull String fragTag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragTag);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(fragTag);
        if (prev != null) {
            ft.remove(prev);
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        }
//        ft.addToBackStack(null);
    }

    /**
     * Add fragment that uses
     * {@link FragmentTransaction#commitAllowingStateLoss()
     */
    public static void addFragmentAllowingStateLoss(@NonNull FragmentManager fragmentManager,
            @NonNull Fragment fragment, @NonNull String fragTag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        checkNotNull(fragTag);
        fragmentManager.beginTransaction().add(fragment, fragTag).commitAllowingStateLoss();
    }

    /**
     * Check whether the fragment is allowed to show now.
     */
    public static boolean allowShowFragmentNow(@NonNull Activity activity) {
        checkNotNull(activity);
        boolean notAllowShow = activity.isFinishing() || (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && activity.isDestroyed()) || (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        && activity.isChangingConfigurations());
        return !notAllowShow;
    }
}
