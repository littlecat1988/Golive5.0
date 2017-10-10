package com.golive.cinema.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerExt extends ViewPager {

    public ViewPagerExt(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public ViewPagerExt(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return false;
//		return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return false;
//        return super.onTouchEvent(arg0);  
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}
