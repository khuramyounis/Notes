package com.khuram.notes

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.khuram.notes.framework.presentation.TestBaseApplication
import com.khuram.notes.util.ViewShownIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.hamcrest.Matcher


@ExperimentalCoroutinesApi
@FlowPreview
abstract class BaseTest {

    val application: TestBaseApplication
            = ApplicationProvider.getApplicationContext<Context>() as TestBaseApplication

    abstract fun injectTest()

    // wait for a certain view to be shown.
    // ex: waiting for splash screen to transition to NoteListFragment
    fun waitViewShown(matcher: Matcher<View>) {
        val idlingResource: IdlingResource = ViewShownIdlingResource(matcher,
            ViewMatchers.isDisplayed()
        )
        try {
            IdlingRegistry.getInstance().register(idlingResource)
            Espresso.onView(ViewMatchers.withId(0)).check(ViewAssertions.doesNotExist())
        } finally {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
