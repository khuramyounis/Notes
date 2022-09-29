package com.khuram.notes.business.interactors.common

import com.khuram.notes.business.data.cache.CacheResponseHandler
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.data.util.safeApiCall
import com.khuram.notes.business.data.util.safeCacheCall
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.deleteNote(note.id)
        }

        val response = object: CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<ViewState> {
                return if(resultObj > 0){
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
                else{
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_FAILED,
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

        // update network
        if(response?.stateMessage?.response?.message.equals(DELETE_NOTE_SUCCESS)){

            // delete from 'notes' node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.deleteNote(note.id)
            }

            // insert into 'deletes' node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object{
        const val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        const val DELETE_NOTE_PENDING = "Delete pending..."
        const val DELETE_NOTE_FAILED = "Failed to delete note."
        const val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"
    }
}
