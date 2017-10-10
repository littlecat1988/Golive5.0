package com.golive.cinema.filmdetail;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.StringUtils;
import com.initialjie.log.Logger;

import java.io.File;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class FilmDetailActivity extends BaseActivity {

    public static void jumpToFilmDetailActivity(@NonNull Context context, @NonNull String filmId,
            int from, boolean startForResult, int requestCode) {
        checkNotNull(context);
        checkNotNull(filmId);
        Intent intent = new Intent(context, FilmDetailActivity.class);
        intent.putExtra(Constants.EXTRA_FILM_ID, filmId);
        intent.putExtra(Constants.EXTRA_FROM, from);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // startActivityForResult?
        if (startForResult) {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity.startActivityForResult(intent, requestCode);
                return;
            }
        }

        // normal start activity
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.filmdetail_act);
        Intent intent = getIntent();
        openFilmDetail(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Logger.d("onNewIntent");
        openFilmDetail(intent);
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    /**
     * Open film detail view
     *
     * @param intent intent
     */
    private void openFilmDetail(Intent intent) {
        Logger.d("openFilmDetail");
        // Get the requested film id
        String filmId = intent.getStringExtra(Constants.EXTRA_FILM_ID);
        if (StringUtils.isNullOrEmpty(filmId)) {
            // compatible for old version
            filmId = intent.getStringExtra(Constants.INTENT_FILM_ID);
        }
        int from = intent.getIntExtra(Constants.EXTRA_FROM, VIEW_CODE_MAIN_ACTIVITY);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FilmDetailFragment filmDetailFragment =
                (FilmDetailFragment) fragmentManager.findFragmentById(R.id.contentFrame);

        // has previous fragment
        if (filmDetailFragment != null) {
            // remove old fragment
            ActivityUtils.removeFragmentToActivity(fragmentManager, filmDetailFragment);
        }

        // Create the fragment
        filmDetailFragment = FilmDetailFragment.newInstance(filmId, from);
        // Add new fragment
        ActivityUtils.addFragmentToActivity(fragmentManager, filmDetailFragment, R.id.contentFrame);

        // Create the presenter
        Context applicationContext = getApplicationContext();
        File dataFile = new File(getApplicationInfo().dataDir, "files");
        FilmDetailPresenter filmDetailPresenter = new FilmDetailPresenter(filmId,
                dataFile.getAbsolutePath(), filmDetailFragment,
                Injection.provideGetFilmDetail(applicationContext),
                Injection.provideGetMovieRecommendUseCase(applicationContext),
                Injection.provideGetValidOrder(applicationContext),
                Injection.provideGetUserWalletUseCase(applicationContext),
                Injection.provideGetUserCreditWalletUseCase(applicationContext),
                Injection.providePurchaseFilmUseCase(applicationContext),
                Injection.provideGetUserInfoUseCase(applicationContext),
                Injection.provideGetPlaybackValidityUseCase(applicationContext),
                Injection.provideGetDownloadTaskInfoUseCase(applicationContext),
                Injection.provideGetPlayTicketUseCase(applicationContext),
                Injection.provideReportEnterFilmDetailUseCase(applicationContext),
                Injection.provideReportExitFilmDetailUseCase(applicationContext),
                Injection.provideSchedulerProvider());
    }
}
