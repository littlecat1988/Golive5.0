package com.golive.cinema.user.history;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

/**
 * Created by Mowl on 2016/11/28.
 */

public class RecommendPostersAdapter extends
        RecyclerView.Adapter<RecommendPostersAdapter.SimpleViewHolder> {
    private final Fragment mFragment;
    private final List<MovieRecommendFilm> mList;
    private final LayoutInflater mInflater;

    public RecommendPostersAdapter(Fragment fragment, List<MovieRecommendFilm> list) {
        this.mFragment = fragment;
        this.mList = list;
        mInflater = LayoutInflater.from(mFragment.getContext());
    }

    @Override
    public RecommendPostersAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        final View view = mInflater.inflate(R.layout.user_history_comm_item, parent, false);
        return new RecommendPostersAdapter.SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendPostersAdapter.SimpleViewHolder holder, int position) {
        MovieRecommendFilm posterFilm = (MovieRecommendFilm) getItem(position);
        if (posterFilm != null) {
            String name = posterFilm.getName();
            if (!StringUtils.isNullOrEmpty(name)) {
                holder.tvName.setText(name);
            }
            String posterUrl = posterFilm.getBigposter();
            Glide.with(mFragment)
                    .load(posterUrl)
                    .error(R.drawable.movie_init_bkg)
                    .into(holder.imageView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    private Object getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView tvName;

        public SimpleViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.poster_name_tv);
            imageView = (ImageView) view.findViewById(R.id.poster_igv);
        }
    }
}