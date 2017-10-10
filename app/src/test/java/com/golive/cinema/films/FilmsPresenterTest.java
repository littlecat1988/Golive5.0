package com.golive.cinema.films;

/**
 * Created by Wangzj on 2016/7/12.
 */

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.films.domain.usecase.GetFilmListUseCase;
import com.golive.cinema.films.domain.usecase.GetFilmsUseCase;
import com.golive.cinema.util.schedulers.ImmediateSchedulerProvider;
import com.golive.network.entity.Film;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Unit tests for the implementation of {@link FilmsPresenter}
 */
public class FilmsPresenterTest {

    //@Rule
    //public RxSchedulersOverrideRule myRule = new RxSchedulersOverrideRule();

    @Mock
    private FilmsDataSource mDataSource;

    @Mock
    private FilmsContract.View mFilmsView;

    private FilmsPresenter mFilmsPresenter;
    private List<Film> FILMS;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mFilmsPresenter = givenFilmsPresenter();

        // The presenter won't update the view unless it's active.
        when(mFilmsView.isActive()).thenReturn(true);

        FILMS = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String id = String.valueOf(i);
            Film film = new Film();
            film.setReleaseid(id);
            film.setName(id);
            film.setIntroduction(id);
            FILMS.add(film);
        }
    }

    private FilmsPresenter givenFilmsPresenter() {
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();
        GetFilmsUseCase getFilms =
                new GetFilmsUseCase(mDataSource, schedulerProvider);
        GetFilmListUseCase getFilmListUseCase = new GetFilmListUseCase(mDataSource,
                null, schedulerProvider);
        return new FilmsPresenter(mFilmsView, getFilms, getFilmListUseCase);
    }

    @Test
    public void loadAllFilmsFromRepositoryAndLoadIntoView() {

        // Given an initialized TasksPresenter with initialized films
        when(mDataSource.getFilms()).thenReturn(Observable.just(FILMS));

        // When loading of films is requested
        mFilmsPresenter.setFiltering(FilmsFilterType.ALL_FILMS);
        mFilmsPresenter.loadFilms(true);

        // Callback is captured and invoked with stubbed films
        verify(mDataSource).getFilms();

        // Then progress indicator is shown
        verify(mFilmsView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mFilmsView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mFilmsView).showFilms(showTasksArgumentCaptor.capture());
        assertTrue(3 == showTasksArgumentCaptor.getValue().size());
        //verify(mFilmsView).showLoadingFilmsError();
    }
}
