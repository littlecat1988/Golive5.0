package com.golive.cinema.films;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;
import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2016/7/12.
 */
public class FilmsActivity extends BaseActivity {

    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    private FilmsPresenter mFilmsPresenter;

    public static void navigateToFilmsActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, FilmsActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("onCreate");

        setContentView(R.layout.films_act);

        FilmsFragment filmsFragment =
                (FilmsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (null == filmsFragment) {
            // Create the fragment
            filmsFragment = FilmsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), filmsFragment,
                    R.id.contentFrame);
        }

        // Create the presenter
        mFilmsPresenter =
                new FilmsPresenter(filmsFragment,
                        Injection.provideGetFilms(getApplicationContext())
                        , Injection.provideGetFilmList(getApplicationContext()));

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            FilmsFilterType currentFiltering =
                    (FilmsFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mFilmsPresenter.setFiltering(currentFiltering);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURRENT_FILTERING_KEY, mFilmsPresenter.getFiltering());
    }
}
