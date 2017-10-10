package com.golive.cinema.data.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.golive.network.entity.Film;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import rx.Observable;
import rx.observers.TestSubscriber;

/**
 * Created by Wangzj on 2016/9/5.
 */

public class FilmsRepositoryTest {

    private FilmsRepository mFilmsRepository;

    @Mock
    private FilmsDataSource mFilmsRemoteDataSource;

    @Mock
    private FilmsDataSource mFilmsLocalDataSource;

    private List<Film> FILMS;

    private TestSubscriber<List<Film>> mFilmsTestSubscriber;

    @Before
    public void setupFilmsRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mFilmsRepository = FilmsRepository.getInstance(
                mFilmsRemoteDataSource, mFilmsLocalDataSource);

        mFilmsTestSubscriber = new TestSubscriber<>();

        FILMS = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Film film = new Film();
            String id = String.valueOf(1);
            film.setReleaseid(id);
            film.setName(id);
            FILMS.add(film);
        }
    }

    @After
    public void destroyRepositoryInstance() {
        FilmsRepository.destroyInstance();
    }

    @Test
    public void deleteFilm_deleteFilmToServiceAPIRemovedFromCache() {

        // Given a film in the repository
        Film film = new Film();
        String id = String.valueOf(1);
        film.setReleaseid(id);
        film.setName(id);
        mFilmsRepository.saveFilm(film);
        assertThat(mFilmsRepository.getCachedFilms().snapshot().containsKey(film.getReleaseid()),
                is(true));

        // When deleted
        mFilmsRepository.deleteFilm(film.getReleaseid());

        // Verify the data sources were called
        verify(mFilmsRemoteDataSource).deleteFilm(film.getReleaseid());
        verify(mFilmsLocalDataSource).deleteFilm(film.getReleaseid());

        // Verify it's removed from repository
        assertThat(mFilmsRepository.getCachedFilms().snapshot().containsKey(film.getReleaseid()),
                is(false));
    }

    @Test
    public void deleteAllFilms_deleteFilmsToServiceAPIUpdatesCache() {

        // Given 3 Films in the repository
        for (int i = 0; i < 3; i++) {
            Film film = new Film();
            String id = String.valueOf(i);
            film.setReleaseid(id);
            film.setName(id);
            mFilmsRepository.saveFilm(film);
        }

        // When all Films are deleted to the Films repository
        mFilmsRepository.deleteAllFilms();

        // Verify the data sources were called
        verify(mFilmsRemoteDataSource).deleteAllFilms();
        verify(mFilmsLocalDataSource).deleteAllFilms();

        assertThat(mFilmsRepository.getCachedFilms().size(), is(0));
    }

    @Test
    public void getFilm_requestsSingleFilmFromLocalDataSource() {
        // Given a stub completed Film with title and description in the local repository

        Film film = new Film();
        String id = String.valueOf(1);
        film.setReleaseid(id);
        film.setName(id);

        setFilmAvailable(mFilmsLocalDataSource, film);
        // And the Film not available in the remote repository
        setFilmNotAvailable(mFilmsRemoteDataSource, film.getReleaseid());

        // When a Film is requested from the Films repository
        TestSubscriber<Film> testSubscriber = new TestSubscriber<>();
        mFilmsRepository.getFilm(film.getReleaseid()).subscribe(testSubscriber);

        // Then the Film is loaded from the database
        verify(mFilmsLocalDataSource).getFilm(eq(film.getReleaseid()));
        testSubscriber.assertValue(film);
    }

    @Test
    public void getFilmDetail_requestsSingleFilmFromLocalDataSource() {
        // Given a stub completed Film with title and description in the local repository

        Film film = new Film();
        String id = String.valueOf(1);
        film.setReleaseid(id);
        film.setName(id);

        setFilmDetailAvailable(mFilmsLocalDataSource, film);
        // And the Film not available in the remote repository
        setFilmDetailNotAvailable(mFilmsRemoteDataSource, film.getReleaseid());

        // When a Film is requested from the Films repository
        TestSubscriber<Film> testSubscriber = new TestSubscriber<>();
        mFilmsRepository.getFilmDetail(film.getReleaseid()).subscribe(testSubscriber);

        // Then the Film is loaded from the database
        verify(mFilmsLocalDataSource).getFilmDetail(eq(film.getReleaseid()));
        testSubscriber.assertValue(film);
    }

    @Test
    public void getFilms_refreshesLocalDataSource() {

        // Given that the remote data source has data available
        setFilmsAvailable(mFilmsRemoteDataSource, FILMS);

        // Mark cache as dirty to force a reload of data from remote data source.
        mFilmsRepository.refreshFilms();

        // When calling getFilms in the repository
        mFilmsRepository.getFilms().toBlocking().subscribe(mFilmsTestSubscriber);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mFilmsLocalDataSource, times(FILMS.size())).saveFilm(any(Film.class));
        mFilmsTestSubscriber.assertValue(FILMS);
    }

    @Test
    public void getFilmsWithDirtyCache_filmsAreRetrievedFromRemote() {

        // Given that the remote data source has data available
        List tmpFilms = FILMS;
        setFilmsAvailable(mFilmsRemoteDataSource, tmpFilms);

        // When calling getFilms in the repository with dirty cache
        mFilmsRepository.refreshFilms();
        mFilmsRepository.getFilms().subscribe(mFilmsTestSubscriber);

        // Verify the FILMS from the remote data source are returned, not the local
        verify(mFilmsLocalDataSource, never()).getFilms();
        verify(mFilmsRemoteDataSource).getFilms();
        mFilmsTestSubscriber.assertValue(tmpFilms);
    }

    @Test
    public void getFilmsWithLocalDataSourceUnavailable_FilmsAreRetrievedFromRemote() {

        List tmpFilms = FILMS;

        // Given that the local data source has no data available
        setFilmsNotAvailable(mFilmsLocalDataSource);

        // And the remote data source has data available
        setFilmsAvailable(mFilmsRemoteDataSource, tmpFilms);

        // When calling getFilms in the repository
        mFilmsRepository.getFilms().toBlocking().subscribe(mFilmsTestSubscriber);

        // Verify the Films from the remote data source are returned
        verify(mFilmsRemoteDataSource).getFilms();
        mFilmsTestSubscriber.assertValue(tmpFilms);
    }

    @Test
    public void getFilmWithBothDataSourcesUnavailable_firesOnError() {

        // Given a film id
        final String filmId = "123";

        // the local data source has no data available
        setFilmNotAvailable(mFilmsLocalDataSource, filmId);

        // And the remote data source has no data available
        setFilmNotAvailable(mFilmsRemoteDataSource, filmId);

        // When calling getFilms in the repository
        TestSubscriber<Film> testSubscriber = new TestSubscriber<>();
        mFilmsRepository.getFilm(filmId).toBlocking().subscribe(testSubscriber);

        // Verify that error is returned
        testSubscriber.assertError(NoSuchElementException.class);
    }

    @Test
    public void getFilmsWithBothDataSourcesUnavailable_firesOnError() {

        // the local data source has no data available
        setFilmsNotAvailable(mFilmsLocalDataSource);

        // And the remote data source has no data available
        setFilmsNotAvailable(mFilmsRemoteDataSource);

        // When calling getFilms in the repository
        mFilmsRepository.getFilms().toBlocking().subscribe(mFilmsTestSubscriber);

        // Verify that error is returned
        mFilmsTestSubscriber.assertError(NoSuchElementException.class);
    }

    @Test
    public void saveFilm() {

        Film film = new Film();
        String id = String.valueOf(1);
        film.setReleaseid(id);
        film.setName(id);

        // save film
        mFilmsRepository.saveFilm(film);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mFilmsLocalDataSource).saveFilm(film);
        verify(mFilmsRemoteDataSource).saveFilm(film);

        assertThat(mFilmsRepository.getCachedFilms().size(), is(1));
    }

    @Test
    public void saveFilmDetail() {

        Film film = new Film();
        String id = String.valueOf(1);
        film.setReleaseid(id);
        film.setName(id);

        // save film
        mFilmsRepository.saveFilmDetail(film);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mFilmsLocalDataSource).saveFilmDetail(film);
        verify(mFilmsRemoteDataSource).saveFilmDetail(film);

        assertThat(mFilmsRepository.getCachedFilmDetails().size(), is(1));
    }

    private void setFilmNotAvailable(FilmsDataSource dataSource, String filmId) {
        when(dataSource.getFilm(eq(filmId))).thenReturn(Observable.<Film>empty());
    }

    private void setFilmAvailable(FilmsDataSource dataSource, Film film) {
        when(dataSource.getFilm(eq(film.getReleaseid()))).thenReturn(Observable.just(film));
    }

    private void setFilmsNotAvailable(FilmsDataSource dataSource) {
        when(dataSource.getFilms()).thenReturn(Observable.<List<Film>>empty());
    }

    private void setFilmsAvailable(FilmsDataSource dataSource, List<Film> films) {
        when(dataSource.getFilms()).thenReturn(Observable.just(films));
    }

    private void setFilmDetailNotAvailable(FilmsDataSource dataSource, String filmId) {
        when(dataSource.getFilmDetail(eq(filmId))).thenReturn(Observable.<Film>empty());
    }

    private void setFilmDetailAvailable(FilmsDataSource dataSource, Film film) {
        when(dataSource.getFilmDetail(eq(film.getReleaseid()))).thenReturn(Observable.just(film));
    }
}
