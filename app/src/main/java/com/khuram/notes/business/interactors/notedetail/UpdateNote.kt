package com.khuram.notes.business.interactors.notedetail

import com.khuram.notes.business.data.cache.CacheResponseHandler
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.data.util.safeApiCall
import com.khuram.notes.business.data.util.safeCacheCall
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.framework.presentation.notedetail.state.NoteDetailViewState
import com.khuram.notes.business.domain.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class UpdateNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    fun updateNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteDetailViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.updateNote(
                primaryKey = note.id,
                newTitle = note.title,
                newBody = note.body,
                timestamp = null
            )
        }

        val response = object: CacheResponseHandler<NoteDetailViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<NoteDetailViewState> {
                return if(resultObj > 0){
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
                else{
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_FAILED,
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
        if(response.equals(UPDATE_NOTE_SUCCESS)){

            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
        }
    }

    companion object{
        const val UPDATE_NOTE_SUCCESS = "Successfully updated note."
        const val UPDATE_NOTE_FAILED = "Failed to update note."
        const val UPDATE_NOTE_FAILED_PK = "Update failed. Note is missing primary key."
    }
}