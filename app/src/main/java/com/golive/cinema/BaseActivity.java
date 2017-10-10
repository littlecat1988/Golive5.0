package com.golive.cinema;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;

import com.golive.cinema.util.EspressoIdlingResource;
import com.golive.cinema.util.SystemNavigationUtils;
import com.golive.cinema.util.UIHelper;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.initialjie.log.Logger;

/**
 * @author Wangzj
 * @date 2016/6/29
 * @Description
 */
public class BaseActivity extends AppCompatActivity {

    private static final int HIDE_SYSTEM_UI_DELAY = 3000;

    private View mRootView;
    private View mHeaderView;
    private ViewGroup mContainerView;
    private ViewStub mViewStub;
    private boolean mIsSafeToCommitFragment;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        int flags = getIntent().getFlags();
        Logger.d("onCreate, " + getClass().getSimpleName() + ", task id : " + getTaskId()
                + ", flags : " + Integer.toHexString(flags));

        // If the Android version is lower than Jellybean, use this call to hideSystemUi
        // the status bar.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // Comment it for UI test
        // hideNavigation(window);
        hideSystemUi();

        // Window window = getWindow();
        // window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // getWindow().setFormat(PixelFormat.RGBA_8888);
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        RxBus.get().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("onResume, " + getClass().getSimpleName());
        // Comment it for UI test
        // hideNavigation(window);
        hideSystemUi();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mIsSafeToCommitFragment = true;
//        Logger.d("onPostResume, " + getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsSafeToCommitFragment = false;
        Logger.d("onPause, " + getClass().getSimpleName());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d("onNewIntent, " + getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy, " + getClass().getSimpleName());

        RxBus.get().unregister(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        inflateBaseLayout();
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(layoutResID, mContainerView);
        super.setContentView(mRootView);
        initView();
    }

    @Override
    public void setContentView(View view) {
        inflateBaseLayout();
        mContainerView.addView(view);
        super.setContentView(mRootView);
        initView();
    }

    private void inflateBaseLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mRootView = inflater.inflate(R.layout.activity_base, null);
        mViewStub = (ViewStub) mRootView.findViewById(R.id.header_vs);
        mContainerView = (ViewGroup) mRootView.findViewById(R.id.container);
    }

    private void initView() {

    }

    /**
     * Set visibility of the header view
     *
     * @param visible Visible or not.
     */
    public void setHeaderViewVisible(boolean visible) {
        if (visible) {
            if (null == mHeaderView) {
                mHeaderView = mViewStub.inflate();
            }
            UIHelper.setViewVisible(mHeaderView, true);
        } else {
            if (mHeaderView != null) {
                UIHelper.setViewVisibleOrGone(mHeaderView, false);
            }
        }
    }

    /**
     * Hide system UI
     */
    private void hideSystemUi() {
        SystemNavigationUtils.hideSystemUi(this, HIDE_SYSTEM_UI_DELAY);
    }

    private void hideNavigation(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                WindowManager.LayoutParams params = window.getAttributes();
                params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                window.setAttributes(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

    @Override
    public void finish() {
        super.finish();
        Logger.d("finish, cls : " + getClass().getSimpleName());
        overridePendingTransition(0, R.anim.activity_close_out_anim);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.activity_open_enter_anim,
                R.anim.activity_close_out_anim);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.activity_open_enter_anim,
                R.anim.activity_close_out_anim);
    }

    public boolean isSafeToCommitFragment() {
        return mIsSafeToCommitFragment;
    }

    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_FINISH_ACTIVITY)}
    )
    public void finishActivity(Object obj) {
        Logger.d("finishActivity");
        finish();
    }
}