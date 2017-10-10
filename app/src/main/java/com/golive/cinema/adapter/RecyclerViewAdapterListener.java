package com.golive.cinema.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Wangzj on 2017/5/22.
 */

public interface RecyclerViewAdapterListener<VH extends RecyclerView.ViewHolder> {
    void onViewRecycled(VH holder);

    void onViewAttachedToWindow(VH holder);

    void onViewDetachedFromWindow(VH holder);

    interface OnItemSelectedListener {
        void onItemSelected(RecyclerView recyclerView, int position, View v);
    }

    interface OnItemDisSelectedListener {
        void onItemDisSelected(RecyclerView recyclerView, int position, View v);
    }

    interface OnItemClickListener {
        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

    interface OnItemLongClickListener {
        boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
    }
}
