package com.khuram.notes.framework.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.khuram.notes.business.interactors.splash.SyncDeletedNotes
import com.khuram.notes.business.interactors.splash.SyncNotes
import com.khuram.notes.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteNetworkSyncManager
@Inject
constructor(
    private val syncNotes: SyncNotes,
    private val syncDeletedNotes: SyncDeletedNotes
) {
    private val _hasSyncBeenExecuted: MutableLiveData<Boolean> = MutableLiveData(false)

    val hasSyncBeenExecuted: LiveData<Boolean>
        get() = _hasSyncBeenExecuted

    fun executeDataSync(coroutineScope: CoroutineScope){
        if(_hasSyncBeenExecuted.value!!){
            return
        }

        val syncJob = coroutineScope.launch {
            val deletesJob = launch {
                printLogD("SyncNotes",
                    "syncing deleted notes.")
                syncDeletedNotes.syncDeletedNotes()
            }
            deletesJob.join()

            launch {
                printLogD("SyncNotes",
                    "syncing notes.")
                syncNotes.syncNotes()
            }
        }
        syncJob.invokeOnCompletion {
            CoroutineScope(Dispatchers.Main).launch{
                _hasSyncBeenExecuted.value = true
            }
        }
    }
}