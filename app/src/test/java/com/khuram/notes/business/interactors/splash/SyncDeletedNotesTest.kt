package com.khuram.notes.business.interactors.splash

import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.model.NoteFactory
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.di.DependencyContainer
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


/*

Test cases:
1. deleteNetworkNotes_confirmCacheSync()
    a) select some notes for deleting from network
    b) delete from network
    c) perform sync
    d) confirm notes from cache were deleted
 */

@InternalCoroutinesApi
class SyncDeletedNotesTest {

    // system in test
    private val syncDeletedNotes: SyncDeletedNotes

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory
    private val dateUtil: DateUtil

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        dateUtil = dependencyContainer.dateUtil
        syncDeletedNotes = SyncDeletedNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNetworkNotes_confirmCacheSync() = runBlocking {

        // select some notes to be deleted from cache
        val networkNotes = noteNetworkDataSource.getAllNotes()
        val notesToDelete: ArrayList<Note> = ArrayList()
        for(note in networkNotes){
            notesToDelete.add(note)
            noteNetworkDataSource.deleteNote(note.id)
            noteNetworkDataSource.insertDeletedNote(note)
            if(notesToDelete.size > 3){
                break
            }
        }

        // perform sync
        syncDeletedNotes.syncDeletedNotes()

        // confirm notes were deleted from cache
        for(note in notesToDelete){
            val cachedNote = noteCacheDataSource.searchNoteById(note.id)
            Assertions.assertTrue { cachedNote == null }
        }
    }
}
