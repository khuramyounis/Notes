package com.khuram.notes.framework.presentation.notelist

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.khuram.notes.BaseTest
import com.khuram.notes.R
import com.khuram.notes.di.TestAppComponent
import com.khuram.notes.framework.datasource.cache.database.NOTE_PAGINATION_PAGE_SIZE
import com.khuram.notes.framework.datasource.cache.database.NoteDao
import com.khuram.notes.framework.datasource.cache.mappers.CacheMapper
import com.khuram.notes.framework.datasource.cache.model.NoteCacheEntity
import com.khuram.notes.framework.datasource.data.NoteDataFactory
import com.khuram.notes.framework.presentation.TestNoteFragmentFactory
import com.khuram.notes.framework.presentation.UIController
import com.khuram.notes.util.EspressoIdlingResourceRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.random.Random
import kotlin.test.assertTrue


/*
    **WARNING**
    - This test will fail randomly. Seems to be bug because it doesn't make sense.

    --Test cases:
    1. is the test data being displayed in the RecyclerView?
    2. Make a search using the searchview
    3. clear the search so all list items show again
    3. Toolbar states

    --Notes:
    1. I really dislike these FragmentScenario tests. It seems to randomly fail for no reason.
    And you don't have the option to "clear state" because then you'd have to use test
    orchestrator. And if you use test orchestrator you can't get reports. There's always a
    downside, whichever route you choose.

    2. Test fails when run via gradle (./gradlew connectedAndroidTest)
     but passses if "right click > run"????? >_<

    3. SearchViews should have an option to "submit an empty query". It's a pain to deal
    with and create work-arounds.
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteListFragmentTests: BaseTest() {

    @get: Rule
    val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Inject
    lateinit var fragmentFactory: TestNoteFragmentFactory

    @Inject
    lateinit var cacheMapper: CacheMapper

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var dao: NoteDao

    private val testEntityList: List<NoteCacheEntity>

    val uiController = mockk<UIController>(relaxed = true)

    val navController = mockk<NavController>(relaxed = true)

    init {
        injectTest()
        testEntityList = cacheMapper.noteListToEntityList(
            noteDataFactory.produceListOfNotes()
        )
        prepareDataSet(testEntityList)
    }

    @Before
    fun before(){
        setupUIController()
    }

    private fun setupUIController(){
        fragmentFactory.uiController = uiController
    }

    // ** Must clear network and cache so there is no previous state issues **
    private fun prepareDataSet(testData: List<NoteCacheEntity>) = runBlocking{
        // clear any existing data so recyclerview isn't overwhelmed
        dao.deleteAllNotes()
        dao.insertNotes(testData)
    }

    /**
     * I decided to write a single large test when testing fragments in isolation.
     * Because if I make multiple tests, they have issues sharing state. I can solve
     * that issue by using test orchestrator, but that will prevent me from getting
     * reports. And I want reports.
     */
    @Test
    fun generalListFragmentTest() = runBlocking{

        // setup
        val scenario = launchFragmentInContainer<NoteListFragment>(
            factory = fragmentFactory
        ).onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }

        // test
        val recyclerView = onView(withId(R.id.recycler_view))

        recyclerView.check(matches(isDisplayed()))

        for(entity in testEntityList){
            recyclerView.perform(
                RecyclerViewActions.scrollTo<NoteListAdapter.NoteViewHolder>(hasDescendant(withText(entity.title)))
            )
            onView(withText(entity.title))
                .check(matches(isDisplayed()))
        }

        // choose a random note and search its title
        val randomNote = testEntityList.get(Random.nextInt(0, testEntityList.size-1))
        onView(withId(R.id.search_view))
            .perform(TypeSearchViewTextAndSubmit(randomNote.title))

        // clear text in searchview so it doesn't conflict the assertions
        onView(withId(R.id.search_view))
            .perform(TypeSearchViewTextAndSubmit(""))

        // make sure the query worked
        val expectedSearchResults = dao.searchNotesOrderByDateASC(
            randomNote.title,
            1,
            NOTE_PAGINATION_PAGE_SIZE
        )

        onView(withId(R.id.recycler_view))
            .check(RecyclerViewItemCountAssertion(expectedSearchResults.size))
        for(entity in expectedSearchResults){
            recyclerView.perform(
                RecyclerViewActions.scrollTo<NoteListAdapter.NoteViewHolder>(hasDescendant(withText(entity.title)))
            )
            onView(withText(entity.title))
                .check(matches(isDisplayed()))
        }

        // clear search query to reset the list so all notes are visible
        onView(withId(R.id.search_view))
            .perform(TypeSearchViewTextAndSubmit(""))
        scenario.onFragment { fragment ->
            fragment.viewModel.clearSearchQuery()
        }

//        for(entity in testEntityList){
//            recyclerView.perform(
//                RecyclerViewActions.scrollTo<NoteListAdapter.NoteViewHolder>(hasDescendant(withText(entity.title)))
//            )
//            onView(withText(entity.title)).check(matches(isDisplayed()))
//        }

        // engage 'MultiSelectionState'
        recyclerView.perform(
            RecyclerViewActions.actionOnItemAtPosition<NoteListAdapter.NoteViewHolder>(
                0,
                ViewActions.longClick()
            )
        )

        // confirm 'MultiSelectionState' is active
        onView(withId(R.id.action_exit_multiselect_state)).check(matches(isDisplayed()))

        // engage 'SearchViewState'
        onView(withId(R.id.action_exit_multiselect_state)).perform(click())

        // confirm 'SearchViewState' is active
        onView(withId(R.id.search_view)).check(matches(isDisplayed()))

        // select a note and confirm navigate function was called
        val selectedPosition = 1
        recyclerView.perform(
            RecyclerViewActions.actionOnItemAtPosition<NoteListAdapter.NoteViewHolder>(selectedPosition, click())
        )
        verify {
            navController.navigate(
                R.id.action_note_list_fragment_to_noteDetailFragment,
                any()
            )
        }
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}

class TypeSearchViewTextAndSubmit(private val text: String?): ViewAction {

    override fun getDescription(): String {
        return "Enter searchview text"
    }

    override fun getConstraints(): Matcher<View> {
        return CoreMatchers.allOf(
            isDisplayed(),
            isAssignableFrom(SearchView::class.java)
        )
    }

    override fun perform(uiController: UiController?, view: View?) {
        val searchView = (view as SearchView)
        searchView.setQuery(text, true)
    }
}

class RecyclerViewItemCountAssertion(
    private val expectedCount: Int
) : ViewAssertion {

    override fun check(
        view: View,
        noViewFoundException: NoMatchingViewException?
    ) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assertTrue { adapter!!.itemCount == expectedCount }
    }
}

