package com.golive.cinema.views.metroviews.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import com.golive.cinema.R;

/**
 * Created by Administrator on 2017/6/8.
 */

public class PastTopicRecyclerView extends RecyclerView{
    private int mSelectedItemOffsetStart;
    private int mSelectedItemOffsetEnd;
    public PastTopicRecyclerView(Context context) {
        super(context);
    }

    public PastTopicRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PastTopicRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PastTopicRecyclerView,
                defStyle, 0);
        mSelectedItemOffsetStart = a.getDimensionPixelOffset(
                R.styleable.PastTopicRecyclerView_pastTopic_selectedItemOffsetStart,0);
        mSelectedItemOffsetEnd = a.getDimensionPixelOffset(
                R.styleable.PastTopicRecyclerView_pastTopic_selectedItemOffsetEnd,0);
        a.recycle();
    }
    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;
        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();
        final int offScreenLeft = Math.min(0, childLeft - parentLeft - mSelectedItemOffsetStart);
        final int offScreenTop = Math.min(0, childTop - parentTop - mSelectedItemOffsetStart);
        final int offScreenRight = Math.max(0, childRight - parentRight + mSelectedItemOffsetEnd);
        final int offScreenBottom = Math.max(0,
                childBottom - parentBottom + mSelectedItemOffsetEnd);
        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();
        final int dx;
        if (canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }
        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if (canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }
        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            return true;
        }
        // 重绘是为了选中item置顶，具体请参考getChildDrawingOrder方法
        postInvalidate();
        return false;
    }
}
