package com.golive.cinema.topic.past;

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
 * Created by Administrator on 2017/6/2.
 */

public class PastTopicAdapter extends RecyclerView.Adapter<PastTopicAdapter.PastTopicViewHolder> {
    private List<FilmTopic> mFilmTopics;
    private Fragment mFragment;
    private RecyclerView mRecyclerView;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewAdapterListener.OnItemClickListener mOnItemClickListener;
    private RecyclerViewAdapterListener.OnItemSelectedListener mOnItemSelectedListener;
    private RecyclerViewAdapterListener.OnItemDisSelectedListener mOnItemDisSelectedListener;

    public PastTopicAdapter(List<FilmTopic> list, Fragment fragment) {
        this.mFilmTopics = list;
        this.mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(fragment.getContext());
    }

    @Override
    public PastTopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.past_topic_item, parent, false);
        return new PastTopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PastTopicViewHolder holder, int position) {
        final FilmTopic filmTopic = getItem(position);
        if (null == filmTopic) {
            return;
        }
        String posterUrl = filmTopic.getCoverposter();
        Glide.with(mFragment)
                .load(posterUrl)
                .error(R.color.default_bg)
                .into(holder.imageView_past);
    }

    public FilmTopic getItem(int position) {
        if (mFilmTopics != null) {
            return mFilmTopics.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (mFilmTopics != null) {
            return mFilmTopics.size();
        }
        return 0;
    }

    public void refreshData(List<FilmTopic> specialList) {
        mFilmTopics = specialList;
        notifyDataSetChanged();
    }

    public class PastTopicViewHolder extends RecyclerView.ViewHolder implements
            RecyclerView.OnFocusChangeListener, View.OnClickListener {
        private ImageView imageView_past;
        private View view;

        public PastTopicViewHolder(View itemView) {
            super(itemView);
            imageView_past = (ImageView) itemView.findViewById(R.id.imageView_past);
            view = itemView.findViewById(R.id.frameLayout);
            view.setOnFocusChangeListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (mOnItemDisSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(mRecyclerView, getAdapterPosition(),
                            itemView);
                }
            } else {
                if (mOnItemDisSelectedListener != null) {
                    mOnItemDisSelectedListener.onItemDisSelected(mRecyclerView,
                            getAdapterPosition(),
                            itemView);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClicked(mRecyclerView, getAdapterPosition(), itemView);
            }
        }

        public View getView() {
            return view;
        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
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

}
