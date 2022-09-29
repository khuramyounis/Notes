package com.khuram.notes.di

import com.khuram.notes.framework.datasource.cache.NoteDaoServiceTests
import com.khuram.notes.framework.datasource.network.NoteFirestoreServiceTests
import com.khuram.notes.framework.presentation.TestBaseApplication
import com.khuram.notes.framework.presentation.end_to_end.NotesFeatureTest
import com.khuram.notes.framework.presentation.notedetail.NoteDetailFragmentTests
import com.khuram.notes.framework.presentation.notelist.NoteListFragmentTests
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
@Component(
    modules = [
        TestModule::class,
        AppModule::class,
        TestNoteFragmentFactoryModule::class,
        NoteViewModelModule::class
    ]
)

interface TestAppComponent: AppComponent {

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(noteDaoServiceTests: NoteDaoServiceTests)

    fun inject(firestoreServiceTests: NoteFirestoreServiceTests)

    fun inject(noteListFragmentTests: NoteListFragmentTests)

    fun inject(noteDetailFragmentTests: NoteDetailFragmentTests)

    fun inject(notesFeatureTest: NotesFeatureTest)
}
