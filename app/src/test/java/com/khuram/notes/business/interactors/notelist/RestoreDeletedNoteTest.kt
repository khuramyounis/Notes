package com.khuram.notes.business.interactors.notelist

import com.khuram.notes.business.data.cache.CacheErrors
import com.khuram.notes.business.data.cache.FORCE_GENERAL_FAILURE
import com.khuram.notes.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.domain.model.NoteFactory
import com.khuram.notes.business.domain.state.DataState
import com.khuram.notes.business.interactors.notelist.RestoreDeletedNote.Companion.RESTORE_NOTE_FAILED
import com.khuram.notes.business.interactors.notelist.RestoreDeletedNote.Companion.RESTORE_NOTE_SUCCESS
import com.khuram.notes.di.DependencyContainer
import com.khuram.notes.framework.presentation.notelist.state.NoteListStateEvent
import com.khuram.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*


/*
Test cases:
1. restoreNote_success_confirmCacheAndNetworkUpdated()
    a) create a new note and insert it into the "deleted" node of network
    b) restore that note
    c) Listen for success msg RESTORE_NOTE_SUCCESS from flow
    d) confirm note is in the cache
    e) confirm note is in the network "notes" node
    f) confirm note is not in the network "deletes" node
2. restoreNote_fail_confirmCacheAndNetworkUnchanged()
    a) create a new note and insert it into the "deleted" node of network
    b) restore that note (force a failure)
    c) Listen for success msg RESTORE_NOTE_FAILED from flow
    d) confirm note is not in the cache
    e) confirm note is not in the network "notes" node
    f) confirm note is in the network "deletes" node
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) create a new note and insert it into the "deleted" node of network
    b) restore that note (force an exception)
    c) Listen for success msg CACHE_ERROR_UNKNOWN from flow
    d) confirm note is not in the cache
    e) confirm note is not in the network "notes" node
    f) confirm note is in the network "deletes" node
 */

@InternalCoroutinesApi
class RestoreDeletedNoteTest {

    // system in test
    private val restoreDeletedNote: RestoreDeletedNote

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        restoreDeletedNote = RestoreDeletedNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun restoreNote_success_confirmCacheAndNetworkUpdated() =  runBlocking {

        // create a new note and insert into network "deletes" node
        val restoredNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        // confirm that note is in the "deletes" node before restoration
        var deletedNotes = noteNetworkDataSource.getDeletedNotes()
        Assertions.assertTrue { deletedNotes.contains(restoredNote) }

        restoreDeletedNote.restoreDeletedNote(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    RESTORE_NOTE_SUCCESS
                )
            }
        })

        // confirm note is in the cache
        val noteInCache = noteCacheDataSource.searchNoteById(restoredNote.id)
        Assertions.assertTrue { noteInCache == restoredNote }

        // confirm note is in the network "notes" node
        val noteInNetwork = noteNetworkDataSource.searchNote(restoredNote)
        Assertions.assertTrue { noteInNetwork == restoredNote }

        // confirm note is not in the network "deletes" node
        deletedNotes = noteNetworkDataSource.getDeletedNotes()
        Assertions.assertFalse { deletedNotes.contains(restoredNote) }
    }

    @Test
    fun restoreNote_fail_confirmCacheAndNetworkUnchanged() =  runBlocking {

        // create a new note and insert into network "deletes" node
        val restoredNote = noteFactory.createSingleNote(
            id = FORCE_GENERAL_FAILURE, // force insert failure
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        // confirm that note is in the "deletes" node before restoration
        var deletedNotes = noteNetworkDataSource.getDeletedNotes()
        Assertions.assertTrue { deletedNotes.contains(restoredNote) }

        restoreDeletedNote.restoreDeletedNote(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    RESTORE_NOTE_FAILED
                )
            }
        })

        // confirm note is not in the cache
        val noteInCache = noteCacheDataSource.searchNoteById(restoredNote.id)
        Assertions.assertTrue { noteInCache == null }

        // confirm note is not in the network "notes" node
        val noteInNetwork = noteNetworkDataSource.searchNote(restoredNote)
        Assertions.assertTrue { noteInNetwork == null }

        // confirm note is in the network "deletes" node
        deletedNotes = noteNetworkDataSource.getDeletedNotes()
        Assertions.assertTrue { deletedNotes.contains(restoredNote) }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() =  runBlocking {

        // create a new note and insert into network "deletes" node
        val restoredNote = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION, // force insert exception
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        // confirm that note is in the "deletes" node before restoration
        var deletedNotes = noteNetworkDataSource.getDeletedNotes()
        Assertions.assertTrue { deletedNotes.contains(restoredNote) }

        restoreDeletedNote.restoreDeletedNote(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm note is not in the cache
        val noteInCache = noteCacheDataSource.searchNoteById(restoredNote.id)
        Assertions.assertTrue { noteInCache == null }

        // confirm note is not in the network "notes" node
        val noteInNetwork = noteNetworkDataSource.searchNote(restoredNote)
        Assertions.assertTrue { noteInNetwork == null }

        // confirm note is in the network "deletes" node
        deletedNotes = noteNetworkDataSource.getDeletedNotes()
        Assertions.assertTrue { deletedNotes.contains(restoredNote) }
    }
}

