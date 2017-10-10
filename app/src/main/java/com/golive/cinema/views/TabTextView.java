package com.golive.cinema.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.network.response.ApplicationPageResponse;


/**
 * Created by chgang on 2016/11/19.
 */

public class TabTextView extends android.support.v7.widget.AppCompatTextView implements
        View.OnFocusChangeListener {

    private int line_measured_width_default = 0;
    private int textview_left_default = 0;
    private static final int tab_index_page_default = Integer.MAX_VALUE;

    private int lineView_left = 0;
    private int lineView_bottom = 0;

    private int tabIndex = tab_index_page_default;
    private int tabPage = tab_index_page_default;
    private boolean isMainTab = true;
    private float fontScale;

    private ApplicationPageResponse.Data data;

    public TabTextView(Context context) {
        this(context, null, 0);
    }

    public TabTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void init(Context context) {
        Resources res = context.getResources();
        fontScale = res.getDisplayMetrics().scaledDensity;

        line_measured_width_default =
                (int) res.getDimension(R.dimen.main_tab_line_bg_default_width);
        textview_left_default = (int) res.getDimension(R.dimen.main_tab_line_bg_default_left);
        lineView_bottom = (int) res.getDimension(R.dimen.main_tab_item_parent_margin_bottom);

        this.setTextSize(res.getDimensionPixelSize(R.dimen.main_tab_item_text_size));
        this.setTextColor(res.getColor(R.color.main_tab_text_color));
        this.setGravity(Gravity.CENTER);
        this.setBackground(res.getDrawable(R.drawable.main_tab_bg_selector));
        this.setClickable(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setSingleLine(true);

        int left = (int) res.getDimension(R.dimen.main_tab_item_text_left);
        int top = (int) res.getDimension(R.dimen.main_tab_item_text_top);
        int right = (int) res.getDimension(R.dimen.main_tab_item_text_left);
        int bottom = (int) res.getDimension(R.dimen.main_tab_item_text_top);
        this.setPadding(
                left
                , top
                , right
                , bottom
        );

        this.setId(generateViewId());
        this.setOnFocusChangeListener(this);
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(px2sp(size));
    }

    private int px2sp(float pxValue) {
        return (int) (pxValue / fontScale + 0.5f);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            this.setTextColor(getResources().getColor(R.color.main_tab_text_focus_color));
            this.getPaint().setFakeBoldText(hasFocus);
        } else {
            if (!((isSelected() || isPressed()))) {
                this.setTextColor(getResources().getColor(isMainTab ? R.color.main_tab_text_color :
                        R.color.film_lib_tab_text_color));
                this.getPaint().setFakeBoldText(hasFocus);
            }
        }
        if (isSelected() || isPressed()) {
            setLineViewVisibility(this, !hasFocus);
        }
    }

    public void setData(ApplicationPageResponse.Data data) {
        this.data = data;
        if (data != null && !StringUtils.isNullOrEmpty(data.getTitle())) {
            this.setText(data.getTitle());
        }
    }

    public void setNormalColor() {
        isMainTab = false;
    }

    public int getIndex() {
        if (data != null && !StringUtils.isNullOrEmpty(data.getActionContent())) {
            try {
                return Integer.parseInt(data.getActionContent()) - 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public int getAction() {
        if (data != null && !StringUtils.isNullOrEmpty(data.getActionContent())) {
            try {
                return Integer.parseInt(data.getActionContent());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable
// .message_point_tip);
//
////        int width = getWidth();
////        int paddingRight = getPaddingRight();
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setAntiAlias(true);
//        paint.setDither(true);
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
////        canvas.drawCircle(width - getPaddingRight() / 2, paddingRight / 2, paddingRight/2,
// paint);
//        Log.d(TAG, "getWidth():"+getWidth());
//        Log.d(TAG, "getHeight():"+getHeight());
//        canvas.save();
////        canvas.drawBitmap(bitmap, getWidth(), getHeight(), paint);
//        RectF rectF = new RectF();
//        canvas.drawBitmap(bitmap, null, rectF, paint);
//        canvas.restore();
    }

    public void addToParentView(LinearLayout tabLayout, int index) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin =
                (int) getResources().getDimension(R.dimen.main_tab_item_text_margin_left_right);
        params.rightMargin =
                (int) getResources().getDimension(R.dimen.main_tab_item_text_margin_left_right);
        tabLayout.addView(this, index, params);
    }

    public void setMessagePointTips(boolean visible) {
        if (visible) {
            Drawable drawable = getResources().getDrawable(R.drawable.message_point_tip);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            this.setCompoundDrawablePadding(2);
            this.setCompoundDrawables(null, null, drawable, null);
        } else {
            this.setCompoundDrawables(null, null, null, null);
        }
    }

    private ImageView lineView;

    public void setSelectedOrPressed(boolean isSP, ImageView view) {
        this.lineView = view;
        this.setSelected(isSP);
        this.setPressed(isSP);
        this.getPaint().setFakeBoldText(isSP);
        if (isSP) {
            setLineViewVisibility(this, true);
            this.setTextColor(getResources().getColor(R.color.main_tab_text_focus_color));
        } else {
            this.setTextColor(getResources().getColor(
                    isMainTab ? R.color.main_tab_text_color : R.color.film_lib_tab_text_color));
        }
    }

    private void setLineViewVisibility(View child, boolean visible) {
        if (lineView == null) return;

        if (child != null && visible) {
            int leftPadding = child.getPaddingLeft();
            int rightPadding = child.getPaddingRight();
            int space = leftPadding + rightPadding;
            int measuredWidth = child.getMeasuredWidth() != 0 ? child.getMeasuredWidth()
                    : line_measured_width_default;
            int width = measuredWidth - space;
            int left = child.getLeft() != 0 ? child.getLeft() : textview_left_default;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.bottomMargin = lineView_bottom;
            params.leftMargin = left + leftPadding + lineView_left;
            lineView.setLayoutParams(params);
            lineView.setVisibility(View.VISIBLE);
//            lineView.animate().translationX(left + leftPadding).setDuration(100).start();
        } else {
            lineView.setVisibility(View.GONE);
        }
    }

    public void setLineView_left(int lineView_left) {
        this.lineView_left = lineView_left;
    }

    public void setLineView_bottom(int lineView_bottom) {
        this.lineView_bottom = lineView_bottom;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public int getTabPage() {
        return tabPage;
    }

    public void setTabPage(int tabPage) {
        this.tabPage = tabPage;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getRepeatCount() == 0)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    LinearLayout parent = (LinearLayout) this.getParent();
                    if (parent == null || parent.getChildCount() <= 0) return false;
                    View child;
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        child = parent.getChildAt(i);
                        if (child.isSelected() || child.isPressed()) {
                            setLineViewVisibility(child, true);
                            return false;
                        }
                    }
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
