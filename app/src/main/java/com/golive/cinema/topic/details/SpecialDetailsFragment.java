package com.golive.cinema.topic.details;


import static com.golive.cinema.Constants.SCALE_DURATION;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.util.DensityUtil;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.metroviews.widget.PastTopicRecyclerView;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/22.
 */

public class SpecialDetailsFragment extends MvpFragment implements SpecialDetailsContract.View,
        RecyclerViewAdapterListener.OnItemSelectedListener,
        RecyclerViewAdapterListener.OnItemDisSelectedListener,
        RecyclerViewAdapterListener.OnItemClickListener {
    private static final int DATA_LIST_COUNT = 6;
    private SpecialDetailsContract.Presenter mPresenter;
    private ImageView mSpecialIv;
    private FrameLayout mFragmentError;
    private PastTopicRecyclerView mPastTopicRecyclerView;
    private int mMoveDistance;
    private int mScreenWidth;
    //计算是向左还是向右
    private int mLoseFocusPosition;
    private SpecialDetailsAdapter mAdapter;
    private List<MovieRecommendFilm> mFilms;
    private String mTopicId;

    public static SpecialDetailsFragment newInstance(String mTopicId) {
        SpecialDetailsFragment specialDetailsFragment = new SpecialDetailsFragment();
        if (!TextUtils.isEmpty(mTopicId)) {
            Bundle fragmentBundle = new Bundle();
            fragmentBundle.putString(SpecialDetailsActivity.SPECIAL_DETAILS_ID, mTopicId);
            specialDetailsFragment.setArguments(fragmentBundle);
        }
        return specialDetailsFragment;
    }

    @Override
    protected SpecialDetailsContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTopicId = getArguments().getString(SpecialDetailsActivity.SPECIAL_DETAILS_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speacial_detail_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFilms = new ArrayList<>();
        if (mPresenter != null) {
            mPresenter.loadTopicDetail(mTopicId);
        }
        //获取屏幕的宽度
        mScreenWidth = DensityUtil.getScreenWidth(getContext());
        mAdapter = new SpecialDetailsAdapter(mFilms, this);
        //  mFilms=test();
        initView(getView());
    }

    private void initView(View view) {
        //Toast.makeText(mActivity,"width:"+DensityUtil.getScreenWidth(mActivity)+"height;
        // "+DensityUtil.getScreenHeight(mActivity)+"密度:"+DensityUtil.getDensity(mActivity),Toast
        // .LENGTH_LONG).show();
        mSpecialIv = (ImageView) view.findViewById(R.id.imageView);
        mPastTopicRecyclerView = (PastTopicRecyclerView) view.findViewById(R.id.recyleView_detail);
        mAdapter.setOnItemSelectedListener(this);
        mAdapter.setOnItemDisSelectedListener(this);
        mAdapter.setmOnItemClickListener(this);
        mPastTopicRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPastTopicRecyclerView.setLayoutManager(linearLayoutManager);
        setItemDecoration();
        mFragmentError = (FrameLayout) view.findViewById(R.id.fragment_error);
        //    showDetailView(mFilms,"http://huidu.img.golivetv
        // .tv/uploadfiles/cinema/2017042709232179.png11s");
//        loadImageView(R.drawable.my_background);
    }

    private void setItemDecoration() {
        if (mPastTopicRecyclerView != null) {
            mPastTopicRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                        RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    //dp转成px  设置item的间距
                    int size = mFilms != null ? mFilms.size() : 0;
                    if (parent.getChildAdapterPosition(view) == 0) {
                        if (size > DATA_LIST_COUNT) {
                            //左右两边都设置间距
                            outRect.left = (int) getResources().getDimension(
                                    R.dimen.special_detail_margin_left);
                        } else {
                            outRect.left = (int) getResources().getDimension(
                                    R.dimen.special_detail_item_left_first_offset);
                        }
                    } else {
                        if (size > DATA_LIST_COUNT && parent.getChildAdapterPosition(view)
                                == size - 1) {
                            outRect.right = (int) getResources().getDimension(
                                    R.dimen.special_detail_margin_left);
                        }
                        outRect.left = -(int) getResources().getDimension(
                                R.dimen.special_detail_item_left_offset);
                    }
                    outRect.top = (int) getResources().getDimension(
                            R.dimen.special_detail_item_top_offset);
                    outRect.bottom = (int) getResources().getDimension(
                            R.dimen.special_detail_item_top_offset);
                }
            });
        }
    }

    @Override
    public void showErrorView(String errMsg) {
        String text = getString(R.string.topic_detail_get_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEmptyView() {
        mFragmentError.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDetailView(List<MovieRecommendFilm> list, String url) {
        //加在图片
        loadImageView(url);
        if (list != null) {
            mFilms = list;
            mAdapter.refreshData(list);
        }

    }

    private void loadImageView(String imageUrl) {
        //if(TextUtils.isEmpty(imageUrl))return;
        Glide.with(this)
                .load(imageUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap,
                            GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap == null) {
                            return;
                        }

                        //大背景图片每次移动的距离
                        mSpecialIv.setImageBitmap(bitmap);
                        if (mFilms != null) {
                            int moveCount = mFilms.size() - 1;
                            int bitmapWidth = bitmap.getWidth();
                            if (moveCount > 0 && bitmapWidth > mScreenWidth) {
                                //重新设置imageview的宽度
                                ViewGroup.MarginLayoutParams params =
                                        (ViewGroup.MarginLayoutParams) mSpecialIv.getLayoutParams();
                                params.width = bitmapWidth;
                                mSpecialIv.setLayoutParams(params);
                                mMoveDistance = (bitmapWidth - mScreenWidth) / moveCount;
                            }
                        }
                    }
                });
    }

    @Override
    public void onItemSelected(RecyclerView recyclerView, int position, View v) {
        //选中
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        showAnimation(holder.itemView, 1.06f);
        int direction = position - mLoseFocusPosition;
        setImageViewAnimation(direction);
    }

    @Override
    public void onItemDisSelected(RecyclerView recyclerView, int position, View v) {
        //失去焦点
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        mLoseFocusPosition = position;
        showAnimation(holder.itemView, 1.0f);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        String filmId = mFilms.get(position).getReleaseid();
        if (StringUtils.isNullOrEmpty(filmId)) {
            Toast.makeText(getContext(), R.string.film_detail_missing_film,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId, 0, false, 0);
    }

    @Override
    public void setPresenter(SpecialDetailsContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        //获取焦点
        return isAdded();
    }

    private void showAnimation(View itemView, float scale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            itemView.clearAnimation();
            itemView.animate().scaleX(scale).scaleY(scale).setDuration(SCALE_DURATION).start();
        }

    }


    private void setImageViewAnimation(int direction) {
        float positionX = mSpecialIv.getX();
        if (direction > 0) {
            //右移
            ObjectAnimator.ofFloat(mSpecialIv, "translationX", positionX,
                    positionX - mMoveDistance).setDuration(100).start();
        } else if (direction < 0) {
            //左移
            ObjectAnimator.ofFloat(mSpecialIv, "translationX", positionX,
                    positionX + mMoveDistance).setDuration(100).start();
        }
    }
}

