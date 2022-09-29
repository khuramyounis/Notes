package com.khuram.notes.framework.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khuram.notes.business.domain.model.NoteFactory
import com.khuram.notes.business.interactors.notedetail.NoteDetailInteractors
import com.khuram.notes.business.interactors.notelist.NoteListInteractors
import com.khuram.notes.framework.presentation.notedetail.NoteDetailViewModel
import com.khuram.notes.framework.presentation.notelist.NoteListViewModel
import com.khuram.notes.framework.presentation.splash.NoteNetworkSyncManager
import com.khuram.notes.framework.presentation.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class NoteViewModelFactory
constructor(
    private val noteListInteractors: NoteListInteractors,
    private val noteDetailInteractors: NoteDetailInteractors,
    private val noteNetworkSyncManager: NoteNetworkSyncManager,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when(modelClass){

            NoteListViewModel::class.java -> {
                NoteListViewModel(
                    noteInteractors = noteListInteractors,
                    noteFactory = noteFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }

            NoteDetailViewModel::class.java -> {
                NoteDetailViewModel(
                    noteInteractors = noteDetailInteractors
                ) as T
            }

            SplashViewModel::class.java -> {
                SplashViewModel(
                    noteNetworkSyncManager = noteNetworkSyncManager
                ) as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}



