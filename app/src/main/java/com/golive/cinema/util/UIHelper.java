package com.golive.cinema.util;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.golive.cinema.R;
import com.golive.cinema.views.DataLoadingProgressDialog;

/**
 * This provides methods to help with UI manipulations. Created by Wangzj on 2016/8/22.
 */

public class UIHelper {

    public static void setViewVisible(@NonNull View view, boolean visible) {
        checkNotNull(view);
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public static void setViewVisibleOrGone(@NonNull View view, boolean visible) {
        checkNotNull(view);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Set background to view
     *
     * @param view  view
     * @param resId background resId
     */
    public static void setBackground(@NonNull View view, int resId) {
        checkNotNull(view);
        Context context = view.getContext();
        Drawable background = ContextCompat.getDrawable(context, resId);
        setBackground(view, background);
    }

    /**
     * Set background to view
     *
     * @param view       view
     * @param background background
     */
    public static void setBackground(@NonNull View view, Drawable background) {
        checkNotNull(view);
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

    /**
     * Generate a simple ProgressDialog.
     */
    public static ProgressDialog generateSimpleProgressDialog(@NonNull Context context,
            String title, String message) {
        checkNotNull(context);
        DataLoadingProgressDialog progressDialog = new DataLoadingProgressDialog(context,R.style.data_loading_dialog_fullscreen);
        progressDialog.setTextMessage(message);
        return progressDialog;
    }

    /**
     * Dismiss the dialog if showing.
     */
    public static void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * set all parents' clipChildren and clipToPadding to <code>enabled<code/> of the view.
     *
     * @param v       The child view.
     * @param enabled Enable or not.
     */
    public static void setAllParentsClip(@NonNull View v, boolean enabled) {

        if (v instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v;
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
        }

        while (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v.getParent();
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
            v = viewGroup;
        }
    }

    public static void positionToast(Toast toast, View view, Window window, int offsetX,
            int offsetY) {
        // toasts are positioned relatively to decor view, views relatively to their parents, we
        // have to gather additional data to have a common coordinate system
        Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        // covert anchor view absolute position to a position which is relative to decor view
        int[] viewLocation = new int[2];
        view.getLocationInWindow(viewLocation);
        int viewLeft = viewLocation[0] - rect.left;
        int viewTop = viewLocation[1] - rect.top;

        // measure toast to center it relatively to the anchor view
        DisplayMetrics metrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(metrics.widthPixels,
                MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(metrics.heightPixels,
                MeasureSpec.UNSPECIFIED);
        toast.getView().measure(widthMeasureSpec, heightMeasureSpec);
        int toastWidth = toast.getView().getMeasuredWidth();

        // compute toast offsets
        int toastX = viewLeft + (view.getWidth() - toastWidth) / 2 + offsetX;
        int toastY = viewTop + view.getHeight() + offsetY;

        toast.setGravity(Gravity.LEFT | Gravity.TOP, toastX, toastY);
    }

    public static void addViewAnimScalePlay(View v, float scale) {
        Animation scaleAnim = new ScaleAnimation(1.0f, scale, 1.0f, scale,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setInterpolator(new DecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setDuration(100);
        v.clearAnimation();
        v.startAnimation(scaleAnim);
    }

    public static void addViewAnimTranslate(View v, boolean isShow) {
        Animation showAction;
        if (isShow) {
//            showAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation
// .RELATIVE_TO_SELF, -0.0f,
//                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            showAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        } else {
//            showAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -0.0f, Animation
// .RELATIVE_TO_SELF, 1.0f,
//                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            showAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        }

        showAction.setDuration(400);
        v.clearAnimation();
        v.startAnimation(showAction);
    }

}
