package com.golive.cinema.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.golive.cinema.R;
import com.golive.cinema.library.widget.AbsHListView;
import com.golive.cinema.library.widget.HListView;
import com.initialjie.log.Logger;

public class CenterSelectListView extends HListView {
    private static final String TAG = CenterSelectListView.class
            .getSimpleName();

    private static final boolean SMOOTHS_SCROLL = true;
    private static final int DURATION = 1200;
    private View mFooterView;
    private OnScrollListener mOnScrollListener;
    private com.golive.cinema.library.widget.AdapterView.OnItemSelectedListener
            mOnItemSelectedListener;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean mIsLoading;
    public final boolean scrollControl = false;//活动页控制
    private boolean bCenterSelect = true; // 是否居中选中项

    private View mFooterContentview;

    private int mLeft;

    public CenterSelectListView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CenterSelectListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CenterSelectListView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mFooterView = View.inflate(context, R.layout.load_more_footer, null);
        mFooterContentview = mFooterView.findViewById(R.id.layout);
        addFooterView(mFooterView, null, false);
        hideFooterView();
        /*
         * Must use super.setOnScrollListener() here to avoid override when call
		 * this view's setOnScrollListener method
		 */
        super.setOnScrollListener(superOnScrollListener);

        super.setOnItemSelectedListener(superOnItemSelectedListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (bCenterSelect) {
            int position = getSelectedItemPosition();
            View childView = getSelectedView();
            if (AdapterView.INVALID_POSITION != position && childView != null) {
                int childWidth = childView.getWidth();
                boolean scroll = false;

                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        ++position;
                        if (getCount() <= position) {
                            position = getCount() - 1;
                        }
                        scroll = true;
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (position == 0) {
                            scroll = true;
                            return true;
                        }
                        --position;
                        if (0 > position) {
                            position = 0;
                        }
                        scroll = true;
                        break;
                    default:
                        break;
                }

                if (!scrollControl && getCount() <= 5) {
                    scroll = false;
                }

                if (scroll) {
                    mLeft = (getWidth() - childWidth) >> 1;
                    if (SMOOTHS_SCROLL) {
                        smoothScrollToPositionFromLeft(position, mLeft,
                                DURATION);
                    } else {
                        setSelectionFromLeft(position, mLeft);
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Hide the load more view(footer view)
     */
    private void hideFooterView() {
        // Logger.d("hideFooterView");
        mFooterView.setVisibility(View.GONE);
        // mFooterContentview.setVisibility(View.GONE);
    }

    /**
     * Show load more view
     */
    private void showFooterView() {
        mFooterView.setVisibility(View.VISIBLE);
        mFooterContentview.setVisibility(View.VISIBLE);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    /**
     * Set load more listener, usually you should get more data here.
     *
     * @param listener OnLoadMoreListener
     * @see OnLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    /**
     * When complete load more data, you must use this method to hide the footer
     * view, if not the footer view will be shown all the time.
     */
    public void onLoadMoreComplete() {
        setLoading(false);
        hideFooterView();
    }

    /**
     * When load all data
     */
    public void onLoadAll() {
        onLoadMoreComplete();
        removeFooter();
    }

    /**
     *
     */
    private boolean loadMore() {
        boolean loadMore = false;
        if (mOnLoadMoreListener != null && !isLoading()) {
            setLoading(true);
            loadMore = mOnLoadMoreListener.onLoadMore();
            if (loadMore) {
                showFooterView();
            } else {
                // mIsLoading = false;
                // removeFooter();
                onLoadAll();
            }
        }
        Logger.d("loadMore, " + loadMore);
        return loadMore;
    }

    /**
     *
     */
    private void removeFooter() {
        // hideFooterView();
        removeFooterView(mFooterView);
    }

    private final OnItemSelectedListener superOnItemSelectedListener =
            new OnItemSelectedListener() {

                @Override
                public void onItemSelected(
                        com.golive.cinema.library.widget.AdapterView<?> parent,
                        View view, int position, long id) {
                    // Logger.d("onItemSelected, position : " + position
                    // + ", getChildCount() : " + getChildCount()
                    // + ", getCount() : " + getCount());

                    if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener.onItemSelected(parent, view, position,
                                id);
                    }

                    // last item
                    if (position == getCount() - 1 - getFooterViewsCount()
                            && isFillScreenItem()) {
                        if (loadMore()) {
                            // int left = getWidth() - getPaddingLeft()
                            // - getPaddingRight() - view.getWidth()
                            // - getDividerWidth() - mFooterView.getWidth();
                            // smoothScrollToPositionFromLeft(position, left);

                            smoothScrollToPosition(getCount() - 1);
                            // setSelection(getCount()-1);
                            // int footerCount = getFooterViewsCount();
                            // smoothScrollToPosition(getCount() - footerCount);
                        }
                    }
                }

                @Override
                public void onNothingSelected(
                        com.golive.cinema.library.widget.AdapterView<?> parent) {
                    if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener.onNothingSelected(parent);
                    }
                }
            };

    protected int mLastVisibleIndex;

    protected int mCurrentScrollState;

    /**
     * 条目是否填满整个屏幕
     */

    private boolean isFillScreenItem() {

//        final int firstVisiblePosition = getFirstVisiblePosition();
//        final int lastVisiblePostion = getLastVisiblePosition()
//                - getFooterViewsCount();
//        final int visibleItemCount = lastVisiblePostion - firstVisiblePosition
//                + 1;
//        final int totalItemCount = getCount() - getFooterViewsCount();
        int firstViewLeft = 0;
        View firstView = getChildAt(0);
        if (firstView != null) {
            firstViewLeft = firstView.getLeft();
        }

        return firstViewLeft + getPaddingLeft() <= 0;

    }

    private final OnScrollListener superOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsHListView view, int scrollState) {
            // Logger.d("onScrollStateChanged, scrollState : " + scrollState
            // + ",  getCount() : " + getCount()
            // + ", mLastVisibleIndex : " + mLastVisibleIndex);
            mCurrentScrollState = scrollState;
            // Avoid override when use setOnScrollListener
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }

            if (scrollState == SCROLL_STATE_IDLE) {
                // Fix for scrolling bug
                int selectedItemPosition = getSelectedItemPosition();
                if (INVALID_POSITION != selectedItemPosition) {
                    setSelectionFromLeft(selectedItemPosition, mLeft);
                    // smoothScrollToPositionFromLeft(selectedItemPosition,
                    // mLeft, DURATION);
                }
            }

            if (!isLoading() && mCurrentScrollState == SCROLL_STATE_IDLE
                    && mLastVisibleIndex == getCount() - 1) {
                loadMore();
            }
        }

        @Override
        public void onScroll(AbsHListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            // Logger.d("onScroll, firstVisibleItem : " + firstVisibleItem
            // + ", visibleItemCount : " + visibleItemCount
            // + ", totalItemCount : " + totalItemCount);

            // 计算最后可见条目的索引
            mLastVisibleIndex = firstVisibleItem + visibleItemCount - 1;

            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem,
                        visibleItemCount, totalItemCount);
            }
            // The count of footer view will be add to visibleItemCount also are
            // added to totalItemCount
            if (visibleItemCount == totalItemCount) {
                // If all the item can not fill screen, we should make the
                // footer view invisible.
                // hideFooterView();
            }
        }
    };

    public boolean isCenterSelect() {
        return bCenterSelect;
    }

    public void setCenterSelect(boolean bCenterSelect) {
        this.bCenterSelect = bCenterSelect;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setLoading(boolean pIsLoading) {
        mIsLoading = pIsLoading;
    }

    /**
     * Interface for load more
     */
    public interface OnLoadMoreListener {
        /**
         * Load more data.
         */
        boolean onLoadMore();
    }
}
