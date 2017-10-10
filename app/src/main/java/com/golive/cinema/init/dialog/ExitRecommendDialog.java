package com.golive.cinema.init.dialog;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.R;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.player.dialog.RecommendFilmAlertAdapter;
import com.golive.cinema.util.ItemClickSupport;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chgang on 2016/12/20.
 */

public class ExitRecommendDialog extends BaseDialog implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "ExitRecommendDialog_TAG";
    private View mFocusView;
    private Button mCommitButton, mCancelButton;
    private RecommendFilmAlertAdapter mRecommendFilmAlertAdapter;
    private final List<MovieRecommendFilm> mRecommendFilms = new ArrayList<>();
    private boolean misExit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            List<MovieRecommendFilm> recommendList =
                    (List<MovieRecommendFilm>) getArguments().getSerializable(FRAGMENT_TAG);
            if (recommendList != null && recommendList.size() > 0) {
                mRecommendFilms.addAll(recommendList);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        setCancelable(true);
        return inflater.inflate(R.layout.exit_dialog_recommend_movie, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.TRANSPARENT);

        mCommitButton = (Button) view.findViewById(R.id.exit_film_detail_recommend_commit_btn);
        mCommitButton.setOnClickListener(this);
        mCancelButton = (Button) view.findViewById(R.id.exit_film_detail_recommend_cancel_btn);
        mCancelButton.setOnClickListener(this);
        TextView titleView = (TextView) view.findViewById(R.id.exit_film_detail_recommend_title);
        TextView tipView = (TextView) view.findViewById(R.id.exit_film_detail_recommend_tips);
        RelativeLayout exitLayout =
                (RelativeLayout) view.findViewById(R.id.exit_film_detail_recommend_operate_layout);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(
                R.id.exit_film_detail_recommend_poster_list);

        if (mRecommendFilms != null && !mRecommendFilms.isEmpty()) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(itemDecoration);
            ItemClickSupport.addTo(recyclerView).setOnItemClickListener(
                    new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            MovieRecommendFilm poster = mRecommendFilmAlertAdapter.getItem(
                                    position);
                            String filmId = poster.getReleaseid();
                            if (!StringUtils.isNullOrEmpty(filmId)) {
                                FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId,
                                        VIEW_CODE_MAIN_ACTIVITY, false, 0);
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), R.string.film_detail_missing_film,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            if (null == mRecommendFilmAlertAdapter) {
                mRecommendFilmAlertAdapter = new RecommendFilmAlertAdapter(this,
                        mRecommendFilms);
                mRecommendFilmAlertAdapter.setHasStableIds(true);
                recyclerView.setAdapter(mRecommendFilmAlertAdapter);
            } else {
                mRecommendFilmAlertAdapter.replaceData(mRecommendFilms);
            }
        } else {
            titleView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);

            RelativeLayout.LayoutParams tipParams =
                    (RelativeLayout.LayoutParams) tipView.getLayoutParams();
            tipParams.topMargin =
                    (int) getResources().getDimension(
                            R.dimen.film_detail_txt_recommend_player_tips_top_null);
            tipView.setLayoutParams(tipParams);

            RelativeLayout.LayoutParams exitParams =
                    (RelativeLayout.LayoutParams) exitLayout.getLayoutParams();
            exitParams.topMargin =
                    (int) getResources().getDimension(R.dimen.player_recommend_btn_layout_top_null);
            exitLayout.setLayoutParams(exitParams);
        }

        mCancelButton.requestFocus();
        mFocusView = mCancelButton;
    }

    private final RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                RecyclerView.State state) {
            outRect.left =
                    (int) getResources().getDimension(
                            R.dimen.player_recommend_item_margin_left_right);
            outRect.right =
                    (int) getResources().getDimension(
                            R.dimen.player_recommend_item_margin_left_right);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (mFocusView != null) {
            mFocusView.requestFocus();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCommitButton != null && mCommitButton.isFocused()) {
            mFocusView = mCommitButton;
        } else if (mCancelButton != null && mCancelButton.isFocused()) {
            mFocusView = mCancelButton;
        } else {
            if (mRecommendFilmAlertAdapter != null
                    && mRecommendFilmAlertAdapter.getChildFocusView() != null) {
                mFocusView = mRecommendFilmAlertAdapter.getChildFocusView();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.exit_film_detail_recommend_commit_btn) {
            misExit = true;
        }
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mOnDismissListener != null) {
            mOnDismissListener.onDialogDismiss(misExit);
        }
        super.onDismiss(dialog);
    }


}
