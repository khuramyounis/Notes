package com.khuram.notes.business.interactors.splash

import com.khuram.notes.business.data.cache.CacheResponseHandler
import com.khuram.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.khuram.notes.business.data.network.ApiResponseHandler
import com.khuram.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.khuram.notes.business.data.util.safeApiCall
import com.khuram.notes.business.data.util.safeCacheCall
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.state.DataState
import com.khuram.notes.util.printLogD
import kotlinx.coroutines.Dispatchers


/*
    Search firestore for all notes in the "deleted" node.
    It will then search the cache for notes matching those deleted notes.
    If a match is found, it is deleted from the cache.
 */
class SyncDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    suspend fun syncDeletedNotes(){

        val apiResult = safeApiCall(Dispatchers.IO){
            noteNetworkDataSource.getDeletedNotes()
        }

        val response = object: ApiResponseHandler<List<Note>, List<Note>>(
            response = apiResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>> {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }

        val notes = response.getResult()?.data?: ArrayList()

        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.deleteNotes(notes)
        }

        object: CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<Int> {
                printLogD("SyncNotes", "num deleted notes: $resultObj")
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()
    }
}



