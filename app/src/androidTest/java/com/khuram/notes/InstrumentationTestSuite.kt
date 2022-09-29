package com.khuram.notes

import com.khuram.notes.framework.datasource.cache.NoteDaoServiceTests
import com.khuram.notes.framework.datasource.network.NoteFirestoreServiceTests
import com.khuram.notes.framework.presentation.end_to_end.NotesFeatureTest
import com.khuram.notes.framework.presentation.notedetail.NoteDetailFragmentTests
import com.khuram.notes.framework.presentation.notelist.NoteListFragmentTests
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite


@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    NoteDaoServiceTests::class,
    NoteFirestoreServiceTests::class,
    NoteDetailFragmentTests::class,
    NoteListFragmentTests::class,
    NotesFeatureTest::class
)
class InstrumentationTestSuite