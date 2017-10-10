package com.golive.cinema.topic.past;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.topic.details.SpecialDetailsActivity;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.metroviews.widget.PastTopicRecyclerView;
import com.golive.network.entity.FilmTopic;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/1.
 */

public class PastTopicFragment extends MvpFragment implements PastTopicContract.View,
        RecyclerViewAdapterListener.OnItemSelectedListener,
        RecyclerViewAdapterListener.OnItemDisSelectedListener,
        RecyclerViewAdapterListener.OnItemClickListener {
    private PastTopicRecyclerView mRecyclerView;
    private PastTopicAdapter mTopicAdapter;
    private List<FilmTopic> mFilmTopics;
    private PastTopicContract.Presenter mPresenter;
    private int mDataSize;
    private OnPastItemSelectListener mOnPastItemSelectListener;

    public static PastTopicFragment newInstance() {
        PastTopicFragment fragment = new PastTopicFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.past_topic_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (PastTopicRecyclerView) view.findViewById(R.id.past_topic_list);
        setItemDecoration();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFilmTopics = new ArrayList<>();
        //mFilmTopics = test();
        mTopicAdapter = new PastTopicAdapter(mFilmTopics, this);
        mTopicAdapter.setOnItemClickListener(this);
        mTopicAdapter.setOnItemDisSelectedListener(this);
        mTopicAdapter.setOnItemSelectedListener(this);
        mRecyclerView.setAdapter(mTopicAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2,
                LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        // showPostTopicView(mFilmTopics);
        new PastTopicPresenter(this,
                Injection.provideGetOldFilmTopicsUseCase(getContext().getApplicationContext()));
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    private void setItemDecoration() {
        if (mRecyclerView != null) {
            mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                        RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    int position = parent.getChildAdapterPosition(view);
                    if (position - 1 > 0) {
                        outRect.left = -(int) getResources().getDimension(
                                R.dimen.past_topic_item_spacing);
                    } else {
                        outRect.left = (int) getResources().getDimension(
                                R.dimen.past_topic_first_margin_padding);
                    }
                    if (mDataSize % 2 == 0) {
                        //偶数个 最后两个设置边框
                        if (mDataSize - position <= 2) {
                            outRect.right = (int) getResources().getDimension(
                                    R.dimen.past_topic_first_margin_padding);
                        }
                    } else {
                        //奇数个
                        if (mDataSize - position == 1) {
                            outRect.right = (int) getResources().getDimension(
                                    R.dimen.past_topic_first_margin_padding);
                        }
                    }
                    //基数
                    if (position % 2 != 0) {
                        //第二排
                        outRect.top = -5;
                    } else {
                        //第一排
                        outRect.bottom = -10;
                    }
                }
            });
        }
    }

    @Override
    protected IBasePresenter getPresenter() {
        return mPresenter;
    }

//    private List<FilmTopic> test() {
//        //测试
//        List<FilmTopic> list = new ArrayList<>();
//        FilmTopic specialDetail = new FilmTopic();
//        specialDetail.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail.setTitle("变形精钢1");
//        specialDetail.setId(1);
//        list.add(specialDetail);
//
//        FilmTopic specialDetail1 = new FilmTopic();
//        specialDetail1.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail1.setTitle("变形精钢2");
//        specialDetail1.setId(2);
//        list.add(specialDetail1);
//
//        FilmTopic specialDetail2 = new FilmTopic();
//        specialDetail2.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail2.setTitle("变形精钢3");
//        specialDetail2.setId(3);
//        list.add(specialDetail2);
//
//        FilmTopic specialDetail3 = new FilmTopic();
//        specialDetail3.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail3.setTitle("变形精钢4");
//        specialDetail3.setId(4);
//        list.add(specialDetail3);
//
//        FilmTopic specialDetail4 = new FilmTopic();
//        specialDetail4.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail4.setTitle("变形精钢5");
//        specialDetail4.setId(5);
//        list.add(specialDetail4);
//
//        FilmTopic specialDetail5 = new FilmTopic();
//        specialDetail5.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail5.setTitle("变形精钢6");
//        specialDetail5.setId(6);
//        list.add(specialDetail5);
//
//        FilmTopic specialDetail6 = new FilmTopic();
//        specialDetail6.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail6.setTitle("变形精钢7");
//        specialDetail6.setId(7);
//        list.add(specialDetail6);
//
//        FilmTopic specialDetail7 = new FilmTopic();
//        specialDetail7.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail7.setTitle("变形精钢8");
//        specialDetail7.setId(8);
//        list.add(specialDetail7);
//
//        FilmTopic specialDetail8 = new FilmTopic();
//        specialDetail8.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail8.setTitle("变形精钢9");
//        specialDetail8.setId(9);
//        list.add(specialDetail8);
//
//        FilmTopic specialDetail9 = new FilmTopic();
//        specialDetail9.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail9.setTitle("变形精钢10");
//        specialDetail9.setId(10);
//        list.add(specialDetail9);
//
//        FilmTopic specialDetail10 = new FilmTopic();
//        specialDetail10.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail10.setTitle("变形精钢11");
//        specialDetail10.setId(11);
//        list.add(specialDetail10);
//
//        FilmTopic specialDetail11 = new FilmTopic();
//        specialDetail11.setCoverposter("http://huidu.img.golivetv
// .tv/uploadfiles/cinema/2017042709232179.png");
//        specialDetail11.setTitle("变形精钢12");
//        specialDetail11.setId(12);
//        list.add(specialDetail11);
//        return list;
//    }

    @Override
    public void showErrorView(String errMsg) {
        String text = getString(R.string.topic_list_get_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void showEmptyView() {
        //数据为空效果

    }

    @Override
    public void showPostTopicView(List<FilmTopic> list) {
        if (list == null || list.size() == 0) return;
        mDataSize = list.size();
        //展示数据
        mTopicAdapter.refreshData(list);
        mRecyclerView.setVisibility(View.VISIBLE);

    }


    @Override
    public void setPresenter(PastTopicContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onItemSelected(RecyclerView recyclerView, int position, View v) {
        if (mOnPastItemSelectListener != null) {
            FilmTopic filmTopic = mTopicAdapter.getItem(position);
            PastTopicAdapter.PastTopicViewHolder viewHolder =
                    (PastTopicAdapter.PastTopicViewHolder) recyclerView
                            .findViewHolderForAdapterPosition(
                            position);
            mOnPastItemSelectListener.onItemSelect(filmTopic, mDataSize, viewHolder);
        }
    }


    @Override
    public void onItemDisSelected(RecyclerView recyclerView, int position, View v) {

    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        FilmTopic filmTopic = mTopicAdapter.getItem(position);
        SpecialDetailsActivity.start(getActivity(), filmTopic);
    }

    public void setOnPastItemSelectListener(OnPastItemSelectListener onPastItemSelectListener) {
        this.mOnPastItemSelectListener = onPastItemSelectListener;
    }

    public interface OnPastItemSelectListener {
        void onItemSelect(FilmTopic filmTopic, int mDataSize,
                PastTopicAdapter.PastTopicViewHolder viewHolder);
    }

}