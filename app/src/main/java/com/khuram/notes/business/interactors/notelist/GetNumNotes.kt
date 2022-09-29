package com.khuram.notes.business.interactors.notelist

import com.khuram.notes.business.data.cache.CacheResponseHandler
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.util.safeCacheCall
import com.khuram.notes.framework.presentation.notelist.state.NoteListViewState
import com.khuram.notes.business.domain.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class GetNumNotes(
    private val noteCacheDataSource: NoteCacheDataSource
){
    fun getNumNotes(
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.getNumNotes()
        }

        val response =  object: CacheResponseHandler<NoteListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState> {
                val viewState = NoteListViewState(
                    numNotesInCache = resultObj
                )
                return DataState.data(
                    response = Response(
                        message = GET_NUM_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }

    companion object{
        const val GET_NUM_NOTES_SUCCESS = "Successfully retrieved the number of notes from the cache."
        const val GET_NUM_NOTES_FAILED = "Failed to get the number of notes from the cache."
    }
}