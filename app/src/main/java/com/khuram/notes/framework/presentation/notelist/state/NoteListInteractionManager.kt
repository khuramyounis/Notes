package com.khuram.notes.framework.presentation.notelist.state

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.framework.presentation.notelist.state.NoteListToolbarState.MultiSelectionState


class NoteListInteractionManager {

    private val _selectedNotes: MutableLiveData<ArrayList<Note>> = MutableLiveData()

    private val _toolbarState: MutableLiveData<NoteListToolbarState>
            = MutableLiveData(NoteListToolbarState.SearchViewState)

    val selectedNotes: LiveData<ArrayList<Note>>
        get() = _selectedNotes

    val toolbarState: LiveData<NoteListToolbarState>
        get() = _toolbarState

    fun setToolbarState(state: NoteListToolbarState){
        _toolbarState.value = state
    }

    fun getSelectedNotes(): ArrayList<Note> = _selectedNotes.value?: ArrayList()

    fun isMultiSelectionStateActive(): Boolean{
        return _toolbarState.value.toString() == MultiSelectionState.toString()
    }

    fun addOrRemoveNoteFromSelectedList(note: Note){
        var list = _selectedNotes.value
        if(list == null){
            list = ArrayList()
        }
        if (list.contains(note)){
            list.remove(note)
        }
        else{
            list.add(note)
        }
        list.let { _selectedNotes.value = it }
    }

    fun isNoteSelected(note: Note): Boolean{
        return _selectedNotes.value?.contains(note)?: false
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun clearSelectedNotes(){
        _selectedNotes.value = null
    }
}


