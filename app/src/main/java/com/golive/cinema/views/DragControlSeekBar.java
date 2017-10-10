package com.golive.cinema.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;


/**
 * Created by chgang on 2016/9/18.
 */

public class DragControlSeekBar extends android.support.v7.widget.AppCompatSeekBar {


    private boolean canDrag = true;

    private boolean longPress = false;

    private boolean mStartTracking = false;

    public DragControlSeekBar(Context context) {
        super(context);
    }

    public DragControlSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragControlSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 禁止拖动
        if (!canDrag) {
            return false;
        }

        return super.onTouchEvent(event);
    }

    public boolean isCanDrag() {
        return canDrag;
    }

    public void setCanDrag(boolean canDrag) {
        this.canDrag = canDrag;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Logger.d("onKeyDown>>>>>>>>:" + keyCode);
        if ((event.getAction() == KeyEvent.ACTION_DOWN)) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:

                    if (!mStartTracking) {
                        mStartTracking = true;
                        if (mOnKeyCallback != null) {
                            mOnKeyCallback.onStartTrackingTouch();
                        }
                    }

                    if (longPress) {
                        if (mOnKeyCallback != null && mOnKeyCallback.getLongPressed()) {
                            mOnKeyCallback.onLongPressing(keyCode);
                        }
                    }
                    event.startTracking();
                    if (event.getRepeatCount() == 0) {
                        longPress = true;
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:

                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (mOnKeyCallback != null && !mOnKeyCallback.getLongPressed()) {
                        mOnKeyCallback.doForward(keyCode);
                    }
                } else {
                    if (mOnKeyCallback != null && !mOnKeyCallback.getLongPressed()) {
                        mOnKeyCallback.doBackward(keyCode);
                    }
                }

                if (longPress) {
                    longPress = false;
                }

                if (mOnKeyCallback != null) {
                    if (mOnKeyCallback.getLongPressed()) {
                        mOnKeyCallback.onLongPressStop(keyCode);
                        mOnKeyCallback.setLongPressed(false);
                    }

                    mStartTracking = false;
                    mOnKeyCallback.onStopTrackingTouch();
                }
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:

                if (mOnKeyCallback != null) {
                    mOnKeyCallback.onLongPressStart(keyCode);
                    mOnKeyCallback.setLongPressed(true);
                }

                return true;
            default:
                break;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private OnKeyCallback mOnKeyCallback = null;

    public interface OnKeyCallback {
        boolean getLongPressed();

        void setLongPressed(boolean longPressed);

        void doForward(int keyCode);

        void doBackward(int keyCode);

        void onStartTrackingTouch();

        void onStopTrackingTouch();

        void onLongPressStart(int keyCode);

        void onLongPressing(int keyCode);

        void onLongPressStop(int keyCode);

    }

    public void setOnKeyCallback(OnKeyCallback onKeyCallback) {
        this.mOnKeyCallback = onKeyCallback;
    }
}
