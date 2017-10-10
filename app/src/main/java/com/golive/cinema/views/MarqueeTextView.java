package com.golive.cinema.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chgang on 2016/11/15.
 */

public class MarqueeTextView extends android.support.v7.widget.AppCompatTextView {
    private static final int circleSpeed = 26;//滚动速度

    private int lineWidth = 0;
    private Paint contentPaint;
    private float startX;
    private int count; // 计数器
    private Timer mTimer; // 计时器
    private final int FIRST_INVALIDATE = 0;
    private final int SECODE_INVALIDATE = 1;
    private String textStr;
    private float textLength = 0f;
    private float textHeight = 0f;
    private final float frameSpeed = 1.0f;
    private float textSize;
    private float mScale = 0f;

    private int circleTimes = 0;//滚动次数
    private int hasCircled = 0;

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void initView(Context context) {
        int textColor = this.getCurrentTextColor();
        textSize = getTextSize();

        contentPaint = new Paint();
        contentPaint.setColor(textColor);
        contentPaint.setAntiAlias(true);
        contentPaint.setTextSize(textSize);
        contentPaint.setTypeface(Typeface.DEFAULT);
        contentPaint.setTextAlign(Paint.Align.LEFT);

        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mScale = displayMetrics.density;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        textStr = this.getText().toString();
        if (TextUtils.isEmpty(textStr)) {
            return;
        }

        textLength = contentPaint.measureText(textStr);
        textHeight = getFontHeight();
        lineWidth = this.getWidth();//0
        if (lineWidth > 0) {
            setMeasuredDimension(0, (int) (textHeight + mScale * 3));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float xx = startX - getMovement();
        if (!TextUtils.isEmpty(textStr)) {
            canvas.drawText(textStr, xx, textHeight - mScale * 1.5f, contentPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    private int getFontHeight() {
        contentPaint.setTextSize(textSize);
        Paint.FontMetrics fm = contentPaint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == SECODE_INVALIDATE) {
                // 完成一次滚动
//                if (lineWidth + textLength/2 < getMovement()) {
                if (startX + textLength < getMovement()) {
                    //累加已经滚动次数
                    hasCircled++;
                    //停止滚动
                    stopScroll();
                    //继续定时刷新
                    updateText();
                } else {
                    invalidate();
                }
            } else if (msg.what == FIRST_INVALIDATE) {
                updateText();
            }
        }
    };

    /**
     * 写个定时器不停更新刷新TextView
     */
    private void updateText() {
        if (circleTimes > 0 && hasCircled >= circleTimes) {
            setVisibility(View.GONE);
            stopScroll();
            return;
        }

        count = 0;
        stopScroll();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                count++;
                handler.sendEmptyMessage(SECODE_INVALIDATE);
            }
        }, 0, circleSpeed);//100
    }

    /**
     * 获取已经滚动的距离
     */
    private float getMovement() {
        return frameSpeed * count;
    }

    /** 设置滚动次数，达到次数后设置不可见 */
    public void setCircleTimes(int circleTimes) {
        this.circleTimes = circleTimes;
    }

    public void startScrollShow() {
        if (this.getVisibility() != View.VISIBLE) {
            this.setVisibility(View.VISIBLE);
        }

        hasCircled = 0;
        stopScroll();

        handler.sendEmptyMessage(FIRST_INVALIDATE);
    }

    public void setLineWidth(int x) {
        lineWidth = x;
        startX = x + 20;
    }

    public void stopScroll() {
        if (null != mTimer) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }
    }
}
