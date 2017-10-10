package com.golive.cinema.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Auto center the selected child view in RecyclerView.
 * Created by Wangzj on 2017/5/23.
 */

public class CenterLinearLayoutManager extends LinearLayoutManager {

    private final Interpolator mDefaultInterpolator = new DecelerateInterpolator(1.5f);

    public CenterLinearLayoutManager(Context context) {
        super(context);
    }

    public CenterLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CenterLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect,
            boolean immediate) {

        boolean isVertical = VERTICAL == getOrientation();
        int offset = isVertical ? (getFreeHeight() - child.getHeight()) >> 1
                : (getFreeWidth() - child.getWidth()) >> 1;

        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int childLeft = child.getLeft() + rect.left - child.getScrollX();
        final int childTop = child.getTop() + rect.top - child.getScrollY();
        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft - (isVertical ? 0 : offset));
        final int offScreenTop = Math.min(0, childTop - parentTop - (isVertical ? offset : 0));
        final int offScreenRight = Math.max(0,
                childRight - parentRight + (isVertical ? 0 : offset));
        final int offScreenBottom = Math.max(0,
                childBottom - parentBottom + (isVertical ? offset : 0));

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
            dx = offScreenRight != 0 ? offScreenRight
                    : Math.max(offScreenLeft, childRight - parentRight);
        } else {
            dx = offScreenLeft != 0 ? offScreenLeft
                    : Math.min(childLeft - parentLeft, offScreenRight);
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy = offScreenTop != 0 ? offScreenTop
                : Math.min(childTop - parentTop, offScreenBottom);

        if (dx != 0 || dy != 0) {
            if (immediate) {
                parent.scrollBy(dx, dy);
            } else {
                parent.smoothScrollBy(dx, dy, mDefaultInterpolator);
            }
            return true;
        }
        return false;
    }

    private int getFreeHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getFreeWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
