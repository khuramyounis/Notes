package com.khuram.notes.business.interactors.notelist

import com.khuram.notes.business.data.cache.CacheResponseHandler
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.data.util.safeApiCall
import com.khuram.notes.business.data.util.safeCacheCall
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.framework.presentation.notelist.state.NoteListViewState
import com.khuram.notes.business.domain.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class RestoreDeletedNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    fun restoreDeletedNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.insertNote(note)
        }

        val response = object: CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState> {
                return if(resultObj > 0){
                    val viewState = NoteListViewState(
                        notePendingDelete = NoteListViewState.NotePendingDelete(note = note)
                    )
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(response)

        updateNetwork(response?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(response: String?, note: Note) {

        if(response.equals(RESTORE_NOTE_SUCCESS)){
            // insert into "notes" node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.insertOrUpdateNote(note)
            }

            // remove from "deleted" node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.deleteDeletedNote(note)
            }
        }
    }

    companion object{
        const val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        const val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."
    }
}
