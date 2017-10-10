package com.golive.cinema.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class RotateTextView extends android.support.v7.widget.AppCompatTextView {

    public RotateTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RotateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateTextView(Context context) {
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
        canvas.rotate(45, (this.getMeasuredWidth() / 2f), this.getMeasuredHeight() / 2f);
        super.onDraw(canvas);
        canvas.restore();
    }
}