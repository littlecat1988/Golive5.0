package com.golive.cinema.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.golive.cinema.util.DeviceUtils;

/**
 * Created by Administrator on 2017/3/30.
 */

public class AdvertVideoView extends VideoView {


    public AdvertVideoView(Context context) {
        super(context);
    }

    public AdvertVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvertVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(DeviceUtils.getScreenW(getContext()),
                DeviceUtils.getScreenH(getContext()));
    }
}
