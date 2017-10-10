package com.golive.cinema.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.order.OrderManager;
import com.golive.cinema.player.kdm.KdmPlayerFragment;
import com.golive.cinema.player.noencrypt.PlayerFragment;
import com.golive.cinema.player.voole.VoolePlayerFragment;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Ad;
import com.golive.network.entity.Order;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2016/9/12.
 */

public class PlayerActivity extends BaseActivity {
    private PlayerContract.Presenter mPlayerPresenter;
    private BasePlayerFragment mPlayerFragment;

    public static void navigateToPlayerActivity(@NonNull Context context,
            @NonNull String filmId, @Nullable String mediaID, @Nullable String mediaName,
            boolean isOnline, @NonNull String encryptionType, @NonNull List<String> urls,
            @NonNull List<Ad> advertList, @Nullable String posterUrl,
            boolean isTrailer, int[] colorBg, List<String> ranks) {
        Intent intent = getNavigateIntent(context, filmId, mediaID, mediaName, isOnline,
                encryptionType, urls, ranks, advertList, posterUrl, isTrailer, colorBg);
        context.startActivity(intent);
    }

    @NonNull
    public static Intent getNavigateIntent(@NonNull Context context, @NonNull String filmId,
            @Nullable String mediaID, @Nullable String mediaName, boolean isOnline,
            @NonNull String encryptionType, @NonNull List<String> urls, List<String> ranks,
            @Nullable List<Ad> advertList, @Nullable String posterUrl, boolean isTrailer,
            int[] colorBg) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(Constants.PLAYER_INTENT_FILM_ID, filmId);
        intent.putExtra(Constants.PLAYER_INTENT_MEDIA_ID, mediaID);
        intent.putExtra(Constants.PLAYER_INTENT_NAME, mediaName);
        intent.putExtra(Constants.EXTRA_IS_ONLINE, isOnline);
        intent.putExtra(Constants.PLAYER_INTENT_ENCRYPTION_TYPE, encryptionType);
        if (urls != null) {
            ArrayList<String> urlList = new ArrayList<>(urls);
            intent.putStringArrayListExtra(Constants.PLAYER_INTENT_URLS, urlList);
        }
        if (ranks != null) {
            ArrayList<String> rankList = new ArrayList<>(ranks);
            intent.putStringArrayListExtra(Constants.PLAYER_INTENT_RANKS, rankList);
        }
        intent.putExtra(Constants.PLAYER_INTENT_FILM_ID_POSTER, posterUrl);
        if (advertList != null) {
            ArrayList<Ad> adverts = new ArrayList<>(advertList);
            intent.putExtra(Constants.PLAYER_INTENT_FILM_ADVERT_ALL, adverts);
        }
        intent.putExtra(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER, isTrailer);
        intent.putExtra(Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR, colorBg);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_act);
        mPlayerFragment =
                (BasePlayerFragment) getSupportFragmentManager().findFragmentById(
                        R.id.contentFrame);
        Intent intent = getIntent();
        String filmId = intent.getStringExtra(Constants.PLAYER_INTENT_FILM_ID);
        String encryptionType = intent.getStringExtra(Constants.PLAYER_INTENT_ENCRYPTION_TYPE);
        String playType = Constants.ONLINE;
        if (null == mPlayerFragment) {
            String mediaName = intent.getStringExtra(Constants.PLAYER_INTENT_NAME);
            // default is online
            boolean isOnline = intent.getBooleanExtra(Constants.EXTRA_IS_ONLINE, true);
            List<String> urls = intent.getStringArrayListExtra(Constants.PLAYER_INTENT_URLS);
            List<String> ranks = intent.getStringArrayListExtra(Constants.PLAYER_INTENT_RANKS);
            String posterUrl = intent.getStringExtra(Constants.PLAYER_INTENT_FILM_ID_POSTER);
            List<Ad> advertList = (List<Ad>) intent.getSerializableExtra(
                    Constants.PLAYER_INTENT_FILM_ADVERT_ALL);

            int[] colorBg = intent.getIntArrayExtra(Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR);

            Logger.d("onCreate, urls : " + urls);
            boolean isTrailer = intent.getBooleanExtra(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER,
                    false);
            if (!StringUtils.isNullOrEmpty(encryptionType)) {
                switch (encryptionType) {
                    case Constants.PLAYER_ENCRYPTION_TYPE_KDM:
                        mPlayerFragment = KdmPlayerFragment.newInstance(urls, ranks, isOnline,
                                filmId, mediaName, posterUrl, advertList, isTrailer, colorBg);
                        playType = isOnline ? Constants.ONLINE_KDM : Constants.DOWNLOAD_KDM;
                        break;
                    case Constants.PLAYER_ENCRYPTION_TYPE_VOOLE:
                        mPlayerFragment = VoolePlayerFragment.newInstance(urls, ranks, mediaName,
                                filmId, isOnline, posterUrl, advertList, isTrailer, colorBg);
                        break;
                    case Constants.PLAYER_ENCRYPTION_TYPE_DEFAULT: // combine with default!
                    default:
                        mPlayerFragment = PlayerFragment.newInstance(urls, ranks, mediaName, filmId,
                                posterUrl, advertList, isTrailer, colorBg);
                        break;
                }
            }

            if (null == mPlayerFragment) {
                // use default player
                mPlayerFragment = PlayerFragment.newInstance(urls, ranks, mediaName, filmId,
                        posterUrl, advertList, isTrailer, colorBg);
            }

            // set film id
            mPlayerFragment.setFilmId(filmId);
            // set media name
            mPlayerFragment.setMediaName(mediaName);
            // set online
            mPlayerFragment.setOnlinePlay(isOnline);

            // Create the fragment
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mPlayerFragment,
                    R.id.contentFrame);

            // Create the presenter
            Context context = getApplicationContext();
            mPlayerPresenter =
                    new PlayerPresenter(mPlayerFragment,
                            Injection.provideGetMainConfigUseCase(context),
                            Injection.provideGetMovieRecommendUseCase(context),
                            Injection.provideGetUserInfoUseCase(context),
                            Injection.provideAddHistoryUseCase(context),
                            Injection.provideGetValidOrder(context),
                            Injection.provideAdvertUseCase(context),
                            Injection.provideSchedulerProvider(), filmId, playType, isTrailer);

            addFilmToHistory(filmId, Order.PRODUCT_TYPE_THEATRE_ONLINE);
        }

    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPlayerFragment != null) {
            return mPlayerFragment.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void addFilmToHistory(String filmId, String mediaType) {
        if (mPlayerPresenter != null) {
            Order filmOrder = OrderManager.getInstance().getOrder(filmId, mediaType);
            if (filmOrder != null) {
                mPlayerPresenter.addToHistory(filmOrder.getSerial());
            }
        }

    }
}
