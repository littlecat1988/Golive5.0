package com.golive.cinema.player.dialog;

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
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

/**
 * Created by chgang on 2016/11/29.
 */

public class RecommendFilmAlertAdapter extends
        RecyclerView.Adapter<RecommendFilmAlertAdapter.RecommendPosterHolder> {

    private List<MovieRecommendFilm> mRecommendPosters;
    private final Fragment mFragment;
    private View mChildFocusView;

    public View getChildFocusView() {
        return mChildFocusView;
    }

    private void setChildFocusView(View childFocusView) {
        this.mChildFocusView = childFocusView;
    }

    public RecommendFilmAlertAdapter(Fragment fragment, List<MovieRecommendFilm> recommendPosters) {
        mRecommendPosters = checkNotNull(recommendPosters);
        this.mFragment = fragment;
    }

    @Override
    public RecommendPosterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View rowView = inflater.inflate(R.layout.filmdetail_recommend_poster_player, parent, false);
        return new RecommendFilmAlertAdapter.RecommendPosterHolder(rowView);
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
        return mRecommendPosters.get(position);
    }

    public void replaceData(List<MovieRecommendFilm> posterList) {
        mRecommendPosters = checkNotNull(posterList);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecommendPosterHolder holder,
            int position) {
        final MovieRecommendFilm poster = getItem(position);
        holder.mPosterNameTv.setText(poster.getName());
        String posterUrl = poster.getBigposter();
        Glide.with(mFragment)
                .load(posterUrl)
                .error(R.drawable.movie_init_bkg)
                .into(holder.mPosterIgv);
    }

    class RecommendPosterHolder extends RecyclerView.ViewHolder {

        private final ImageView mPosterIgv;
        private final TextView mPosterNameTv;

        RecommendPosterHolder(View itemView) {
            super(itemView);
            mPosterIgv = (ImageView) itemView.findViewById(R.id.player_poster_igv);
            mPosterNameTv = (TextView) itemView.findViewById(R.id.player_poster_name_tv);
            itemView.findViewById(R.id.player_poster_vg).setOnFocusChangeListener(
                    mOnFocusChangeListener);
        }
    }

    private final View.OnFocusChangeListener mOnFocusChangeListener =
            new View.OnFocusChangeListener() {
                //            private float scaleOnFocus = getContext().getResources().getDimension(
//                    R.dimen.film_detail_recommend_poster_scale_on_focus);
                private final float scaleOnFocus = 1.1f;

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    v.setSelected(hasFocus);
//                    float scale = 1.0f;
//                    if (hasFocus) {
//                        scale = scaleOnFocus;
//                        setChildFocusView(v);
//                    }
//                    v.setScaleX(scale);
//                    v.setScaleY(scale);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        float scale = hasFocus ? SCALE_FACTOR : 1f;
                        v.clearAnimation();
                        v.animate()
                                .scaleX(scale)
                                .scaleY(scale)
                                .setDuration(SCALE_DURATION)
                                .start();
                    }
                }
            };
}
