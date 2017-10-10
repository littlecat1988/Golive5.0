package com.golive.cinema.views;

/**
 * Created by Moweiling on 2016/10/31.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class RotateTextViewVip extends android.support.v7.widget.AppCompatTextView {

    public RotateTextViewVip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RotateTextViewVip(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateTextViewVip(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(45, this.getMeasuredWidth() / 3f, this.getMeasuredHeight() / 2f);
        super.onDraw(canvas);
        canvas.restore();
    }
}