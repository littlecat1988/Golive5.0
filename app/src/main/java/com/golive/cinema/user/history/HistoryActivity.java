package com.golive.cinema.user.history;

import android.content.Context;
import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

public class HistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        HistoryFragment fragment = HistoryFragment.newInstance();
        Context context = getApplicationContext();
        new HistoryPresenter(fragment,
                Injection.provideGetHistoryListUseCase(context),
                Injection.provideDeleteHistoryUseCase(context),
                Injection.provideGetMovieRecommendUseCase(context));
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.userContentFrame);
    }
}
