package com.golive.cinema.views;

/**
 * Created by Mowl on 2016/11/4.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * @author fmliang
 */
public class ItalicTextView extends android.support.v7.widget.AppCompatTextView {

    private int mDegrees;
    private float dx;
    private float dy;

    public ItalicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ItalicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItalicTextView(Context context) {
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
        canvas.rotate(45, this.getMeasuredWidth() / 3f, this.getMeasuredHeight() / 3f);
        super.onDraw(canvas);
        canvas.restore();
    }

    public void setDegrees(int degrees) {
        mDegrees = degrees;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }


}