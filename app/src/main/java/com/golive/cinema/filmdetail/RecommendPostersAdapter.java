package com.golive.cinema.filmdetail;


import static com.golive.cinema.Constants.SCALE_DURATION;
import static com.golive.cinema.Constants.SCALE_FACTOR;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.os.Build;
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
 * Created by Wangzj on 2016/9/14.
 */

public class RecommendPostersAdapter extends
        RecyclerView.Adapter<RecommendPostersAdapter.RecommendPosterHolder> {

    private final LayoutInflater mLayoutInflater;
    private List<MovieRecommendFilm> mRecommendPosters;
    private final Fragment mFragment;

    public RecommendPostersAdapter(Fragment fragment, List<MovieRecommendFilm> recommendPosters) {
        mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(mFragment.getContext());
        setList(recommendPosters);
    }

    @Override
    public void onBindViewHolder(RecommendPosterHolder holder, final int position) {
        final MovieRecommendFilm poster = getItem(position);
        String name = poster.getName();
        if (!StringUtils.isNullOrEmpty(name)) {
            holder.mPosterNameTv.setText(name);
        }
        String posterUrl = poster.getBigposter();
        Glide.with(mFragment)
                .load(posterUrl)
                .error(R.drawable.movie_init_bkg)
                .into(holder.mPosterIgv);
    }

    @Override
    public RecommendPosterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mLayoutInflater.inflate(R.layout.filmdetail_recommend_poster, parent, false);
        return new RecommendPosterHolder(rowView);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        if (null == mRecommendPosters) {
            return 0;
        }
        return mRecommendPosters.size();
    }

    public MovieRecommendFilm getItem(int position) {
        if (mRecommendPosters != null) {
            return mRecommendPosters.get(position);
        }
        return null;
    }

    public void replaceData(List<MovieRecommendFilm> posterList) {
        setList(posterList);
        notifyDataSetChanged();
    }

    private void setList(List<MovieRecommendFilm> posterList) {
        mRecommendPosters = checkNotNull(posterList);
    }

    class RecommendPosterHolder extends RecyclerView.ViewHolder {

        private final ImageView mPosterIgv;
        private final TextView mPosterNameTv;

        RecommendPosterHolder(View itemView) {
            super(itemView);
            mPosterIgv = (ImageView) itemView.findViewById(R.id.poster_igv);
            mPosterNameTv = (TextView) itemView.findViewById(R.id.poster_name_tv);
            itemView.setOnFocusChangeListener(mOnFocusChangeListener);
        }
    }

    private final View.OnFocusChangeListener mOnFocusChangeListener =
            new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // for marquee
                    v.setSelected(hasFocus);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        float scale = hasFocus ? SCALE_FACTOR : 1f;
                        v.clearAnimation();
                        v.animate()
                                .scaleX(scale)
                                .scaleY(scale)
                                .setDuration(SCALE_DURATION)
                                .start();
//                        v.setScaleX(scale);
//                        v.setScaleY(scale);
                    }
                }
            };

//    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
//        //            private float scaleOnFocus = getContext().getResources().getDimension(
////                    R.dimen.film_detail_recommend_poster_scale_on_focus);
//        private final float SCALE_ON_FOCUS = 1.05f;
//
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            float scale = 1;
//            if (hasFocus) {
//                scale = SCALE_ON_FOCUS;
//            }
//            v.setScaleX(scale);
//            v.setScaleY(scale);
//        }
//    };
}
