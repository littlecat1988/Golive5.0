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

package com.golive.cinema;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.Window;

import com.golive.cinema.util.SystemNavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainScreenTest {

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p> Rules are interceptors which are executed for each test method and are important
     * building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                }
            };

    @Test
    public void showsFilmsScreen() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                MainActivity activity = mMainActivityTestRule.getActivity();
                Window window = activity.getWindow();
//                showNavigationBar(window);
                SystemNavigationUtils.showNavigationBar(window);
            }
        });
//        onView(withId(R.id.activity_main_btn_login)).perform(ViewActions.click());
        onView(withId(R.id.filmsContainer)).check(matches(isDisplayed()));
    }
}
