package com.golive.cinema.player.dialog;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_PLAYER;

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
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.util.ItemClickSupport;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chgang on 2016/11/29.
 */

public class RecommendAlertDialog extends BaseDialog implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "RecommendAlertDialog_TAG";
    private Button mCommitBtn, mCancelBtn;
    private View mFocusView;
    private RecommendFilmAlertAdapter mRecommendFilmAlertAdapter;
    private final List<MovieRecommendFilm> recommendPosterList = new ArrayList<>();
    private String mFilmName;
    private boolean mExit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            List<MovieRecommendFilm> recommendList =
                    (List<MovieRecommendFilm>) getArguments().getSerializable(FRAGMENT_TAG);
            if (recommendList != null && recommendList.size() > 0) {
                recommendPosterList.addAll(recommendList);
            }
            mFilmName = getArguments().getString(Constants.PLAYER_INTENT_NAME);
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setBackgroundDrawableResource(R.color.play_recommend_layout_bg);
        setCancelable(true);
        mExit = false;
        return inflater.inflate(R.layout.play_recommend_movie_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.TRANSPARENT);

        mCommitBtn = (Button) view.findViewById(R.id.play_film_detail_recommend_commit_btn);
        mCommitBtn.setOnClickListener(this);
        mCancelBtn = (Button) view.findViewById(R.id.play_film_detail_recommend_cancel_btn);
        mCancelBtn.setOnClickListener(this);
        TextView tipView = (TextView) view.findViewById(R.id.play_film_detail_recommend_tips);

        RecyclerView recommendPosterRv = (RecyclerView) view.findViewById(
                R.id.play_film_detail_recommend_poster_list);

        if (recommendPosterList != null && !recommendPosterList.isEmpty()) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
            recommendPosterRv.setLayoutManager(linearLayoutManager);
            recommendPosterRv.addItemDecoration(itemDecoration);
            ItemClickSupport.addTo(recommendPosterRv).setOnItemClickListener(
                    new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            MovieRecommendFilm poster = mRecommendFilmAlertAdapter.getItem(
                                    position);
                            String filmId = poster.getReleaseid();
                            if (!StringUtils.isNullOrEmpty(filmId)) {
                                FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId,
                                        VIEW_CODE_PLAYER, false, 0);
                                mExit = true;
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), R.string.film_detail_missing_film,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            if (null == mRecommendFilmAlertAdapter) {
                mRecommendFilmAlertAdapter = new RecommendFilmAlertAdapter(this,
                        recommendPosterList);
                mRecommendFilmAlertAdapter.setHasStableIds(true);
                recommendPosterRv.setAdapter(mRecommendFilmAlertAdapter);
            } else {
                mRecommendFilmAlertAdapter.replaceData(recommendPosterList);
            }
        } else {
            view.findViewById(R.id.play_film_detail_recommend_title).setVisibility(View.INVISIBLE);
            recommendPosterRv.setVisibility(View.INVISIBLE);

            RelativeLayout.LayoutParams tipParams =
                    (RelativeLayout.LayoutParams) tipView.getLayoutParams();
            tipParams.topMargin =
                    (int) getResources().getDimension(
                            R.dimen.film_detail_txt_recommend_player_tips_top_null);
            tipView.setLayoutParams(tipParams);

            RelativeLayout exitLayout =
                    (RelativeLayout) view.findViewById(R.id.play_film_detail_recommend_btn_layout);
            RelativeLayout.LayoutParams exitParams =
                    (RelativeLayout.LayoutParams) exitLayout.getLayoutParams();
            exitParams.topMargin =
                    (int) getResources().getDimension(R.dimen.player_recommend_btn_layout_top_null);
            exitLayout.setLayoutParams(exitParams);
        }

        tipView.setText(
                String.format(getString(R.string.play_film_detail_recommend_tips), mFilmName));
        mCommitBtn.requestFocus();
        mFocusView = mCommitBtn;
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
        if (mCommitBtn != null && mCommitBtn.isFocused()) {
            mFocusView = mCommitBtn;
        } else if (mCancelBtn != null && mCancelBtn.isFocused()) {
            mFocusView = mCancelBtn;
        } else {
            if (mRecommendFilmAlertAdapter != null
                    && mRecommendFilmAlertAdapter.getChildFocusView() != null) {
                mFocusView = mRecommendFilmAlertAdapter.getChildFocusView();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_film_detail_recommend_cancel_btn) {
            mExit = true;
        }
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mOnDismissListener != null) {
            mOnDismissListener.onDialogDismiss(mExit);
        }
        super.onDismiss(dialog);
    }
}
