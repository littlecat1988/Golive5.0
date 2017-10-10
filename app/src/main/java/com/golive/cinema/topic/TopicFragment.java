package com.golive.cinema.topic;

import static com.golive.cinema.Constants.SCALE_DURATION;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.filmlibrary.FilmLibraryActivity;
import com.golive.cinema.topic.details.SpecialDetailsActivity;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.CenterLinearLayoutManager;
import com.golive.network.entity.FilmTopic;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2017/5/23.
 */

public class TopicFragment extends MvpFragment<TopicContract.Presenter> implements
        TopicContract.View {

    private static final float SCALE_FACTOR = 1.1f;
    private static final int MSG_SHOW_INDICATOR = 1;
    private static final int ANIM_ALPHA_DURATION = 300;

    private TopicContract.Presenter mPresenter;
    private ImageView mPosterIv;
    private RecyclerView mRecommendRv;
    private RecommendTopicsAdapter mTopicsAdapter;
    private View mIndicatorView;
    private int mLastSelectedPos = -1;
    private ObjectAnimator mAnimator;
    private Target<GlideDrawable> mTarget;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (MSG_SHOW_INDICATOR == msg.what) {
                setIndicatorActive(true);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topic_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPosterIv = (ImageView) view.findViewById(R.id.topic_poster);
        mRecommendRv = (RecyclerView) view.findViewById(R.id.topic_recommend_rv);
        mIndicatorView = view.findViewById(R.id.topic_indicator);
        if (mIndicatorView != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mIndicatorView.animate().scaleX(SCALE_FACTOR).scaleY(SCALE_FACTOR).start();
        }
        initRecommendPosterView();
        mAnimator = ObjectAnimator.ofFloat(mPosterIv, "alpha", 0.7f, 1f);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(ANIM_ALPHA_DURATION);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext().getApplicationContext();
        TopicPresenter presenter = new TopicPresenter(this,
                Injection.provideGetRecommendFilmTopicsUseCase(context));
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected TopicContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(TopicContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void initRecommendPosterView() {
        LinearLayoutManager linearLayoutManager = new CenterLinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mRecommendRv.setLayoutManager(linearLayoutManager);
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(mRecommendRv);

        final int itemOffSet = (int) getResources().getDimension(
                R.dimen.topic_recommend_item_offset);
        final int offsetW = (int) getResources().getDimension(
                R.dimen.topic_recommend_item_margin_w);
        final int offsetH = (int) getResources().getDimension(
                R.dimen.topic_recommend_item_margin_h);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                    RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                if (0 == position) {
                    outRect.left = offsetW;
                } else {
                    outRect.left = itemOffSet;
                    if (mRecommendRv.getAdapter().getItemCount() - 1 == position) {
                        outRect.right = offsetW;
                    }
                }
                outRect.top = offsetH;
                outRect.bottom = offsetH;

//                if (position!=0){
//                    outRect.left = itemOffSet;
//                }
            }
        });

        mRecommendRv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Logger.d("onFocusChange, hasFocus : " + hasFocus);
                if (!hasFocus) {
                    if (View.VISIBLE == mIndicatorView.getVisibility()) {
                        setIndicatorActive(false);
                    }
                }
            }
        });

//        UIHelper.setAllParentsClip(mRecommendRv, false);
    }

    @Override
    public void showTopics(List<FilmTopic> filmTopics, final boolean hasMoreTopics) {
        List<FilmTopic> topicList = filmTopics;
        if (filmTopics != null && hasMoreTopics) {
            topicList = new ArrayList<>(filmTopics);
            // add a temp topic
            FilmTopic filmTopic = new FilmTopic();
            filmTopic.setOldTopics(true);
            topicList.add(filmTopic);
        }

        if (null == mTopicsAdapter) {
            mTopicsAdapter = new RecommendTopicsAdapter(this, topicList, SCALE_FACTOR,
                    SCALE_DURATION);
            mTopicsAdapter.setOnItemClickListener(
                    new RecyclerViewAdapterListener.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            Logger.d("onItemClicked, position : " + position);
                            // go to old topics
                            if (hasMoreTopics && position == mTopicsAdapter.getItemCount() - 1) {
//                                startActivity(new Intent(getContext(), PastTopicActivity.class));
                                FilmLibraryActivity.navigateTo(getContext(), true);
                                return;
                            }

                            // go to topic detail
                            FilmTopic filmTopic = mTopicsAdapter.getItem(position);
                            SpecialDetailsActivity.start(getActivity(), filmTopic);
                        }
                    });
            mTopicsAdapter.setOnItemSelectedListener(
                    new RecyclerViewAdapterListener.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(RecyclerView recyclerView, final int position,
                                View v) {
                            if (mTopicsAdapter.isInCenter(position)) {
                                if (View.VISIBLE != mIndicatorView.getVisibility()) {
                                    // last selected pos == current position || is not in center
                                    if (position == mLastSelectedPos || !mTopicsAdapter.isInCenter(
                                            mLastSelectedPos)) {
                                        mHandler.removeMessages(MSG_SHOW_INDICATOR);
                                        mHandler.sendEmptyMessageDelayed(MSG_SHOW_INDICATOR,
                                                SCALE_DURATION);
                                    } else {
                                        setIndicatorActive(true);
                                    }
                                }
                            } else {
                                if (View.VISIBLE == mIndicatorView.getVisibility()) {
                                    setIndicatorActive(false);
                                }
                            }

                            FilmTopic filmTopic = mTopicsAdapter.getItem(position);
                            if (filmTopic != null) {
                                String posterUrl = filmTopic.getBackgroundposter();
                                refreshPoster(posterUrl);
                            }

                            mLastSelectedPos = position;
                        }
                    });
            mTopicsAdapter.setOnItemDisSelectedListener(
                    new RecyclerViewAdapterListener.OnItemDisSelectedListener() {
                        @Override
                        public void onItemDisSelected(RecyclerView recyclerView, int position,
                                View v) {
                            mHandler.removeMessages(MSG_SHOW_INDICATOR);
                            if (View.VISIBLE == mIndicatorView.getVisibility()) {
                                setIndicatorActive(false);
                            }
                        }
                    });
            mRecommendRv.setAdapter(mTopicsAdapter);
            mRecommendRv.requestFocus();
        } else {
            mTopicsAdapter.replaceData(topicList);
        }
    }

    private void setIndicatorActive(boolean active) {
        UIHelper.setViewVisible(mIndicatorView, active);
    }

    /**
     * refresh poster by url
     *
     * @param posterUrl url
     */
    private void refreshPoster(String posterUrl) {
        if (!StringUtils.isNullOrEmpty(posterUrl)) {
            if (mTarget != null) {
                Glide.clear(mTarget);
            }

            mTarget = Glide.with(this)
                    .load(posterUrl)
                    .error(R.color.default_bg)
//                    .crossFade
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                Target<GlideDrawable> target,
                                boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                Target<GlideDrawable> target, boolean isFromMemoryCache,
                                boolean isFirstResource) {
                            mAnimator.cancel();
                            mAnimator.start();
                            return false;
                        }
                    })
                    .into(mPosterIv);
        }
    }
}
