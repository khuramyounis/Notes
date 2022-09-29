package com.khuram.notes.di

import com.khuram.notes.business.data.NoteDataFactory
import com.khuram.notes.business.data.cache.FakeNoteCacheDataSourceImpl
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.FakeNoteNetworkDataSourceImpl
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.domain.model.NoteFactory
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*


class DependencyContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil = DateUtil(dateFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDataFactory: NoteDataFactory

    init {
        isUnitTest = true // for Logger.kt
    }

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            noteDataFactory = NoteDataFactory(classLoader)
        }
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            ),
            deletedNotesData = HashMap(),
            dateUtil = dateUtil
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            ),
            dateUtil = dateUtil
        )
    }
}