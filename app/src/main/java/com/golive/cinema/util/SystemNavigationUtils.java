package com.golive.cinema.util;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;

/**
 * Created by Wangzj on 2016/9/26.
 */

public class SystemNavigationUtils {

    /**
     * Hide system Ui
     *
     * @param reHideDelay hide delay when system ui re-show.
     */
    public static void hideSystemUi(@NonNull Activity activity, final int reHideDelay) {
        checkNotNull(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final int hideSystemUiOptions = getHideSystemUiOptions();
            final View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(hideSystemUiOptions);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            decorView.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        decorView.setSystemUiVisibility(hideSystemUiOptions);
                                    }
                                }, reHideDelay);
                            }
                        }
                    });

            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    /**
     * Show system Ui
     *
     * @param window window
     */
    public static void showNavigationBar(@NonNull Window window) {
        checkNotNull(window);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final int flags = 0;
            final View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(flags);
        }
    }

    public static int getHideSystemUiOptions() {
        final int uiOptions;

        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        final int baseUiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions = baseUiOptions
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            uiOptions = baseUiOptions
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        }
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        else {
            uiOptions = baseUiOptions
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // hide nav bar
        }

        return uiOptions;
    }
}
