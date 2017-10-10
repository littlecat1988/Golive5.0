package com.golive.cinema.download;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.download.domain.model.DownloadMedia;
import com.golive.cinema.util.DateHelper;
import com.golive.cinema.util.UIHelper;

import java.util.List;

/**
 * Created by Wangzj on 2017/6/5.
 */

public class DownloadMediasAdapter extends
        RecyclerView.Adapter<DownloadMediasAdapter.DownloadMediasHolder> {

    private final List<DownloadMedia> mDownloadMedias;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private RecyclerViewAdapterListener.OnItemClickListener mOnItemClickListener;
    private RecyclerView mRecyclerView;

    public DownloadMediasAdapter(Context context, List<DownloadMedia> downloadMedias) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDownloadMedias = downloadMedias;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public DownloadMediasHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadMediasHolder(
                mLayoutInflater.inflate(R.layout.sel_dl_media_path_media_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(DownloadMediasHolder holder, int position) {
        DownloadMedia downloadMedia = mDownloadMedias.get(position);
        String text = downloadMedia.mediaSharpness + " " + Formatter.formatFileSize(mContext,
                downloadMedia.mediaSize);
        holder.btn.setText(text);
        long estimatedDownloadTime = downloadMedia.estimatedDownloadTime;
        if (estimatedDownloadTime > 0) {
            String timeLeftStr = DateHelper.toHMSTime(1000 * estimatedDownloadTime);
            holder.dlTimeTv.setText(timeLeftStr);
            UIHelper.setViewVisibleOrGone(holder.dlTimeTv, true);
        } else {
            UIHelper.setViewVisibleOrGone(holder.dlTimeTv, false);
        }

        UIHelper.setViewVisibleOrGone(holder.recommendView, downloadMedia.isRecommend);
    }

    @Override
    public int getItemCount() {
        if (mDownloadMedias != null) {
            return mDownloadMedias.size();
        }
        return 0;
    }

    public void setOnItemClickListener(
            RecyclerViewAdapterListener.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class DownloadMediasHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Button btn;
        private final TextView dlTimeTv;
        private final View recommendView;

        DownloadMediasHolder(View itemView) {
            super(itemView);
            btn = (Button) itemView.findViewById(R.id.btn);
            dlTimeTv = (TextView) itemView.findViewById(R.id.download_time_tv);
            recommendView = itemView.findViewById(R.id.recommend_tv);

            btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClicked(mRecyclerView, getAdapterPosition(), itemView);
            }
        }
    }
}
