package com.golive.cinema.download;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.download.domain.model.DownloadStorage;
import com.golive.cinema.util.UIHelper;

import java.util.List;

/**
 * Created by Wangzj on 2017/6/5.
 */

public class DownloadStoragesAdapter extends
        RecyclerView.Adapter<DownloadStoragesAdapter.DownloadStoragesHolder> {

    private List<DownloadStorage> mDownloadStorages;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapterListener.OnItemClickListener mOnItemClickListener;
    private RecyclerViewAdapterListener.OnItemSelectedListener mOnItemSelectedListener;

    public DownloadStoragesAdapter(Context context, List<DownloadStorage> downloadStorages) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDownloadStorages = downloadStorages;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public DownloadStoragesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadStoragesHolder(
                mLayoutInflater.inflate(R.layout.sel_dl_media_path_storage_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(DownloadStoragesHolder holder, int position) {
        DownloadStorage downloadStorage = mDownloadStorages.get(position);

        View storageView = holder.storageView;
        TextView nameTv = holder.nameTv;
        TextView capacityTv = holder.capacityTv;
        TextView autoSelTv = holder.autoSelTv;

        String txt = mContext.getString(R.string.download_sel_media_storage_num);
        txt = String.format(txt, String.valueOf(position + 1));
        nameTv.setText(txt);

        boolean isCapacityEnough = downloadStorage.isCapacityEnough;
        int btnResId = isCapacityEnough ? R.drawable.selector_bg_btn_storage
                : R.drawable.selector_bg_btn_storage_disable;
        UIHelper.setBackground(storageView, btnResId);
        storageView.setTag(downloadStorage.path);
        storageView.setFocusable(isCapacityEnough);
        storageView.setFocusableInTouchMode(isCapacityEnough);
        storageView.setClickable(isCapacityEnough);

        if (isCapacityEnough) {
            String str = mContext.getString(R.string.download_sel_media_storage_available_capacity);
            String text = String.format(str,
                    Formatter.formatFileSize(mContext, downloadStorage.availableCapacity));
            capacityTv.setText(Html.fromHtml(text));
        } else {
            capacityTv.setText(R.string.download_sel_media_storage_capacity_not_enough);
        }

//        if (downloadStorage.isRecommend) {
//            String str = mContext.getString(R.string.download_sel_media_storage_auto_select);
//            String text = String.format(str, String.valueOf(10));
//            autoSelTv.setText(text);
//        } else {
//            autoSelTv.setText("");
//        }
        autoSelTv.setText("");
    }

    @Override
    public int getItemCount() {
        if (mDownloadStorages != null) {
            return mDownloadStorages.size();
        }
        return 0;
    }

    public void replaceData(List<DownloadStorage> downloadStorages) {
        mDownloadStorages = downloadStorages;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(
            RecyclerViewAdapterListener.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(
            RecyclerViewAdapterListener.OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    class DownloadStoragesHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnFocusChangeListener {
        public final View storageView;
        public final TextView nameTv;
        public final TextView capacityTv;
        public final TextView autoSelTv;

        public DownloadStoragesHolder(View itemView) {
            super(itemView);
            storageView = itemView.findViewById(R.id.btn);
            nameTv = (TextView) itemView.findViewById(R.id.storage_name_tv);
            capacityTv = (TextView) itemView.findViewById(R.id.storage_capacity_tv);
            autoSelTv = (TextView) itemView.findViewById(R.id.storage_auto_select_tv);
            storageView.setOnClickListener(this);
            storageView.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClicked(mRecyclerView, getAdapterPosition(), itemView);
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(mRecyclerView, getAdapterPosition(),
                        itemView);
            }
        }
    }
}
