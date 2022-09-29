package com.khuram.notes.framework.presentation.end_to_end

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.khuram.notes.BaseTest
import com.khuram.notes.R
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.di.TestAppComponent
import com.khuram.notes.framework.datasource.cache.database.NoteDao
import com.khuram.notes.framework.datasource.cache.mappers.CacheMapper
import com.khuram.notes.framework.datasource.cache.model.NoteCacheEntity
import com.khuram.notes.framework.datasource.data.NoteDataFactory
import com.khuram.notes.framework.presentation.MainActivity
import com.khuram.notes.framework.presentation.notelist.NoteListAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


/*
    --Test cases:
    1. start SplashFragment, confirm logo is visible
    2. Navigate NoteListFragment, confirm list is visible
    3. Select a note from list, confirm correct title and body is visible
    4. Navigate BACK, confirm NoteListFragment in view
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NotesFeatureTest: BaseTest() {

    //@get: Rule
    //val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Inject
    lateinit var cacheMapper: CacheMapper

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var dao: NoteDao

    @Inject
    lateinit var noteNetworkDataSource: NoteNetworkDataSource

    private val testEntityList: List<NoteCacheEntity>

    init {
        injectTest()
        testEntityList = cacheMapper.noteListToEntityList(
            noteDataFactory.produceListOfNotes()
        )
        prepareDataSet(testEntityList)
    }

    // ** Must clear network and cache so there is no previous state issues **
    private fun prepareDataSet(testData: List<NoteCacheEntity>) = runBlocking{
        // clear any existing data so recyclerview isn't overwhelmed
        dao.deleteAllNotes()
        noteNetworkDataSource.deleteAllNotes()
        dao.insertNotes(testData)
    }

    @Test
    fun generalEndToEndTest(){

        launchActivity<MainActivity>()

        // Wait for NoteListFragment to come into view
        waitViewShown(withId(R.id.recycler_view))

        val recyclerView = Espresso.onView(withId(R.id.recycler_view))

        // confirm NoteListFragment is in view
        recyclerView.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Select a note from the list
        recyclerView.perform(
            RecyclerViewActions.actionOnItemAtPosition<NoteListAdapter.NoteViewHolder>(1, ViewActions.click())
        )

        // Wait for NoteDetailFragment to come into view
        waitViewShown(withId(R.id.note_scroll_view))

        // Confirm NoteDetailFragment is in view
        Espresso.onView(withId(R.id.note_scroll_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.note_title))
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.withText(""))))
        //Espresso.onView(withId(R.id.note_body)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.withText(""))))

        // press back arrow in toolbar
        Espresso.onView(withId(R.id.toolbar_primary_icon)).perform(ViewActions.click())

        // confirm NoteListFragment is in view
        recyclerView.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}
