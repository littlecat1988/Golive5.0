package com.golive.cinema.data.source;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.golive.cinema.data.source.local.FilmsDbHelper;
import com.golive.cinema.data.source.local.FilmsLocalDataSource;
import com.golive.cinema.util.schedulers.ImmediateSchedulerProvider;
import com.golive.network.entity.Film;
import com.golive.network.entity.Media;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.observers.TestSubscriber;

/**
 * Integration test for the {@link FilmsLocalDataSource}, which uses the {@link FilmsDbHelper}.
 * <p/>
 * Created by Wangzj on 2016/9/5.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilmsLocalDataSourceTest {

    public static final String RELEASEID = "1";
    private FilmsLocalDataSource mLocalDataSource;

    @Before
    public void setup() {
        FilmsLocalDataSource.destroyInstance();
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();
        mLocalDataSource = FilmsLocalDataSource.getInstance(
                InstrumentationRegistry.getTargetContext(), schedulerProvider);
    }

    @After
    public void cleanUp() {
        mLocalDataSource.deleteAllFilms();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void deleteAllFilms_emptyListOfRetrievedFilm() {
        // Given a new Film in the persistent repository and a mocked callback
        final Film newFilm = new Film();
        newFilm.setReleaseid(RELEASEID);
        newFilm.setName(RELEASEID);
        newFilm.setIntroduction(RELEASEID);
        mLocalDataSource.saveFilm(newFilm);

        // When all Films are deleted
        mLocalDataSource.deleteAllFilms();

        // Then the retrieved Films is an empty list
        TestSubscriber<List<Film>> testSubscriber = new TestSubscriber<>();
        mLocalDataSource.getFilms().subscribe(testSubscriber);
        List<Film> result = testSubscriber.getOnNextEvents().get(0);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void getFilm_whenFilmNotSaved() {
        //Given that no Film has been saved
        //When querying for a Film, null is returned.
        TestSubscriber<Film> testSubscriber = new TestSubscriber<>();
        mLocalDataSource.getFilm("1").subscribe(testSubscriber);
        testSubscriber.assertValue(null);
    }

    @Test
    public void saveFilm_retrieves() {

        // Given a new film
        final Film newFilm = new Film();
        newFilm.setReleaseid(RELEASEID);
        newFilm.setName(RELEASEID);
        newFilm.setIntroduction(RELEASEID);

        // When saved into the persistent repository
        mLocalDataSource.saveFilm(newFilm);

        //final Film tempFilm = mLocalDataSource.getFilm(RELEASEID)
        //        .toBlocking().first();
        //assertEquals(newFilm.getReleaseid(), tempFilm.getReleaseid());

        TestSubscriber<Film> testSubscriber = new TestSubscriber<>();
        // Then the Film can be retrieved from the persistent repository
        mLocalDataSource.getFilm(RELEASEID).subscribe(testSubscriber);
        //testSubscriber.assertValue(newFilm);
        final Film tempFilm = testSubscriber.getOnNextEvents().get(0);
        assertEquals(newFilm.getReleaseid(), tempFilm.getReleaseid());
    }

    @Test
    public void saveFilmDetail_retrieves() {

        // Given a new film
        final Film newFilm = new Film();
        newFilm.setReleaseid(RELEASEID);
        newFilm.setName(RELEASEID);
        newFilm.setIntroduction(RELEASEID);

        String price = "5";
        String vipPrice = "1";
        newFilm.setPrice(price);
        newFilm.setOnlineprice(price);
        newFilm.setDownloadprice(price);
        newFilm.setVipprice(vipPrice);
        newFilm.setViponlineprice(vipPrice);
        newFilm.setVipdownloadprice(vipPrice);

        List<Media> medias = new ArrayList<>();
        Media media;

        final String MEDIA_ID = "MEDIA_ID_1";
        final String URL = "http://www.baidu.com";
        media = new Media();
        media.setId(MEDIA_ID);
        media.setName(MEDIA_ID);
        media.setRankname("高清");
        media.setType(Media.MEDIA_TYPE_ONLINE);
        media.setUrl(URL);
        media.setEncryption(Media.TYPE_NO_ENCRYPT);
        medias.add(media);

        newFilm.setMedias(medias);

        // When saved into the persistent repository
        mLocalDataSource.saveFilmDetail(newFilm);

        //final Film tempFilm = mLocalDataSource.getFilm(RELEASEID)
        //        .toBlocking().first();
        //assertEquals(newFilm.getReleaseid(), tempFilm.getReleaseid());

        TestSubscriber<Film> testSubscriber = new TestSubscriber<>();
        // Then the Film can be retrieved from the persistent repository
        mLocalDataSource.getFilmDetail(newFilm.getReleaseid()).subscribe(testSubscriber);
        //testSubscriber.assertValue(newFilm);
        final Film tempFilm = testSubscriber.getOnNextEvents().get(0);
        assertEquals(newFilm.getReleaseid(), tempFilm.getReleaseid());
        assertEquals(newFilm.getName(), tempFilm.getName());
        assertEquals(newFilm.getIntroduction(), tempFilm.getIntroduction());
        assertEquals(price, tempFilm.getPrice());
        assertEquals(price, tempFilm.getOnlineprice());
        assertEquals(price, tempFilm.getDownloadprice());
        assertEquals(vipPrice, tempFilm.getVipprice());
        assertEquals(vipPrice, tempFilm.getViponlineprice());
        assertEquals(vipPrice, tempFilm.getVipdownloadprice());
        assertEquals(1, newFilm.getMediaCount());
        Media media1 = newFilm.getMedias().get(0);
        assertEquals(MEDIA_ID, media1.getId());
        assertEquals(URL, media1.getUrl());
    }
}
