package com.nurhidayaatt.storyapp.presentation.login

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.presentation.main.MainActivity
import com.nurhidayaatt.storyapp.util.EspressoIdlingResource
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginLogout_success() {
        Intents.init()
        onView(withId(R.id.ed_login_email)).perform(click()).perform(typeText("test"))
        onView(withId(R.id.ed_login_password)).perform(click()).perform(typeText("test"))
        onView(withId(R.id.ed_login_email)).check(matches(hasErrorText(resources.getString(R.string.error_email))))
        onView(withId(R.id.ed_login_password)).check(matches(hasErrorText(resources.getString(R.string.error_password))))

        onView(withId(R.id.ed_login_email)).perform(click()).perform(replaceText("dayat@email.com"))
        onView(withId(R.id.ed_login_password)).perform(click()).perform(replaceText("12345678"))
        onView(withId(R.id.btn_login)).perform(click())

        intended(hasComponent(MainActivity::class.java.name))
        onView(withId(R.id.rv_story)).check(matches(isDisplayed()))
        onView(withId(R.id.action_logout)).perform(click())

        intended(hasComponent(LoginActivity::class.java.name))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }
}