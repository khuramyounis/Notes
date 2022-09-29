package com.khuram.notes.business.interactors.notelist

import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.data.util.safeApiCall
import com.khuram.notes.business.data.util.safeCacheCall
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.framework.presentation.notelist.state.NoteListViewState
import com.khuram.notes.business.domain.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*


// For testing
class InsertMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    fun insertNotes(
        numNotes: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val noteList = NoteListTester.generateNoteList(numNotes)
        safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.insertNotes(noteList)
        }

        emit(
            DataState.data(
                response = Response(
                    message = "success",
                    uiComponentType = UIComponentType.None,
                    messageType = MessageType.None
                ),
                data = null,
                stateEvent = stateEvent
            )
        )

        updateNetwork(noteList)
    }

    private suspend fun updateNetwork(noteList: List<Note>){
        safeApiCall(Dispatchers.IO){
            noteNetworkDataSource.insertOrUpdateNotes(noteList)
        }
    }
}

private object NoteListTester {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateUtil = DateUtil(dateFormat)

    fun generateNoteList(numNotes: Int): List<Note>{
        val list: ArrayList<Note> = ArrayList()
        for(id in 0..numNotes){
            list.add(generateNote())
        }
        return list
    }

    fun generateNote(): Note {
        return Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }
}