package com.golive.cinema.user.history;

/**
 * Created by Mowl on 2016/11/4.
 */

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
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.RotateTextViewVip;

import java.util.List;

public class UserMoviesScrollAdapter extends
        RecyclerView.Adapter<UserMoviesScrollAdapter.UserMoviesHolder> {

    private final Fragment mFragment;
    private final List<HistoryFilm> mFilmList;
    private final LayoutInflater mInflater;
    private boolean mEditMode = false;

    public UserMoviesScrollAdapter(Fragment fragment, List<HistoryFilm> filmList,
            boolean editMode) {
        this.mFragment = fragment;
        this.mFilmList = filmList;
        this.mEditMode = editMode;
        this.mInflater = LayoutInflater.from(fragment.getContext());
    }

    @Override
    public UserMoviesScrollAdapter.UserMoviesHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        final View view = mInflater.inflate(R.layout.user_history_item, parent, false);
        return new UserMoviesScrollAdapter.UserMoviesHolder(view);
    }

    @Override
    public void onBindViewHolder(UserMoviesScrollAdapter.UserMoviesHolder holder, int position) {
        HistoryFilm film = mFilmList.get(position);
        //海报
        String posterUrl = film.getBigcover();
        String name = film.getName();
        Glide.with(mFragment)
                .load(posterUrl)
                .error(R.drawable.movie_init_bkg)
                .into(holder.imageView);

        //名字
        if (!StringUtils.isNullOrEmpty(name)) {
            holder.movieName.setText(name);
            holder.nameBg.setVisibility(View.VISIBLE);
        } else {
            holder.movieName.setText("");
            holder.nameBg.setVisibility(View.GONE);
        }
        setTimeOutText(holder.itvTimeOut, film, holder.angleView);
        setDeleteViewVisible(holder.ivDelete, mEditMode);
    }

    @Override
    public int getItemCount() {
        if (mFilmList != null) {
            return mFilmList.size();
        }
        return 0;
    }

    public Object getItem(int arg0) {
        if (mFilmList != null) {
            return mFilmList.get(arg0);
        }
        return null;
    }

    public void setEditMode(boolean edit) {
        this.mEditMode = edit;
    }

    private void setDeleteViewVisible(ImageView ivDelete, boolean edit) {
        UIHelper.setViewVisible(ivDelete, edit);
    }

    private void setTimeOutText(RotateTextViewVip itvTimeOutTv, HistoryFilm film,
            ImageView bgImage) {
        itvTimeOutTv.setVisibility(View.VISIBLE);
        bgImage.setVisibility(View.VISIBLE);
        if (film.isOffLine()) {//下架
            bgImage.setImageResource(R.drawable.theatre_valid_down);
            itvTimeOutTv.setText(R.string.valid_time_out_text_downshow);
        } else if (film.isTimeOut()) {
            bgImage.setImageResource(R.drawable.theatre_valid_time_small_ten);
            itvTimeOutTv.setText(R.string.valid_time_out_text);
        } else if (StringUtils.isNullOrEmpty(film.getValiTime()) || film.isKdmFilm()) {
            itvTimeOutTv.setVisibility(View.GONE);
            bgImage.setVisibility(View.GONE);
        } else {
            int hh = -1;
            try {
                hh = Integer.parseInt(film.getValiTime());
            } catch (NumberFormatException e) {
                itvTimeOutTv.setVisibility(View.GONE);
                bgImage.setVisibility(View.GONE);
                e.printStackTrace();
            }

            if (hh > 0) {
                if (hh < 49) {
                    bgImage.setImageResource(hh < 7 ?
                            R.drawable.theatre_valid_time_out
                            : R.drawable.theatre_valid_time_large_ten);
                    itvTimeOutTv.setText(String.format(mFragment.getResources()
                            .getString(R.string.valid_time_remain_hour), String.valueOf(hh)));
                } else {
                    int days = hh / 24;
                    bgImage.setImageResource(R.drawable.theatre_valid_time_large_ten);
                    itvTimeOutTv.setText(String.format(mFragment.getResources()
                            .getString(R.string.valid_time_remain_day), String.valueOf(days)));
                }
            }
        }
    }

    public static class UserMoviesHolder extends RecyclerView.ViewHolder {
        public final ImageView ivDelete;
        public final RotateTextViewVip itvTimeOut;
        public final ImageView imageView;
        public final ImageView viewFocus;
        public final ImageView angleView;
        public final View allValiteView;
        public final View focusViewFrame;
        public final TextView movieName;
        public final ImageView nameBg;

        public UserMoviesHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.igv_lst_frag_theatre_item_igv_cover);
            viewFocus = (ImageView) view.findViewById(R.id.user_movies_item_click_view);
            ivDelete = (ImageView) view.findViewById(R.id.user_movies_delete);
            itvTimeOut = (RotateTextViewVip) view.findViewById(R.id.theatre_valid_time_tv);
            angleView = (ImageView) view.findViewById(R.id.theatre_valid_time_bg);
            allValiteView = view.findViewById(R.id.theatre_valid_time_allview);
            focusViewFrame = view.findViewById(R.id.user_movies_bg_view_frame);
            movieName = (TextView) view.findViewById(R.id.user_movies_view_name_tv);
            nameBg = (ImageView) view.findViewById(R.id.user_movies_itemview_shadow);
        }
    }
}