package com.golive.cinema.views;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Set the same spacing between columns or rows.
 * Created by Wangzj on 2017/5/24.
 */

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpanCount;
    private int mSpacing;
    private boolean mIncludeEdge;
    private int mHeaderNum;

    public GridSpacingItemDecoration(int spacing, boolean includeEdge, int headerNum) {
        this.mSpacing = spacing;
        this.mIncludeEdge = includeEdge;
        this.mHeaderNum = headerNum;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
            RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // item position
        int position = parent.getChildAdapterPosition(view) - mHeaderNum;

        if (position < 0) {
            return;
        }

        // spanCount
        GridLayoutManager gridLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int spanCount = gridLayoutManager.getSpanCount();

        // orientation
        int orientation = gridLayoutManager.getOrientation();
        boolean isVertical = LinearLayoutManager.VERTICAL == orientation;

        // span index
        GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
        int spanIndex = spanSizeLookup.getSpanIndex(position, spanCount);

        int curSpace = spanIndex * mSpacing / spanCount;
        int curSpace2 = (spanIndex + 1) * mSpacing / spanCount;
        if (mIncludeEdge) {
            if (isVertical) {
                outRect.left = mSpacing - curSpace;
                outRect.right = curSpace2;
                if (position < spanCount) {
                    // top edge
                    outRect.top = mSpacing;
                }
                // item bottom
                outRect.bottom = mSpacing;
            } else {
                outRect.top = mSpacing - curSpace;
                outRect.bottom = curSpace2;
                if (position < spanCount) {
                    // left edge
                    outRect.left = mSpacing;
                }
                // item right
                outRect.right = mSpacing;
            }
        } else {
            if (isVertical) {
                outRect.left = curSpace;
                outRect.right = mSpacing - curSpace2;
                if (position >= spanCount) {
                    // item top
                    outRect.top = mSpacing;
                }
            } else {
                outRect.top = curSpace;
                outRect.bottom = mSpacing - curSpace2;
                if (position >= spanCount) {
                    // item left
                    outRect.left = mSpacing;
                }
            }
        }
    }
}
