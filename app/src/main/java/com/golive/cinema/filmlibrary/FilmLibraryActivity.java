package com.golive.cinema.filmlibrary;

import static com.golive.cinema.Constants.INCLUDE_TOPICS_DEFAULT;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

/**
 * Created by Administrator on 2016/11/7.
 * 片库
 */

public class FilmLibraryActivity extends BaseActivity {

    public static void navigateTo(Context context, boolean showTopics) {
        navigateTo(context, INCLUDE_TOPICS_DEFAULT, showTopics);
    }

    public static void navigateTo(Context context, boolean includeTopics, boolean showTopics) {
        Intent intent = new Intent(context, FilmLibraryActivity.class);
        intent.putExtra(Constants.EXTRA_INCLUDE_TOPICS, includeTopics);
        intent.putExtra(Constants.EXTRA_SHOW_TOPICS, showTopics);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.film_library_act);

        boolean includeTopics = false;
        boolean showTopics = false;
        Intent intent = getIntent();
        if (intent != null) {
            includeTopics = intent.getBooleanExtra(Constants.EXTRA_INCLUDE_TOPICS,
                    INCLUDE_TOPICS_DEFAULT);
            showTopics = intent.getBooleanExtra(Constants.EXTRA_SHOW_TOPICS, false);
        }

        // Create the fragment
        FilmLibraryFragment fragment = FilmLibraryFragment.newInstance(includeTopics, showTopics);
        // Add new fragment
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.contentFrame);
        // Create the presenter
        Context context = getApplicationContext();
        FilmLibraryPresenter presenter = new FilmLibraryPresenter(fragment,
                Injection.provideGetFilmTabUseCase(context),
                Injection.provideGetFilmListUseCase(context)
        );
    }
}
