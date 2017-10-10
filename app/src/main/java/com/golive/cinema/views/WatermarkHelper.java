package com.golive.cinema.views;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

import com.initialjie.log.Logger;

import java.util.Random;

public class WatermarkHelper {
    private static final int MARGIN = 50;
    private static final int MARGIN_LEFT = 50;
    private static final int MARGIN_TOP = 150;
    private TextView mTv;
    private final String mShowText;
    private int mShowTime = 0; //显示水印时长，单位s、默认0s
    private int mIntervalTime = 16 * 60; //水印出现间隔时长，单位s、默认16分钟
    private int mParentWidth, mParentHeight;
    private Handler mHandler;
    private Runnable mSpaceRun, mShowRun; //分别是间隔执行任务，展示计时任务
    private boolean mIsPrepared = true; //是否准备好
    private final Random mRandom = new Random();

    public WatermarkHelper(String showText, int showTime, int intervalTime) {
        mShowText = showText;
        mShowTime = showTime;
        mIntervalTime = intervalTime;
    }

    public void initView(Context context, final ViewGroup parent) {
        if (mShowTime <= 0) {
            Logger.w("未开启防盗水印");
            return;
        }

        this.mHandler = new Handler();
        mTv = new TextView(context);
        mTv.setTextSize(26 / 1.5f);
        mTv.setAlpha(0.7f);
        mTv.setText(mShowText);
        mTv.setVisibility(View.INVISIBLE);
        //当布局完成会执行，这样才能获取真正的长宽
        parent.post(new Runnable() {
            @Override
            public void run() {
                mParentWidth = parent.getWidth();
                mParentHeight = parent.getHeight();
                parent.addView(mTv, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }
        });
        mSpaceRun = new Runnable() {
            @Override
            public void run() {
                //设置显示水印位置并显示
                if (mIsPrepared) {
                    setViewPosition();
                } else {
                    toDelayShow();
                }
            }
        };
        mShowRun = new Runnable() {
            @Override
            public void run() {
                mTv.setVisibility(View.INVISIBLE);
                start();//开始下一次计时
            }
        };
    }

    public void setIsPrepared(boolean isPrepared) {
        this.mIsPrepared = isPrepared;
        // 如果视频还没准备好，并且当前显示了水印
        if (!isPrepared && mTv != null && View.VISIBLE == mTv.getVisibility()) {
            mTv.setVisibility(View.INVISIBLE);
        }
    }

    private void toDelayShow() {
        if (mSpaceRun != null && mHandler != null) {
            Logger.d("延迟5秒显示");
            mHandler.removeCallbacks(mSpaceRun);//清除已经在排队的任务
            mHandler.postDelayed(mSpaceRun, 5 * 1000);//间隔5s时间后执行一次
        }
    }

    public void start() {
        if (mSpaceRun != null && mHandler != null) {
            Logger.d("开启显示水印倒计时");
            mHandler.removeCallbacks(mSpaceRun);//清除已经在排队的任务
            mHandler.postDelayed(mSpaceRun, mIntervalTime * 1000);//间隔spaceTime时间后执行一次
        }
    }

    public void stop() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mTv != null) {
            mTv.setVisibility(View.GONE);
        }
    }

    private void setViewPosition() {
        int position = mRandom.nextInt(4);
        MarginLayoutParams params = (MarginLayoutParams) mTv.getLayoutParams();
        int width = mTv.getWidth();
        int height = mTv.getHeight();
        Logger.d("position:" + position + "  parentW:" + mParentWidth + "  parentH:" + mParentHeight
                + "  w:" + width + "  h:" + height);
        switch (position) {
            case 0:
                params.leftMargin = MARGIN_LEFT;
                params.topMargin = MARGIN_TOP;
                break;
            case 1:
                params.leftMargin = MARGIN_LEFT;
                params.topMargin = mParentHeight - height - MARGIN_TOP - MARGIN;
                break;
            case 2:
                params.leftMargin = mParentWidth - width - MARGIN_LEFT;
                params.topMargin = MARGIN_TOP;
                break;
            case 3:
                params.leftMargin = mParentWidth - width - MARGIN_LEFT;
                params.topMargin = mParentHeight - height - MARGIN_TOP - MARGIN;
                break;
        }
        mTv.setLayoutParams(params);
        mTv.setVisibility(View.VISIBLE);
        if (mShowRun != null && mHandler != null) {
            mHandler.postDelayed(mShowRun, mShowTime * 1000);
        }
    }
}
