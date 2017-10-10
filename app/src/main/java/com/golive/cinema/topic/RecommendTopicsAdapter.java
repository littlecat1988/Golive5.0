package com.golive.cinema.topic;


import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.network.entity.FilmTopic;

import java.util.List;

/**
 * Created by Wangzj on 2016/9/14.
 */

public class RecommendTopicsAdapter extends
        RecyclerView.Adapter<RecommendTopicsAdapter.RecommendPosterHolder> {

    private final LayoutInflater mLayoutInflater;
    private final int mScaleDuration;
    private final float mScaleFactor;
    private List<FilmTopic> mFilmTopics;
    private final Fragment mFragment;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapterListener.OnItemClickListener mOnItemClickListener;
    private RecyclerViewAdapterListener.OnItemSelectedListener mOnItemSelectedListener;
    private RecyclerViewAdapterListener.OnItemDisSelectedListener mOnItemDisSelectedListener;

    public RecommendTopicsAdapter(Fragment fragment, List<FilmTopic> filmTopics,
            float scaleFactor, int scaleDuration) {
        mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(mFragment.getContext());
        mFilmTopics = filmTopics;
        mScaleFactor = scaleFactor;
        mScaleDuration = scaleDuration;
    }

    @Override
    public void onBindViewHolder(RecommendPosterHolder holder, final int position) {
        final FilmTopic poster = getItem(position);
        if (null == poster) {
            return;
        }

        if (poster.isOldTopics()) {
            holder.mPosterIgv.setImageResource(R.drawable.old_topics);
        } else {
            String posterUrl = poster.getCoverposter();
            Glide.with(mFragment)
                    .load(posterUrl)
                    .error(R.color.default_bg)
                    .into(holder.mPosterIgv);
        }

        holder.mVg.setBackgroundResource(
                isInCenter(position) ? R.drawable.selector_bg_recommend_poster_center
                        : R.drawable.selector_bg_recommend_poster);
    }

    @Override
    public RecommendPosterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mLayoutInflater.inflate(R.layout.topic_recommend_items, parent, false);
        return new RecommendPosterHolder(rowView);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        if (null == mFilmTopics) {
            return 0;
        }
        return mFilmTopics.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public FilmTopic getItem(int position) {
        if (mFilmTopics != null) {
            return mFilmTopics.get(position);
        }
        return null;
    }

    public void replaceData(List<FilmTopic> posterList) {
        mFilmTopics = posterList;
        notifyDataSetChanged();
    }

    public boolean isInCenter(int position) {
        return getItemCount() >= 3 && position > 0 && position < getItemCount() - 1;
    }

    public void setOnItemClickListener(
            RecyclerViewAdapterListener.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(
            RecyclerViewAdapterListener.OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public void setOnItemDisSelectedListener(
            RecyclerViewAdapterListener.OnItemDisSelectedListener onItemDisSelectedListener) {
        mOnItemDisSelectedListener = onItemDisSelectedListener;
    }

    class RecommendPosterHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnFocusChangeListener {

        private final View mVg;
        private final ImageView mPosterIgv;

        RecommendPosterHolder(View itemView) {
            super(itemView);
            mPosterIgv = (ImageView) itemView.findViewById(R.id.poster_igv);
            mVg = itemView.findViewById(R.id.poster_vg);
            mVg.setOnFocusChangeListener(this);
            mVg.setOnClickListener(this);
//            itemView.setOnFocusChangeListener(mOnFocusChangeListener);
//            mPosterIgv.setOnFocusChangeListener(mOnFocusChangeListener);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClicked(mRecyclerView, getAdapterPosition(), itemView);
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(mRecyclerView, getAdapterPosition(),
                            itemView);
                }
            } else {
                if (mOnItemDisSelectedListener != null) {
                    mOnItemDisSelectedListener.onItemDisSelected(mRecyclerView,
                            getAdapterPosition(), itemView);
                }
            }
            mOnFocusChangeListener.onFocusChange(v, hasFocus);
        }
    }

    private final View.OnFocusChangeListener mOnFocusChangeListener =
            new View.OnFocusChangeListener() {

//                private static final int DURATION = SCALE_DURATION;
//                private static final float SCALE_ON_FOCUS = 1.08f;

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    v.clearAnimation();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        float value = hasFocus ? mScaleFactor : 1f;
                        v.animate()
                                .scaleX(value)
                                .scaleY(value)
                                .setDuration(mScaleDuration)
                                .start();
                    }
                }
            };
}
