package com.golive.cinema.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.golive.cinema.R;

/**
 * Created by Mowl on 2016/11/28.
 */

public class CircleView extends View {

    private int color = 0;
    private float rwh = 50;
    private final Paint paint = new Paint();// 定义画笔1

    public CircleView(Context context) {
        super(context);
        color = context.getResources().getColor(R.color.phonenumber_color_686868);
        rwh = context.getResources().getDimension(R.dimen.topup_circle_line_r);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        color = context.getResources().getColor(R.color.phonenumber_color_686868);
        rwh = context.getResources().getDimension(R.dimen.topup_circle_line_r);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);// 设置画布的背景颜色

        paint.setStyle(Paint.Style.STROKE);//空心
        paint.setAntiAlias(true);// 消除锯齿
        paint.setColor(color);// 设置画笔的颜色
        paint.setStrokeWidth(2); // 设置paint的外框宽度

        // 画一个圆
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, rwh, paint);
    }
}

