package com.golive.cinema.topic.past;

import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

/**
 * Created by Administrator on 2017/6/1.
 */

public class PastTopicActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.special_detail_activity);
        PastTopicFragment fragment= PastTopicFragment.newInstance();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.detail_frameLayout);
    }
}
