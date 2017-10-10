package com.golive.cinema.player.noencrypt;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.VideoView;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.player.BasePlayerFragment;
import com.golive.cinema.player.PlayerContract;
import com.golive.cinema.player.PlayerOperation;
import com.golive.cinema.util.NetworkUtils;
import com.golive.network.entity.Ad;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for playing unencrypted medias.
 * <p/>
 * Created by Wangzj on 2016/9/12.
 */
public class PlayerFragment extends BasePlayerFragment {

    private VideoView mVideoView;

    public static PlayerFragment newInstance(List<String> mediaUrls, List<String> ranks,
            String mediaName, String filmId, String posterUrl, List<Ad> advertList,
            boolean isTrailer, int[] colorBg) {
        Logger.d("newInstance, urls : " + mediaUrls);
        PlayerFragment fragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PLAYER_INTENT_FILM_ID, filmId);
        ArrayList<String> urlList = new ArrayList<>(mediaUrls);
        ArrayList<String> rankList = new ArrayList<>(ranks);
        bundle.putStringArrayList(Constants.PLAYER_INTENT_URLS, urlList);
        bundle.putStringArrayList(Constants.PLAYER_INTENT_RANKS, rankList);
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
        setContentView(R.layout.player_frag);
        mVideoView = (VideoView) view.findViewById(R.id.player_videoView);
        init();
    }

    @Override
    protected String getErrorDescription(int errCode, int extra) {
        String errMsg;
        switch (errCode) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                errMsg = getString(R.string.play_error_unknown);
                break;

            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
            default:
                errMsg = getString(R.string.play_error_unknown);
                break;
        }

        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO: {
                String string;
                if (NetworkUtils.isNetworkAvailable(getContext())) {
                    string = getString(R.string.play_error_media_error);
                } else {
                    string = getString(R.string.play_error_network_error);
                }
                errMsg += "," + string;
            }
            break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                errMsg += "," + getString(R.string.play_error_connect_timeout);
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                errMsg += "," + getString(R.string.play_error_format_not_support);
                break;

            default:
                break;
        }
        return errMsg;
    }

    private void init() {
        mVideoView.getHolder().addCallback(this);

        Bundle bundle = getArguments();
        String filmId = bundle.getString(Constants.PLAYER_INTENT_FILM_ID);

        List<String> mediaUrls = bundle.getStringArrayList(Constants.PLAYER_INTENT_URLS);
        Logger.d("init, urls : " + mediaUrls);

        // generate a player operator
        PlayerOperation playerOperation = new PlayerOperationImpl(mVideoView);
        playerOperation.setMediaUrls(mediaUrls);
        Logger.d("url:" + mediaUrls.toString());
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
            playerOperation.setRanks(ranks);
        }

        int rank = UserInfoHelper.getUserPlayMediaRank(getContext(), getFilmId());
        if (rank > 0) {
            playerOperation.setRank(rank);
        }
    }
}
