package com.golive.cinema.player.voole;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.player.BasePlayerFragment;
import com.golive.cinema.player.PlayerContract;
import com.golive.cinema.player.PlayerOperation;
import com.golive.network.entity.Ad;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;
import com.voole.player.lib.core.VooleMediaPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2016/12/27.
 */

public class VoolePlayerFragment extends BasePlayerFragment {

    private VooleMediaPlayer mVooleMediaPlayer;

    public static VoolePlayerFragment newInstance(List<String> mediaUrls, List<String> ranks,
            String mediaName, String filmId, boolean isOnline, String posterUrl,
            List<Ad> advertList, boolean isTrailer, int[] colorBg) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PLAYER_INTENT_FILM_ID, filmId);
        ArrayList<String> urlList = new ArrayList<>(mediaUrls);
        ArrayList<String> rankList = new ArrayList<>(ranks);
        bundle.putStringArrayList(Constants.PLAYER_INTENT_URLS, urlList);
        bundle.putStringArrayList(Constants.PLAYER_INTENT_RANKS, rankList);
        bundle.putString(Constants.PLAYER_INTENT_NAME, mediaName);
        bundle.putBoolean(Constants.EXTRA_IS_ONLINE, isOnline);
        bundle.putString(Constants.PLAYER_INTENT_FILM_ID_POSTER, posterUrl);
        bundle.putSerializable(Constants.PLAYER_INTENT_FILM_ADVERT_ALL, (Serializable) advertList);
        bundle.putBoolean(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER, isTrailer);
        bundle.putIntArray(Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR, colorBg);

        VoolePlayerFragment fragment = new VoolePlayerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentView(R.layout.voole_player_frag);
        mVooleMediaPlayer = (VooleMediaPlayer) view.findViewById(R.id.voole_player);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("onResume");
        startPlayer(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d("onPause");
        stopPlayer();
    }

    @Override
    public boolean isReadyToPlay() {
        return super.isReadyToPlay() || isResumed();
    }

    @Override
    protected String getErrorDescription(int errCode, int extra) {
        return null;
    }

    private void init() {

        Bundle arguments = getArguments();
//        boolean isOnline = arguments.getBoolean(Constants.EXTRA_IS_ONLINE);

        Context applicationContext = getContext().getApplicationContext();
        Voole voole = new Voole(applicationContext);
        // create player operator
        PlayerOperation playerOperation = new VooleOperationImpl(mVooleMediaPlayer, voole);

        List<String> mediaUrls = arguments.getStringArrayList(Constants.PLAYER_INTENT_URLS);
        playerOperation.setMediaUrls(mediaUrls);

        String mediaName = arguments.getString(Constants.PLAYER_INTENT_NAME);
        playerOperation.setMediaName(mediaName);
        setMediaName(mediaName);

        String filmId = arguments.getString(Constants.PLAYER_INTENT_FILM_ID);
        playerOperation.setFilmId(filmId);
        setFilmId(filmId);

        List<Ad> advertList = (List<Ad>) arguments.getSerializable(
                Constants.PLAYER_INTENT_FILM_ADVERT_ALL);
//        playerOperation.setAdvertList(advertList);
        getPresenter().setAdvertList(advertList);
        boolean trailer = arguments.getBoolean(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER, false);
        setTrailer(trailer);

        PlayerContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            // set this player operator to the Presenter.
            presenter.setPlayerOperation(playerOperation);
        }

        List<String> ranks = arguments.getStringArrayList(Constants.PLAYER_INTENT_RANKS);
        if (ranks != null && ranks.size() > 0) {
            playerOperation.setRanks(ranks);
        }

        int rank = UserInfoHelper.getUserPlayMediaRank(getContext(), getFilmId());
        if (rank > 0) {
            playerOperation.setRank(rank);
        }
    }
}
