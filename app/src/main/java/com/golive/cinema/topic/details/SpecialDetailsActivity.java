package com.golive.cinema.topic.details;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;
import com.golive.network.entity.FilmTopic;

/**
 * Created by Administrator on 2017/5/22.
 */

public class SpecialDetailsActivity extends BaseActivity {
    public static final String SPECIAL_DETAILS_ID = "id";
    private String mTopicId;

    public static void start(Activity fromActivity, FilmTopic filmTopic) {
        Intent intent = new Intent(fromActivity, SpecialDetailsActivity.class);
        if (filmTopic != null) {
            String detailId = String.valueOf(filmTopic.getId());
            intent.putExtra(SpecialDetailsActivity.SPECIAL_DETAILS_ID, detailId);
        }
        fromActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.special_detail_activity);
        if (getIntent() != null) {
            mTopicId = getIntent().getStringExtra(SPECIAL_DETAILS_ID);
        }
        setHeaderViewVisible(true);
        SpecialDetailsFragment fragment = SpecialDetailsFragment.newInstance(mTopicId);
        new SpecialDetaliPresenter(fragment,
                Injection.provideGetFilmTopicDetailUseCase(getApplicationContext()));
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.detail_frameLayout);
    }
}
