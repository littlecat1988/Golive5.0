package com.golive.cinema.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;

/**
 * Created by Wangzj on 2016/11/7.
 */

public class BoldButton extends android.support.v7.widget.AppCompatButton {


    public BoldButton(Context context) {
        super(context);
        init();
    }

    public BoldButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // for english
        setTypeface(getTypeface(), Typeface.BOLD);

        // for Chinese
        TextPaint tp = getPaint();
        tp.setFakeBoldText(true);
    }
}
