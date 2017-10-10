package com.golive.cinema.player.kdm;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.player.BasePlayerFragment;
import com.golive.cinema.player.PlayerContract;
import com.golive.cinema.player.PlayerOperation;
import com.golive.cinema.util.ResourcesUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Ad;
import com.golive.player.kdm.KDMDeviceID;
import com.golive.player.kdm.KDMPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for playing unencrypted medias.
 * <p/>
 * Created by Wangzj on 2016/9/12.
 */
public class KdmPlayerFragment extends BasePlayerFragment {

    private SurfaceView mSurfaceView;

    public static KdmPlayerFragment newInstance(List<String> mediaUrls, List<String> ranks,
            boolean isOnline, String filmId, String mediaName, String posterUrl,
            List<Ad> advertList, boolean isTrailer, int[] colorBg) {
        KdmPlayerFragment fragment = new KdmPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PLAYER_INTENT_FILM_ID, filmId);
        ArrayList<String> urlList = new ArrayList<>(mediaUrls);
        ArrayList<String> rankList = new ArrayList<>(ranks);
        bundle.putStringArrayList(Constants.PLAYER_INTENT_URLS, urlList);
        bundle.putStringArrayList(Constants.PLAYER_INTENT_RANKS, rankList);
        bundle.putBoolean(Constants.EXTRA_IS_ONLINE, isOnline);
        bundle.putString(Constants.PLAYER_INTENT_NAME, mediaName);
        bundle.putString(Constants.PLAYER_INTENT_FILM_ID_POSTER, posterUrl);
        bundle.putSerializable(Constants.PLAYER_INTENT_FILM_ADVERT_ALL, (Serializable) advertList);
        bundle.putBoolean(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER, isTrailer);
        bundle.putIntArray(Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR, colorBg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentView(R.layout.kdm_player_frag);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.kdm_player_surfaceView);
        init();
    }

    @Override
    protected String getErrorDescription(int errCode, int extra) {
        return ResourcesUtils.getErrorDescription(getContext(), String.valueOf(100000 + errCode));
    }

    @Override
    public boolean isRetrySupport() {
        // retry not support!
        return false;
    }

    private void init() {
        mSurfaceView.getHolder().addCallback(this);

        Bundle bundle = getArguments();
        List<String> mediaUrls = bundle.getStringArrayList(Constants.PLAYER_INTENT_URLS);
        boolean isOnline = bundle.getBoolean(Constants.EXTRA_IS_ONLINE);

        // generate a player operator
        Context context = getContext().getApplicationContext();
        PlayerOperation playerOperation = new KdmOperationImpl(context,
                isOnline ? KDMPlayer.ONLINE : KDMPlayer.DOWNLOAD, true,
                KDMDeviceID.CompanyType.TCL);
        playerOperation.setMediaUrls(mediaUrls);

        String filmId = bundle.getString(Constants.PLAYER_INTENT_FILM_ID);
        //film name
        String mediaName = bundle.getString(Constants.PLAYER_INTENT_NAME);
        playerOperation.setFilmId(filmId);
        playerOperation.setMediaName(mediaName);
        setFilmId(filmId);
        setMediaName(mediaName);

        List<Ad> advertList = (List<Ad>) bundle.getSerializable(
                Constants.PLAYER_INTENT_FILM_ADVERT_ALL);
        getPresenter().setAdvertList(advertList);
        boolean trailer = bundle.getBoolean(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER, false);
        setTrailer(trailer);

        PlayerContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            // set this player operator to the Presenter.
            presenter.setPlayerOperation(playerOperation);
        }

        List<String> ranks = bundle.getStringArrayList(Constants.PLAYER_INTENT_RANKS);
        if (ranks != null && ranks.size() > 0) {
            int rank = 0;
            if (!StringUtils.isNullOrEmpty(ranks.get(0))) {
                rank = Integer.parseInt(ranks.get(0));
            }
            if (rank > 0) {
                playerOperation.setRank(rank);
            }
            playerOperation.setRanks(ranks);
        }
    }
}
