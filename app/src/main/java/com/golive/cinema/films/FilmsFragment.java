package com.golive.cinema.films;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_FILM_LIST;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.util.ItemClickSupport;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.ScrollChildSwipeRefreshLayout;
import com.golive.network.entity.Film;
import com.golive.network.response.FilmListResponse;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2016/7/12.
 */

public class FilmsFragment extends MvpFragment implements FilmsContract.View {

    private FilmsContract.Presenter mPresenter;
    private RecyclerView mRecyclerView;
    private FilmsAdapter mListAdapter;

    private boolean mIsFirstLoad = true;

    public FilmsFragment() {
        // Requires empty public constructor
    }

    public static FilmsFragment newInstance() {
        Logger.d("newInstance");
        return new FilmsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d("onCreate");
        super.onCreate(savedInstanceState);

        ArrayList<Film> films = new ArrayList<>(0);
        mListAdapter = new FilmsAdapter(films);
        mListAdapter.setHasStableIds(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getPresenter() != null) {
            getPresenter().start();
        }
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Logger.d("onCreateView");
        View root = inflater.inflate(R.layout.films_frag, container, false);

        // Set up films view
        mRecyclerView = (RecyclerView) root.findViewById(R.id.films_list);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) mRecyclerView.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setSupportsChangeAnimations(false);
        }
        mRecyclerView.setAdapter(mListAdapter);
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Logger.d("onItemClicked, position : " + position);
                        String filmId = mListAdapter.getItem(position).getReleaseid();
                        if (!StringUtils.isNullOrEmpty(filmId)) {
                            getPresenter().openFilmDetail(filmId);
                        }
                    }
                });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(mRecyclerView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPresenter().loadFilms(false);
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.films_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                getPresenter().loadFilms(true);
                break;
            default:
                getPresenter().loadFilms(true);
                break;
        }
        return true;
    }

    private void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_films, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.now:
                        getPresenter().setFiltering(FilmsFilterType.ON_NOW_FILMS);
                        break;
                    default:
                        getPresenter().setFiltering(FilmsFilterType.ALL_FILMS);
                        break;
                }
                getPresenter().loadFilms(false);
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (null == getView()) {
            return;
        }

        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showFilms(List<Film> films) {
        mListAdapter.replaceData(films);

        if (mIsFirstLoad) {
            mIsFirstLoad = false;
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.requestFocus();
                }
            });
        }
    }

    @Override
    public void showFilms(FilmListResponse films) {

    }

    @Override
    public void showFilmDetailUi(@NonNull String filmId) {
        FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId, VIEW_CODE_MAIN_FILM_LIST,
                false, 0);
    }

    @Override
    public void showLoadingFilmsError() {
        Logger.e("showLoadingFilmsError");
        Toast.makeText(getContext(), R.string.films_load_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showAllFilterLabel() {

    }

    @Override
    public void showOnNowFilterLabel() {

    }

    @Override
    public void showUpComingFilterLabel() {

    }

    @Override
    public void showEndingFilterLabel() {

    }

    @Override
    public void showNoFilms() {
        Logger.w("showNoFilms");
    }

    @Override
    public void showNoOnNowFilms() {

    }

    @Override
    public void showNoUpcomingFilms() {

    }

    @Override
    public void showEndingFilms() {

    }

    @Override
    public void setPresenter(FilmsContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected FilmsContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}