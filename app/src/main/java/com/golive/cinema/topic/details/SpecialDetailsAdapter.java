package com.golive.cinema.topic.details;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

/**
 * Created by Administrator on 2017/5/22.
 */

public class SpecialDetailsAdapter extends
        RecyclerView.Adapter<SpecialDetailsAdapter.DetailsViewHolder> {
    private List<MovieRecommendFilm> mFilms;
    private Fragment mFragment;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewAdapterListener.OnItemSelectedListener mOnItemSelectedListener;
    private RecyclerViewAdapterListener.OnItemDisSelectedListener mOnItemDisSelectedListener;
    private RecyclerViewAdapterListener.OnItemClickListener mOnItemClickListener;
    private RecyclerView recyclerView;

    public SpecialDetailsAdapter(List<MovieRecommendFilm> films, Fragment fragment) {
        this.mFilms = films;
        this.mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(fragment.getContext());
    }

    @Override
    public SpecialDetailsAdapter.DetailsViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        final View view = mLayoutInflater.inflate(R.layout.special_detail_item, parent, false);
        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SpecialDetailsAdapter.DetailsViewHolder holder,
            final int position) {
        MovieRecommendFilm item = mFilms.get(position);
        if (null == item) {
            return;
        }
        String bigposter = item.getBigposter();
        Glide.with(mFragment)
                .load(bigposter)
                .error(R.drawable.special_default_image)
                .into(holder.bgIv);
        String name = item.getName();
        holder.nameTv.setText(name);
    }

    @Override
    public int getItemCount() {
        if (mFilms != null) {
            return mFilms.size();
        }
        return 0;
    }

    public void refreshData(List<MovieRecommendFilm> specialList) {
        mFilms = specialList;
        notifyDataSetChanged();
    }

    class DetailsViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener,
            View.OnClickListener {
        private TextView nameTv;
        private ImageView bgIv;

        public DetailsViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.textView_name);
            bgIv = (ImageView) itemView.findViewById(R.id.imagView_film);
            itemView.setOnClickListener(this);
            itemView.setOnFocusChangeListener(this);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (mOnItemDisSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(recyclerView, getAdapterPosition(),
                            itemView);
                }
            } else {
                if (mOnItemDisSelectedListener != null) {
                    mOnItemDisSelectedListener.onItemDisSelected(recyclerView, getAdapterPosition(),
                            itemView);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClicked(recyclerView, getAdapterPosition(), itemView);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void setOnItemSelectedListener(
            RecyclerViewAdapterListener.OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public void setOnItemDisSelectedListener(
            RecyclerViewAdapterListener.OnItemDisSelectedListener onItemDisSelectedListener) {
        mOnItemDisSelectedListener = onItemDisSelectedListener;
    }

    public void setmOnItemClickListener(
            RecyclerViewAdapterListener.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
