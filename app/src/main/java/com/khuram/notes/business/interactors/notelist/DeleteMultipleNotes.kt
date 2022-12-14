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


class DeleteMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    // set true if an error occurs when deleting any of the notes from cache
    private var onDeleteError: Boolean = false

    /**
     * Logic:
     * 1. execute all the deletes and save result into an ArrayList<DataState<NoteListViewState>>
     * 2a. If one of the results is a failure, emit an "error" response
     * 2b. If all success, emit success response
     * 3. Update network with notes that were successfully deleted
     */
    fun deleteNotes(
        notes: List<Note>,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val successfulDeletes: ArrayList<Note> = ArrayList() // notes that were successfully deleted
        for(note in notes){
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                noteCacheDataSource.deleteNote(note.id)
            }

            val response = object: CacheResponseHandler<NoteListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
                    if(resultObj < 0){ // if error
                        onDeleteError = true
                    }
                    else{
                        successfulDeletes.add(note)
                    }
                    return null
                }
            }.getResult()

            // check for random errors
            if(response?.stateMessage?.response?.message?.contains(stateEvent.errorInfo()) == true) {
                onDeleteError = true
            }
        }

        if(onDeleteError) {
            emit(
                DataState.data(
                    response = Response(
                        message = DELETE_NOTES_ERRORS,
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Success
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        } else {
            emit(
                DataState.data(
                    response = Response(
                        message = DELETE_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.Toast,
                        messageType = MessageType.Success
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes)
    }

    private suspend fun updateNetwork(successfulDeletes: ArrayList<Note>) {

        for (note in successfulDeletes){
            // delete from "notes" node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.deleteNote(note.id)
            }

            // insert into "deletes" node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object{
        const val DELETE_NOTES_SUCCESS = "Successfully deleted notes."
        const val DELETE_NOTES_ERRORS = "Not all the notes you selected were deleted. There was some errors."
        const val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
        const val DELETE_NOTES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}