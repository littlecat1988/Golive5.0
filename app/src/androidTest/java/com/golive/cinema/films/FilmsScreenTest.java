/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.golive.cinema.films;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Checks.checkArgument;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.TestUtils;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Film;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the films screen, the main screen which contains a list of all films.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilmsScreenTest {

    private static final String DESCRIPTION = "DESCR";

    private static final String TITLE = "test Film";

    private IdlingResource mIdlingResource;

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p> Rules are interceptors which are executed for each test method and are important
     * building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<FilmsActivity> mFilmsActivityTestRule =
            new ActivityTestRule<FilmsActivity>(FilmsActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();

                    // To avoid a long list of films and the need to scroll through the list to
                    // find a
                    // film, we call {@link filmsDataSource#deleteAllFilms()} before each test.
                    // Doing this in @Before generates a race condition.
                    deleteAllFilms();
                }
            };

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mFilmsActivityTestRule.getActivity().getCountingIdlingResource();
        // To prove that the test fails, omit this call:
        Espresso.registerIdlingResources(mIdlingResource);
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            Espresso.unregisterIdlingResources(mIdlingResource);
        }
    }

    @After
    public void cleanUp() {
        deleteAllFilms();
    }

    /**
     * A custom {@link Matcher} which matches an item in a {@link RecyclerView} by its text. <p>
     * View constraints: <ul> <li>View must be a child of a {@link RecyclerView} <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!StringUtils.isNullOrEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void showAllFilms() {

        // Add a film
        createFilm(TITLE, DESCRIPTION);

        //Verify that all our films are shown
        viewAllFilms();

        onView(withItemText(TITLE)).check(matches(isDisplayed()));
    }

    @Test
    public void orientationChange_FilterAllPersists() {

        // Add a completed film
        createFilm(TITLE, DESCRIPTION);

        // when switching to all films.
        viewAllFilms();

        // then films TITLE appear
        onView(withItemText(TITLE)).check(matches(isDisplayed()));

        // when rotating the screen
        TestUtils.rotateOrientation(mFilmsActivityTestRule.getActivity());

        //Verify that all our films are shown
        viewAllFilms();

        // then nothing changes
        onView(withItemText(TITLE)).check(matches(isDisplayed()));
    }

    private void viewAllFilms() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_all)).perform(click());
    }

    private void createFilm(String title, String description) {

        Film film = new Film();
        String id = title;
        film.setReleaseid(id);
        film.setName(id);

        FilmsDataSource dataSource = getFilmsDataSource();
        dataSource.saveFilm(film);
    }

    private void deleteAllFilms() {
        FilmsDataSource dataSource = getFilmsDataSource();
        dataSource.deleteAllFilms();
    }

    private FilmsDataSource getFilmsDataSource() {
        return Injection.provideFilmsDataSource(
                InstrumentationRegistry.getTargetContext());
    }
}
